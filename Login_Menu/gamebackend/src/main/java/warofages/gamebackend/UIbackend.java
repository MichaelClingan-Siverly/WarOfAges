package warofages.gamebackend;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Scanner;

import coms309.mike.clientcomm.ClientComm;
import coms309.mike.clientcomm.VolleyCallback;
import coms309.mike.units.Unit;

/**
 * Handles all of the heavy lifting for the program, telling the UI what to display
 * Created by mike on 6/2/2017.
 */

public class UIbackend implements AsyncResultHandler{
    private final int FRIENDLY_TOWN_ID = 5;
    private final int HOSTILE_TOWN_ID = 6;
    private final int NEUTRAL_TOWN_ID = 7;

    private SparseArray<Town> towns;
    private DisplaysChanges UI;
    private ClientComm comm;
    private byte[] terrainMap;
    private int mapIdManipulated;
    private Player player;
    private boolean gameOn;
    private boolean spectator;

    public UIbackend(Context context, String myName, boolean isSpectator, DisplaysChanges ui){
        player = new InactivePlayer(myName, context);
        spectator = isSpectator;
        UI = ui;
        comm = new ClientComm(context);
        towns = new SparseArray<>();
        mapIdManipulated = -1;
        getMapFromServer();
    }

    private void getMapFromServer(){
        //Makes connection to server and requests the terrain map
        comm.serverPostRequest("adminMap.php", new JSONArray(), new VolleyCallback<JSONArray>() {
            @Override
            public void onSuccess(JSONArray result) {
                try {
                    if (result.getJSONObject(0).getString("code").equals("update_success")) {
                        String mapper = result.getJSONObject(1).getString("Map");
                        Scanner scan = new Scanner(mapper).useDelimiter(":");
                        int mapSize = scan.nextInt();
                        terrainMap = new byte[mapSize];

                        int tID = 0;
                        while (scan.hasNextInt()) {
                            int terrainCode = scan.nextInt();
                            terrainMap[tID] = (byte)terrainCode;
                            //these are subject to change, but I still need to differentiate them for now
                            switch(terrainCode){
                                case FRIENDLY_TOWN_ID:
                                    addTown(tID, "friendly");
                                    break;
                                case HOSTILE_TOWN_ID:
                                    addTown(tID, "hostile");
                                    break;
                                case NEUTRAL_TOWN_ID:
                                    addTown(tID, "null");
                                    break;
                            }
                            tID++;
                        }
                        scan.close();
                        //continues here because Volley is asynchronous and I want to wait until its done
                        finishSettingUp();

                    }

                } catch (org.json.JSONException e) {
                    Log.d("getMapFromServer", e.getLocalizedMessage());
                }
            }
        });
    }

    private void finishSettingUp(){
        UI.displayTerrain(terrainMap.length);
        UI.setInfoBar("Cash: " + player.getCash());
        if(!spectator)
            readyToStart();
        ((InactivePlayer) player).waitForTurn(this);
    }

    public int getTerrainAtLocation(int index){
        if(index < terrainMap.length)
            return terrainMap[index];
        return -1;
    }

    private void addTown(int mapID, String owner){
        Town town;
        if(owner != null)
            town = new Town(mapID, owner);
        else
            town = new Town(mapID);
        //add new town to SparseArray only if there is no other town in it with same key (mapID) - shouldn't happen
        if(towns.get(mapID) == null)
            towns.put(mapID, town);
    }

    private void setTownOwnership(JSONArray townArray){
        if(townArray == null)
            return;
        for(int i = 0; i < townArray.length(); i++){
            String owner;
            int mapID;
            int newTerID;
            try {
                mapID = townArray.getJSONObject(i).getInt("GridID");
                owner = townArray.getJSONObject(i).getString("Owner");
                //json says town belongs to one player while I think its another's
                if(owner.equals(player.getName()) && !towns.get(mapID).getOwner().equals(player.getName())
                        || owner.equals(player.getEnemyName()) && !towns.get(mapID).getOwner().equals(player.getEnemyName())){
                    if(owner.equals(player.getName()))
                        newTerID = 5;
                    else
                        newTerID = 6;
                    towns.get(mapID).setOwner(owner);
                    UI.changeTownOwnership(newTerID, mapID);
                }
            }
            catch(JSONException e){
                Log.d("setTownOwnership", e.getLocalizedMessage());
            }
        }
    }

    private boolean playerIsActive(){
        return player instanceof ActivePlayer;
    }

    private void readyToStart(){
        JSONArray nameArray = new JSONArray();
        JSONObject nameObject = new JSONObject();
        try {
            nameObject.put("userID", player.getName());
        }
        catch(JSONException e){
            Log.d("readyToStart", e.getLocalizedMessage());
        }
        nameArray.put(nameObject);
        comm.serverPostRequest("getPlayers.php", nameArray, new VolleyCallback<JSONArray>() {
            @Override
            public void onSuccess(JSONArray result) {
                //here is where I'll set towns to their proper owners, since it tells me who is player 1 or 2
            }
        });
    }

    private void endTurnHelper(){
        String end = checkIfGameOver();
        if(end.equals("Game in Progress")){
            UI.setInfoBar("Cash: " + player.getCash());
            if(playerIsActive()) {
                player = new InactivePlayer(player);
                ((InactivePlayer) player).waitForTurn(this);
            }
        }
        else{
            gameOn = false;
            UI.setInfoBar(end);
        }
    }

    public void endTurn(){
        if(!gameOn)
            UI.setInfoBar(checkIfGameOver());
        else if(playerIsActive()){
            //checkActivePlayer was a bad name choice. It switches an active player to inactive and vice versa
            comm.serverPostRequest("checkActivePlayer.php", new JSONArray(), new VolleyCallback<JSONArray>() {
                @Override
                public void onSuccess(JSONArray result) {
                    endTurnHelper();
                }
            });
        }
    }

    private void beginTurn(){
        if(!playerIsActive()) {
            player = new ActivePlayer(player, towns);
        }
        UI.setInfoBar("Cash: " + player.getCash());
    }

    private String checkIfGameOver(){
        if(player.checkIfNoUnits(true) && player.checkIfNoUnits(false))
            return "Game is a Draw";
        else if(player.checkIfNoUnits(true)) {
            return player.getEnemyName() + " wins";
        }
        else if(player.checkIfNoUnits(false)){
            return player.myName + " wins";
        }
        return "Game in Progress";
    }

    /**
     * helper for the buttons
     * @param mapID mapID of the space to be checked for a unit
     * @param friendly indicate if a friendly (true) or hostile (false) unit is wanted
     * @return the friendly/hostile unit corresponding to the space clicked, or null if there is no match
     */
    private Unit getUnitFromMap(final int mapID, boolean friendly){
        if(mapID < 0 || mapID >= terrainMap.length)
            return null;
        else if(friendly)
            return player.getFriendlyUnit(mapID);
        else
            return player.getEnemyUnit(mapID);
    }

    private Unit getMovingUnit(int mapID){
        //no unit is selected as moving yet, so search the space clicked on (mapID)
        if (((ActivePlayer)player).getMoveFromMapID() == -1)
            return getUnitFromMap(mapID, true);
        //a unit has been selected to move, so return that instead of the mapID selected
        else
            return getUnitFromMap(((ActivePlayer)player).getMoveFromMapID(), true);
    }

    public void resetMapIdManipulated(){
        mapIdManipulated = -1;
    }

    //TODO I want to be able to recruit only on a town - should be a lot easier than this
    public void recruitFromTownMenu(int unitIdToAdd){
        Unit movingUnit = getUnitFromMap(mapIdManipulated, true);
        if(movingUnit != null && !movingUnit.checkIfMoved()){
            //take all surrounding tiles and add unit to first empty one
            int[] moves = new terrainCalculations(terrainMap, UI.getTileSize()).checkSurroundingTerrain(movingUnit, player, false);
            for (int move : moves) {
                if (move > -1 && move < terrainMap.length && ((ActivePlayer)player).checkIfUnitOnSpace(move) == 1
                        && getTerrainAtLocation(move) != 6
                        && !(unitIdToAdd == 5 && getTerrainAtLocation(move) == 4)
                        && !(unitIdToAdd == 2 && getTerrainAtLocation(move) == 4)) {
                    //creates unit and sends it to server
                    createUnit(move, unitIdToAdd);
                    // don't actually move the unit, but dont let it move anymore
                    movingUnit.moveUnit(movingUnit.getMapID());
                    //close popup
                    resetMapIdManipulated();
                    return;
                }
            }
        }
        //no empty space found to recruit on or recruiting unit has already moved
        resetMapIdManipulated();
        UI.makeToast("no space found to recruit unit");
    }

    public void helpWithMapClicks(int mapIdClicked){
        if(!gameOn){
            UI.setInfoBar(checkIfGameOver());
            return;
        }
        else if(!playerIsActive())
            return;

        Unit movingUnit = getMovingUnit(mapIdClicked);
        Unit enemyUnit = getUnitFromMap(mapIdClicked, false);

        if(movingUnit != null && movingUnit.checkIfMoved() && movingUnit.checkIfAttacked())
            UI.makeToast("This unit has no more actions this turn");
        //if a friendly unit was not selected, see if an enemy unit is there and display its stats if so
        else if(enemyUnit != null) {
            //display unit stats
            double[] stats = ((ActivePlayer)player).getUnitStats(mapIdClicked, false);
            UI.setInfoBar("Enemy Health: " + (int) stats[0] + ", Attack: " + (int) stats[1] + ", Defense: " + stats[2]);
        }
        //if there was no friendly or enemy unit there, display cash instead
        else
            UI.setInfoBar("Cash: " + player.getCash());

        //if friendly unit on a town and you click it, open town menu
        if(movingUnit != null && mapIdClicked >= 0 && mapIdClicked < terrainMap.length &&
                getTerrainAtLocation(mapIdClicked) == 5 && mapIdManipulated == -1
                && ((ActivePlayer)player).getMoveFromMapID() == -1) {
            mapIdManipulated = mapIdClicked;
            UI.displayTownMenu();
        }
        //nothing has been selected to move yet
        else if(player instanceof ActivePlayer && mapIdManipulated < 0){
            if(movingUnit == null){
                resetMapIdManipulated();
                return;
            }
            if(((ActivePlayer)player).getMoveFromMapID() == -1){
                beginMoveOrAttack();
            }
            else{
                finishMoveOrAttack(movingUnit, mapIdClicked);
            }
        }
    }

    private void finishMoveOrAttack(Unit movingUnit, int mapIdClicked){
        resetMapIdManipulated();
        ((ActivePlayer)player).setMoveFromMapID(-1);
        //TODO, if I'm given the movngUnit, using player to get the movingUnit's mapId seems unnecessary
        int[] largestArea = findLargestArea(movingUnit);
        int moving = ((ActivePlayer)player).getMoveFromMapID();
        for (int move : largestArea) {
            int mapID = move;
            int moveCheck = ((ActivePlayer)player).checkIfUnitOnSpace(move);
            int unitID = 0;
            boolean friendly = true;
            switch(moveCheck){
                case 0: //"no move"
                    if(getUnitFromMap(move, true) != null) {
                        Unit u = getUnitFromMap(move, true);
                        if(u == null)
                            return;
                        unitID = u.getUnitID();
                    }
                    break;
                case 1: //"canMoveTerrain"
                    if(move == mapIdClicked && !movingUnit.checkIfMoved()){
                        sendMove(mapIdClicked, moving);
                        //set the new foreground. It's friendly because I can't move an unfriendly unit
                        mapID = movingUnit.getMapID();
                        unitID = movingUnit.getUnitID();
                        //display cash
                        UI.setInfoBar("Cash: " + player.getCash());
                    }
                    break;
                case 2: //"canMoveEnemy"
                    //un-highlight enemy unit
                    Unit u = getUnitFromMap(move, false);
                    if(u == null)
                        return;
                    unitID = u.getUnitID();
                    friendly = false;
                    //after its un-highlighted, do combat
                    // (I un-highlight first in case the attack is out of range)
                    if (move == mapIdClicked && !movingUnit.checkIfAttacked()) {
                        UIAttack(movingUnit, player.getEnemyUnit(move));
                    }
                    break;
            }
            UI.displayForeground(mapID, unitID, friendly, false);
        }
    }

    private void sendMove(int newID, int oldID){
        if(!((ActivePlayer)player).moveUnit(oldID, newID))
            return;
        JSONArray move= new JSONArray();
        JSONObject juseid = new JSONObject();
        JSONObject jnewID = new JSONObject();
        JSONObject joldID = new JSONObject();
        try{
            juseid.put("userID",player.getName());
            jnewID.put("newID",newID); //old mapID of the unit
            joldID.put("oldID",oldID); //new mapID of the unit
        }
        catch(JSONException e){
            Log.d("sendMoveJSON", e.getLocalizedMessage());
        }
        move.put(juseid);
        move.put(jnewID);
        move.put(joldID);
        comm.serverPostRequest("movement.php", move, new VolleyCallback<JSONArray>() {
            @Override
            public void onSuccess(JSONArray result) {
                //not much to show since the units already moved
            }
        });
    }

    public void beginMoveOrAttack(){
        Unit movingUnit = getUnitFromMap(mapIdManipulated, true);
        if(movingUnit == null){
            resetMapIdManipulated();
            return;
        }
        highlightSurroundings(movingUnit);

        //current location of unit in the terrain map
        ((ActivePlayer)player).setMoveFromMapID(mapIdManipulated);
        //display unit stats
        double[] stats = ((ActivePlayer)player).getUnitStats(mapIdManipulated, true);
        UI.setInfoBar("Health: " + (int) stats[0] + ", Attack: " + (int) stats[1] + ", Defense: " + stats[2]);
        //close menu
        resetMapIdManipulated();
    }

    private void highlightSurroundings(Unit movingUnit){
        int[] largestArea = findLargestArea(movingUnit);

        for (int move : largestArea) {
            int moveCheck = ((ActivePlayer)player).checkIfUnitOnSpace(move);
            //Only highlights friendly units is it is the one currently moving
            if(moveCheck == 0 && move == movingUnit.getMapID())
                UI.displayForeground(move, movingUnit.getUnitID(), true, true);
            if (moveCheck == 1)
                UI.displayForeground(move, 0, true, true);
            else if (moveCheck == 2) {
                Unit u = getUnitFromMap(move, false);
                if(u == null)
                    return;
                UI.displayForeground(move, u.getUnitID(), false, true);
            }
        }
    }

    private int[] findLargestArea(Unit u){
        int[] moves = new terrainCalculations(terrainMap, UI.getTileSize()).checkSurroundingTerrain(u, player, false);
        int[] attacks = new terrainCalculations(terrainMap, UI.getTileSize()).checkSurroundingTerrain(u, player, true);

        if (moves.length > attacks.length && u != null && !u.checkIfMoved())
            return moves;
        else
            return attacks;
    }

    //Decided to move a lot of the checks for activePlayer name and stuff to here because I'd like
    // if the UI didn't need any knowledge of that stuff, and only worked on displaying what its told to
    @Override
    public void handlePollResult(JSONArray result){
        JSONArray townArray = null;
        // seemed a bit much to have the server send info about towns every time,
        // so I have to make sure to update towns only when it sends town info
        if(result.length() == 3) {
            try {
                townArray = result.getJSONArray(1);
                result.remove(1);
            } catch (JSONException e) {
                Log.d("handlePollResult", e.getLocalizedMessage());
            }
            setTownOwnership(townArray);
        }
        boolean active = false;
        String activePlayer;
        //defer JSON work to the InactivePlayer, since its the only one who needs the manipulations
        if(!playerIsActive()){
            //I let the server always send unit info because of movement: I can't keep track of
            // which unit is which otherwise. Maybe one day I can have the server assign a unique ID
            // for each unit so that  it can only send info about units which have changed
            active = ((InactivePlayer)player).receiveNewJSON(result);
            displayPollResult();
            String end = checkIfGameOver();
            if (!end.equals("Game in Progress")) {
                gameOn = false;
            }
        }
        if(active)
            activePlayer = player.getName();
        else
            activePlayer = player.getEnemyName();

        if(activePlayer.equals("null"))
            UI.setEndText("Need additional player to start game");
        else if(activePlayer.equals(player.getName())){
            //now all I have to do is display the changes
            UI.dismissEndText();
            if(!checkIfGameOver().equals("Game in Progress"))
                gameOn = false;
            beginTurn();
        }
        else if(spectator)
            UI.setEndText(activePlayer + " is currently playing");
        else
            UI.setEndText("It is " + activePlayer + "'s turn.");

    }

    private void displayPollResult(){
        UI.clearMap();
        SparseArray<Unit> units = player.getMyUnits();
        //friendly units
        for (int i = 0; i < units.size(); i++) {
            UI.displayForeground(units.valueAt(i).getMapID(), units.valueAt(i).getUnitID(), true, false);
        }
        //hostile units
        units = player.getEnemyUnits();
        for (int i = 0; i < units.size(); i++) {
            UI.displayForeground(units.valueAt(i).getMapID(), units.valueAt(i).getUnitID(), false, false);
        }

    }

    private void UIAttack(Unit attackingUnit, Unit defendingUnit){
        int[] possibleAttacks = new terrainCalculations(terrainMap, UI.getTileSize()).checkSurroundingTerrain(attackingUnit, player, true);
        //if enemy if outside of attack range, it will return without attempting an attack
        for(int index : possibleAttacks){
            if(defendingUnit.getMapID() == index){
                int myMapID = attackingUnit.getMapID();
                int enemyMapID = defendingUnit.getMapID();
                double myHealth, enemyHealth;
                byte attackerTerrain = terrainMap[myMapID];
                byte defenderTerrain = terrainMap[enemyMapID];
                String attackResults = ((ActivePlayer)player).attack(attackingUnit, attackerTerrain, defendingUnit, defenderTerrain);
                switch(attackResults){
                    case "Draw":
                        UI.displayForeground(attackingUnit.getMapID(), 0, true, false);
                        UI.displayForeground(defendingUnit.getMapID(), 0, true, false);
                        myHealth = 0;
                        enemyHealth = 0;
                        break;
                    case "Your unit died":
                        UI.displayForeground(attackingUnit.getMapID(), 0, true, false);
                        myHealth = 0;
                        enemyHealth = defendingUnit.getHealth();
                        break;
                    case "Enemy unit killed":
                        UI.displayForeground(defendingUnit.getMapID(), 0, true, false);
                        myHealth = attackingUnit.getHealth();
                        enemyHealth = 0;
                        break;
                    default:
                        myHealth = attackingUnit.getHealth();
                        enemyHealth = defendingUnit.getHealth();
                }
                UI.setInfoBar(attackResults);
                sendAttack(myMapID, myHealth, enemyMapID, enemyHealth);
                return;
            }
        }
    }

    private void sendAttack(int myUnitMapID, double myUnitHealth, int enemyUnitMapID, double enemyUnitHealth) {
        JSONArray jsonhealth = new JSONArray();
        JSONObject myObjectID = new JSONObject();
        JSONObject myObjectHealth = new JSONObject();
        JSONObject enemyObjectID = new JSONObject();
        JSONObject enemyObjectHealth = new JSONObject();
        try {
            myObjectID.put("myUnitID", myUnitMapID);
            myObjectHealth.put("myUnitHealth", myUnitHealth);
            enemyObjectID.put("enemyUnitID", enemyUnitMapID);
            enemyObjectHealth.put("enemyUnitHealth", enemyUnitHealth);

        } catch (JSONException e) {
            Log.d("formAttackJSON", e.getLocalizedMessage());
        }
        jsonhealth.put(myObjectID);
        jsonhealth.put(myObjectHealth);
        jsonhealth.put(enemyObjectID);
        jsonhealth.put(enemyObjectHealth);
        comm.serverPostRequest("attack.php", jsonhealth, new VolleyCallback<JSONArray>() {
            @Override
            public void onSuccess(JSONArray result) {
                //could update the UI here, but since I already have the info I'd rather do it before
            }
        });
    }

    private void createUnit(int mapID, int unitID){
        final Object[] stuff= ((ActivePlayer)player).createUnit(mapID, unitID);
        UI.makeToast((String)stuff[0]);
        if(stuff.length > 1){
            //set unit image
            UI.displayForeground(mapID, unitID, true, false);

            JSONArray requestArray = new JSONArray();
            JSONObject nameObject = new JSONObject();
            JSONObject gridObject = new JSONObject();
            JSONObject unitObject = new JSONObject();
            JSONObject unitHealth = new JSONObject();
            try {
                nameObject.put("userID", player.getName());
                gridObject.put("GridID", mapID);
                unitObject.put("UnitID", unitID);
                unitHealth.put("health", (double)stuff[1]);
            } catch (JSONException e) {
                //TODO
            }
            requestArray.put(nameObject);
            requestArray.put(gridObject);
            requestArray.put(unitObject);
            requestArray.put(unitHealth);

            comm.serverPostRequest("createUnit.php", requestArray, new VolleyCallback<JSONArray>() {
                @Override
                public void onSuccess(JSONArray result) {
                    Log.d("createUnit", result.toString());
                }
            });
        }
    }

    public void ensurePollKilled(){
        if(!playerIsActive()){
            ((InactivePlayer)player).killPoll();
        }
    }
}

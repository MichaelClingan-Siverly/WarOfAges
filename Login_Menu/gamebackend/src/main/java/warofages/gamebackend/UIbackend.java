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
import coms309.mike.units.RangedUnit;
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
    private int[] highlightedArea;
    //keeps track of where a unit is moving from or where a town menu is opened from
    private int mapIdManipulated;
    private Player player;
    private boolean gameOn;
    private boolean spectator;

    public UIbackend(Context context, String myName, boolean isSpectator, DisplaysChanges ui){
        player = new InactivePlayer(myName, context);
        spectator = isSpectator;
        gameOn = true;
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

    private void setTownOwnership(String owner, int mapID){
        int newTerID;
        if(owner.equals(player.getName()))
            newTerID = 5;
        else
            newTerID = 6;
        towns.get(mapID).setOwner(owner);
        UI.changeTownOwnership(newTerID, mapID);
    }

    //it's possible that the hostile town's owner wont be set before the first player becomes active, but its ok since its still marked as hostile
    private boolean setTownOwnersAfterPoll(JSONArray townArray){
        if(townArray == null)
            return false;
        for(int i = 0; i < townArray.length(); i++){
            String owner;
            int mapID;
            try {
                mapID = townArray.getJSONObject(i).getInt("GridID");
                owner = townArray.getJSONObject(i).getString("Owner");
                if(owner.equals("friendly") || owner.equals("hostile"))
                    return false;
                //json says town belongs to one player while I think its another's
                if(owner.equals(player.getName()) && !towns.get(mapID).getOwner().equals(player.getName())
                        || owner.equals(player.getEnemyName()) && !towns.get(mapID).getOwner().equals(player.getEnemyName())){
                    setTownOwnership(owner, mapID);
                }
            }
            catch(JSONException e){
                Log.d("setTownOwnership", e.getLocalizedMessage());
            }
        }
        return true;
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
            }
        });
    }

    private void endTurnHelper(){
        UI.setInfoBar("Cash: " + player.getCash());
        if(playerIsActive()) {
            player = new InactivePlayer(player);
            ((InactivePlayer) player).waitForTurn(this);
        }
    }

    public void endTurn(){
        String end = checkIfGameOver();
        if(!end.equals("Game in Progress")) {
            UI.setInfoBar(end);
            gameOn = false;
        }
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
        UI.makeToast("It is your turn");
        UI.setInfoBar("Cash: " + player.getCash());
    }

    //the wife liked the idea of
    private String checkIfGameOver(){
        //if I (or the player I spectate as "friendly") has no units or towns, enemy wins
        if(!player.checkIfGeneralAlive(true)) {
            return player.getEnemyName() + " wins";
        }
        //if enemy has no units or towns, I (or the player I spectate as "friendly") win
        else if(!player.checkIfGeneralAlive(false)){
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
        if (mapIdManipulated == -1)
            return getUnitFromMap(mapID, true);
        //a unit has been selected to move, so return that instead of the mapID selected
        else
            return getUnitFromMap(mapIdManipulated, true);
    }

    public void resetMapIdManipulated(){
        mapIdManipulated = -1;
    }

    public void recruitFromTownMenu(int unitIdToAdd){
        Unit movingUnit = getUnitFromMap(mapIdManipulated, true);
        if(movingUnit == null){
            //creates unit and sends it to server
            createUnit(mapIdManipulated, unitIdToAdd);
            //close popup
            resetMapIdManipulated();
            return;
        }
        resetMapIdManipulated();
        UI.makeToast("There is already a unit on the town");
    }

    private void displayStats(int mapID){
        //if a friendly unit was not selected, see if an enemy unit is there and display its stats if so
        if(mapIdManipulated != -1 && getUnitFromMap(mapID, false) != null) {
            //display unit stats
            double[] stats = player.getUnitStats(mapID, terrainMap[mapID], false);
            UI.setInfoBar("Enemy Health: " + (int) stats[0] + ", Attack: " + (int) stats[1] + ", Defense: " + Math.round(stats[2]*10000)/100 + "%");
        }
        else if(mapIdManipulated != -1 && getUnitFromMap(mapID, true) != null){
            double[] stats = player.getUnitStats(mapID, terrainMap[mapID], true);
            UI.setInfoBar("Unit Health: " + (int) stats[0] + ", Attack: " + (int) stats[1] + ", Defense: " + Math.round(stats[2]*10000)/100 + "%");
        }
        //if there was no friendly or enemy unit there, display cash instead
        else if(mapIdManipulated != -1)
            UI.setInfoBar("Cash: " + player.getCash());
    }

    public void helpWithMapClicks(int mapIdClicked){
        if(!gameOn){
            UI.setInfoBar(checkIfGameOver());
            return;
        }
        displayStats(mapIdClicked);

        Unit movingUnit = getMovingUnit(mapIdClicked);

        if(!playerIsActive())
            return;
        else if(movingUnit != null && movingUnit.checkIfMoved() && movingUnit.checkIfAttacked()) {
            UI.makeToast("This unit has no more actions this turn");
            return;
        }

        //TODO this is a lot of stuff. Try cleaning it up some
        //if player is active, clicked on a town, and no unit is on it
        if(movingUnit == null && getUnitFromMap(mapIdClicked, false) == null &&
                getUnitFromMap(mapIdClicked, true) == null && mapIdManipulated == -1 &&
                towns.get(mapIdClicked) != null &&
                towns.get(mapIdClicked).getOwner().equals(player.getName())) {
            mapIdManipulated = mapIdClicked;
            UI.displayTownMenu();
        }
        else if(playerIsActive()){
            //nothing has been selected to move yet
            if(movingUnit == null){
                return;
            }
            if(mapIdManipulated == -1){
                mapIdManipulated = mapIdClicked;
                beginMoveOrAttack();
            }
            else{
                finishMoveOrAttack(movingUnit, mapIdClicked);
            }
        }
    }

    private void finishMoveOrAttack(Unit movingUnit, int mapIdClicked){
        for (int mapID : highlightedArea) {
            int moveCheck = ((ActivePlayer)player).checkIfUnitOnSpace(mapID);
            int unitID = 0;
            Unit unit;
            boolean friendly = true;
            switch(moveCheck){
                case 0: //"no move"
                    unit = getUnitFromMap(mapID, true);
                    if(unit != null) {
                        unitID = unit.getUnitID();
                        friendly = true;
                    }
                    break;
                case 1: //"canMoveTerrain"
                    if(mapID == mapIdClicked && !movingUnit.checkIfMoved()){
                        sendMove(mapIdClicked, mapIdManipulated);
                        //clears the old terrain
                        UI.displayForeground(mapIdManipulated, unitID, true, false);
                        //set the new foreground. It's friendly because I can't move an unfriendly unit
                        mapID = movingUnit.getMapID();
                        unitID = movingUnit.getUnitID();
                        //taking a city uses an attack
                        if(terrainMap[mapID] == 6 || terrainMap[mapID] == 7){
                            setTownOwnership(player.getName(), mapID);
                            unit = getUnitFromMap(mapID, true);
                            if(unit != null) {
                                unit.setHasAttacked();
                            }
                        }
                        //display cash
                        UI.setInfoBar("Cash: " + player.getCash());
                    }
                    else if(mapID == mapIdClicked)
                        UI.makeToast("This unit has already moved.");
                    break;
                case 2: //"canMoveEnemy"
                    //un-highlight enemy unit
                    unit = getUnitFromMap(mapID, false);
                    if(unit != null) {
                        unitID = unit.getUnitID();
                        friendly = false;
                        //after its un-highlighted, do combat
                        // (I un-highlight first in case the attack is out of range)
                        if (mapID == mapIdClicked && !movingUnit.checkIfAttacked()) {
                            int result = UIAttack(movingUnit, getUnitFromMap(mapID, false));
                            //enemy dies, change the unitID so display will be properly updated
                            if(result == 1)
                                unitID = 0;
                            //my unit dies, must change display myself since its mapID isnt part of highlightedArea
                            else if(result == -1)
                                UI.displayForeground(movingUnit.getMapID(), 0, true, false);
                        }
                    }
                    break;
            }
            UI.displayForeground(mapID, unitID, friendly, false);
        }
        highlightedArea = null;
        resetMapIdManipulated();
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
                try {
                    UI.makeToast(result.getJSONObject(0).getString("message"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //not much to show since the units already moved
            }
        });
    }

    public void beginMoveOrAttack(){
        Unit movingUnit = getUnitFromMap(mapIdManipulated, true);
        //I check before calling this, but its a second check - maybe something else will call it eventually
        if(movingUnit == null || (movingUnit.checkIfAttacked() && movingUnit.checkIfMoved())){
            resetMapIdManipulated();
            return;
        }
        highlightSurroundings(movingUnit);

        //display unit stats
        double[] stats = player.getUnitStats(mapIdManipulated, terrainMap[mapIdManipulated], true);
        UI.setInfoBar("Health: " + (int) stats[0] + ", Attack: " + (int) stats[1] + ", Defense: " + stats[2] * 100 + "%");
    }

    private void highlightSurroundings(Unit movingUnit){
        findLargestArea(movingUnit);

        for (int move : highlightedArea) {
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

    private void findLargestArea(Unit u){
        int[] moves = new terrainCalculations().checkSurroundingTerrain(u, player, false, terrainMap);
        int[] attacks = new terrainCalculations().checkSurroundingTerrain(u, player, true, terrainMap);

        if (moves.length > attacks.length && u != null && !u.checkIfMoved())
            highlightedArea = moves;
        else
            highlightedArea = attacks;
    }

    //Decided to move a lot of the checks for activePlayer name and stuff to here because I'd like
    // if the UI didn't need any knowledge of that stuff, and only worked on displaying what its told to
    @Override
    public void handlePollResult(JSONArray result){
        boolean townsAllUpdated = false;
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
            townsAllUpdated = setTownOwnersAfterPoll(townArray);
        }
        boolean active = false;
        String activePlayer;
        //defer JSON work to the InactivePlayer, since its the only one who needs the manipulations
        if(!playerIsActive()){
            //I let the server always send unit info because of movement: I can't keep track of
            // which unit is which otherwise. Maybe one day I can have the server assign a unique ID
            // for each unit so that  it can only send info about units which have changed
            active = ((InactivePlayer)player).receiveNewJSON(result);
            if(active && townsAllUpdated)
                ((InactivePlayer)player).killPoll();

            displayPollResult();
            if (!checkIfGameOver().equals("Game in Progress")) {
                gameOn = false;
            }
        }
        if(active)
            activePlayer = player.getName();
        else
            activePlayer = player.getEnemyName();

        if(activePlayer.equals("hostile"))
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

    //returns -1 if friendly killed, 0 if neither dies, 1 if enemy dies
    private int UIAttack(Unit attackingUnit, Unit defendingUnit){
        int results = 0;
        terrainCalculations mapCalc = new terrainCalculations();
        int[] possibleAttacks = mapCalc.checkSurroundingTerrain(attackingUnit, player, true, terrainMap);
        //if enemy if outside of attack range, it will return without attempting an attack
        for(int index : possibleAttacks){
            if(defendingUnit.getMapID() == index){
                int myMapID = attackingUnit.getMapID();
                int enemyMapID = defendingUnit.getMapID();
                int rowLength = (int)Math.sqrt(terrainMap.length);
                int startRow = myMapID%rowLength;
                int startCol = myMapID/rowLength;
                int endRow = enemyMapID%rowLength;
                int endCol = enemyMapID/rowLength;
                int distance = mapCalc.findManhattanDistance(startRow,
                        startCol, endRow, endCol);
                double myHealth, enemyHealth;
                byte attackerTerrain = terrainMap[myMapID];
                byte defenderTerrain = terrainMap[enemyMapID];
                boolean canReach = true;
                String attackResults;

                if(attackingUnit instanceof RangedUnit && ((RangedUnit)attackingUnit).needsLineOfSight()){
                    terrainCalculations.Hexagon[] path = mapCalc.checkLine(myMapID, enemyMapID, terrainMap.length);
                    //I don't care about what terrain the attacker is in
                    for(int i = 1; i < path.length; i++){
                        int terID = terrainMap[path[i].col + path[i].row];
                        if(terID == 2 || terID >= 4 && terID <= 7 || terID == 9){
                            canReach = false;
                        }
                    }
                }

                if(canReach) {
                    attackResults = ((ActivePlayer) player).attack(attackingUnit, attackerTerrain,
                            defendingUnit, defenderTerrain, distance, terrainMap.length);
                    switch (attackResults) {
                        case "You lose":
                            gameOn = false;
                        case "Your unit died": //mine dies
                            myHealth = 0;
                            enemyHealth = defendingUnit.getHealth();
                            results = -1;
                            break;
                        case "Enemy loses":
                            gameOn = false;
                        case "Enemy unit killed": //theirs die
                            myHealth = attackingUnit.getHealth();
                            enemyHealth = 0;
                            results = 1;
                            break;
                        default: //neither die
                            myHealth = attackingUnit.getHealth();
                            enemyHealth = defendingUnit.getHealth();
                    }
                    sendAttack(myMapID, myHealth, enemyMapID, enemyHealth);
                }
                else
                    attackResults = "The enemy is protected by terrain";

                UI.setInfoBar(attackResults);
                return results;
            }
        }
        return results;
    }

    private void sendAttack(int myUnitMapID, double myUnitHealth, int enemyUnitMapID, double enemyUnitHealth) {
        JSONArray jsonhealth = new JSONArray();
        JSONObject myUnit = new JSONObject();
        JSONObject enemyUnit = new JSONObject();
        try {
            myUnit.put("myGridID", myUnitMapID);
            myUnit.put("myUnitHealth", myUnitHealth);
            enemyUnit.put("enemyGridID", enemyUnitMapID);
            enemyUnit.put("enemyUnitHealth", enemyUnitHealth);

        } catch (JSONException e) {
            Log.d("formAttackJSON", e.getLocalizedMessage());
        }
        jsonhealth.put(myUnit);
        jsonhealth.put(enemyUnit);
        comm.serverPostRequest("attack.php", jsonhealth, new VolleyCallback<JSONArray>() {
            @Override
            public void onSuccess(JSONArray result) {
                //could update the UI here, but since I already have the info I'd rather do it before
            }
        });
    }

    private void createUnit(int mapID, int unitID){
        final String message = ((ActivePlayer)player).createUnit(mapID, unitID);
        UI.makeToast(message);
        if(message.endsWith("recruited.")){
            double health = getUnitFromMap(mapID, true).getHealth();
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
                unitHealth.put("health", health);
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

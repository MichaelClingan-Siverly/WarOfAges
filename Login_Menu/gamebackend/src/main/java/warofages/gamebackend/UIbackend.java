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
import coms309.mike.units.Archer;
import coms309.mike.units.Cavalry;
import coms309.mike.units.General;
import coms309.mike.units.RangedUnit;
import coms309.mike.units.Spearman;
import coms309.mike.units.Swordsman;
import coms309.mike.units.Unit;

/**
 * Created by mike on 6/2/2017.
 */

public class UIbackend implements AsyncResultHandler{
    /*  IDE suggested using a SparseArray here. I only add towns once, they aren't removed, and there
        should usually be few of them in comparison to the size of the map. Seems like any performance
        loss would be worth the memory saved
    */
    private SparseArray<Town> towns;
    private DisplaysChanges UI;
    private ClientComm comm;
    private int[] terrainMap;
    private int mapIdManipulated;
    private Player player;
    private boolean gameOn;
    private boolean townsAssigned;
    private boolean spectator;

    public UIbackend(Context context, String myName, boolean isSpectator, DisplaysChanges ui){
        player = new InactivePlayer(myName, context, ui);
        spectator = isSpectator;
        UI = ui;
        comm = new ClientComm(context);
        towns = new SparseArray<>();
        mapIdManipulated = -1;
        townsAssigned = false;
        getMapFromServer();
    }

    public void getMapFromServer(){
        //Makes connection to server and requests the terrain map
        comm.serverPostRequest("adminMap.php", new JSONArray(), new VolleyCallback<JSONArray>() {
            @Override
            public void onSuccess(JSONArray result) {
                try {
                    if (result.getJSONObject(0).getString("code").equals("update_success")) {
                        String mapper = result.getJSONObject(1).getString("Map");
                        Scanner scan = new Scanner(mapper).useDelimiter(":");
                        int mapSize = scan.nextInt();

                        terrainMap = new int[mapSize];

                        int tID = 0;
                        while (scan.hasNextInt()) {
                            int terrainCode = scan.nextInt();
                            terrainMap[tID] = terrainCode;
                            switch(terrainCode){
                                //TODO I know which towns are friendly/hostile before game starts, but I don't know if I'm friendly or hostile until then
                                //TODO what I'll probably do is have the server's json return town ownership as well as units
                                case 5:
                                    addTown(tID, "friendly");
                                    break;
                                case 6:
                                    addTown(tID, "hostile");
                                    break;
                                case 7:
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

    public int[] getMap(){
        return terrainMap;
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

    public SparseArray<Town> getTowns(){
        return towns;
    }

    public Player getPlayer(){
        return player;
    }

    public boolean playerIsActive(){
        return player instanceof ActivePlayer;
    }

    public void readyToStart(){
        JSONArray nameArray = new JSONArray();
        JSONObject nameObject = new JSONObject();
        try {
            nameObject.put("userID", player.getName());
        }
        catch(JSONException e){
            //TODO
        }
        nameArray.put(nameObject);
        comm.serverPostRequest("getPlayers.php", nameArray, new VolleyCallback<JSONArray>() {
            @Override
            public void onSuccess(JSONArray result) {
                Log.d("getPlayers", result.toString());
                //Nothing needs to be done. getPlayers.php only tells the server that I'm a player, not spectator
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
                    Log.d("checkActivePlayer", result.toString());
                    endTurnHelper();
                }
            });
        }
    }

    private void beginTurn(){
        if(!playerIsActive()) {
            player = new ActivePlayer(player);
            player.incrementCash(towns);
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
        if (((ActivePlayer)player).moving == -1)
            return getUnitFromMap(mapID, true);
        //a unit has been selected to move, so return that instead of the mapID selected
        else
            return getUnitFromMap(((ActivePlayer)player).moving, true);
    }

    private Integer[] getMoves(int mapID){
        Unit u = getUnitFromMap(mapID, true);
        if(u == null)
            return new Integer[0];
        return ((ActivePlayer)player).checkArea(u.getMapID(), u.getMoveSpeed(), u.getUnitID(), terrainMap, false);
    }

    private Integer[] getAttackRange(int mapID){
        int attackRange;
        Unit u = getUnitFromMap(mapID, true);
        if(u instanceof RangedUnit)
            attackRange = ((RangedUnit) u).getAttackRange();
        else
            attackRange = 1;

        return ((ActivePlayer)player).checkArea(u.getMapID(), attackRange, u.getUnitID(), terrainMap, true);
    }

    public void resetMapIdManipulated(){
        mapIdManipulated = -1;
    }

    public void recruitFromTownMenu(int unitIdToAdd){
        Unit movingUnit = getUnitFromMap(mapIdManipulated, true);
        if(movingUnit != null && !movingUnit.checkIfMoved()){
            //take all surrounding tiles and add unit to first empty one
            Integer[] moves = ((ActivePlayer)player).checkArea(mapIdManipulated, 1, unitIdToAdd, terrainMap, false);
            for (int move : moves) {
                if (move > -1 && move < terrainMap.length && ((ActivePlayer)player).spaceAvaliableMove(move) == 1
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
            double[] stats = ((ActivePlayer)player).getEnemyStats(mapIdClicked);
            UI.setInfoBar("Enemy Health: " + (int) stats[0] + ", Attack: " + (int) stats[1] + ", Defense: " + stats[2]);
        }
        //if there was no friendly or enemy unit there, display cash instead
        else
            UI.setInfoBar("Cash: " + player.getCash());

        //if friendly unit on a town and you click it, open town menu
        if(movingUnit != null && mapIdClicked >= 0 && mapIdClicked < terrainMap.length &&
                getTerrainAtLocation(mapIdClicked) == 5 && mapIdManipulated == -1
                && ((ActivePlayer)player).moving == -1) {
            mapIdManipulated = mapIdClicked;
            UI.displayTownMenu();
        }
        //nothing has been selected to move yet
        else if(player instanceof ActivePlayer && mapIdManipulated < 0){
            if(movingUnit == null){
                resetMapIdManipulated();
                return;
            }
            if(((ActivePlayer)player).moving == -1){
                beginMoveOrAttack();
            }
            else{
                finishMoveOrAttack(movingUnit, mapIdClicked);
            }
        }
    }

    private void finishMoveOrAttack(Unit movingUnit, int mapIdClicked){
        Integer[] largestArea = findLargestArea(movingUnit);
        int moving = ((ActivePlayer)player).moving;
        for (int move : largestArea) {
            int mapID = move;
            int moveCheck = ((ActivePlayer)player).spaceAvaliableMove(move);
            int unitID = 0;
            boolean friendly = true;
            switch(moveCheck){
                case 0:
                    if(getUnitFromMap(move, true) != null)
                        unitID = getUnitFromMap(move, true).getUnitID();
                    break;
                case 1:
                    if(move == mapIdClicked && !movingUnit.checkIfMoved()){
                        ((ActivePlayer)player).sendMove(mapIdClicked, moving);
                        //set the new foreground. It's friendly because I can't move an unfriendly unit
                        mapID = movingUnit.getMapID();
                        unitID = movingUnit.getUnitID();
                        //display cash
                        UI.setInfoBar("Cash: " + player.getCash());
                    }
                    break;
                case 2:
                    //un-highlight enemy unit
                    unitID = getUnitFromMap(move, false).getUnitID();
                    friendly = false;
                    //after its un-highlighted, do combat
                    // (I un-highlight first in case the attack is out of range)
                    if (move == mapIdClicked && !movingUnit.checkIfAttacked()) {
                        UIAttack(movingUnit, moving, move);
                    }
                    break;
            }
            UI.displayForeground(mapID, unitID, friendly, false);
        }
        resetMapIdManipulated();
        ((ActivePlayer)player).moving = -1;
    }

    public void beginMoveOrAttack(){
        Unit movingUnit = getUnitFromMap(mapIdManipulated, true);
        if(movingUnit == null){
            resetMapIdManipulated();
            return;
        }
        highlightSurroundings(movingUnit);

        //current location of unit in the terrain map
        ((ActivePlayer)player).moving = mapIdManipulated;
        //display unit stats
        double[] stats = ((ActivePlayer)player).getMyStats(mapIdManipulated);
        UI.setInfoBar("Health: " + (int) stats[0] + ", Attack: " + (int) stats[1] + ", Defense: " + stats[2]);
        //close menu
        resetMapIdManipulated();
    }

    private void highlightSurroundings(Unit movingUnit){
        Integer[] largestArea = findLargestArea(movingUnit);

        for (int move : largestArea) {
            int moveCheck = ((ActivePlayer)player).spaceAvaliableMove(move);
            //Only highlights friendly units is it is the one currently moving
            if(moveCheck == 0 && move == movingUnit.getMapID())
                UI.displayForeground(move, movingUnit.getUnitID(), true, true);
            if (moveCheck == 1)
                UI.displayForeground(move, 0, true, true);
            else if (moveCheck == 2)
                UI.displayForeground(move, getUnitFromMap(move, false).getUnitID(), false, true);
        }
    }

    private Integer[] findLargestArea(Unit u){
        Integer moves[] = getMoves(mapIdManipulated);
        Integer attacks[] = getAttackRange(mapIdManipulated);

        if (moves.length > attacks.length && u != null && !u.checkIfMoved())
            return moves;
        else
            return attacks;
    }

    //Decided to move a lot of the checks for activePlayer name and stuff to here because I'd like
    // if the UI didn't need any knowledge of that stuff, and only worked on displaying what its told to
    @Override
    public void handlePollResult(JSONArray result){
        boolean active = false;
        String activePlayer;
        //defer JSON work to the InactivePlayer, since its the only one who needs the manipulations
        if(!playerIsActive()){
            active = ((InactivePlayer)player).receiveNewJSON(result);
            UI.displayPollResult();
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

    //TODO I basically just copy/pasted this from UI here. Probably needs some changing
    private void UIAttack(Unit movingUnit, int attackerGridID, int defenderGridID){
        Integer possibleAttacks[] = getAttackRange(movingUnit.getMapID());
        //if enemy if outside of attack range, it will return without attempting an attack
        for(int index : possibleAttacks){
            if(defenderGridID == index){
                String attackResults = ((ActivePlayer)player).attack(defenderGridID, attackerGridID, getMap());
                if (attackResults.equals("Fail")) {
                    UI.displayForeground(attackerGridID, 0, true, false);
                }
                else if (attackResults.equals("Success")) {
                    UI.displayForeground(defenderGridID, 0, true, false);
                }
                UI.setInfoBar(attackResults);
                break;
            }
        }
    }

    //TODO like with UIAttack, pretty much just copy/paste of the UI version
    private void createUnit(int mapID, int unitID){
        Unit newUnit;
        String message;
        int cost;
        switch(unitID){
            case 1:
                cost = 100;
                newUnit = new Archer(mapID, unitID, player.getName(), 300.0);
                message = "Archer";
                break;
            case 2:
                cost = 250;
                newUnit = new Cavalry(mapID, unitID, player.getName(), 900.0);
                message = "Cavalry";
                break;
            case 3:
                cost = 150;
                newUnit = new Swordsman(mapID, unitID, player.getName(), 600.0);
                message = "Swordsman";
                break;
            case 4:
                cost = 200;
                newUnit = new Spearman(mapID, unitID, player.getName(), 450.0);
                message = "spearman";
                break;
            case 5:
                cost = 100000;
                newUnit = new General(mapID, unitID, player.getName(), 2000.0);
                message = "General";
                break;
            default:
                return;
        }
        if(player.getCash() <= cost) {
            player.setCash(player.getCash() - cost);
            message = message + " has been recruited.";
            //Didn't actually move, but sets its moved boolean because new units cant move
            newUnit.moveUnit(mapID);
            newUnit.setHasAttacked(); //ensure the new unit doesn't attack
            player.getMyUnits().put(mapID, newUnit);
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
                unitHealth.put("health", newUnit.getHealth());
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
                    //TODO check results
                }
            });
        }
        else{
            message = "You do not have enough cash.";
        }
        UI.makeToast(message);
    }
}

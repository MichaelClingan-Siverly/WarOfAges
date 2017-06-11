package warofages.gamebackend;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import org.json.JSONArray;

import java.util.Scanner;

import coms309.mike.clientcomm.ClientComm;
import coms309.mike.clientcomm.VolleyCallback;
import coms309.mike.units.RangedUnit;
import coms309.mike.units.Unit;

/**
 * Created by mike on 6/2/2017.
 */

public class UIbackend {
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

    public UIbackend(Context context, String myName, DisplaysChanges ui){
        player = new InactivePlayer(myName, context, ui);
        UI = ui;
        comm = new ClientComm(context);
        towns = new SparseArray<>();
        mapIdManipulated = -1;
        //TODO constructor
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
                                case 5:
                                    //TODO add friendly town
                                    break;
                                case 6:
                                    //TODO add hostile town
                                    break;
                                case 7:
                                    //TODO add neutral town
                                    break;
                            }

                            tID++;
                        }
                        scan.close();
                        //continues here because Volley is asynchronous and I want to wait until its done
//                        finishSettingUp();
                        UI.continueAfterTerrainLoaded();
                    }

                } catch (org.json.JSONException e) {
                    Log.d("getMapFromServer", e.getLocalizedMessage());
                }
            }
        });
    }

    public void waitForTurn(){
        ((InactivePlayer)player).waitForTurn();
    }

    public int getMapSize(){
        if(terrainMap != null)
            return terrainMap.length;
        return -1;
    }

    public int getTerrainAtLocation(int index){
        if(index < terrainMap.length)
            return terrainMap[index];
        return -1;
    }

    public int[] getMap(){
        return terrainMap;
    }

    public void addTown(int mapID, String owner){
        Town town;
        if(owner != null)
            town = new Town(mapID, owner);
        else
            town = new Town(mapID);
        if(!towns.get(mapID).equals(null))
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

    public void becomeInactive(){
        if(player instanceof  InactivePlayer){
            player = new InactivePlayer(player);
            ((InactivePlayer)player).waitForTurn();
        }
    }

    public String checkIfGameOver(){
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
    public Unit getUnitFromMap(final int mapID, boolean friendly){
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

    public Integer[] getMoves(int mapID){
        Unit u = getUnitFromMap(mapID, true);
        return ((ActivePlayer)player).checkArea(u.getMapID(), u.getMoveSpeed(), u.getUnitID(), terrainMap, false);
    }

    public Integer[] getAttackRange(int mapID){
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
                    //TODO I have this marked in UI to move out of it
                    createUnit(move, unitIDtoAdd);
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
            Integer[] largestArea = findLargestArea(movingUnit);
            if(((ActivePlayer)player).moving == -1){
                beginMoveOrAttack();
            }
            else{
                finishMoveOrAttack(movingUnit, mapIdClicked);
            }
        }
    }

    public void finishMoveOrAttack(Unit movingUnit, int mapIdClicked){
        Integer[] largestArea = findLargestArea(movingUnit);
        int moving = ((ActivePlayer)player).moving;
        for (int move : largestArea) {
            int moveCheck = ((ActivePlayer)player).spaceAvaliableMove(move);
            //Clears the images for spaces around where unit moves from
            if (moveCheck == 1) {
                UI.displayForeground(move, 0, true, false);
            }
            //TODO finish this display stuff
            //clears the image for current space and moves unit to new space
            if (move == mapIdClicked && moveCheck == 1 && !movingUnit.checkIfMoved()) {
                ((ActivePlayer)player).sendMove(mapIdClicked, moving);
                //set the new foreground
                displaySingleUnit(movingUnit, false);
                //display cash
                UI.setInfoBar("Cash: " + player.getCash());
            }
            else if (moveCheck == 2) {
                //I need to un-highlight the unit
                displaySingleUnit(getUnitFromMap(move, false), false);
                //after its un-highlighted, do combat
                // (I un-highlight first in case the attack is out of range)
                if (move == mapIdClicked && !movingUnit.checkIfAttacked()) {
                    UIAttack(movingUnit, moving, move);
                }
            }
            else if (moveCheck == 0) {
                displaySingleUnit(getUnitFromMap(move, true), false);
            }
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
}

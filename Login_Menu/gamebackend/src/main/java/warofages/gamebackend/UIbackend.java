package warofages.gamebackend;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Scanner;

import coms309.mike.clientcomm.ClientComm;
import coms309.mike.clientcomm.VolleyCallback;
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
    private Player player;
    public UIbackend(Context context, String myName, DisplaysChanges ui){
        player = new InactivePlayer(myName, context, ui);
        UI = ui;
        comm = new ClientComm(context);
        towns = new SparseArray<>();
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
        if(friendly)
            return player.getFriendlyUnit(mapID);
        else
            return player.getEnemyUnit(mapID);
    }

    public void helpWithMapClicks(){

    }

    public void helpWithTownMenuClicks(){

    }
}

package warofages.gamebackend;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Scanner;

import coms309.mike.clientcomm.ClientComm;
import coms309.mike.clientcomm.VolleyCallback;

/**
 * Created by mike on 6/2/2017.
 */

public class UIbackend {
    private Hashtable table;
    private DisplaysChanges UI;
    private ClientComm comm;
    private int[] terrainMap;
    public UIbackend(Context context, DisplaysChanges ui){
        UI = ui;
        comm = new ClientComm(context);
        table = new Hashtable();
        //TODO constructor
    }

    public void getMapFromServer(){
        JSONArray map = new JSONArray();
        JSONObject map1 = new JSONObject();
        try {
            map1.put("map", "map1");
        } catch (JSONException e) {
            //EditText t = (EditText) findViewById(R.id.error);
            //t.setText(e.toString());
            // t.setVisibility(View.VISIBLE);
        }
        //This is for if we had more than one map available
        //map.put(map1);

        //Makes connection to server and requests the terrain map
        comm.serverPostRequest("adminMap.php", map, new VolleyCallback<JSONArray>() {
            @Override
            public void onSuccess(JSONArray result) {
                try {
                    if (result.getJSONObject(0).getString("code").equals("update_success")) {
                        String mapper = result.getJSONObject(1).getString("Map");
                        Scanner scan = new Scanner(mapper).useDelimiter(":");
                        int mapSize = scan.nextInt();

                        //get rid of key
//                        setButtons();

                        terrainMap = new int[mapSize];

                        int tID = 0;
                        while (scan.hasNextInt()) {
                            terrainMap[tID] = scan.nextInt();
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
        table.put(mapID, town);
    }


}

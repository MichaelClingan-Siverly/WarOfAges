package com.example.bakes.login_menu;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import coms309.mike.clientcomm.ClientComm;
import coms309.mike.clientcomm.VolleyCallback;
/**
 * Created by Bakes on 11/17/16.
 */

public class Admin extends AppCompatActivity{
    private int countLength=0;
    private ArrayList<String> Terrain= new ArrayList<>();
    private ArrayList<String> MapID= new ArrayList<>();
    private int mapSize;
    private Context context;
    public Admin(Context context, int mapSize){
        this.mapSize=mapSize;
        this.context=context;
    }
    public String enable(int size){
        if(countLength==size){
            return "JustRight";
        }
        else if(countLength<size){
            return "TooSmall";
        }
        else{
            return "TooBig";
        }
    }
    public void addTile(int terrain, int mapID){
        Terrain.add(Integer.toString(terrain));
        MapID.add(Integer.toString(mapID));
        countLength++;
    }
    public boolean deleteTile(int terrain, int mapID){
        if(Terrain.contains(Integer.toString(terrain))&& MapID.contains(Integer.toString(mapID))){
            Terrain.remove(Integer.toString(terrain));
            MapID.remove(Integer.toString(mapID));
            countLength--;
            return true;
        }
        else{
            return false;
        }
    }
    public boolean sendMap(){
        if(!enable(mapSize).equals("JustRight")){
            Toast.makeText(context, "Map is not complete.", Toast.LENGTH_SHORT).show();
            return false;
        }
        ClientComm comm = new ClientComm(context);
        JSONArray map = new JSONArray();
        JSONObject terrainob = new JSONObject();
        JSONObject mapIDob = new JSONObject();
        try{
            mapIDob.put("MapID", MapID);
            terrainob.put("TerrainID", Terrain);
        }
        catch (JSONException e){
            System.out.println(e.toString());  //printing e itself can lead to errors.
        }
        map.put(mapIDob);
        map.put(terrainob);
        comm.serverPostRequest("makeMap.php", map, new VolleyCallback<JSONArray>() {
            @Override
            public void onSuccess(JSONArray result) {
                //Toasts display a quick popup message at the bottom of the screen
                Toast toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
                try {
                    if (result.getJSONObject(0).getString("code").equals("update_success")) {
                        toast.setText("Map created. You may press back now.");
                    }
                    else{
                        toast.setText("There was a server error.");
                    }
                }
                catch(JSONException e){
                    toast.setText("There was a client error. Sorry");
                    //it'll default to returning false anyway
                }
                toast.show();
            }
        });
        return true;
    }
}
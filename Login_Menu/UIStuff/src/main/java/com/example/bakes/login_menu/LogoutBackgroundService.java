package com.example.bakes.login_menu;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import coms309.mike.clientcomm.ClientComm;
import coms309.mike.clientcomm.VolleyCallback;

/**
 * Created by msiverly on 12/3/16.
 * A background intent to help log out players when they have swiped the app closed
 * Used https://developer.android.com/training/best-background.html
 * and http://stackoverflow.com/a/38496939
 *
 */

public class LogoutBackgroundService extends IntentService {
    public LogoutBackgroundService(){
        super(LogoutBackgroundService.class.getName());
    }

    /**
     * Immediately logs the player out. To be used when the user kills the app by swiping
     * @param intent the intent used to start this service.
     *               Requires a String extra called "username" containing the name of the player
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("background intent", "intent started");
        String username = intent.getStringExtra("username");
        ClientComm comm = new ClientComm(getApplicationContext());
        JSONArray nameArray = new JSONArray();
        JSONObject nameObject = new JSONObject();
        try {
            nameObject.put("userID", username);
        }
        catch(JSONException e){
            //TODO
        }
        nameArray.put(nameObject);
        comm.serverPostRequest("logout.php", nameArray, new VolleyCallback<JSONArray>() {
            @Override
            public void onSuccess(JSONArray result) {
                Log.d("backgroundLogout", "successful");
            }
        });
    }
//    @Override
//    public void onTaskRemoved(Intent rootIntent){
//        ClientComm comm = new ClientComm(getApplicationContext());
//        JSONArray nameArray = new JSONArray();
//        JSONObject nameObject = new JSONObject();
//        try {
//            nameObject.put("userID", username);
//        }
//        catch(JSONException e){
//            //TODO
//        }
//        nameArray.put(nameObject);
//        comm.serverPostRequest("logout.php", nameArray, new VolleyCallback<JSONArray>() {
//            @Override
//            public void onSuccess(JSONArray result) {
//                Log.d("backgroundLogout", "successful");
//            }
//        });
//    }
}

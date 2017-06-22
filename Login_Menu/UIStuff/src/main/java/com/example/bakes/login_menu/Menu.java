package com.example.bakes.login_menu;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import coms309.mike.clientcomm.ClientComm;
import coms309.mike.clientcomm.VolleyCallback;

public class Menu extends AppCompatActivity {
    String name = "";
    boolean movedToOtherIntent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu2);


        Intent intent2 = getIntent();
        name = intent2.getStringExtra("username");
        Button playGame = (Button) findViewById(R.id.NewGame);
        Button watchGame = (Button) findViewById(R.id.Spectator);
        Button logOut = (Button) findViewById(R.id.Logout);
        Button editor = (Button) findViewById(R.id.Editor);
        final Context context = this;

        if(intent2.hasExtra("message") && intent2.getStringExtra("message").equals("leftGame")){
            Toast.makeText(context, "Please remember to logout.", Toast.LENGTH_LONG).show();
        }

        playGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToGame(name, false);
            }
        });
        watchGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToGame(name, true);
            }
        });
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doLogout();
            }
        });
        editor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(name.equals("admin")) {
                    Intent intent = new Intent(context, UIAdmin.class);
                    intent.putExtra("username", name);
                    startActivity(intent);
                    movedToOtherIntent = true;
                    finish();
                }
                else{
                    Toast.makeText(context, "Only admins may create maps.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void goToGame(final String name, boolean spectator){
        Intent intent = new Intent(getApplicationContext(), com.example.bakes.login_menu.UI.class);
        intent.putExtra("username", name);
        if(spectator){
            intent.putExtra("spectator", "spectator");
        }
        startActivity(intent);
        movedToOtherIntent = true;
        finish();
    }

    @Override
    public void onBackPressed(){
        doLogout();
    }
    private void doLogout(){
        final Context context = getApplicationContext();
        ClientComm comm = new ClientComm(context);
        JSONArray nameArray = new JSONArray();
        JSONObject nameObject = new JSONObject();
        try {
            nameObject.put("userID", name);
        }
        catch(JSONException e){
            Log.d("MenuUserID", e.getLocalizedMessage());
        }
        nameArray.put(nameObject);
        comm.serverPostRequest("logout.php", nameArray, new VolleyCallback<JSONArray>() {
            @Override
            public void onSuccess(JSONArray result) {
                //sends user back to the login page.
                try {
                    if (result.getJSONObject(0).getString("code").equals("Game_over")) {
                        Intent intent = new Intent(context, Login.class);
                        intent.putExtra("message", "Logout successful");
                        startActivity(intent);
                        movedToOtherIntent = true;
                        finish();
                    }
                    else{
                        Log.d("logout result", "result did not give Game_over code");
                    }
                }
                catch(JSONException e){
                    Log.d("logout result", "json exception caught");
                }

            }
        });
    }
    //Forces a logout if the user swipes the app closed
    @Override
    public void onDestroy(){
        if(isFinishing() && !movedToOtherIntent){
            Intent forceLogout = new Intent(this, com.example.bakes.login_menu.LogoutBackgroundService.class);
            forceLogout.putExtra("username", name);
            startService(forceLogout);
        }
        super.onDestroy();
    }
}
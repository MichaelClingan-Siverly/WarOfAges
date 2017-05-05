package com.example.bakes.login_menu;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import coms309.mike.clientcomm.ClientComm;
import coms309.mike.clientcomm.VolleyCallback;

import static com.example.bakes.login_menu.R.attr.showText;

public class Register extends AppCompatActivity {
    Button button;
    private String ruse;
    private String rpass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        logMeIn();
    }
    public void logMeIn(){
        final Context context=this;
        button=(Button) findViewById(R.id.logmein);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegtoString();
                Intent intent = new Intent(context, Login.class);
                startActivity(intent);
            }
        });
    }
    public void RegtoString(){
        ClientComm comm = new ClientComm(this.getApplicationContext());
        JSONArray register= new JSONArray();
        JSONObject user = new JSONObject();
        JSONObject pass = new JSONObject();

        EditText rusertext= (EditText) findViewById(R.id.rusername);
        EditText rpasstext= (EditText) findViewById(R.id.rpassword);
        ruse= rusertext.getText().toString();
        rpass= rpasstext.getText().toString();
        try {
            user.put("userID", ruse);
            pass.put("password", rpass);
        }
        catch(JSONException e){
            System.out.println(e);
        }
        register.put(user);
        register.put(pass);
        comm.serverPostRequest("register.php", register, new VolleyCallback<JSONArray>() {
            @Override
            public void onSuccess(JSONArray result) {
                //TODO something with the array returned from the server

            }
        });
    }
}
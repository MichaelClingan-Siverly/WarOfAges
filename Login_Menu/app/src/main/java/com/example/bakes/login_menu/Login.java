package com.example.bakes.login_menu;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import coms309.mike.clientcomm.ClientComm;
import coms309.mike.clientcomm.VolleyCallback;


public class Login extends AppCompatActivity {
    private String use;
    private String pass;
    Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        startReg();
        loginCheck();
    }
    public void startReg(){
        final Context context=this;
        button= (Button) findViewById(R.id.registerme);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Register.class);
                startActivity(intent);
            }
        });

    }
    public void loginCheck(){
        final Context context=this;
        button= (Button) findViewById(R.id.letmein);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                UPtoString();
            }
        });
    }
    public void UPtoString(){
        final Context context=this;
        ClientComm comm = new ClientComm(this.getApplicationContext());
        JSONArray login= new JSONArray();
        JSONObject user = new JSONObject();
        JSONObject password = new JSONObject();
        EditText usertext= (EditText) findViewById(R.id.username);
        EditText passtext= (EditText) findViewById(R.id.password);
        use= usertext.getText().toString();
        pass= passtext.getText().toString();
        try {
            user.put("userID", use);
            password.put("password", pass);
        }
        catch(JSONException e){
            System.out.println(e);
        }

        login.put(user);
        login.put(password);
        comm.serverPostRequest("login.php", login, new VolleyCallback<JSONArray>() {
            @Override
            public void onSuccess(JSONArray result) {
                try{
                    if(result.getString(0).contains("success")){
                        Intent intent = new Intent(context, Menu.class);
                        startActivity(intent);
                    }
                    else{
                        EditText t = (EditText) findViewById(R.id.error);
                        t.setText(result.getString(1));
                        t.setVisibility(View.VISIBLE);
                    }
                }
                catch(org.json.JSONException e){
                    EditText t = (EditText) findViewById(R.id.error);
                    t.setText(e.toString());
                    t.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
//        comm.serverGetRequest(null, null, new VolleyCallback<String>() {
//            @Override
//            public void onSuccess(String result) {
//                if(result.equals(use)){
//                    startReg();
//                }
//            }
//        });
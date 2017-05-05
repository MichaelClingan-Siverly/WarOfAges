package com.example.bakes.registration;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    private String regis="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void RegtoString(){
        EditText usertext= (EditText) findViewById(R.id.rusername);
        EditText passtext= (EditText) findViewById(R.id.rpassword);
        regis+= usertext.getText().toString();
        regis+= passtext.getText().toString();
    }
}

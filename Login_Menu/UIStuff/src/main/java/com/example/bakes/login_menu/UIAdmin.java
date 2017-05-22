package com.example.bakes.login_menu;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import warofages.mapmaker.Admin;

public class UIAdmin extends AppCompatActivity {
    boolean movedToOtherIntent = false;
    //The number of squares in the map. Needs clean squareroot
    int mapSize = 100;
    //size of the map tiles
    int tileSize = 100;
    //popup window
    PopupWindow popup;
    //current tile to be changed
    int changing = -1;
    //enter size popup window
    PopupWindow sizePopup;
    //size text box
    EditText sizeBox;
    //Admin object for server connection
    Admin sender;
    //array of map terrain types
    final int desert = 1;
    final int forest = 2;
    final int meadow = 3;
    final int mountain = 4;
    final int town = 5;
    final int pond = 6;
    final int OFFSET = 10000;
//TODO: make layout for the grid sepections after size entered.
    private final View.OnClickListener sizeEnter = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //get the EditText result from the map_maker_size_popup layout
            EditText sizeEdit = (EditText) sizePopup.getContentView().findViewById(R.id.sizePopupEdit);
            //only do stuff if something is actually entered.
            //TODO just ran it and it crashed when nothing was entered. gotta fix that.
            if (sizeEdit.getText().toString() != "") {
                int sizeEntered = Integer.parseInt(sizeEdit.getText().toString());

                if (sizeEntered >= 100) {
                    Toast.makeText(getApplicationContext(), "Too large. Map size reduced to 99.", Toast.LENGTH_SHORT).show();
                    sizeEntered = 99;
                } else if (sizeEntered < 2) {
                    Toast.makeText(getApplicationContext(), "Too small. Map size increased to 2.", Toast.LENGTH_SHORT).show();
                    sizeEntered = 2;
                }
                mapSize = sizeEntered * sizeEntered;
                sizePopup.dismiss();
                initialize();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_maker);

        //on click listener for create button
        Button b = (Button)findViewById(R.id.B3);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //inflate and display the popup
                LayoutInflater inflater1 = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                View layout = inflater1.inflate(R.layout.map_maker_size_popup, (ViewGroup)findViewById(R.id.sizePopup));
                sizePopup = new PopupWindow(layout,300,370,true);
                sizePopup.setBackgroundDrawable(new BitmapDrawable());
                sizePopup.showAtLocation(layout,Gravity.TOP,0,500);

                //set button to close the size popup
                Button close = (Button)layout.findViewById(R.id.sizePopupEnter);
                close.setOnClickListener(sizeEnter);
            }
        });
    }

    public void initialize(){
        //initialize admin
        sender = new Admin(this.getApplicationContext(), mapSize);

        //initialize types

        //change end turn button to send button
        Button endTurn = (Button) findViewById(R.id.B3);
        endTurn.setText("Send");

        //add buttons to onClickListener(zoom in and zoom out)
        Button b = (Button)findViewById(R.id.B1);
        b.setOnClickListener(onClickListener);
        b = (Button)findViewById(R.id.B2);
        b.setOnClickListener(onClickListener);
        b = (Button)findViewById(R.id.B3);
        b.setOnClickListener(onClickListener);

        //Create table of any size(must be square)

        //Creates an initial row
        TableRow rowTerrain = new TableRow(this);

        //Getting the terrain and army grid from the xml
        TableLayout layoutT = (TableLayout) findViewById(R.id.inLayout);
        TableLayout layoutA = (TableLayout) findViewById(R.id.outLayout);

        //Adds current row to their respective grid (Army is created stacked over terrain)
        layoutT.addView(rowTerrain);

        int rowlength = 0;
        TableRow.LayoutParams params = new TableRow.LayoutParams(tileSize,tileSize);
        for(int id = 0;id < mapSize;id++){
            //if row is filled
            if(rowlength == Math.sqrt(mapSize)){
                //Creates a new row for both grids
                rowTerrain = new TableRow(this);
                //Adds row to grids
                layoutT.addView(rowTerrain);
                rowlength = 0;
            }

            //creates image and adds it to terrain
            ImageView image = new ImageView(this);
            image.setId(id);
            rowTerrain.addView(image);
            //he had a separate method just for this, which is odd since it always adds the same image
            image.setImageResource(R.drawable.p0);
            image.setLayoutParams(params);
            image.setOnClickListener(onClickListener);
            rowlength++;

        }
        //initialize popup window
        initPopup();
    }

    private void initPopup(){
        LinearLayout popLayout;
        ImageView image;
        popup = new PopupWindow(this);
        popLayout = new LinearLayout(this);
        //I start at 1, because 'p0' is a blank tile, which we don't want the admin to place on the map
        for(int i = 1; i <= 6; i++){
            image = new ImageView(this);
            //I didn't know how to get the identifier when cleaning things up.
            //Found it below in loadTerrain, in a line from an old group member.
            int resID = getResources().getIdentifier("p"+i,"drawable", getPackageName());
            image.setImageResource(resID);
            //added 10000, because I know it'll be larger than any id generated as part of the map
            //and sizeBox is at id 10000
            image.setId(i + OFFSET);
            image.setOnClickListener(onClickListener);
            popLayout.addView(image, new LinearLayout.LayoutParams(100,100));
        }
        popup.setContentView(popLayout);
        popup.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    //processes clicks
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //increases/decreases size of map when clicked button is increase size button
            if(R.id.B1 == v.getId() || R.id.B2 == v.getId()) {
                if(v.getId() == R.id.B1)
                    tileSize += 100;
                else if(tileSize > 100)
                    tileSize -= 100;

                //loops through entire map by getting their imageviews from the ids
                for (int id = 0; id < mapSize; id++) {
                    ImageView image = (ImageView) findViewById(id);

                    //sets sizes to size variable increased by 100
                    TableRow.LayoutParams parms = new TableRow.LayoutParams(tileSize, tileSize);
                    image.setLayoutParams(parms);
                    image = (ImageView) findViewById(id + mapSize);
                    image.setLayoutParams(parms);
                }
            }
            else if(R.id.B3 == v.getId()){
                sender.sendMap();
            }
            else if(changing != -1) {
                int resId = 0;
                String terrainName = "";
                //subtracted 10000 because I ended up adding it when creating the popup id's
                int id = v.getId() - OFFSET;
                switch(id){
                    case desert:
                        resId = R.drawable.p1;
                        terrainName = "desert";
                        break;
                    case forest:
                        resId = R.drawable.p2;
                        terrainName = "forest";
                        break;
                    case meadow:
                        resId = R.drawable.p3;
                        terrainName = "meadow";
                        break;
                    case mountain:
                        resId = R.drawable.p4;
                        terrainName = "mountain";
                        break;
                    case town:
                        resId = R.drawable.p5;
                        terrainName = "town";
                        break;
                    case pond:
                        resId = R.drawable.p6;
                        terrainName = "pond";
                        break;
                }
                if(resId != 0){
                    ImageView toChange = (ImageView) findViewById(changing);
                    toChange.setImageResource(resId);
                    sender.addTile(terrainName, changing);
                }
                popup.dismiss();
                changing = -1;
            }
            else{
                ScrollView scroll = (ScrollView) findViewById(R.id.scroll);
                popup.showAtLocation(scroll, Gravity.TOP, 0, 500);
                changing = v.getId();
            }
        }
    };
    @Override
    public void onBackPressed(){
        if(sizePopup != null && sizePopup.isShowing()){
            sizePopup.dismiss();
            sizePopup = null;
        }
        else {
            Intent menuIntent = new Intent(getApplicationContext(), com.example.bakes.login_menu.Menu.class);
            menuIntent.putExtra("username", getIntent().getStringExtra("username"));
            startActivity(menuIntent);
            movedToOtherIntent = true;
            finish();
        }
    }

    @Override
    public void onDestroy(){
        if(isFinishing() && !movedToOtherIntent){
            Intent forceLogout = new Intent(this, com.example.bakes.login_menu.LogoutBackgroundService.class);
            //name must be admin to even be here
            forceLogout.putExtra("username", "admin");
            startService(forceLogout);
        }
        super.onDestroy();
    }
}
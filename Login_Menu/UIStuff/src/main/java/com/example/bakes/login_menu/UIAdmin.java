package com.example.bakes.login_menu;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
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

public class UIAdmin extends AppCompatActivity {
    boolean movedToOtherIntent = false;
    //The number of squares in the map. Needs clean squareroot
    int mapSize = 100;
    //size of the map tiles
    int size = 100;
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
    int[] types;
    final int desert = 0;
    final int forest = 1;
    final int meadow = 2;
    final int mountain = 3;
    final int town = 4;
    final int pond = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //create popup to set map size
        sizePopup = new PopupWindow(this);
        LinearLayout sizeLayout = new LinearLayout(this);
        sizeLayout.setOrientation(LinearLayout.VERTICAL);

        //add text boxes to popup to enter mapsize
        sizeBox = new EditText(this);
        sizeBox.setInputType(InputType.TYPE_CLASS_NUMBER);
        sizeBox.setHint("ex: 10");
        sizeBox.setId(10005);
        ViewGroup.LayoutParams sizeParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView text = new TextView(this);
        text.setText("enter map size");
        Button enter = new Button(this);
        enter.setText("enter");
        sizeLayout.addView(text, sizeParams);
        sizeLayout.addView(sizeBox, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sizeLayout.addView(enter, sizeParams);
        sizePopup.setContentView(sizeLayout);
        sizePopup.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        sizePopup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        sizePopup.getBackground().setAlpha(51); //20% opaque
//        sizePopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //on click listener for enter button
        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int sizeEntered = Integer.parseInt(sizeBox.getText().toString());
                if(sizeEntered >= 100){
                    Toast.makeText(getApplicationContext(), "Too large. Map size reduced to 99.", Toast.LENGTH_SHORT).show();
                    sizeEntered = 99;
                }
                else if(sizeEntered < 2){
                    Toast.makeText(getApplicationContext(), "Too small. Map size increased to 2.", Toast.LENGTH_SHORT).show();
                    sizeEntered = 2;
                }
                mapSize = sizeEntered * sizeEntered;
                sizePopup.setFocusable(false);
                sizePopup.update();
                sizePopup.dismiss();
                initialize();
            }
        });

        //change end turn button to create button
        Button b = (Button) findViewById(R.id.B3);
        b.setText("create");

        //on click listener for create button
//        Button b = (Button)findViewById(R.id.B3);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show popup
                ScrollView scroll = (ScrollView) findViewById(R.id.scroll);
                sizePopup.showAtLocation(scroll, Gravity.TOP, 0, 500);
                sizePopup.setFocusable(true);
                sizePopup.update();
            }
        });
    }

    public void initialize(){
        //initialize admin
        sender = new Admin(this.getApplicationContext(), mapSize);

        //initialize types
//        types = new int[mapSize];

        //change end turn button to send button
        Button endTurn = (Button) findViewById(R.id.B3);
        endTurn.setText("Send");

        //initialize popup window
        initPopup();

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
        TableRow rowArmy = new TableRow(this);

        //Getting the terrain and army grid from the xml
        TableLayout layoutT = (TableLayout) findViewById(R.id.inLayout);
        TableLayout layoutA = (TableLayout) findViewById(R.id.outLayout);

        //Adds current row to their respective grid (Army is created stacked over terrain)
        layoutT.addView(rowTerrain);
        layoutA.addView(rowArmy);

        int rowlength = 0;
        for(int id = 0;id < mapSize;id++){
            //if row is filled
            if(rowlength == Math.sqrt(mapSize)){
                //Creates a new row for both grids
                rowTerrain = new TableRow(this);
                rowArmy = new TableRow(this);
                //Adds row to grids
                layoutT.addView(rowTerrain);
                layoutA.addView(rowArmy);
                rowlength = 0;
            }

            //creates image and adds it to terrain
            ImageView image = new ImageView(this);
            image.setId(id);
            rowTerrain.addView(image);

            //creates image and adds it to army
            image = new ImageView(this);
            image.setId(id + mapSize);
            //Sets and onclick listener to the army id
            image.setOnClickListener(onClickListener);
            rowArmy.addView(image);
            rowlength++;
        }

        for(int x = 0;x < mapSize;x++){
            loadTerrain(0, x);
        }
    }

    private void initPopup(){
        LinearLayout popLayout;
        ImageView image;
        popup = new PopupWindow(this);
        popLayout = new LinearLayout(this);
        for(int i = 0; i <= 5; i++){
            image = new ImageView(this);
            //I would have liked to iterate over the imageResources in a better way
            //but I don't know how, so this is the best I can do right now
            switch(i){
                case desert:
                    image.setImageResource(R.drawable.p1);
                    break;
                case forest:
                    image.setImageResource(R.drawable.p2);
                    break;
                case meadow:
                    image.setImageResource(R.drawable.p3);
                    break;
                case mountain:
                    image.setImageResource(R.drawable.p4);
                    break;
                case town:
                    image.setImageResource(R.drawable.p5);
                    break;
                case pond:
                    image.setImageResource(R.drawable.p6);
                    break;
            }
            image.setId(10000 + i);
            image.setOnClickListener(onClickListener);
            popLayout.addView(image, new LinearLayout.LayoutParams(100,100));
        }
        popup.setContentView(popLayout);
        popup.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void loadTerrain(int terrain, int id){
        //gets imageview object at given id
        ImageView image = (ImageView) findViewById(id);
        //finds terrain image (called p + given terrain ex p1) and puts it into the imageview
        String picName = "p" + Integer.toString(terrain);

        //gets and sets reference for picture ID
        int resID = getResources().getIdentifier(picName, "drawable", getPackageName());
        image.setImageResource(resID);

        //sets sizes of the image and the overlapping army layer using size variable.
        //army is id + mapsize because the army is an overlapping, seperate map
        TableRow.LayoutParams parms = new TableRow.LayoutParams(size,size);
        image.setLayoutParams(parms);
        image = (ImageView) findViewById(id + mapSize);
        image.setLayoutParams(parms);
    }

    //processes clicks
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //increases size of map when clicked button is increase size button
            if(R.id.B1 == v.getId()) {
                size += 100;
                //loops through entire map by getting their imageviews from the ids
                for (int id = 0; id < mapSize; id++) {
                    ImageView image = (ImageView) findViewById(id);

                    //sets sizes to size variable increased by 100
                    TableRow.LayoutParams parms = new TableRow.LayoutParams(size, size);
                    image.setLayoutParams(parms);
                    image = (ImageView) findViewById(id + mapSize);
                    image.setLayoutParams(parms);
                }
            }//decreases size of map when button clicked is decrease size button
            else if(R.id.B2 == v.getId()) {
                if (size > 100) {
                    size -= 100;
                }
                //loops through entire map by getting their imageviews from the ids
                for (int id = 0; id < mapSize; id++) {
                    ImageView image = (ImageView) findViewById(id);

                    //sets sizes to size variable decreased by 100 (min of 100)
                    TableRow.LayoutParams parms = new TableRow.LayoutParams(size, size);
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
                switch(v.getId() - 10000){
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
                    ImageView toChange = (ImageView) findViewById(changing + mapSize);
                    toChange.setImageResource(resId);
                    sender.addTile(terrainName, changing);
                }
                popup.dismiss();
                changing = -1;
            }
            else{
                ScrollView scroll = (ScrollView) findViewById(R.id.scroll);
                popup.showAtLocation(scroll, Gravity.TOP, 0, 500);
                changing = v.getId() - mapSize;
            }
        }
    };
    @Override
    public void onBackPressed(){
        Intent menuIntent = new Intent(getApplicationContext(), com.example.bakes.login_menu.Menu.class);
        menuIntent.putExtra("username", getIntent().getStringExtra("username"));
        startActivity(menuIntent);
        movedToOtherIntent = true;
        finish();
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
package com.example.bakes.login_menu;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.Toast;

import warofages.mapmaker.Admin;

public class UIAdmin extends AppCompatActivity {
    final int desert = 1;
    final int forest = 2;
    final int meadow = 3;
    final int mountain = 4;
    final int town = 5;
    final int pond = 6;
    final int NUM_TER_KINDS = 6;
    final int OFFSET = 10000;

    boolean movedToOtherIntent = false;
    //The number of squares in the map. Needs clean squareroot
    int mapSize;
    //size of the map tiles
    int tileSize = 100;
    //popup window
    PopupWindow terrainPopup;
    //location of current tile to be changed. Since map IDs are >= 0, neg #'s mean no tile changing
    int changing = -1;
    //enter size popup window
    AlertDialog sizeDialog;
    //Admin object for server connection
    Admin sender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_maker);

        //on click listener for create button
        Button b = (Button)findViewById(R.id.confirm);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildSizeDialog();
                //set button which closes the size popup
                Button close = (Button)sizeDialog.findViewById(R.id.sizePopupEnter);
                if(close != null)
                    close.setOnClickListener(sizeEnter);
            }
        });
    }
    private void buildSizeDialog(){
        /*
         * Ok, so this doesn't seem worth it. Not only did I make the xml, all this stuff
         * that I couldn't seem to put in the xml, but I also think the old popup looked better
         *
         * Oh well, it was good practice. (Hooray for Google!)
         */
        //inflate the layout
        LayoutInflater inflater1 = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater1.inflate(R.layout.map_maker_size_popup, (ViewGroup)findViewById(R.id.sizePopup));

        //create the dialog and add the inflated layout to it
        AlertDialog.Builder builder = new AlertDialog.Builder(UIAdmin.this);
        AlertDialog dialog = builder.create();
        dialog.setView(layout);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        //I also save the dialog because I need to get the text from it
        sizeDialog = dialog;
    }

    private final View.OnClickListener sizeEnter = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //get the EditText result from dialog
            EditText sizeEdit = (EditText)sizeDialog.findViewById(R.id.sizePopupEdit);

            //only do stuff if something is actually entered.
            if(sizeEdit != null && !sizeEdit.getText().toString().equals("")) {
                sizeDialog.dismiss();
                sizeDialog = null;
                //can initialize Admin with the size entered by user
                int sizeEntered = Integer.parseInt(sizeEdit.getText().toString());
                proceedAfterSizeEntered(sizeEntered);

            }
            //user pressed enter without providing input (don't dismiss the popup in this case)
            else{
                Toast.makeText(getApplicationContext(), "Enter a side length or press back to cancel.", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void proceedAfterSizeEntered(int chosenMapSize){
        sender = new Admin(getApplicationContext(), chosenMapSize);
        //mapSize may not be what was entered, as there is a min and max size we allow.
        mapSize = sender.getMapSize();
        if(mapSize < chosenMapSize*chosenMapSize)
            Toast.makeText(getApplicationContext(), "Map too large. Reduced to 99.", Toast.LENGTH_SHORT).show();
        else if(mapSize > chosenMapSize*chosenMapSize)
            Toast.makeText(getApplicationContext(), "Map too small. Increased to 2.", Toast.LENGTH_SHORT).show();
        initMap();
        initTerrainPopup();
    }
    //Create square table of any size within the limits which are already enforced
    public void initMap(){
        //change create button to send button
        Button b = (Button) findViewById(R.id.confirm);
        b.setText("Send");
        b.setOnClickListener(editMapClicks);
        //add buttons to onClickListener(zoom in and zoom out)
        b = (Button)findViewById(R.id.zoomIn);
        b.setOnClickListener(editMapClicks);
        b = (Button)findViewById(R.id.zoomOut);
        b.setOnClickListener(editMapClicks);

        //Creates an initial row
        TableRow rowTerrain = new TableRow(this);

        //Getting the terrain and army grid from the xml
        TableLayout mapLayout = (TableLayout) findViewById(R.id.mapLayout);

        //Adds current row to their respective grid (Army is created stacked over terrain)
        mapLayout.addView(rowTerrain);

        TableRow.LayoutParams params = new TableRow.LayoutParams(tileSize,tileSize);
        for(int id = 0, rowLength = 0; id < mapSize; id++, rowLength++){
            //if row is filled
            if(rowLength == Math.sqrt(mapSize)){
                //Creates a new row for both grids
                rowTerrain = new TableRow(this);
                //Adds row to grids
                mapLayout.addView(rowTerrain);
                rowLength = 0;
            }

            //creates image and adds it to terrain
            ImageView image = new ImageView(this);
            image.setId(id);
            rowTerrain.addView(image);
            //he had a separate method just for this, which is odd since it always adds the same image
            image.setImageResource(R.drawable.p0);
            image.setLayoutParams(params);
            image.setOnClickListener(editMapClicks);
        }
    }

    //I'm actually ok with leaving this as it is. It's not very big, and allows for more terrain to
    //be added more easily than if I were to make a layout for the popup (having to add more code to
    //for each new terrain added
    private void initTerrainPopup(){
        LinearLayout popLayout;
        ImageView image;
        terrainPopup = new PopupWindow(this);
        popLayout = new LinearLayout(this);
        //I start at 1, because 'p0' is a blank tile, which we don't want the admin to place on the map
        for(int i = 1; i <= NUM_TER_KINDS; i++){
            image = new ImageView(this);
            //I didn't know how to get the identifier when cleaning things up.
            //Found it below in loadTerrain, in a line from an old group member.
            int resID = getResources().getIdentifier("p"+i,"drawable", getPackageName());
            image.setImageResource(resID);
            //added 10000, because I know it'll be larger than any id generated as part of the map
            image.setId(i + OFFSET);
            image.setOnClickListener(editMapClicks);
            popLayout.addView(image, new LinearLayout.LayoutParams(100,100));
        }
        terrainPopup.setContentView(popLayout);
        terrainPopup.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        terrainPopup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    //processes clicks (after size is selected)
    View.OnClickListener editMapClicks = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //zoom button clicked, so everything in grid needs resized
            if(R.id.zoomIn == v.getId() || R.id.zoomOut == v.getId()) {
                if(v.getId() == R.id.zoomIn)
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
            //send button clicked
            else if(R.id.confirm == v.getId()){
                sender.sendMap();
            }
            //map grid is clicked: find the ID of the tile to be changed
            else if(changing == -1){
                ScrollView scroll = (ScrollView) findViewById(R.id.scroll);
                terrainPopup.showAtLocation(scroll, Gravity.TOP, 0, 500);
                changing = v.getId();
            }
            else{
                int resId = 0;
                String terrainName = "";
                //subtracted 10000 because I ended up adding it when creating the popup id's
                int id = v.getId() - OFFSET;
                //finds which image was selected in the terrain popup
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
                terrainPopup.dismiss();
                changing = -1;
            }
        }
    };
    @Override
    public void onBackPressed(){
        if(terrainPopup != null && terrainPopup.isShowing())
            terrainPopup.dismiss();
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
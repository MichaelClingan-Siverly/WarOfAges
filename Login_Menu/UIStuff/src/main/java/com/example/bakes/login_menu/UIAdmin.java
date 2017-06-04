package com.example.bakes.login_menu;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import android.widget.Toast;
import java.lang.reflect.Field;

import java.util.ArrayList;

import warofages.mapmaker.Admin;

public class UIAdmin extends AppCompatActivity {
    final int OFFSET = 10000;
    boolean movedToOtherIntent = false;
    //size of the map tiles
    int tileSize = 100;
    //popup window
    PopupWindow terrainPopup;
    //enter size popup window
    AlertDialog sizeDialog;
    //Admin object for server connection
    Admin sender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_maker);
        sender = new Admin(getApplicationContext());
        sender.findTiles(R.drawable.class.getFields());

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
        sender.initMap(chosenMapSize);
        //mapSize may not be what was entered, as there is a min and max size we allow.
        if(sender.getMapSize() < chosenMapSize*chosenMapSize)
            Toast.makeText(getApplicationContext(), "Map too large. Reduced to 99.", Toast.LENGTH_SHORT).show();
        else if(sender.getMapSize() > chosenMapSize*chosenMapSize)
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

        //Getting the outer layout from the xml
        LinearLayout mapLayout = (LinearLayout) findViewById(R.id.mapMakerLayout);

        //Creates an initial column
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        mapLayout.addView(column);

        //set images to all columns. create new columns as needed
        for(int id = 0, rowLength = 0; id < sender.getMapSize(); id++, rowLength++){
            //if column is filled, begin new column and set parameters for it
            if(rowLength == Math.sqrt(sender.getMapSize())){
                //Creates a new row for both grids
                column = new LinearLayout(this);
                //we move every second column down a bit
                if((id / rowLength) % 2 == 1)
                    adjustColumnParams(column, rowLength, false);
                else
                    adjustColumnParams(column, rowLength, true);

                //Adds column to the matrix
                mapLayout.addView(column);
                rowLength = 0;
            }

            //creates image and adds it to terrain. HexagonMaskView has default size of 100x100
            HexagonMaskView image = new HexagonMaskView(this);
            image.setId(id);
            column.addView(image);

            //he had a separate method just for this, which is odd since it always adds the same image
            image.setImageResource(R.drawable.blank_tile);
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
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100,100);
        popLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout popRow = new LinearLayout(this);
        //made it add tiles to the popup dynamically - in case more tile types are added later
        for(int i = 0; i < sender.getNumTiles(); i++){
            image = new ImageView(this);
            int resID = sender.getTileID(i);
            image.setImageResource(resID);
            //added 10000, because I know it'll be larger than any id generated as part of the map
            image.setId(i + OFFSET);
            image.setOnClickListener(editMapClicks);
            popRow.addView(image, params);

            if(i % 5 == 4){
                popLayout.addView(popRow);
                popRow = new LinearLayout(this);
            }
        }
        popLayout.addView(popRow);
        terrainPopup.setContentView(popLayout);
        terrainPopup.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        terrainPopup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        terrainPopup.setBackgroundDrawable(null);
    }

    /* Should not be called on first column
     * first column doesn't need special parameters besides being set to vertical.
     * evenIndex: true if index 0,2,4... false if its odd
     */
    private void adjustColumnParams(LinearLayout column, int rowLength, boolean evenIndex){
        //set orientation just in case it hasn't been done at creation
        column.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params;
        if(evenIndex) {
            params = new LinearLayout.LayoutParams(tileSize, rowLength * tileSize);
            params.setMargins(-tileSize/4, 0,0,0);
        }
        else {
            params = new LinearLayout.LayoutParams(tileSize, rowLength * tileSize + tileSize / 2);
            params.setMargins(-tileSize/4, (int)(Math.sqrt(3.0)/2 * tileSize / 2), 0, -(int)(Math.sqrt(3.0)/2 * tileSize / 2));
        }
        column.setLayoutParams(params);
    }

    public void zoomClick(View v){
        boolean changed = false;
        switch (v.getId()){
            case R.id.mapMakerZoomIn:
                if(tileSize < 500) {
                    tileSize += 100;
                    changed = true;
                }
                break;
            case R.id.mapMakerZoomOut:
                if(tileSize > 100) {
                    tileSize -= 100;
                    changed = true;
                }
                break;
        }
        if(changed){
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(tileSize, tileSize);
            //increases size of all map images
            for (int id = 0; id < sender.getMapSize(); id++) {
                HexagonMaskView image = (HexagonMaskView) findViewById(id);
                //sets sizes to size variable increased by 100
                image.setLayoutParams(imageParams);
                //was able to remove the second image resize here, since THERE IS NO SECOND IMAGE LAYER
            }

            //loop through all columns after the first and adjust their parameters
            LinearLayout rootLayout = (LinearLayout) findViewById(R.id.mapMakerLayout);
            LinearLayout column;
            //number of columns = number of rows, since I only make square maps, so I can use count as rowLength
            int count = rootLayout.getChildCount();

            for (int i = 1; i < count; i++) {
                column = (LinearLayout)rootLayout.getChildAt(i);
                if(i % 2 == 1)
                    adjustColumnParams(column, count, false);
                else
                    adjustColumnParams(column, count, true);
            }
        }
    }

    private void setTile(int index){
        //user can click on the map while terrainPopup is showing, so I check for that
        if(index >= sender.getNumTiles() || index < 0)
            return;

        int resID = sender.addTile(index);
        //updates the terrain that was selected to be changed
        ImageView toChange = (ImageView) findViewById(sender.getChangingIndex());
        toChange.setImageResource(resID);
    }

    //processes clicks (after size is selected)
    View.OnClickListener editMapClicks = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //send button clicked
            if(R.id.confirm == v.getId()){
                sender.sendMap();
            }
            //map grid is clicked: find the ID of the tile to be changed
            else if(sender.getChangingIndex() == -1){
                ScrollView scroll = (ScrollView) findViewById(R.id.mapMakerScroll);
                terrainPopup.showAtLocation(scroll, Gravity.TOP, 0, 500);
                sender.setChanging(v.getId());
            }
            else{
                //subtracted 10000 because I ended up adding it when creating the popup id's
                int id = v.getId() - OFFSET;
                setTile(id);
                terrainPopup.dismiss();
                sender.resetChanging();
            }
        }
    };
    @Override
    public void onBackPressed(){
        if(terrainPopup != null && terrainPopup.isShowing()) {
            terrainPopup.dismiss();
            sender.resetChanging();
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
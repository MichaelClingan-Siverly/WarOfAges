package com.example.bakes.login_menu;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;

import coms309.mike.units.Unit;
import warofages.gamebackend.DisplaysChanges;
import warofages.gamebackend.InactivePlayer;
import warofages.gamebackend.UIbackend;

public class UI extends AppCompatActivity implements DisplaysChanges {
    boolean movedToOtherIntent = false;

    private UIbackend uiBackend;
    private final int MOVE_ICON_ID = 10000;

    //The number of squares in the map. Used so often that I save the value in here
    private int mapSize;
    //size of the map tiles image
    int tileSize = 100;
    //username
    String username = "";
    //current player
//    Player player;
//    //list of my units.     Using until active player has move checking implimmented
//    ArrayList<Unit> myArmy;
//    //list of enemy units
//    ArrayList<Unit> enemyArmy;
    //list of terrain locations
//    int terrainMap[];
    int cash;
    //if click on town with friendly unit and 0, open town menu.
    // if 1, currently using popup. if 2, moving unit.
    int unitVtown;
    //town menu
    PopupWindow townMenu;
    //popul showing whose turn it is
    PopupWindow endMenu;
    //text in endMenu
    TextView endText;
    //scrollview. class wide so inactive player can use it.
    ScrollView scroller;
    boolean gameOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        username = getIntent().getStringExtra("username");
        boolean spectator = getIntent().hasExtra("spectator");

        //initialize gameOn
        gameOn = true;

        //initialize scroller
        scroller = (ScrollView) findViewById(R.id.scroll);

        unitVtown = -1;

        makeTownMenu();
        makeEndMenu();

        uiBackend = new UIbackend(getApplicationContext(), username, spectator, this);
    }

    //shows whose turn it is
    private void makeEndMenu(){
        endMenu = new PopupWindow(this);
        LinearLayout popLayout = new LinearLayout(this);
        endText = new TextView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        endText.setText(R.string.EndTextDefault);
        popLayout.addView(endText, layoutParams);
        endMenu.setContentView(popLayout);
        endMenu.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        endMenu.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        //I didn't like how ugly the black box is
        endMenu.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    //initialize townMenu window
    private void makeTownMenu(){
        townMenu = new PopupWindow(this);
        LinearLayout popLayout = new LinearLayout(this);
        ImageView image;
        ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(150, 150);
        ArrayList<Integer> icons = findTownMenuIDs();

        for(int i = 0; i < icons.size(); i++){
            image = new ImageView(this);
            image.setImageResource(icons.get(i));
            image.setId(MOVE_ICON_ID + i);
            image.setOnClickListener(townMenuListener);
            popLayout.addView(image, layoutParams);
        }
        townMenu.setContentView(popLayout);
        //The menu didn't show up on mobile devices (worked on emulator though). fixed by http://stackoverflow.com/a/39363218
        townMenu.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        townMenu.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        townMenu.getBackground().setAlpha(102); //40% opaque
    }

    private ArrayList<Integer> findTownMenuIDs(){
        ArrayList<Integer> unitIDs = new ArrayList<>();
        unitIDs.add(R.drawable.move_icon);
        Field[] fields = R.drawable.class.getFields();
        for(Field field : fields){
            if(field.getName().startsWith("unit") && field.getName().endsWith("friendly")){
                try {
                    unitIDs.add(field.getInt(null));
                }
                catch(IllegalAccessException e){

                }
            }
        }
        return unitIDs;
    }

    //Create table of any size(must be square)
    private void createTerrainButtons(){
        //Creates an initial row
        LinearLayout mapLayout = (LinearLayout) findViewById(R.id.gameLayout);

        //Creates an initial column
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        mapLayout.addView(column);

        //set images to all columns. create new columns as needed
        for(int id = 0, rowLength = 0; id < mapSize; id++, rowLength++){
            //if column is filled, begin new column and set parameters for it
            if(rowLength == Math.sqrt(mapSize)){
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
            image.setOnClickListener(gameClickListener);
        }
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

    private void loadSingleTerrainToButton(int tID, int mapID){
        String picName = "";
        switch (tID) {
            case 1:
                picName = "tile_desert";
                break;
            case 2:
                picName = "tile_forest";
                break;
            case 3:
                picName = "tile_meadow";
                break;
            case 4:
                picName = "tile_mountain";
                break;
            case 5:
                picName = "tile_town_friendly";
                break;
            case 6:
                picName = "tile_town_hostile";
                break;
            case 7:
                picName = "tile_town_neutral";
                break;
            case 8:
                picName = "tile_water";
                break;
        }
        //gets and sets reference for picture ID. "p12" would indicate something went wrong
        int resID = getResources().getIdentifier(picName.equals("") ? "p12" : picName, "drawable", getPackageName());
        HexagonMaskView image = getImage(mapID);
        Drawable oldForeground = image.getForeground();

        image.setImageResource(resID);
        image.setForeground(oldForeground);
    }

    //load given terrain at given id
    public void loadTerrainToButtons(){
        for(int id = 0; id < mapSize; id++) {
            int terrainTypeID = uiBackend.getTerrainAtLocation(id);
            //gets imageview object at given id
            loadSingleTerrainToButton(terrainTypeID, id);
        }
    }

    HexagonMaskView getImage(int mapID){
        int mapRoot = (int)Math.sqrt(mapSize);
        int x = mapID / mapRoot;
        int y = mapID % mapRoot;
        LinearLayout layout = (LinearLayout)findViewById(R.id.gameLayout);
        layout = (LinearLayout)layout.getChildAt(x);
        return (HexagonMaskView)layout.getChildAt(y);
    }

    public void clearMap(){
        for(int x = 0; x < mapSize; x++){
            clearImage(x);
        }
    }
    private void clearImage(int mapID){
        if(getImage(mapID).getForeground() != null)
            getImage(mapID).setForeground(null);
    }

    public void updateUnits(boolean friendly){
        SparseArray<Unit> units;
        if(friendly) {
            units = uiBackend.getPlayer().getMyUnits();
        }
        else{
            units = uiBackend.getPlayer().getEnemyUnits();
        }
        for (int i = 0; i < units.size(); i++) {
            displaySingleUnit(units.valueAt(i), false);
        }
    }

    public void bottomBarListener(View v){
        boolean resized = false;
        switch (v.getId()){
            case R.id.endTurn:
                uiBackend.endTurn();
                return;
            case R.id.zoomIn:
                if(tileSize < 500) {
                    tileSize += 100;
                    resized = true;
                }
                break;
            case R.id.zoomOut:
                if(tileSize > 100) {
                    tileSize -= 100;
                    resized = true;
                }
                break;
        }
        if(resized){
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(tileSize, tileSize);
            for (int id = 0; id < mapSize; id++) {
                HexagonMaskView image = getImage(id);
                image.setLayoutParams(params);
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

    //processes clicks when the town menu is open
    View.OnClickListener townMenuListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //clicked on move button
            if (v.getId() == MOVE_ICON_ID)
                uiBackend.beginMoveOrAttack();
            //clicked on one of the add unit buttons
            //TODO eventually towns will be able to recruit without a friendly unit on it
            else if (v.getId() > MOVE_ICON_ID) {
                int unitIDtoAdd = v.getId() - MOVE_ICON_ID;
                uiBackend.recruitFromTownMenu(unitIDtoAdd);
            }
            /*  if user didn't click on move or unit icons, they must have pressed the map or
             *  something, in which case this listener shouldn't have been notified so this shouldn't happen
             */
            else
                uiBackend.resetMapIdManipulated();
            //close menu regardless of the user's choice
            townMenu.dismiss();
        }
    };

    //processes clicks
    View.OnClickListener gameClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v){
            uiBackend.helpWithMapClicks(v.getId());
        }
    };

    public void displayTownMenu(){
        ScrollView scroll = (ScrollView) findViewById(R.id.scroll);
        townMenu.showAtLocation(scroll, Gravity.TOP, 0, 500);
    }

    public void dismissTownMenu(){
        townMenu.dismiss();
    }

    public void displayForeground(int mapID, int unitID, boolean friendly, boolean selected){
        int drawableID;
        switch(unitID){
            case 0:
                if(selected)
                    drawableID = R.drawable.selected_tile;
                else
                    drawableID = -1;
                break;
            case 1: //archer
                if(friendly && selected)
                    drawableID = R.drawable.unit_archer_friendly_selected;
                else if(friendly)
                    drawableID = R.drawable.unit_archer_friendly;
                else if(selected)
                    drawableID = R.drawable.unit_archer_hostile_selected;
                else
                    drawableID = R.drawable.unit_archer_hostile;
                break;
            case 2: //cavalry
                if(friendly && selected)
                    drawableID = R.drawable.unit_cavalry_friendly_selected;
                else if(friendly)
                    drawableID = R.drawable.unit_cavalry_friendly;
                else if(selected)
                    drawableID = R.drawable.unit_cavalry_hostile_selected;
                else
                    drawableID = R.drawable.unit_cavalry_hostile;
                break;
            case 3: //swordsman
                if(friendly && selected)
                    drawableID = R.drawable.unit_sword_friendly_selected;
                else if(friendly)
                    drawableID = R.drawable.unit_sword_friendly;
                else if(selected)
                    drawableID = R.drawable.unit_sword_hostile_selected;
                else
                    drawableID = R.drawable.unit_sword_hostile;
                break;
            case 4: //spearman
                if(friendly && selected)
                    drawableID = R.drawable.unit_spear_friendly_selected;
                else if(friendly)
                    drawableID = R.drawable.unit_spear_friendly;
                else if(selected)
                    drawableID = R.drawable.unit_spear_hostile_selected;
                else
                    drawableID = R.drawable.unit_spear_hostile;
                break;
            case 5: //general
                if(friendly && selected)
                    drawableID = R.drawable.unit_general_friendly_selected;
                else if(friendly)
                    drawableID = R.drawable.unit_general_friendly;
                else if(selected)
                    drawableID = R.drawable.unit_general_hostile_selected;
                else
                    drawableID = R.drawable.unit_general_hostile;
                break;
            default:
                return;
        }
        HexagonMaskView image = getImage(mapID);
        if(drawableID != -1)
            image.setForeground(getDrawable(drawableID));
        else
            image.setForeground(null);
    }

    private void displaySingleUnit(Unit unit, boolean selected){
        boolean friendly = false;
        if(unit.getOwner().equals(username)){
            friendly = true;
        }
        int unitDrawableID = -1;
        switch(unit.getUnitID()){
            case 1: //archer
                if(friendly && selected)
                    unitDrawableID = R.drawable.unit_archer_friendly_selected;
                else if(friendly)
                    unitDrawableID = R.drawable.unit_archer_friendly;
                else if(selected)
                    unitDrawableID = R.drawable.unit_archer_hostile_selected;
                else
                    unitDrawableID = R.drawable.unit_archer_hostile;
                break;
            case 2: //cavalry
                if(friendly && selected)
                    unitDrawableID = R.drawable.unit_cavalry_friendly_selected;
                else if(friendly)
                    unitDrawableID = R.drawable.unit_cavalry_friendly;
                else if(selected)
                    unitDrawableID = R.drawable.unit_cavalry_hostile_selected;
                else
                    unitDrawableID = R.drawable.unit_cavalry_hostile;
                break;
            case 3: //swordsman
                if(friendly && selected)
                    unitDrawableID = R.drawable.unit_sword_friendly_selected;
                else if(friendly)
                    unitDrawableID = R.drawable.unit_sword_friendly;
                else if(selected)
                    unitDrawableID = R.drawable.unit_sword_hostile_selected;
                else
                    unitDrawableID = R.drawable.unit_sword_hostile;
                break;
            case 4: //spearman
                if(friendly && selected)
                    unitDrawableID = R.drawable.unit_spear_friendly_selected;
                else if(friendly)
                    unitDrawableID = R.drawable.unit_spear_friendly;
                else if(selected)
                    unitDrawableID = R.drawable.unit_spear_hostile_selected;
                else
                    unitDrawableID = R.drawable.unit_spear_hostile;
                break;
            case 5: //general
                if(friendly && selected)
                    unitDrawableID = R.drawable.unit_general_friendly_selected;
                else if(friendly)
                    unitDrawableID = R.drawable.unit_general_friendly;
                else if(selected)
                    unitDrawableID = R.drawable.unit_general_hostile_selected;
                else
                    unitDrawableID = R.drawable.unit_general_hostile;
                break;
        }

        HexagonMaskView image = getImage(unit.getMapID());
        image.setForeground(getDrawable(unitDrawableID));
    }

    @Override
    public void onBackPressed(){
        if(!uiBackend.playerIsActive()){
            ((InactivePlayer)uiBackend.getPlayer()).killPoll();
        }
        Intent intent = new Intent(getApplicationContext(), com.example.bakes.login_menu.Menu.class);
        intent.putExtra("username", username);
        intent.putExtra("message", "leftGame");
        startActivity(intent);
        finish();
    }

    @Override
    public void onDestroy(){
        Log.d("UI Destroy", "destroy called");
        if(isFinishing() && !movedToOtherIntent) {
            endMenu.dismiss();
            Intent forceLogout = new Intent(this, com.example.bakes.login_menu.LogoutBackgroundService.class);
            forceLogout.putExtra("username", username);
            startService(forceLogout);
        }
        else{
            Log.d("destroying", "was not finishing");
        }
        super.onDestroy();
    }

    public void setInfoBar(String text){
        TextView info = (TextView) findViewById(R.id.infoBar);
        info.setText(text);
    }

    public void makeToast(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setEndText(String text){
        endText.setText(text);
        endMenu.showAtLocation(scroller, Gravity.BOTTOM, 0, 400);
    }

    @Override
    public void dismissEndText(){
        if(endMenu.isShowing())
            endMenu.dismiss();
    }

    @Override
    public void displayPollResult(){
        clearMap();
        updateUnits(true);
        updateUnits(false);
    }

    @Override
    public void displayTerrain(int mapSize){
        this.mapSize = mapSize;
        createTerrainButtons();
        loadTerrainToButtons();
    }

    @Override
    public void changeTownOwnership(int newTerrainID, int mapID){
        loadSingleTerrainToButton(newTerrainID, mapID);
    }
}
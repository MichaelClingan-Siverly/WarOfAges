package com.example.bakes.login_menu;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Scanner;

import coms309.mike.clientcomm.ClientComm;
import coms309.mike.clientcomm.VolleyCallback;
import coms309.mike.units.Archer;
import coms309.mike.units.Cavalry;
import coms309.mike.units.General;
import coms309.mike.units.Spearman;
import coms309.mike.units.Swordsman;
import coms309.mike.units.Unit;

public class UI extends AppCompatActivity {
    boolean movedToOtherIntent = false;

    //The number of squares in the map. Needs clean squareroot
    static int mapSize = 100;
    //size of the map tiles image
    int size = 100;
    //username
    String username = "";
    //current player
    static Player player;
    //list of my units.     Using until active player has move checking implimmented
    static ArrayList<Unit> myArmy;
    //list of enemy units
    static ArrayList<Unit> enemyArmy;
    //list of terrain locations
    int terrainMap[];
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

        //initialize gameOn
        gameOn = true;

        //initialize scroller
        scroller = (ScrollView) findViewById(R.id.scroll);

        //initialize terrainMap
//        terrainMap = new ArrayList<>();
        unitVtown = -1;

        //Image selections of popup
        ImageView image1;
        ImageView image2;
        ImageView image3;
        ImageView image4;
        ImageView image5;
        //popup layout
        LinearLayout popLayout;

        //initialize townMenu window
        townMenu = new PopupWindow(this);
        popLayout = new LinearLayout(this);
        image1 = new ImageView(this);
        image2 = new ImageView(this);
        image3 = new ImageView(this);
        image4 = new ImageView(this);
        image5 = new ImageView(this);
        ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(150, 150);
        image1.setImageResource(R.drawable.p11);
        image1.setId(10000);
        image1.setOnClickListener(onClickListener);
        image2.setImageResource(R.drawable.archer_friendly);
        image2.setId(10001);
        image2.setOnClickListener(onClickListener);
        image3.setImageResource(R.drawable.cavalry_friendly);
        image3.setId(10002);
        image3.setOnClickListener(onClickListener);
        image4.setImageResource(R.drawable.spear_friendly);
        image4.setId(10003);
        image4.setOnClickListener(onClickListener);
        image5.setImageResource(R.drawable.sword_friendly);
        image5.setId(10004);
        image5.setOnClickListener(onClickListener);

        popLayout.addView(image1, layoutParams);
        popLayout.addView(image2, layoutParams);
        popLayout.addView(image3, layoutParams);
        popLayout.addView(image4, layoutParams);
        popLayout.addView(image5, layoutParams);
        townMenu.setContentView(popLayout);
        //The menu didn't show up on mobile devices (worked on emulator though). fixed by http://stackoverflow.com/a/39363218
        townMenu.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        townMenu.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        townMenu.getBackground().setAlpha(102); //40% opaque

        //initialize end popup window
        endMenu = new PopupWindow(this);
        popLayout = new LinearLayout(this);
        endText = new TextView(this);
        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        endText.setText("Turn over");
        popLayout.addView(endText, layoutParams);
        endMenu.setContentView(popLayout);
        endMenu.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        endMenu.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        //I didn't like how ugly the black box is
        endMenu.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //add buttons to onClickListener(zoom in and zoom out)
        Button b = (Button)findViewById(R.id.B1);
        b.setOnClickListener(onClickListener);
        b = (Button)findViewById(R.id.B2);
        b.setOnClickListener(onClickListener);
        b = (Button)findViewById(R.id.B3);
        b.setOnClickListener(onClickListener);

        getTerrain();



    }

    private void finishSettingUp(){
        //get passed in username
        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        player = new InactivePlayer(this.getApplicationContext(), username);
        //players start game with 1000 cash
//        player.setCash(1000);
        cash = 1000;

        //display cash
        setInfoBar("Cash: " + cash);

        //Only players call this. Spectators do not need to get players.
        if(!intent.hasExtra("spectator")){
            Log.d("game start", "calling getPlayers");
            getPlayers();
        }

        ((InactivePlayer)player).waitForTurn(this);
    }

    private void setButtons(){
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
        //TODO I'm positive the map size bug is around here.
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
    }

    //gets map from server
    public void getTerrain() {
        ClientComm comm = new ClientComm(getApplicationContext());
        JSONArray map = new JSONArray();
        JSONObject map1 = new JSONObject();
        try {
            map1.put("map", "map1");
        } catch (JSONException e) {
            //EditText t = (EditText) findViewById(R.id.error);
            //t.setText(e.toString());
            // t.setVisibility(View.VISIBLE);
        }
        //This is for if we had more than one map available
        //map.put(map1);

        //Makes connection to server and requests the terrain map
        comm.serverPostRequest("adminMap.php", map, new VolleyCallback<JSONArray>() {
            @Override
            public void onSuccess(JSONArray result) {
                Log.d("basicMap result", result.toString());
                try {
                    if (result.getJSONObject(0).getString("code").contains("update_success")) {
                        String mapper = result.getJSONObject(1).toString();
                        //get rid of key
                        mapper = mapper.substring(8);
                        adjustMapSize(mapper);
                        Log.d("med", "map size: " +mapSize);
                        setButtons();
                        terrainMap = new int[mapSize];
                        //get rid of all characters which were necessary for a connection success
                        mapper=mapper.replaceAll("A","");
                        Scanner scan=new Scanner(mapper).useDelimiter(":");
                        while (scan.hasNextInt()) {
                            int terrain = scan.nextInt();
                            int tid = scan.nextInt();
                            loadTerrain(terrain, tid);
                        }
                        scan.close();
                        finishSettingUp();
                    }

                } catch (org.json.JSONException e) {

                }
            }
        });
    }

    private void adjustMapSize(String mapString){
        mapSize = 0;
        for(int i = 0; i < mapString.length(); i++){
            if(mapString.charAt(i) == ':'){
                mapSize++;
            }
        }
        mapSize = mapSize / 2;
    }

    //gets players from server
    private void getPlayers(){
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
        comm.serverPostRequest("getPlayers.php", nameArray, new VolleyCallback<JSONArray>() {
            @Override
            public void onSuccess(JSONArray result) {
                //don't need to do anything here. But I check code for testing purposes
                Log.d("getPlayers result", result.toString());
            }
        });
    }

    //load given terrain at given id
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

        terrainMap[id] = terrain;
    }

    public void clearMap(){
        for(int x = 0; x < mapSize; x++){
            clearImage(x);
        }
    }

    public void updateUnits(ArrayList<Unit> units, boolean friendly){
        if(friendly) {
            for (int x = 0; x < units.size(); x++) {
                displaySingleUnit(units.get(x), false);
            }
        }
        else{
            for (int x = 0; x < units.size(); x++) {
                displaySingleUnit(units.get(x), false);
            }
        }
    }

    public void endTurn(){
        if(player instanceof ActivePlayer){
            ClientComm comm = new ClientComm(getApplicationContext());
            comm.serverPostRequest("checkActivePlayer.php", new JSONArray(), new VolleyCallback<JSONArray>() {
                @Override
                public void onSuccess(JSONArray result) {
                    endTurnHelper();
                }
            });

        }
    }
    //because I can't access this UI's instance from the onSuccess inner Class
    private void endTurnHelper(){
        String end = player.endgame();
        if(end.equals("Game in Progress")){
            setInfoBar("Cash: " + cash);
            player = new InactivePlayer(player);
            ((InactivePlayer)player).waitForTurn(this);
        }
        else{
            gameOn = false;
            setInfoBar(end);
        }



    }


    //processes clicks
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v){
            if(gameOn) {
                int vID = v.getId();
                //increases size of map when clicked button is increase size button
                if (R.id.B1 == v.getId()) {
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
                else if (R.id.B2 == v.getId()) {
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
                } else if (player instanceof ActivePlayer) {
                    //end turn button
                    if ((R.id.B3 == v.getId()) && (player instanceof ActivePlayer)) {
                        endTurn();
                        return;
                    }

                    int currentMapClicked = v.getId() - mapSize;
                    //Get the unit thats moving (only from myArmy), so I can get its movespeed and stuff
                    Unit movingUnit;
                    if (((ActivePlayer) player).moving == -1) {
                        movingUnit = getUnitFromMap(currentMapClicked, true);
                    } else {
                        movingUnit = getUnitFromMap(((ActivePlayer) player).moving, true);
                    }
                    if (movingUnit != null && movingUnit.checkIfMoved() && movingUnit.checkIfAttacked()) {
                        movingUnit = null;
                    }

                    myArmy = player.getMyUnits();
                    enemyArmy = player.getEnemyUnits();
                    Unit enemyUnit = null;
                    if (movingUnit == null) {
                        enemyUnit = getUnitFromMap(currentMapClicked, false);
                    }
                    //if click on enemy unit, display its stats
                    if ((enemyUnit != null) && (player instanceof ActivePlayer)) {
                        //display unit stats
                        double[] stats = ((ActivePlayer) player).getEnemyStats(currentMapClicked);
                        setInfoBar("Enemy Health: " + (int) stats[0] + ", Attack: " + (int) stats[1] + ", Defense: " + stats[2]);
                    }//if click on empty space, display cash
                    else if ((movingUnit == null) && (v.getId() < 10000) && (player instanceof ActivePlayer)) {
                        //display cash
                        setInfoBar("Cash: " + cash);
                    }
                    //if friendly unit on a town and you click it, open town menu
                    else if (currentMapClicked >= 0 && currentMapClicked <= mapSize - 1 &&
                            terrainMap[currentMapClicked] == 5 && (unitVtown == -1) &&
                            (movingUnit != null) && (player instanceof ActivePlayer)
                            && (((ActivePlayer) player).moving == -1)) {

                        ScrollView scroll = (ScrollView) findViewById(R.id.scroll);
                        townMenu.showAtLocation(scroll, Gravity.TOP, 0, 500);
                        unitVtown = currentMapClicked;
                    }//town menu interactions
                    else if ((unitVtown > -1) && (player instanceof ActivePlayer)) {
                        movingUnit = getUnitFromMap(unitVtown, true);

                        if (v.getId() == 10000) {//clicked on move button
                            // I have this little bit in a few places. feels sloppy, but all of UI feels slopy
                            Integer moves[] = getMoves(movingUnit);
                            Integer attacks[] = getAttackRange(movingUnit);
                            Integer largestArea[];
                            if (moves.length > attacks.length && !movingUnit.checkIfMoved()) {
                                largestArea = moves;
                            } else {
                                largestArea = attacks;
                            }
                            //highlight surrounding area
                            for (int move : largestArea) {
                                int moveCheck = ((ActivePlayer) player).spaceAvaliableMove(move);
                                if (moveCheck == 0) {
                                    displaySingleUnit(getUnitFromMap(move, true), true);
                                } else if (moveCheck == 1) {
                                    ImageView image = (ImageView) findViewById(move + mapSize);
                                    image.setImageResource(R.drawable.selected_tile);
                                } else if (moveCheck == 2) {
                                    displaySingleUnit(getUnitFromMap(move, false), true);
                                }
                            }
                            //display unit stats
                            double[] stats = ((ActivePlayer) player).getMyStats(unitVtown);
                            setInfoBar("Health: " + (int) stats[0] + ", Attack: " + (int) stats[1] + ", Defense: " + stats[2]);
                            //current location of unit in the terrain map
                            ((ActivePlayer) player).moving = unitVtown;
                            //close menu
                            unitVtown = -2;
                            townMenu.dismiss();
                        }
                        //TODO
                        else if (v.getId() > 10000 && !movingUnit.checkIfMoved()) {//clicked on one of the add unit buttons
                            //take all surrounding tiles and add unit to first empty one
                            int unitIDtoAdd = v.getId() - 10000;
                            //surrounding tiles
                            Integer[] moves = ((ActivePlayer) player).checkArea(unitVtown, 1, unitIDtoAdd, terrainMap, false);
                            for (int move : moves) {
                                if ((move > -1) && (move < mapSize) && ((ActivePlayer) player).spaceAvaliableMove(move) == 1
                                        && terrainMap[move] != 6
                                        && !(unitIDtoAdd == 5 && terrainMap[move] == 4)
                                        && !(unitIDtoAdd == 2 && terrainMap[move] == 4)) {
                                    //creates unit and sends it to server
                                    createUnit(move, unitIDtoAdd);
                                    // don't actually move the unit, but dont let it move anymore
                                    movingUnit.moveUnit(movingUnit.getMapID());
                                    //close popup
                                    unitVtown = -1;
                                    townMenu.dismiss();
                                    break;
                                }
                                //if not empty space, close without adding unit
                                unitVtown = -1;
                                townMenu.dismiss();
                            }
                        } else {
                            //if anything other than popup clicked on, close popup
                            unitVtown = -1;
                            townMenu.dismiss();
                        }
                    }

            /*
                The difference between the mapIDs and buttonIDs are mapSize (mapID1 = buttonID1 + mapSize)
                so there will be a lot adding and subtracting mapSize below, as needed to perform the actions.
             */
                    else if (player instanceof ActivePlayer && (unitVtown < 0)) {
                        //must be careful not to get nullPointerExceptions if I click on an empty space.
                        if (movingUnit == null) {
                            unitVtown = -1;
                            return;
                        }
                        Integer moves[] = getMoves(movingUnit);
                        Integer attacks[] = getAttackRange(movingUnit);
                        Integer largestArea[];
                        //even if moves are larger than attacks, doesnt matter if unit already moved
                        if (moves.length > attacks.length && !movingUnit.checkIfMoved()) {
                            largestArea = moves;
                        } else {
                            largestArea = attacks;
                        }
                        //Click on a unit (initiate a move)
                        if (((ActivePlayer) player).moving == -1) {
                            for (int move : largestArea) {
                                int moveCheck = ((ActivePlayer) player).spaceAvaliableMove(move);
                                if (moveCheck == 0) {
                                    displaySingleUnit(getUnitFromMap(move, true), true);
                                } else if (moveCheck == 1) {
                                    ImageView image = (ImageView) findViewById(move + mapSize);
                                    image.setImageResource(R.drawable.selected_tile);
                                } else if (moveCheck == 2) {
                                    displaySingleUnit(getUnitFromMap(move, false), true);
                                }
                            }
                            //current location of unit in the terrain map
                            ((ActivePlayer) player).moving = currentMapClicked;

                            //display unit stats
                            double[] stats = ((ActivePlayer) player).getMyStats(currentMapClicked);
                            setInfoBar("Health: " + (int) stats[0] + ", Attack: " + (int) stats[1] + ", Defense: " + stats[2]);
                        }
                        //Click on a space after clicking on a unit (complete the move)
                        else if (((ActivePlayer) player).moving != -1) {
                            int moving = ((ActivePlayer) player).moving;
                            for (int move : largestArea) {
                                int moveCheck = ((ActivePlayer) player).spaceAvaliableMove(move);
                                //Clears the images for spaces around where unit moves from
                                if (moveCheck == 1) {
                                    clearImage(move);
                                }
                                //clears the image for current space and moves unit to new space
                                if (move == currentMapClicked && moveCheck == 1 && !movingUnit.checkIfMoved()) {
                                    clearImage(moving);
                                    ((ActivePlayer) player).sendMove(currentMapClicked, moving);
                                    displaySingleUnit(movingUnit, false);
                                    //display cash
                                    setInfoBar("Cash: " + cash);
                                } else if (moveCheck == 2) {
                                    //I need to un-highlight the unit
                                    displaySingleUnit(getUnitFromMap(move, false), false);
                                    //after its un-highlighted, do combat
                                    // (I un-highlight first in case the attack is out of range)
                                    if (move == currentMapClicked && !movingUnit.checkIfAttacked()) {
                                        UIAttack(movingUnit, moving, move);
                                    }
                                } else if (moveCheck == 0) {
                                    displaySingleUnit(getUnitFromMap(move, true), false);
                                }
                            }
                            unitVtown = -1;
                            ((ActivePlayer) player).moving = -1;
                        }
                    }
                }
            }
            else{
                String end = player.endgame();
                setInfoBar(end);
            }
        }
    };
    private Integer[] getMoves(Unit movingUnit){
        int moveSpeed = movingUnit.getMoveSpeed();
        int gridID = movingUnit.getMapID();
        return ((ActivePlayer)player).checkArea(gridID, moveSpeed, movingUnit.getUnitID(), terrainMap, false);
    }
    private Integer[] getAttackRange(Unit movingUnit){
        int attackRange = 1;
        if(movingUnit instanceof Archer){
            attackRange = 3;
        }
        return ((ActivePlayer)player).checkArea(movingUnit.getMapID(), attackRange, movingUnit.getUnitID(), terrainMap, true);
    }
    private void UIAttack(Unit movingUnit, int attackerGridID, int defenderGridID){
        Integer possibleAttacks[] = getAttackRange(movingUnit);
        //if enemy if outside of attack range, it will return without attempting an attack
        for(int index : possibleAttacks){
            if(defenderGridID == index){
                String attackResults = ((ActivePlayer)player).attack(defenderGridID, attackerGridID, terrainMap);
                if (attackResults.equals("Fail")) {
                    clearImage(attackerGridID);
                } else if (attackResults.equals("Success")) {
                    clearImage(defenderGridID);
                }
                setInfoBar(attackResults);
                break;
            }
        }
    }
    private void clearImage(int mapID){
        ImageView image = (ImageView)findViewById(mapID + mapSize);
        image.setImageResource(android.R.color.transparent);
    }

    /**
     * helper for the buttons
     * @param mapID mapID of the space to be checked for a unit
     * @return the friendly unit corresponding to the space clicked, or null if there is no match
     */
    private Unit getUnitFromMap(final int mapID, boolean friendly){
        if(friendly) {
            for (int i = 0; i < myArmy.size(); i++) {
                if (myArmy.get(i).getMapID() == mapID) {
                    return myArmy.get(i);
                }
            }
        }
        else{
            for(int i = 0; i < enemyArmy.size(); i++){
                if(enemyArmy.get(i).getMapID() == mapID){
                    return enemyArmy.get(i);
                }
            }
        }
        return null;
    }

    @Override
    public void onBackPressed(){
        if(player instanceof InactivePlayer){
            ((InactivePlayer) player).killPoll();
        }
        Intent intent = new Intent(getApplicationContext(), com.example.bakes.login_menu.Menu.class);
        intent.putExtra("username", username);
        intent.putExtra("message", "leftGame");
        startActivity(intent);
        finish();
    }
    private void displaySingleUnit(Unit unit, boolean selected){
        boolean friendly = false;
        if(unit.getOwner().equals(username)){
            friendly = true;
        }
        ImageView image = (ImageView) findViewById(unit.getMapID() + mapSize);
        switch(unit.getUnitID()){
            case 1: //archer
                if(friendly && selected)
                    image.setImageResource(R.drawable.archer_friendly_selected);
                else if(friendly)
                    image.setImageResource(R.drawable.archer_friendly);
                else if(selected)
                    image.setImageResource(R.drawable.archer_hostile_selected);
                else
                    image.setImageResource(R.drawable.archer_hostile);
                break;
            case 2: //cavalry
                if(friendly && selected)
                    image.setImageResource(R.drawable.cavalry_friendly_selected);
                else if(friendly)
                    image.setImageResource(R.drawable.cavalry_friendly);
                else if(selected)
                    image.setImageResource(R.drawable.cavalry_hostile_selected);
                else
                    image.setImageResource(R.drawable.cavalry_hostile);
                break;
            case 3: //swordsman
                if(friendly && selected)
                    image.setImageResource(R.drawable.sword_friendly_selected);
                else if(friendly)
                    image.setImageResource(R.drawable.sword_friendly);
                else if(selected)
                    image.setImageResource(R.drawable.sword_hostile_selected);
                else
                    image.setImageResource(R.drawable.sword_hostile);
                break;
            case 4: //spearman
                if(friendly && selected)
                    image.setImageResource(R.drawable.spear_friendly_selected);
                else if(friendly)
                    image.setImageResource(R.drawable.spear_friendly);
                else if(selected)
                    image.setImageResource(R.drawable.spear_hostile_selected);
                else
                    image.setImageResource(R.drawable.spear_hostile);
                break;
            case 5: //general
                if(friendly && selected)
                    image.setImageResource(R.drawable.general_friendly_selected);
                else if(friendly)
                    image.setImageResource(R.drawable.general_friendly);
                else if(selected)
                    image.setImageResource(R.drawable.general_hostile_selected);
                else
                    image.setImageResource(R.drawable.general_hostile);
                break;
            default:
                break;
        }
    }

    private void createUnit(int mapID, int unitID){
        ClientComm comm = new ClientComm(getApplicationContext());
        // This doesnt really matter, as if a unit is actually made, it will be overritten,
        // but it keeps me from getting errors later
        Unit newUnit = new Archer(mapID, unitID, username,2,300.0,66.66666, 0.10);
        boolean unitCreated = false;
        String message = "";
        int cost;
        switch(unitID){
            case 1:
                cost = 100;
                if(cash >= cost) {
                    cash -= cost;
                    newUnit = new Archer(mapID, unitID, username,2,300.0,66.66666, 0.10);
                    unitCreated = true;
                    message = "Archer";
                }
                break;
            case 2:
                cost = 250;
                if(cash >= cost) {
                    cash -= cost;
                    newUnit = new Cavalry(mapID, unitID, username,4,900.0,100, 0.35);
                    unitCreated = true;
                    message = "Cavalry";
                }
                break;
            case 3:
                cost = 150;
                if(cash >= cost) {
                    cash -= cost;
                    newUnit = new Swordsman(mapID, unitID, username,2,600.0,50,0.5);
                    unitCreated = true;
                    message = "Swordsman";
                }
                break;
            case 4:
                cost = 200;
                if(cash >= cost) {
                    cash -= cost;
                    newUnit = new Spearman(mapID, unitID, username,1,450.0,58.333333,0.70);
                    unitCreated = true;
                    message = "spearman";
                }
                break;
            case 5:
                cost = 100000;
                if(cash >= cost) {
                    cash -= cost;
                    unitCreated = true;
                    newUnit = new General(mapID, unitID, username,5,2000.0,125,.8);
                    message = "General";
                }
                break;
            default:
                break;
        }
        if(unitCreated) {
            message = message + " has been recruited.";
            newUnit.moveUnit(mapID); //Didn't actually move, but sets its moved boolean
            newUnit.setHasAttacked(); //ensure the new unit doesn't attack
            myArmy.add(newUnit);
            //set unit image
            displaySingleUnit(newUnit, false);

            JSONArray requestArray = new JSONArray();
            JSONObject nameObject = new JSONObject();
            JSONObject gridObject = new JSONObject();
            JSONObject unitObject = new JSONObject();
            JSONObject unitHealth = new JSONObject();
            try {
                nameObject.put("userID", username);
                gridObject.put("GridID", mapID);
                unitObject.put("UnitID", unitID);
                unitHealth.put("health", newUnit.getHealth());
            } catch (JSONException e) {
                //TODO
            }
            requestArray.put(nameObject);
            requestArray.put(gridObject);
            requestArray.put(unitObject);
            requestArray.put(unitHealth);

            comm.serverPostRequest("createUnit.php", requestArray, new VolleyCallback<JSONArray>() {
                @Override
                public void onSuccess(JSONArray result) {
                    Log.d("createUnit", result.toString());
                    //TODO check results
                }
            });
        }
        else{
            message = "You do now have enough cash.";
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
    private void setInfoBar(String text){
        TextView info = (TextView) findViewById(R.id.infoBar);
        info.setText(text);
    }
    public void beginTurnMakeMoney(){
//        player.setCash(oldCashAmount);
//        player.cash = oldCashAmount + player.incrementCash(terrainMap);
//        player.setCash(oldCashAmount + player.incrementCash(terrainMap));
        cash += player.incrementCash(terrainMap);
        setInfoBar("Cash: " + cash);
    }
}
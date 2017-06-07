package com.example.bakes.login_menu;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

import coms309.mike.units.Archer;
import coms309.mike.units.Cavalry;
import coms309.mike.units.General;
import coms309.mike.units.Spearman;
import coms309.mike.units.Swordsman;
import coms309.mike.units.Unit;
import warofages.gamebackend.AsyncResultHandler;
import warofages.gamebackend.DisplaysChanges;
import warofages.gamebackend.PollServerTask;

/**
 * Created by Mike on 10/29/2016.
 */

public class InactivePlayer extends Player implements AsyncResultHandler {
    //This array is compared to the results from the server.
    //I only have one inactive player at a time. NOTE: I did not make this a singleton, so that is not enforced in this class.
    static JSONArray playerAndUnits;
    private PollServerTask poll;
    private boolean isSpectator = false;

    public InactivePlayer(String myName, Context context, DisplaysChanges ui){
        //First thing: construct the superclass.
        super(context, myName, ui);
        playerAndUnits = new JSONArray();
    }

    public InactivePlayer(Player oldPlayer){
        super(oldPlayer.context, oldPlayer.myName, oldPlayer.ui);
        this.enemyUnits = oldPlayer.enemyUnits;
        this.myUnits = oldPlayer.myUnits;
        setCash(oldPlayer.getCash());
    }

    //UI isn't really final. It has static values which are changed inside. But it makes me say its final
    public void waitForTurn() {
        //initialize the static json array with json objects of units.
        convertToJson(myUnits, enemyUnits);
        createPoll();
        poll.execute(context);
        /*
            only finishes when I'm now the active player
            but it will still display changes due to the progressUpdate until then
        */
    }
    private void createPoll(){
        //this is performed asynchronously, but the finishProcess part is not async
        poll = new PollServerTask(playerAndUnits, this);
    }

    public void killPoll(){
        poll.cancel(true);
    }

    public boolean isSpectator(){
        return isSpectator;
    }

    public DisplaysChanges getUI(){
        return ui;
    }

    /*
        Checks the enemyUnits arrayList. if any two units have different names, I know I'm a spectator
        If I am a spectator, adjust myUnits and enemyUnits to differentiate the players' armies
     */
    private void checkIfSpectator(){
        String nameOne = "one";
        if(!myUnits.isEmpty()){
            return;
        }
        for(int i = 0; i < enemyUnits.size(); i++){
            if(i == 0){
                nameOne = enemyUnits.get(i).getOwner();
            }
            else if(!enemyUnits.get(i).getOwner().equals(nameOne)){
                //I don't return here because it also separates armies if I am a spectator.
                //no need to possibly iterate through n-1 units to separate them later
                isSpectator = true;
                int mapID = enemyUnits.get(i).getMapID();
                int unitID = enemyUnits.get(i).getUnitID();
                double unitHealth = enemyUnits.get(i).getHealth();
                addUnit(myUnits, myName, mapID, unitID, unitHealth);
//                myUnits.add(enemyUnits.get(i));
                enemyUnits.remove(i);
                i--;
            }
        }
    }

    /**
     * Takes in arrays of armies and uses it to initialize the static playerAndUnits JSON array
     * @param myArmy arrayList containing this player's army
     * @param enemyArmy arrayList containing enemy player's army
     */
    private void convertToJson(ArrayList<Unit> myArmy, ArrayList<Unit> enemyArmy){
        //I don't need the old array anymore.
        playerAndUnits = new JSONArray();
        JSONArray onlyUnits = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        try {
            //the array contains an object for the player
            jsonObject.put("userID", myName);
            playerAndUnits.put(jsonObject);
            ///and an array containing objects of each units
            //my army
            for(int i = 0; i < myArmy.size(); i++){
                jsonObject = new JSONObject();
                jsonObject.put("UnitID", myArmy.get(i).getUnitID());
                jsonObject.put("GridID", myArmy.get(i).getMapID());
                jsonObject.put("userID", myArmy.get(i).getOwner());
                onlyUnits.put(jsonObject);
            }
            //enemy army
            for(int i = 0; i < enemyArmy.size(); i++){
                jsonObject = new JSONObject();
                jsonObject.put("UnitID", enemyArmy.get(i).getUnitID());
                jsonObject.put("GridID", enemyArmy.get(i).getMapID());
                jsonObject.put("userID", enemyArmy.get(i).getOwner());
                onlyUnits.put(jsonObject);
            }
            playerAndUnits.put(onlyUnits);
        } catch (JSONException e) {
            Log.d("convertToJSON", "caught exception " + e.toString());
        }
        //don't need a return. It populates a static oject
    }

    /**
     * converts the private JSONArray into ArrayLists of Units
     * @param checkForMyArmy use true if you want my army, false if enemy army
     * @return an arrayList of Units belonging to whichever army was indicated by the parameter
     */
    private ArrayList<Unit> convertToArrayList(boolean checkForMyArmy){
        ArrayList<Unit> thisArmy = new ArrayList<>();
        int numUnitsInArray = 0;
        try {
            numUnitsInArray = playerAndUnits.getJSONArray(1).length();
        }
        catch(JSONException e) {
            Log.d("converting response", "contained more than one user: " + e.toString());
        }
        try{
            //server's returned array looks like: [{"userID":activePlayer},[{unit1 stuff},{unit2 stuff}, ...]]
            for(int i = 0; i < numUnitsInArray; i++) {
                JSONArray jsonUnitArray = playerAndUnits.getJSONArray(1);
                boolean thisIsMine = jsonUnitArray.getJSONObject(i).get("userID").equals(myName);
                if (checkForMyArmy && thisIsMine || !checkForMyArmy && !thisIsMine) {
                    int mapID = jsonUnitArray.getJSONObject(i).getInt("GridID");
                    int unitID = jsonUnitArray.getJSONObject(i).getInt("UnitID");
                    String owner = jsonUnitArray.getJSONObject(i).getString("userID");
                    double unitHealth = jsonUnitArray.getJSONObject(i).getDouble("health");
                    //easy way of indicating that polling needs to continue.
                    //server replied to my request before its DB was updated.
                    if(owner.equals("null")){
                        playerAndUnits.getJSONObject(0).put("userID", "null");
                    }
                    addUnit(thisArmy, owner, mapID, unitID, unitHealth);
                }
            }
        }
        catch(JSONException e){
            Log.d("convertToArrayList", e.toString());
        }
        return thisArmy;
    }

    private void addUnit(ArrayList<Unit> army, String owner, int mapID, int unitID, double unitHealth){
        switch(unitID){
            case 1:
                army.add(new Archer(mapID, unitID, owner,unitHealth));
                break;
            case 2:
                army.add(new Cavalry(mapID, unitID, owner,unitHealth));
                break;
            case 3:
                army.add(new Swordsman(mapID, unitID, owner,unitHealth));
                break;
            case 4:
                army.add(new Spearman(mapID, unitID, owner,unitHealth));
                break;
            case 5:
                army.add(new General(mapID, unitID, owner,unitHealth));
                break;
            default:
                break;
        }
    }

    @Override
    public void handlePollResult(JSONArray result) {
        playerAndUnits = result;
        myUnits = convertToArrayList(true);
        enemyUnits = convertToArrayList(false);
        //units update everytime a poll is answered
        checkIfSpectator();

        try {
            //creates the active player originally attempted in UI after waitForTurn is called.
            //Having it here forces us to wait until I'm actually the active player before I become active
            if (result.getJSONObject(0).getString("userID").equals(myName))
                killPoll();
            else{
                JSONObject needToReplaceName = new JSONObject();
                needToReplaceName.put("userID", myName);
                playerAndUnits.put(0, needToReplaceName);
            }
        }
        catch(JSONException e){
            Log.d("JSONException", e.getLocalizedMessage());
        }
        //now all I have to do is display the changes
        ui.displayPollResult(playerAndUnits);
    }
}

package warofages.gamebackend;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

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
    private JSONArray playerAndUnits;
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
        convertToJson();

        poll = new PollServerTask(playerAndUnits, this);
        poll.execute(context);
        /*
            only finishes when I'm now the active player
            but it will still display changes due to the progressUpdate until then
        */
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
        if(!checkIfNoUnits(true)){
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
                addUnit(myName, mapID, unitID, unitHealth);
//                myUnits.add(enemyUnits.get(i));
                enemyUnits.remove(i);
                i--;
            }
        }
    }

    //This did far more work than necessary. The polls overwrite this, and nothing uses it beyond getting the player name
    private void convertToJson(){
        //I don't need the old array anymore.
        playerAndUnits = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        try {
            //the array contains an object for the player
            jsonObject.put("userID", myName);
            playerAndUnits.put(jsonObject);
        } catch (JSONException e) {
            Log.d("convertToJSON", "caught exception " + e.toString());
        }
    }

    /**
     * converts the private JSONArray into ArrayLists of Units
     * @param checkForMyArmy use true if you want my army, false if enemy army
     */
    private void convertArmyFromJSON(boolean checkForMyArmy){
        int numUnitsInJSON = 0;
        try {
            numUnitsInJSON = playerAndUnits.getJSONArray(1).length();
        }
        catch(JSONException e) {
            Log.d("converting response", "contained more than one user: " + e.toString());
        }
        try{
            //server's returned array looks like: [{"userID":activePlayer},[{unit1 stuff},{unit2 stuff}, ...]]
            for(int i = 0; i < numUnitsInJSON; i++) {
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
                    addUnit(owner, mapID, unitID, unitHealth);
                }
            }
        }
        catch(JSONException e){
            Log.d("convertToArrayList", e.toString());
        }
    }

    private void addUnit(String owner, int mapID, int unitID, double unitHealth){
        Unit unit;
        switch(unitID){
            case 1:
                unit = new Archer(mapID, unitID, owner,unitHealth);
                break;
            case 2:
                unit = new Cavalry(mapID, unitID, owner,unitHealth);
                break;
            case 3:
                unit = new Swordsman(mapID, unitID, owner,unitHealth);
                break;
            case 4:
                unit = new Spearman(mapID, unitID, owner,unitHealth);
                break;
            case 5:
                unit = new General(mapID, unitID, owner,unitHealth);
                break;
            default:
                unit = null;
                break;
        }
        if(unit != null){
            if(owner.equals(myName))
                myUnits.put(mapID, unit);
            else
                enemyUnits.put(mapID, unit);
        }
    }

    @Override
    public void handlePollResult(JSONArray result) {
        playerAndUnits = result;
        convertArmyFromJSON(true);
        convertArmyFromJSON(false);
        //units update everytime a poll is answered
        checkIfSpectator();

        try {
            //creates the active player originally attempted in UI after waitForTurn is called.
            //Having it here forces us to wait until I'm actually the active player before I become active
            if (result.getJSONObject(0).getString("userID").equals(myName)) {
                killPoll();
                //TODO become active player
            }
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

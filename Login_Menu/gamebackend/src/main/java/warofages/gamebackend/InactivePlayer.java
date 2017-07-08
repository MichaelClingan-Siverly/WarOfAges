package warofages.gamebackend;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.TreeSet;

import coms309.mike.units.Archer;
import coms309.mike.units.Cavalry;
import coms309.mike.units.General;
import coms309.mike.units.Spearman;
import coms309.mike.units.Swordsman;
import coms309.mike.units.Unit;

/**
 * Class used mostly for polling the server and converting JSON to/from more useful formats,
 * since other classes do not receive anything quite as extensive
 * Created by Mike on 10/29/2016.
 */

class InactivePlayer extends Player{
    //This array is compared to the results from the server.
    //I only have one inactive player at a time. NOTE: I did not make this a singleton, so that is not enforced in this class.
    private JSONArray playerAndUnits;
    private PollServerTask poll;
    //if player is spectator, this is name of the player whose units and towns are displayed as friendly
    private String spectatorWatchPlayer = "";

    InactivePlayer(String myName, Context context){
        //First thing: construct the superclass. Could not make the 1000 a final variable, but it's starting cash
        super(context, myName);
        setCash(STARTING_CASH);
        activateUnits();
    }

    InactivePlayer(Player oldPlayer){
        super(oldPlayer.context, oldPlayer.myName);
        this.enemyUnits = oldPlayer.enemyUnits;
        this.myUnits = oldPlayer.myUnits;
        setCash(oldPlayer.getCash());
        activateUnits();
    }

    @Override
    public String getName(){
        if(spectatorWatchPlayer.equals(""))
            return myName;
        else
            return spectatorWatchPlayer;
    }

    //not in constructors because it seems a bit much to hold the whole backend when not really needed
    void waitForTurn(AsyncResultHandler backend) {
        //initialize the static json array with json objects of units.
        convertToJson();

        poll = new PollServerTask(playerAndUnits, backend);
        poll.execute(context);
        /*
            only finishes when I'm now the active player
            but it will still display changes due to the progressUpdate until then
        */
    }

    void killPoll(){
        poll.cancel(true);
    }

    //moved this from ActivePlayer, because an active player rejoining a game would have their units
    // wrongly activated. So now the booleans reset at the end of a turn instead of the beginning
    private void activateUnits(){
        for(int i = 0; i < myUnits.size(); i++){
            if(myUnits.valueAt(i) != null)
                myUnits.valueAt(i).resetMovedAndAttacked();
        }
    }

    /*
        Checks the enemyUnits arrayList. if any two units have different names, I know I'm a spectator
        If I am a spectator, adjust myUnits and enemyUnits to differentiate the players' armies
     */
    private void checkIfSpectator(){
        String nameOne = "one";
        if(myUnits.size() > 0){
            return;
        }
        for(int i = 0; i < enemyUnits.size(); i++){
            if(i == 0){
                nameOne = enemyUnits.valueAt(i).getOwner();
            }
            else if(!enemyUnits.valueAt(i).getOwner().equals(nameOne)){
                //I don't return here because it also separates armies if I am a spectator.
                //no need to possibly iterate through n-1 units to separate them later
                String nameTwo = enemyUnits.valueAt(i).getOwner();
                spectatorWatchPlayer = nameTwo;
                int mapID = enemyUnits.valueAt(i).getMapID();
                int unitID = enemyUnits.valueAt(i).getUnitID();
                double unitHealth = enemyUnits.valueAt(i).getHealth();
                int moved;
                if(enemyUnits.valueAt(i).checkIfMoved())
                    moved = 1;
                else
                    moved = 0;
                int attacked;
                if(enemyUnits.valueAt(i).checkIfAttacked())
                    attacked = 1;
                else
                    attacked = 0;
                addUnit(nameTwo, mapID, unitID, unitHealth, moved, attacked);
                enemyUnits.remove(enemyUnits.keyAt(i));
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
            Log.d("convertToJSON", "caught exception " + e.getLocalizedMessage());
        }
    }

    /**
     * converts the private JSONArray into ArrayLists of Units
     */
    private void convertArmyFromJSON(){
        int numUnitsInJSON = 0;
        myUnits = new SparseArray<>();
        enemyUnits = new SparseArray<>();

        try {
            numUnitsInJSON = playerAndUnits.getJSONArray(1).length();
        }
        catch(JSONException e) {
            Log.d("converting response", "contained more than one user: " + e.getLocalizedMessage());
        }

        try{
            //server's returned array looks like: [{"userID":activePlayer},[{unit1 stuff},{unit2 stuff}, ...]]
            for(int i = 0; i < numUnitsInJSON; i++) {
                JSONArray jsonUnitArray = playerAndUnits.getJSONArray(1);
                int mapID = jsonUnitArray.getJSONObject(i).getInt("GridID");
                int unitID = jsonUnitArray.getJSONObject(i).getInt("UnitID");
                int moved = jsonUnitArray.getJSONObject(i).getInt("moved");
                int attacked = jsonUnitArray.getJSONObject(i).getInt("attacked");
                String owner = jsonUnitArray.getJSONObject(i).getString("userID");
                double unitHealth = jsonUnitArray.getJSONObject(i).getDouble("health");
                //easy way of indicating that polling needs to continue.
                if(owner.equals("friendly") || owner.equals("hostile")){
                    playerAndUnits.getJSONObject(0).put("userID", "null");
                }
                addUnit(owner, mapID, unitID, unitHealth, moved, attacked);
            }
        }
        catch(JSONException e){
            Log.d("convertToArrayList", e.getLocalizedMessage());
        }
    }

    /**
     * adjusts values in this class based on the JSONArray parameter
     * @param jsonArray a JSONArray presumed to have been received by the caller from the server
     *             also presumed have had the towns removed by the caller
     * @return true if the player should be changed to active, false otherwise
     */
    public boolean receiveNewJSON(JSONArray jsonArray){
        playerAndUnits = jsonArray;
        convertArmyFromJSON();
        //units update everytime a poll is answered
        checkIfSpectator();

        try {
            //creates the active player originally attempted in UI after waitForTurn is called.
            //Having it here forces us to wait until I'm actually the active player before I become active
            if (jsonArray.getJSONObject(0).getString("userID").equals(myName)) {
//                killPoll();
                //let caller know the player may now become active
                return true;
            }
        }
        catch(JSONException e){
            Log.d("JSONException", e.getLocalizedMessage());
        }
        return false;
    }

    private void addUnit(String owner, int mapID, int unitID, double unitHealth, int moved, int attacked){
        Unit unit;
        switch(unitID){
            case 1:
                unit = new Archer(mapID, owner,unitHealth);
                break;
            case 2:
                unit = new Cavalry(mapID, owner,unitHealth);
                break;
            case 3:
                unit = new Swordsman(mapID, owner,unitHealth);
                break;
            case 4:
                unit = new Spearman(mapID, owner,unitHealth);
                break;
            case 5:
                unit = new General(mapID, owner,unitHealth);
                break;
            default:
                unit = null;
                break;
        }
        if(unit != null){
            if(moved == 1)
                unit.moveUnit(mapID);
            if(attacked == 1)
                unit.setHasAttacked();

            if(owner.equals(myName) || owner.equals(spectatorWatchPlayer))
                myUnits.put(mapID, unit);
            else
                enemyUnits.put(mapID, unit);
        }
    }
}

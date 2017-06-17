package warofages.gamebackend;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

import coms309.mike.clientcomm.ClientComm;
import coms309.mike.clientcomm.VolleyCallback;
import coms309.mike.units.Archer;
import coms309.mike.units.Cavalry;
import coms309.mike.units.General;
import coms309.mike.units.Spearman;
import coms309.mike.units.Swordsman;
import coms309.mike.units.Unit;


/**
 * Created by Bakes on 10/31/16.
 */
//TODO well...just about everything...
public class ActivePlayer extends Player {

    //TODO I may not even need this. Feels redundant compared to UIBackground's mapIdManipulated
    //moving is mapID of a unit marked to move, or -1 if there is none
    private int moving =- 1;
    private double myHealth;
    private double enemyHealth;
    private Random rand=new Random();

    public ActivePlayer(Context context, String myName){
        super(context, myName);
        setCash(STARTING_CASH);
    }

    public ActivePlayer(Player oldPlayer){
        super(oldPlayer.context, oldPlayer.myName);
        this.enemyUnits = oldPlayer.enemyUnits;
        this.myUnits = oldPlayer.myUnits;
        setCash(oldPlayer.getCash());
    }

    public double[] getUnitStats(int unitMapID, boolean friendly){
        if(friendly)
            return myUnits.get(unitMapID).getMyStats();
        else
            return enemyUnits.get(unitMapID).getMyStats();
    }

    public void setMoveFromMapID(int unitsMapID){
        if(unitsMapID != -1 && moving==-1 && getFriendlyUnit(unitsMapID) != null)
            moving=unitsMapID;
        else
            moving=-1;
    }
    public int getMoveFromMapID(){
        return moving;
    }

    public int checkIfUnitOnSpace(int newMapID){
        final int noMove=0;
        final int canMoveTerrain=1;
        final int canMoveEnemy=2;
        if(getFriendlyUnit(newMapID) != null){
            return noMove;
        }
        else if (getEnemyUnit(newMapID) != null) {
            return canMoveEnemy;
        }
        return canMoveTerrain;
    }



    //TODO do players move? no? Then its a unit thing.
    public void sendMove(int newID, int oldID){
        Unit u = getFriendlyUnit(oldID);
        if(u == null) {
            Log.d("sendMove", "shouldnt try to move a unit that doesn't exist");
            return;
        }
        int i = u.getMapID(); // its mapID = oldID
        if(getEnemyUnit(newID) != null || getFriendlyUnit(newID) != null){
            Log.d("sendMove", "can't move a unit onto another unit");
            return;
        }
        myUnits.get(i).moveUnit(newID);
        ClientComm comm = new ClientComm(context);
        JSONArray move= new JSONArray();
        JSONObject juseid = new JSONObject();
        JSONObject jnewID = new JSONObject();
        JSONObject joldID = new JSONObject();
        try{
            juseid.put("userID",myName);
            jnewID.put("newID",newID); //old mapID of the unit
            joldID.put("oldID",oldID); //new mapID of the unit
        }
        catch(JSONException e){

        }
        move.put(juseid);
        move.put(jnewID);
        move.put(joldID);
        comm.serverPostRequest("movement.php", move, new VolleyCallback<JSONArray>() {
            @Override
            public void onSuccess(JSONArray result) {
                //Done by UI
            }
        });
    }

    //looks like this is where the actual combat damage is done
    //TODO move this to unit classes. It's not a player thing
    private void calculateHealth(int enemyMapID,int myMapID, int mynum, int ennum, int[] terrainMap){
        //Just checking area immediately surrounding the attacker
        Integer[] nextToAttacker = checkSurroundingTerrain(myMapID, 1,1, terrainMap, true);
        double newEDefense = 1 - calculateDefense(enemyDefense,enemyMapID, terrainMap);
        double newMDefense = 1 - calculateDefense(myDefense,myMapID, terrainMap);
        int myRandom=rand.nextInt(6)+1;
        int enemyRandom=rand.nextInt(6)+1;
        int myUnitType = myUnits.get(mynum).getUnitID();
        int enemyUnitType = enemyUnits.get(ennum).getUnitID();
        if(myUnitType==2 && enemyUnitType==1){
            enemyHealth=enemyHealth-1.5*myattack*newEDefense*myRandom;
            myHealth=myHealth-0.5*enemyAttack*newMDefense*enemyRandom;
        }
        else if(myUnitType==4 && enemyUnitType==2){
            enemyHealth=enemyHealth-1.5*myattack*newEDefense*myRandom;
            myHealth=myHealth-0.5*enemyAttack*newMDefense*enemyRandom;
        }
        else if(myUnitType==5 && enemyUnitType==4){
            enemyHealth=enemyHealth-1.5*myattack*newEDefense*myRandom;
            myHealth=myHealth-0.5*enemyAttack*newMDefense*enemyRandom;
        }
        else if(myUnitType==3 && enemyUnitType==5){
            enemyHealth=enemyHealth-1.5*myattack*newEDefense*myRandom;
            myHealth=myHealth-0.5*enemyAttack*newMDefense*enemyRandom;
        }
        else if(myUnitType==1 && enemyUnitType==3){
            enemyHealth=enemyHealth-1.5*myattack*newEDefense*myRandom;
            for(int mapID : nextToAttacker){
                if(enemyMapID == mapID){
                    myHealth=myHealth-0.5*enemyAttack*newMDefense*enemyRandom;
                }
            }
            //else, my health doesnt change (attacked at range)
        }
        //archers dont get attacked back when attacking at range, unless its by an archer
        else if(myUnitType == 1 && enemyUnitType != 1){
            enemyHealth = enemyHealth - myattack*enemyDefense*myRandom;
            for(int mapID : nextToAttacker){
                if(enemyMapID == mapID){
                    myHealth=myHealth-enemyAttack*newMDefense*enemyRandom;
                }
            }
            //else, my health doesnt change (attacked at range)
        }
        else{
            enemyHealth=enemyHealth-myattack*enemyDefense*myRandom;
            myHealth=myHealth-enemyAttack*newMDefense*enemyRandom;
        }
    }

    //TODO move this to Unit classes
    private double calculateDefense(double Defense, int mapID, int[] terrainMap){
        if(terrainMap[mapID]==5){
            Defense=Defense+0.15;
        }
        else if(terrainMap[mapID]==2){
            Defense=Defense+0.05;
        }
        else if(terrainMap[mapID]==1){
            Defense=Defense-0.1;
        }
        return Defense;
    }

    //TODO move to Unit classes
    public String attack(int enemyUnitMapID,int myUnitMapID, int terrainMap[]){

        getEnemyStats(enemyUnitMapID);
        getMyStats(myUnitMapID);
        //mynum and ennum are only checks to see if a unit is on the mapIDs given,
        // but he doesn't do anything with the checks...
        int mynum=myUnit(myUnitMapID);
        int ennum=enemyUnit(enemyUnitMapID);
        calculateHealth(enemyUnitMapID,myUnitMapID,mynum,ennum, terrainMap);
        myUnits.get(mynum).setHealth(myHealth);
        enemyUnits.get(ennum).setHealth(enemyHealth);
        myUnits.get(mynum).setHasAttacked();
        ClientComm comm = new ClientComm(context);
        JSONArray jsonhealth = new JSONArray();
        JSONObject myObjectID = new JSONObject();
        JSONObject myObjectHealth = new JSONObject();
        JSONObject enemyObjectID= new JSONObject();
        JSONObject enemyObjectHealth= new JSONObject();
        try{
            myObjectID.put("myUnitID",myUnitMapID);
            myObjectHealth.put("myUnitHealth",myHealth);
            enemyObjectID.put("enemyUnitID",enemyUnitMapID);
            enemyObjectHealth.put("enemyUnitHealth",enemyHealth);

        }
        catch (JSONException e){
            Log.d("formAttackJSON", e.getLocalizedMessage());
        }
        jsonhealth.put(myObjectID);
        jsonhealth.put(myObjectHealth);
        jsonhealth.put(enemyObjectID);
        jsonhealth.put(enemyObjectHealth);
        comm.serverPostRequest("attack.php", jsonhealth, new VolleyCallback<JSONArray>() {
            @Override
            public void onSuccess(JSONArray result) {

            }
        });
        if(myHealth<=0){
            myUnits.remove(mynum);
            return "Fail";
        }
        if(enemyHealth<=0){
            enemyUnits.remove(ennum);
            return "Success";
        }
        return "Keep Fighting";
    }

    //TODO like with UIAttack, pretty much just copy/paste of the UI version

    /**
     * creates the unit
     * @param mapID place on the map where unit is to be placed upon creation
     * @param unitID determines which unit is to be created:
     *               1-archer, 2-cavalry, 3-swordsman, 4-spearman, 5-general
     * @return array of Objects: index 0 is a message, index 1 is the units health if one was created
     */
    public Object[] createUnit(int mapID, int unitID){
        Unit newUnit;
        String message;
        switch(unitID){
            case 1:
                newUnit = new Archer(mapID, myName);
                message = "Archer";
                break;
            case 2:
                newUnit = new Cavalry(mapID, myName);
                message = "Cavalry";
                break;
            case 3:
                newUnit = new Swordsman(mapID, myName);
                message = "Swordsman";
                break;
            case 4:
                newUnit = new Spearman(mapID, myName);
                message = "Spearman";
                break;
            case 5:
                newUnit = new General(mapID, myName);
                message = "General";
                break;
            default:
                return new Object[]{"This kind of unit does not exist."};
        }
        if(cash >= newUnit.getCostToRecruit()) {
            cash -= newUnit.getCostToRecruit();
            message = message + " has been recruited.";
            //Didn't actually move, but sets its moved boolean because new units cant move
            newUnit.moveUnit(mapID);
            //ensure the new unit doesn't attack (cant attack on turn it was created)
            newUnit.setHasAttacked();
            myUnits.put(mapID, newUnit);
            return new Object[]{message, newUnit.getHealth()};
        }
        else{
            return new Object[]{"You do not have enough cash."};
        }
    }
}
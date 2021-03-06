package warofages.gamebackend;

import android.content.Context;
import android.util.SparseArray;

import java.util.HashSet;

import coms309.mike.units.General;
import coms309.mike.units.Unit;

/**
 * Created by Mike on 10/29/2016.
 * An abstract Player class designed to hold variables and methods that are common between active
 * and inactive players
 */

public abstract class Player {
    //I may later decide to use hashmaps, but the memory efficiency sounds pretty nice.
    protected SparseArray<Unit> myUnits;
    protected SparseArray<Unit> enemyUnits;
    protected String myName = "a player has no name";
    protected Context context;
    protected int cash;
    protected final int STARTING_CASH = 1000;

    //I need whatever context this player is in. used for the ClientComm stuff
    public Player(Context context, String myName){
        this.context = context;
        this.myName = myName;
        myUnits = new SparseArray<>();
        enemyUnits = new SparseArray<>();
    }

    public void setCash(int newCashAmount){
        cash = newCashAmount;
    }

    public int getCash(){
        return cash;
    }

    public String getName(){
        return myName;
    }

    public SparseArray<Unit> getMyUnits(){
        return myUnits;
    }

    public SparseArray<Unit> getEnemyUnits(){
         return enemyUnits;
    }

    /**
     * gets an enemy unit
     * @param mapID map location to be searched for an enemy unit
     * @return the enemy unit at map location given in parameter, or null if there is none
     */
    public Unit getEnemyUnit(int mapID){
        return enemyUnits.get(mapID);
    }

    /**
     * gets a friendly unit
     * @param mapID map location to be searched for a friendly unit
     * @return the friendly unit at map location given in parameter, or null if there is none
     */
    public Unit getFriendlyUnit(int mapID){
        return myUnits.get(mapID);
    }

    public boolean checkIfGeneralAlive(boolean friendly){
        SparseArray<Unit> army;
        if(friendly && myUnits.size() == 0 || !friendly && enemyUnits.size() == 0)
            return false;

        army = friendly ? getMyUnits() : getEnemyUnits();
        for(int i = 0; i < army.size(); i++){
            if(army.valueAt(i) != null && army.valueAt(i).getUnitID() == 5)
                return true;
        }
        return false;
    }

    public double[] getUnitStats(int unitMapID, byte terID, boolean friendly){
        if(friendly)
            return myUnits.get(unitMapID).getMyStats(terID);
        else
            return enemyUnits.get(unitMapID).getMyStats(terID);
    }

    public String getEnemyName(){
        if(enemyUnits.size() > 0){
            //I know its not empty, so at least index 0 must have a unit in it
            return getEnemyUnits().valueAt(0).getOwner();
        }
        //If the enemy has no units, I should have won...so this is pretty true
        return "An enemy with no army deserves no name";
    }
}
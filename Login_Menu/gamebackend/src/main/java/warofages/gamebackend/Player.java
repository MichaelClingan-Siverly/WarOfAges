package warofages.gamebackend;

import android.annotation.SuppressLint;
import android.content.Context;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import coms309.mike.units.Unit;
import warofages.gamebackend.DisplaysChanges;

/**
 * Created by Mike on 10/29/2016.
 */

//TODO make it implement PlayerInterface

public abstract class Player {
    //TODO think VERY hard about whether I want two HashMaps, or one with LinkedLists of Units as values
    protected HashMap<Integer, Unit> myUnits;
    protected HashMap<Integer, Unit> enemyUnits;
    protected String myName = "a player has no name";
    protected Context context;
    protected DisplaysChanges ui;
    public static int cash = 0;

    //I need whatever context this player is in. used for the ClientComm stuff
    @SuppressLint("UseSparseArrays")
    public Player(Context context, String myName, DisplaysChanges ui){
        this.context = context;
        this.myName = myName;
        this.ui = ui;
        myUnits = new HashMap<>();
        enemyUnits = new HashMap<>();
    }

    public void setCash(int newCashAmount){
        cash = newCashAmount;
    }

    public int getCash(){
        return cash;
    }

    public int incrementCash(int terrainMap[]){
        int addCash = 50; //even with no towns, players gain a base amount of 50
        for(int i = 0; i < myUnits.size(); i++){
            //checks if unit is on a town
            if(terrainMap[myUnits.get(i).getMapID()] == 5){
                addCash = addCash+50;
            }
        }
        return addCash;
    }
    public String getName(){
        return myName;
    }

    public HashMap<Integer, Unit> getMyUnits(){
        return myUnits;
    }

    public HashMap<Integer, Unit> getEnemyUnits(){
         return enemyUnits;
    }

    public Unit getEnemyUnit(int mapID){
        return enemyUnits.get(mapID);
    }

    public Unit getFriendlyUnit(int mapID){
        return myUnits.get(mapID);
    }

    public boolean checkIfNoUnits(boolean friendly){
        if(friendly){
            if(myUnits.isEmpty())
                return true;
            else
                return false;
        }
        else{
            if(enemyUnits.isEmpty())
                return true;
            else
                return false;
        }
    }

    public String getEnemyName(){
        if(!checkIfNoUnits(false)){
            String enemyName;
            Unit[] a = getEnemyUnits().values().toArray(new Unit[0]);
            return a[0].getOwner();
        }
        return "An enemy with no army deserved no name";
    }
}
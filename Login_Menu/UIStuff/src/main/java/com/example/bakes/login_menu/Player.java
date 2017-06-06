package com.example.bakes.login_menu;

import android.content.Context;

import java.util.ArrayList;
import java.util.Observable;

import coms309.mike.units.Unit;
import warofages.gamebackend.AsyncResponse;

/**
 * Created by Mike on 10/29/2016.
 */

//TODO make it implement PlayerInterface

public abstract class Player {
    protected ArrayList<Unit> myUnits;
    protected ArrayList<Unit> enemyUnits;
    protected String myName = "a player has no name";
    protected Context context;
    protected AsyncResponse ui;
    public static int cash = 0;

    //I need whatever context this player is in. used for the ClientComm stuff
    public Player(Context context, String myName, AsyncResponse ui){
        this.context = context;
        this.myName = myName;
        myUnits = new ArrayList<>();
        enemyUnits = new ArrayList<>();
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

    /**
     *
     * @return arraylist of my unitIDs
     */
    public ArrayList<Unit> getMyUnits(){
        return myUnits;
    }

    /**
     *
     * @return arraylist of enemy unitIDs
     */
    public ArrayList<Unit> getEnemyUnits(){
         return enemyUnits;
    }


    public void setMyUnits(ArrayList<Unit> newArmy){
        myUnits = newArmy;
    }
    public void setEnemyUnits(ArrayList<Unit> newArmy){
        enemyUnits = newArmy;
    }

    public String endgame(){
        if (myUnits.size()==0){
            String player = enemyUnits.get(0).getOwner();
            return player + "wins";
        }
        else if(enemyUnits.size()==0){
            String player = myUnits.get(0).getOwner();
            return player + "wins";
        }
        return "Game in Progress";
    }

}
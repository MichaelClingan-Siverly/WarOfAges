package com.example.bakes.login_menu;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import coms309.mike.clientcomm.ClientComm;
import coms309.mike.clientcomm.VolleyCallback;
import warofages.gamebackend.AsyncResponse;


/**
 * Created by Bakes on 10/31/16.
 */
public class ActivePlayer extends Player {
    AsyncResponse ui;

    public int moving=-1;
    public int movespeed;
    private double myattack;
    private double myDefense;
    private double myHealth;
    private double enemyAttack;
    private double enemyDefense;
    private double enemyHealth;
    private int cash;
    private double stats[]= new double[3];
    private Random rand=new Random();

    public ActivePlayer(Context context, String myName, AsyncResponse ui){
        super(context, myName, ui);
    }
    public ActivePlayer(Player oldPlayer){
        super(oldPlayer.context, oldPlayer.myName, oldPlayer.ui);
        this.enemyUnits = oldPlayer.enemyUnits;
        this.myUnits = oldPlayer.myUnits;
        this.cash = oldPlayer.getCash();

    }
    public double[] getMyStats(int UnitID){
        int i=myUnit(UnitID);
        myattack=myUnits.get(i).getAttack();
        myDefense=myUnits.get(i).getDefense();
        myHealth=myUnits.get(i).getHealth();
        stats[0]=myHealth;
        stats[1]=myattack;
        stats[2]=myDefense;
        return stats;
    }
    public double[] getEnemyStats(int UnitID){
        int i=enemyUnit(UnitID);
        enemyAttack=enemyUnits.get(i).getAttack();
        enemyDefense=enemyUnits.get(i).getDefense();
        enemyHealth=enemyUnits.get(i).getHealth();
        stats[0]=enemyHealth;
        stats[1]=enemyAttack;
        stats[2]=enemyDefense;
        return stats;
    }
    public Boolean setmoving(int unitID){
        if(moving==-1 && checkIfMine(unitID)==true){
            moving=unitID;
            return true;
        }
        moving=-1;
        return false;
    }
    public int spaceAvaliableMove(int newMapID){
        final int noMove=0;
        final int canMoveTerrain=1;
        final int canMoveEnemy=2;
        if(checkIfMine(newMapID)==true){
            return noMove;
        }
        else if (checkIfEnemy(newMapID)==true) {
            return canMoveEnemy;
        }
        return canMoveTerrain;
    }
    /**
     *
     * @param oldID current mapID of the unit.
     * @param movespeed unit's movement speed
     * @param unitType type of unit to be checked
     * @return an array of possible moves for a unit, excluding the current position
     */
    public Integer[] checkArea(int oldID, int movespeed, int unitType, int[] terrainMap, boolean attacking){
        ArrayList<Integer> list = new ArrayList<>();
        int mapSize = terrainMap.length;
        int rowlength = (int)Math.sqrt(mapSize);
        //x-value of unit position
        int rowplace=oldID%rowlength;
        //y-value of unit position
        int columnplace = oldID / rowlength;
        //max movement to the right
        int checkwrapright=rowlength-(rowplace)-1;
        //max movement to the left
        int checkwrapleft=rowplace;
        int newmovespeedright=movespeed;
        int newmovespeedleft=movespeed;

        if(checkwrapright<movespeed){
            newmovespeedright=checkwrapright;
        }
        if(checkwrapleft<movespeed){
            newmovespeedleft=checkwrapleft;
        }
        int yStart = Math.max(columnplace - movespeed, 0);
        int yEnd = Math.min(columnplace + movespeed, rowlength - 1);
        int xStart = rowplace - newmovespeedleft;
        int xEnd = rowplace + newmovespeedright;
        for(int y = yStart; y <= yEnd; y ++){
            for(int x = xStart; x <= xEnd; x++){
                if((y * rowlength)+x != oldID) {
                    int terrainType = terrainMap[(y * rowlength)+x];
                    if(attacking && terrainType != 6) {
                        list.add((y * rowlength) + x);
                    }
                    else if(terrainType != 6){
                        switch(unitType){
                            case 1: //archer
                                list.add((y * rowlength)+x);
                                break;
                            case 2: //cavalry
                                if(terrainType != 2 && terrainType != 4){
                                    list.add((y * rowlength)+x);
                                }
                                break;
                            case 3: //sword
                                list.add((y * rowlength)+x);
                                break;
                            case 4: //spear
                                list.add((y * rowlength)+x);
                                break;
                            case 5: //general
                                if(terrainType != 2 && terrainType != 4){
                                    list.add((y * rowlength)+x);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }
        return list.toArray(new Integer[list.size()]);
    }

    private Boolean checkIfMine(int unitID){
        for(int i=0;i<myUnits.size();i++){
            if(myUnits.get(i).getMapID()==unitID){
                return true;
            }
        }
        return false;
    }
    private Boolean checkIfEnemy(int unitID){
        for (int i=0;i<enemyUnits.size();i++){
            if(enemyUnits.get(i).getMapID()==unitID){
                return true;
            }
        }
        return false;
    }
    private int myUnit(int mapID){
        for(int i=0;i<myUnits.size();i++){
            if(myUnits.get(i).getMapID()==mapID){
                return i;
            }
        }
        return -1;
    }
    private int enemyUnit(int mapID){
        for(int i=0;i<enemyUnits.size();i++){
            if(enemyUnits.get(i).getMapID()==mapID){
                return i;
            }
        }
        return -1;
    }
    public void sendMove(int newID, int oldID){
        int i=myUnit(oldID);
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
    private void calculateHealth(int enemyMapID,int myMapID, int mynum, int ennum, int[] terrainMap){
        //Just checking area immediately surrounding the attacker
        Integer[] nextToAttacker = checkArea(myMapID, 1,1, terrainMap, true);
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



    public String attack(int enemyUnitMapID,int myUnitMapID, int terrainMap[]){

        getEnemyStats(enemyUnitMapID);
        getMyStats(myUnitMapID);
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
            System.out.println(e);
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
}
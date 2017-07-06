package coms309.mike.units;

/**
 * Created by Mike on 10/24/2016.
 */


public class Archer extends Unit implements RangedUnit{

    public Archer(int mapID, String ownerID, double health){
        super(mapID, ownerID, 2, health, 66.66666, 0.10);
    }

    public Archer(int mapId, String owner){
        super(mapId, owner, 2, 300.0, 66.66666, 0.10);
    }


    @Override
    public double getMovementCost(byte terID){
        double cost;
        switch(terID){
            case 1:
                cost = 1.25;
                break;
            case 3:
                cost = .66;
                break;
            case 4:
                cost = 2;
                break;
            default:
                cost = super.getMovementCost(terID);
        }
        return cost;
    }

    public boolean needsLineOfSight(){
        return true;
    }

    public int getCostToRecruit(){
        return 100;
    }

    public int getMinAttackRange(){
        return 1;
    }

    public int getMaxAttackRange(){
        return 3;
    }

    public int getUnitID(){
        return 1;
    }
}
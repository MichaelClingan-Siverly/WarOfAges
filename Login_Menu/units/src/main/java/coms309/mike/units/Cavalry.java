package coms309.mike.units;

/**
 * Created by Mike on 10/24/2016.
 */


public class Cavalry extends Unit {

    public Cavalry(int mapID, String ownerID, double health){
        super(mapID, 2, ownerID, 4, health, 100, 0.35);
    }

    public Cavalry(int mapID, String owner){
        super(mapID, 2, owner, 4, 900.0, 100, .35);
    }

    @Override
    public double getMovementCost(int terID){
        double cost;
        switch(terID){
            case 1:
            case 2:
                cost = 2;
                break;
            case 4:
                cost = Double.MAX_VALUE;
                break;
            default:
                cost = super.getMovementCost(terID);
        }
        return cost;
    }

    public int getCostToRecruit(){
        return 250;
    }
}
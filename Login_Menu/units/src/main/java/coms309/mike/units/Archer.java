package coms309.mike.units;

/**
 * Created by Mike on 10/24/2016.
 */


public class Archer extends RangedUnit {

    public Archer(int mapID, String ownerID, double health){
        super(mapID, 1, ownerID, 2, health, 66.66666, 1, 3, 0.10);
    }

    public Archer(int mapId, String owner){
        super(mapId, 1, owner, 2, 300.0, 66.66666, 1, 3, 0.10);
    }

    @Override
    public double getMovementCost(int terID){
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

    public int getCostToRecruit(){
        return 100;
    }

}
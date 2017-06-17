package coms309.mike.units;

/**
 * Created by msiverly on 10/26/16.
 */

public class General extends Unit {

    public General(int mapID, String ownerID, double health){
        super(mapID, 5, ownerID, 5, health, 125, 0.8);
    }

    public General(int mapID, String owner){
        super(mapID, 5, owner, 5, 2000.0, 125, .8);
    }

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
        return 1000;
    }
}
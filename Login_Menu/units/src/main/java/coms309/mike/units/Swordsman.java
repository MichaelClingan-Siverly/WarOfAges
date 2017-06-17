package coms309.mike.units;

/**
 * Created by Mike on 10/24/2016.
 */

public class Swordsman extends Unit {

    public Swordsman(int mapID, String ownerID, double health){
        super(mapID, 3, ownerID, 2, health, 50, 0.5);
    }

    public Swordsman(int mapID, String owner){
        super(mapID, 3, owner, 2, 600.0, 50, .5);
    }

    @Override
    public double getMovementCost(int terID){
        double cost;
        switch(terID){
            case 1:
            case 2:
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
        return 150;
    }
}
package coms309.mike.units;

/**
 * Created by Mike on 10/24/2016.
 */


public class Cavalry extends Unit {

    public Cavalry(int mapID, String ownerID, double health){
        super(mapID, ownerID, 3, health, 100, 0.35);
    }

    public Cavalry(int mapID, String owner){
        super(mapID, owner, 3, 900.0, 100, .35);
    }

    @Override
    public double getMovementCost(byte terID){
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

    @Override
    public double calculateDefenseAfterTerrain(byte terrain) {
        double modifiedDefense;
        switch (terrain) {
            case 1:
                modifiedDefense = defense - .15;
                break;
            case 2:
                modifiedDefense = defense - .3;
                break;
            case 4: //mounted units should not be on a mountain anyway
                modifiedDefense = .0001;
                break;
            case 5:
            case 6:
            case 7:
                modifiedDefense = defense + .1;
                break;
            default:
                modifiedDefense = defense;
        }
        return modifiedDefense;
    }

    public int getUnitID(){
        return 2;
    }
}
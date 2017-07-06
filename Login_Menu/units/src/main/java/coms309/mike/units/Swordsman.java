package coms309.mike.units;

/**
 * Created by Mike on 10/24/2016.
 */

public class Swordsman extends Unit {

    public Swordsman(int mapID, String ownerID, double health){
        super(mapID, ownerID, 2, health, 50, 0.5);
    }

    public Swordsman(int mapID, String owner){
        super(mapID, owner, 2, 600.0, 50, .5);
    }

    @Override
    public double getMovementCost(byte terID){
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

    @Override
    public double calculateDefenseAfterTerrain(byte terrain) {
        double modifiedDefense;
        switch (terrain) {
            case 1:
                modifiedDefense = defense - .25;
                break;
            case 2:
                modifiedDefense = defense + .4;
                break;
            case 4: //mounted units should not be on a mountain anyway
                modifiedDefense = defense + .35;
                break;
            case 5:
            case 6:
            case 7:
                modifiedDefense = defense + 1;
                break;
            default:
                modifiedDefense = defense;
        }
        return modifiedDefense;
    }

    public int getUnitID(){
        return 3;
    }
}
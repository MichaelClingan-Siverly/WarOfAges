package coms309.mike.units;

/**
 * Created by EVA286A on 11/28/16.
 */

public class Spearman extends Unit {

    public Spearman(int mapID, String ownerID, double health){
        super(mapID, ownerID, 1, health, 25, 0.75);
    }
    public Spearman(int mapID, String owner){
        super(mapID, owner, 1, 450, 58.33333, .7);
    }

    @Override
    public double getMovementCost(byte terID){
        double cost;
        switch(terID){
            case 3:
                cost = .5;
                break;
            default:
                cost = super.getMovementCost(terID);
        }
        return cost;
    }

    public int getCostToRecruit(){
        return 200;
    }

    @Override
    public double calculateDefenseAfterTerrain(byte terrain) {
        double modifiedDefense;
        switch (terrain) {
            case 1:
                modifiedDefense = defense - .25;
                break;
            case 2:
                modifiedDefense = defense + .3;
                break;
            case 4:
                modifiedDefense = defense + .5;
                break;
            case 5:
            case 6:
            case 7:
                modifiedDefense = defense + .4;
                break;
            default:
                modifiedDefense = defense;
        }
        return modifiedDefense;
    }

    public int getUnitID(){
        return 4;
    }
}

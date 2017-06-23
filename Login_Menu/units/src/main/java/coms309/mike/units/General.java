package coms309.mike.units;

/**
 * Created by msiverly on 10/26/16.
 */

public class General extends Unit {

    public General(int mapID, String ownerID, double health){
        super(mapID, 5, ownerID, 4, health, 125, 0.8);
    }

    public General(int mapID, String owner){
        super(mapID, 5, owner, 4, 2000.0, 125, .8);
    }

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
        return 1000;
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
}
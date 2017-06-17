package coms309.mike.units;

/**
 * Created by EVA286A on 11/28/16.
 */

public class Spearman extends Unit {

    public Spearman(int mapID, String ownerID, double health){
        super(mapID, 4, ownerID,1,health,58.33333, 0.7);
    }
    public Spearman(int mapID, String owner){
        super(mapID, 4, owner, 1, 450, 58.33333, .7);
    }

    @Override
    public double getMovementCost(int terID){
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
}

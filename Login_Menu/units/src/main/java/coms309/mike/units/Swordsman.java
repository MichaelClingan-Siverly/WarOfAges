package coms309.mike.units;

/**
 * Created by Mike on 10/24/2016.
 */

public class Swordsman extends Unit {

    public Swordsman(int unitID, int mapID, String ownerID, double health){
        super(mapID, unitID, ownerID,2,health,50, 0.5);
    }

}
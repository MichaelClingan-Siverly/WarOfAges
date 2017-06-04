package coms309.mike.units;

/**
 * Created by Mike on 10/24/2016.
 */


public class Cavalry extends Unit {

    public Cavalry(int unitID, int mapID, String ownerID, double health){
        super(mapID, unitID, ownerID,4,health,100, 0.35);
    }


}
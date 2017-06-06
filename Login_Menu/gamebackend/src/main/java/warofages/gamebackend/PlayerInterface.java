package warofages.gamebackend;

import java.util.ArrayList;
import coms309.mike.units.Unit;

/**
 * Created by Mike on 10/29/2016.
 */

//I don't really want to use this. I think extending a base player would be better. Keeping this here until finished with that.

public interface PlayerInterface {

    public int getTurn();
    public String getName();
    public void endTurn();
    public void startTurn();
    public ArrayList<Unit> getUnits();
    public void placeUnits();
}
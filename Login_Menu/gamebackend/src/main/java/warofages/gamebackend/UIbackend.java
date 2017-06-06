package warofages.gamebackend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * Created by mike on 6/2/2017.
 */

public class UIbackend {
    private Hashtable table;
    public UIbackend(){
        table = new Hashtable();
        //TODO constructor
    }

    public void addTown(int mapID, String owner){
        Town town;
        if(owner != null)
            town = new Town(mapID, owner);
        else
            town = new Town(mapID);
        table.put(mapID, town);
    }

}

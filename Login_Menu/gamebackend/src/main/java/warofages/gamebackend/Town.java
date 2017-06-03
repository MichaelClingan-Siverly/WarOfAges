package warofages.gamebackend;

/**
 * Created by msiverly on 11/23/16.
 */

public class Town {
    private int mapID;
    private String owner;
    public Town(int mapID){
        this.mapID = mapID;
        owner = "neutral";
    }

    public void setOwner(String userID){
        owner = userID;
    }
}

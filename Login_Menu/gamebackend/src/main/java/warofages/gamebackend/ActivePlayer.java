package warofages.gamebackend;
import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import coms309.mike.units.Archer;
import coms309.mike.units.Cavalry;
import coms309.mike.units.General;
import coms309.mike.units.Spearman;
import coms309.mike.units.Swordsman;
import coms309.mike.units.Unit;


/**
 * Created by Bakes on 10/31/16.
 * HEAVILY modified by mike
 */
public class ActivePlayer extends Player {

    //TODO I may not even need this. Feels redundant compared to UIBackground's mapIdManipulated
    //moving is mapID of a unit marked to move, or -1 if there is none
    private int moving =- 1;

    public ActivePlayer(Context context, String myName, SparseArray<Town> towns){
        super(context, myName);
        setCash(STARTING_CASH);
        incrementCash(getNumOfMyTowns(towns));
    }

    public ActivePlayer(Player oldPlayer, SparseArray<Town> towns){
        super(oldPlayer.context, oldPlayer.myName);
        this.enemyUnits = oldPlayer.enemyUnits;
        this.myUnits = oldPlayer.myUnits;
        setCash(oldPlayer.getCash());
        incrementCash(getNumOfMyTowns(towns));
    }

    private void incrementCash(int numOfMyTowns){
        final int TURN_CASH_BASE = 50;
        final int TURN_CASH_TOWN = 50;
        int cashFromTowns = TURN_CASH_TOWN * numOfMyTowns;
        cash += (TURN_CASH_BASE + cashFromTowns);
    }

    private int getNumOfMyTowns(SparseArray<Town> towns){
        int numTowns = 0;
        for(int i = 0; i < towns.size(); i++){
            if(towns.valueAt(i).getOwner().equals(myName))
                numTowns++;
        }
        return numTowns;
    }



    public void setMoveFromMapID(int unitsMapID){
        if(unitsMapID != -1 && moving==-1 && getFriendlyUnit(unitsMapID) != null)
            moving=unitsMapID;
        else
            moving=-1;
    }
    public int getMoveFromMapID(){
        return moving;
    }

    public boolean moveUnit(int oldMapID, int newMapID){
        Unit u = getFriendlyUnit(oldMapID);
        if(u == null) {
            Log.d("sendMove", "shouldnt try to move a unit that doesn't exist");
            return false;
        }
        int i = u.getMapID(); // its mapID = oldID
        if(getEnemyUnit(newMapID) != null || getFriendlyUnit(newMapID) != null){
            Log.d("sendMove", "can't move a unit onto another unit");
            return false;
        }
        myUnits.get(i).moveUnit(newMapID);
        if(oldMapID != newMapID){
            myUnits.put(newMapID, myUnits.get(i));
            myUnits.put(i, null);
        }

        return true;
    }

    public int checkIfUnitOnSpace(int newMapID){
        final int noMove=0;
        final int canMoveTerrain=1;
        final int canMoveEnemy=2;
        if(getFriendlyUnit(newMapID) != null){
            return noMove;
        }
        else if (getEnemyUnit(newMapID) != null) {
            return canMoveEnemy;
        }
        return canMoveTerrain;
    }

    public String attack(Unit attacker, byte attackerTerrain, Unit defender, byte defenderTerrain){
        attacker.attack(defender, attackerTerrain, defenderTerrain);
        boolean myUnitKilled = false;
        boolean enemyUnitKilled = false;
        if(attacker.getHealth() <= 0){
            myUnits.remove(attacker.getMapID());
            myUnitKilled = true;
        }
        if(defender.getHealth() <= 0){
            enemyUnits.remove(defender.getMapID());
            enemyUnitKilled = true;
        }

        if(myUnitKilled && enemyUnitKilled)
            return "Draw";
        else if(myUnitKilled)
            return "Your unit died";
        else if(enemyUnitKilled)
            return "Enemy unit killed";
        else
            return "Keep Fighting";
    }

    /**
     * creates the unit
     * @param mapID place on the map where unit is to be placed upon creation
     * @param unitID determines which unit is to be created:
     *               1-archer, 2-cavalry, 3-swordsman, 4-spearman, 5-general
     * @return array of Objects: message indicating if the unit was created
     */
    public String createUnit(int mapID, int unitID){
        Unit newUnit;
        String message;
        switch(unitID){
            case 1:
                newUnit = new Archer(mapID, myName);
                message = "Archer";
                break;
            case 2:
                newUnit = new Cavalry(mapID, myName);
                message = "Cavalry";
                break;
            case 3:
                newUnit = new Swordsman(mapID, myName);
                message = "Swordsman";
                break;
            case 4:
                newUnit = new Spearman(mapID, myName);
                message = "Spearman";
                break;
            case 5:
                newUnit = new General(mapID, myName);
                message = "General";
                break;
            default:
                return "This kind of unit does not exist.";
        }
        if(cash >= newUnit.getCostToRecruit()) {
            cash -= newUnit.getCostToRecruit();
            message = message + " has been recruited.";
            //Didn't actually move, but sets its moved boolean because new units cant move
            newUnit.moveUnit(mapID);
            //ensure the new unit doesn't attack (cant attack on turn it was created)
            newUnit.setHasAttacked();
            myUnits.put(mapID, newUnit);
            return message;
        }
        else{
            return "You do not have enough cash.";
        }
    }
}
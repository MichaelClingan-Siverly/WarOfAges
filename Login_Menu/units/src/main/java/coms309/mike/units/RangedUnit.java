package coms309.mike.units;

/**
 * Created by mike on 6/10/2017.
 */

public abstract class RangedUnit extends Unit {
    private int minAttackRange;
    private int maxAttackRange;

    public RangedUnit(int mapID, int unitID, String ownerID, int MovementSpeed,double health, double attack, int minAttackRange, int maxAttackRange, double defense){
        super(mapID, unitID, ownerID, MovementSpeed, health, attack, defense);
        this.minAttackRange = minAttackRange;
        this.maxAttackRange = maxAttackRange;
    }

    //may eventually have catapults or something which has to fire a minimum distance
    @Override
    public int getMinAttackRange(){
        return minAttackRange;
    }
    @Override
    public int getMaxAttackRange(){
        return maxAttackRange;
    }
}

package coms309.mike.units;

/**
 * Created by mike on 6/10/2017.
 */

public abstract class RangedUnit extends Unit {
    private int attackRange;

    public RangedUnit(int mapID, int unitID, String ownerID, int MovementSpeed,double health, double attack, int attackRange, double defense){
        super(mapID, unitID, ownerID, MovementSpeed, health, attack, defense);
        this.attackRange = attackRange;
    }

    public int getAttackRange(){
        return attackRange;
    }
}

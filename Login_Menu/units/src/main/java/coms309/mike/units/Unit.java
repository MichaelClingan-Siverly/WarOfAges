package coms309.mike.units;


import java.util.HashSet;
import java.util.Random;

/*
 *  Visitor pattern would work here, but there are so many visitors it'd
 *  need that I'd rather not do it unless I start creating many more units
 */
public abstract class Unit {
    private int mapID;
    private HashSet<Integer> moves;
    private HashSet<Integer> attacks;
    private String ownerID;
    private double moveSpeed;
    private double health;
    private double attack;
    protected double defense;
    private boolean moved = false;
    private boolean hasAttacked = false;

    public Unit(int mapID, String ownerID, double MovementSpeed, double health, double attack, double defense) {
        this.mapID = mapID;
        this.ownerID = ownerID;
        this.moveSpeed=MovementSpeed;
        this.health=health;
        this.attack=attack;
        this.defense=defense;
        moves = null;
        attacks = null;
    }

    public double calculateDefenseAfterTerrain(byte terrain){
        double modifiedDefense;
        switch(terrain){
            case 1:
                modifiedDefense = defense - .1;
                break;
            case 2:
                modifiedDefense = defense + .1;
                break;
            case 4:
                modifiedDefense = defense + .3;
                break;
            case 5:
            case 6:
            case 7:
                modifiedDefense = defense + .25;
                break;
            default:
                modifiedDefense = defense;
        }
        return modifiedDefense;
    }

    public void attack(Unit enemyUnit, byte myTerrain, byte theirTerrain, int distance){
        int minRange = 1;
        int maxRange = 1;
        if(hasAttacked)
            return;
        hasAttacked = true;
        moved = true;
        Random rand = new Random();
        //introduce an element of randomness where each attack can do .5 <= x < 1.5 times the damage
        float myRandom = rand.nextFloat() + .5f;
        float enemyRandom = rand.nextFloat() + .5f;
        //set enemy's health
        enemyUnit.setHealth(enemyUnit.getHealth() - attack * myRandom
                / (1+enemyUnit.calculateDefenseAfterTerrain(theirTerrain)));
        if(this instanceof RangedUnit && enemyUnit instanceof RangedUnit) {
            minRange = ((RangedUnit)enemyUnit).getMinAttackRange();
            maxRange = ((RangedUnit)enemyUnit).getMaxAttackRange();
        }
        //if enemy is alive after my attack and can reach me, they can attack back
        if(enemyUnit.getHealth() > 0 && minRange <= distance && distance <= maxRange)
            health = health - enemyUnit.getAttack() * enemyRandom
                    / (1+calculateDefenseAfterTerrain(myTerrain));
    }

    public abstract int getCostToRecruit();

    public double getMovementCost(byte terID){
        //pond or impassable_mountain
        if(terID == 8 || terID == 9)
            return Double.MAX_VALUE;
        else
            return 1;
    }

    public void setPossibleActions(int[] possibleMoves, int[] possibleAttacks){
        if(possibleMoves != null) {
            moves = new HashSet<>(possibleMoves.length * 2);
            for (int move : possibleMoves)
                moves.add(move);
        }
        else
            moves = null;

        if(possibleAttacks != null) {
            attacks = new HashSet<>(possibleAttacks.length * 2);
            for (int attack : possibleAttacks)
                attacks.add(attack);
        }
        else
            attacks = null;
    }

    public boolean checkIfPossibleMove(int mapID){
        return moves != null && moves.contains(mapID);
    }
    public boolean checkIfPossibleAttack(int mapID){
        return attacks != null && attacks.contains(mapID);
    }

    public int getMapID() {
        return mapID;
    }

    public abstract int getUnitID();

    public String getOwner() {
        return ownerID;
    }
    public double getMoveSpeed() {
        return moveSpeed;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double myHealth){
        health = myHealth;
    }

    private double getAttack() {
        return attack;
    }

    /**
     * does not check if the new map space is occupied. only updates internal mapID
     *
     * @param newMapID mapID of where unit will be moved to
     */
    public void moveUnit(int newMapID) {
        mapID = newMapID;
        moved = true;
    }
    public boolean checkIfMoved(){
        return moved;
    }
    public boolean checkIfAttacked(){
        return hasAttacked;
    }
    //used when creating a unit so that it cant attack that same turn
    public void setHasAttacked(){
        hasAttacked = true;
    }

    public double[] getMyStats(byte terID){
        return new double[]{health, attack, calculateDefenseAfterTerrain(terID)};
    }

    public void resetMovedAndAttacked(){
        hasAttacked = false;
        moved = false;
    }
}

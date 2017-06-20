package coms309.mike.units;


import java.util.Random;

public abstract class Unit {
    private int mapID;
    private int unitID;
    private String ownerID;
    private double moveSpeed;
    private double health;
    private double attack;
    protected double defense;
    private boolean moved = false;
    private boolean hasAttacked = false;

    public Unit(int mapID, int unitID, String ownerID, double MovementSpeed, double health, double attack, double defense) {
        this.mapID = mapID;
        this.unitID = unitID;
        this.ownerID = ownerID;
        this.moveSpeed=MovementSpeed;
        this.health=health;
        this.attack=attack;
        this.defense=defense;
    }

    protected double calculateDefenseAfterTerrain(byte terrain){
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

    //TODO I'd like to have units able to have multiple rounds of attack (i.e. Wesnoth)
    public void attack(Unit enemyUnit, byte myTerrain, byte theirTerrain){
        if(hasAttacked)
            return;
        hasAttacked = true;
        Random rand = new Random();
        //introduce an element of randomness where each attack can do .5 <= x < 1.5 times the damage
        float myRandom = rand.nextFloat() + .5f;
        float enemyRandom = rand.nextFloat() + .5f;
        //set enemy's health
        enemyUnit.setHealth(enemyUnit.getHealth() - attack * myRandom
                / enemyUnit.calculateDefenseAfterTerrain(theirTerrain));
        //set my health
        health = health - enemyUnit.getAttack() * enemyRandom
                / calculateDefenseAfterTerrain(myTerrain);
    }

    public abstract int getCostToRecruit();

    public double getMovementCost(byte terID){
        if(terID == 8)
            return Double.MAX_VALUE;
        else
            return 1;
    }

    public int getMapID() {
        return mapID;
    }

    public int getUnitID() {
        return unitID;
    }

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

    public double[] getMyStats(){
        return new double[]{health, attack, defense};
    }

    public void resetMovedAndAttacked(){
        hasAttacked = false;
        moved = false;
    }
}

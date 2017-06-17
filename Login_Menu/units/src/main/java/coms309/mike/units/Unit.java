package coms309.mike.units;


public abstract class Unit {
    private int mapID;
    private int unitID;
    private String ownerID;
    private double moveSpeed;
    private double health;
    public double attack;
    private double defense;
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

    //TODO I'd like to get terrain passed in too, or have a method to set it.
    public void attack(Unit enemyUnit){

    }

    public abstract int getCostToRecruit();

    public double getMovementCost(int terID){
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

    public double getAttack() {
        return attack;
    }

    public double getDefense() {
        return defense;
    }
    public void setHealth(double myHealth){
        health = myHealth;
    }
    /**
     * does not check if the new map space is occupied. only updates internal mapID
     *
     * @param newMapID
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

    public int getMinAttackRange(){
        return 1;
    }
    public int getMaxAttackRange(){
        return 1;
    }
}

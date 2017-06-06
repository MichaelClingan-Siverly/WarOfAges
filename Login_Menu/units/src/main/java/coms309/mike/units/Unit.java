package coms309.mike.units;


public abstract class Unit {
    private int mapID;
    private int unitID;
    private String ownerID;
    private int moveSpeed;
    private double health;
    public double attack;
    private double defense;
    private boolean moved = false;
    private boolean hasAttacked = false;

    public Unit(int mapID, int unitID, String ownerID, int MovementSpeed,double health, double attack, double defense) {
        this.mapID = mapID;
        this.unitID = unitID;
        this.ownerID = ownerID;
        this.moveSpeed=MovementSpeed;
        this.health=health;
        this.attack=attack;
        this.defense=defense;
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
    public int getMoveSpeed() {
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
}

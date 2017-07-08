package warofages.gamebackend;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;

import coms309.mike.units.RangedUnit;
import coms309.mike.units.Unit;

/**
 * Implementation of Dijkstra's algorithm to find which mapIDs may be moved to or attacked
 * Does not check whether a mapID is valid for a move (unit on the space) or attack (no unit on the space)
 *
 * I use http://www.redblobgames.com/grids/hexagons/ a lot for the the line work
 * Created by mike on 6/16/2017.
 */
class terrainCalculations {

    class Hexagon{
        int row, col;
        private Hexagon partnerHex;
        Hexagon(int row, int col){
            this.row = row;
            this.col = col;
            partnerHex = null;
        }
        public void setPartnerHex(Hexagon partner){
            partnerHex = partner;
        }
        public Hexagon getPartnerHex(){
            return partnerHex;
        }
    }
    private class Cube{
        int x, y, z;
        Cube(int x, int y, int z){
            this.x = x;
            this.y = y;
            this.z = z;
        }
        Cube(){
            x = 0; y = 0; z = 0;
        }
    }

    /**
     * finds the rows and columns of the hexagons along the path from start to end
     * @param startMapID mapID of first hexagon along the path
     * @param endMapID mapID of the target hexagon - where the path will end
     * @return 2-D int array where [i][0] is the row and [i][1] is the column
     */
    //If theres a more efficient way to do this (and I'm sure there is), I'd like to know it
    Hexagon[] checkLine(int startMapID, int endMapID, int mapSize){
        //actual tileSize doesn't really matter, but I need something for use in the calculations
        int tileSize = 100;
        float heightOfHexagon = (float)Math.sqrt(3/2f) * tileSize;
        int rowLength = (int)Math.sqrt(mapSize);
        int startCol = startMapID / rowLength;
        int startRow = startMapID % rowLength;
        if(startMapID == endMapID)
            return new Hexagon[]{new Hexagon(startRow, startCol)};

        int endCol = endMapID / rowLength;
        int endRow = endMapID % rowLength;

        //I make the points at the center, hence the half width/height added on each.
        //first column's x = tileSize/2. Each column after that adds 3*tileSize/4
        float startX = startCol * (3 * tileSize / 4) + tileSize / 2;
        float endX = endCol * (3 * tileSize / 4) + tileSize / 2;
        //first row's y = hexHeight/2 if x is even or hexHeight if x is odd. each row after adds hexHeight
        float startY = startRow * heightOfHexagon + heightOfHexagon / (2-(startCol&1));
        float endY = endRow * heightOfHexagon + heightOfHexagon / (2-(endCol&1));

        //I know the line crosses at least (may be on edge between two hexes) this many hexes,
        //so I sample points at this many evenly spaced sections on the line
        int numSamples = findManhattanDistance(startRow, startCol, endRow, endCol);

        //the extra size is because theres a chance the point may be on an edge. I want to check both of them
        ArrayList<Hexagon>  path= new ArrayList<>(numSamples + 1 + (numSamples)/2);
        Hexagon[] nearestHexagons;
        //distance between the samples; add onto the x and y each time to follow the slope of the line
        float xSampleDist = (endX - startX) / numSamples;
        float ySampleDist = (endY - startY) / numSamples;
        float x = startX;
        float y = startY;
        //the first hexagon the line hits is the one where it started
        path.add(new Hexagon(startRow, startCol));


        //sample variable keeps track of what index in path I insert into.
        // can't use i, since sometimes I make two insertions per point
        for(int i = 1, sample = 1; i <= numSamples; i++, sample++){
            x += xSampleDist;
            y += ySampleDist;
            nearestHexagons = getNearestHexToPoint(x,y, tileSize, heightOfHexagon);
            path.add(nearestHexagons[0]);
//            path[sample] = nearestHexagons[0]; //there will always be at least one nearest hexagon
            //possible to have two nearest ones (point on an edge), happens at most numSamples/2 times
            if(nearestHexagons.length > 1) {
//                sample++;
                path.add(nearestHexagons[1]);
//                path[sample] = nearestHexagons[1];
            }
        }
        return path.toArray(new Hexagon[path.size()]);
    }

    //if a point is on an edge between two hexagons, nudges x and y
    // a bit each way and calls itself to determine both hexagons
    private Hexagon[] getNearestHexToPoint(float x, float y, int tileSize, float heightOfHexagon){
        final float E = .00005f;
        final float nudgeValue = .1f;
        int row;
        int col;
        //the tentative stuff is the inverse of finding x,y from column, row
        float tentativeCol = (4*x - 2*tileSize)/(3*tileSize);

        //if it's close enough to the edge that I consider it to be on it
        if(Math.abs(tentativeCol - ((float)Math.floor(tentativeCol) + .5f)) <= E) {
            Hexagon hex1 = getNearestHexToPoint(x-nudgeValue, y-nudgeValue, tileSize, heightOfHexagon)[0];
            Hexagon hex2 = getNearestHexToPoint(x+nudgeValue, y+nudgeValue, tileSize, heightOfHexagon)[0];
            return new Hexagon[]{hex1, hex2};
        }
        else
            col = Math.round(tentativeCol);

        //needed to do row after column, because I need to know column for the bitwise operation
        int offset = (2 - (col&1));
        float top = (y*(offset)-heightOfHexagon);
        float bot = ((offset)*heightOfHexagon);
        float tentativeRow = top/bot;
        //anytime points between two rows, the column will have to be changed, but not vice versa,
        // but since I need to know col to do row, this goes second (even if it would
        if(Math.abs(tentativeRow - ((float)Math.floor(tentativeRow) + .5f)) <= E){
            Hexagon hex1 = getNearestHexToPoint(x-nudgeValue, y-nudgeValue, tileSize, heightOfHexagon)[0];
            Hexagon hex2 = getNearestHexToPoint(x+nudgeValue, y+nudgeValue, tileSize, heightOfHexagon)[0];
            hex1.setPartnerHex(hex2);
            hex2.setPartnerHex(hex1);
            return new Hexagon[]{hex1, hex2};
        }
        else
            row = Math.round(tentativeRow);

        //point is not on an edge
         return new Hexagon[]{new Hexagon(row, col)};
    }

    private Cube convertToCubeCoordinates(int row, int col){
        Cube cube = new Cube();
        cube.x = col; //x
        cube.z = row - (col - (col&1)) / 2; //z
        cube.y = -cube.x-cube.z; //y
        return cube;
    }

    int findManhattanDistance(int startRow, int startCol, int endRow, int endCol){
        Cube startCube = convertToCubeCoordinates(startRow, startCol);
        Cube endCube = convertToCubeCoordinates(endRow, endCol);

        return (Math.abs(startCube.x - endCube.x) + Math.abs(startCube.y - endCube.y) + Math.abs(startCube.z - endCube.z)) / 2;
    }

    /**
     * Could I have selected something better than an array, since some uses just checks if it
     * contains a value? Yes...but even a range of 10 will give a max size of 331, so
     * iterating won't be too much of an issue
     *
     * @param unit unit who is checking the surrounding terrain
     * @param player the player whose unit the terrain is being checked for
     * @param attacking indicated whether the unit is checking terrain for attack or movement
     * @param terrain the terrainMap containing terrain IDs for each mapID
     * @return an array of mapIDs for possible moves for a unit, excluding the current position
     */
    int[] checkSurroundingTerrain(Unit unit, Player player, boolean attacking, byte[] terrain){
        double distance;
        PriorityQueue<TerrainCostTuple> queue = new PriorityQueue<>();
        HashSet<TerrainCostTuple> visited = new HashSet<>(40);

        TerrainCostTuple start = new TerrainCostTuple(unit.getMapID(), terrain[unit.getMapID()]);
        start.setCost(0);
        queue.add(start);

        if(attacking) {
            if(unit instanceof RangedUnit)
                distance = ((RangedUnit)unit).getMaxAttackRange();
            else
                distance = 1;
        }
        else
            distance = unit.getMoveSpeed();

        while(!queue.isEmpty()){
            TerrainCostTuple lowestCostTuple = queue.poll();
            if(lowestCostTuple.getMapID() != unit.getMapID() && !visited.contains(lowestCostTuple) &&
                    !(attacking && unit instanceof RangedUnit && lowestCostTuple.getCost() < ((RangedUnit)unit).getMinAttackRange()))
                visited.add(lowestCostTuple);
            TerrainCostTuple[] neighbors = getNeighborTuples(lowestCostTuple.getMapID(), terrain);
            if(neighbors != null) {
                for (TerrainCostTuple tuple : neighbors) {

                    //projectiles move the same speed in the air regardless of terrain: hence the unit cost
                    if (attacking)
                        tuple.setCost(lowestCostTuple.getCost() + 1);
                        //not attacking, but I don't want units (even friendly units) to move through each other
                    else if (player.getFriendlyUnit(tuple.getMapID()) == null
                            && player.getEnemyUnit(tuple.getMapID()) == null)
                        tuple.setCost(lowestCostTuple.getCost() + unit.getMovementCost(tuple.terID));
                    //no else: units can't move through other units - friendly or hostile

                    //This doesn't care about distance to all mapIDs, only those reachable by the unit
                    boolean visitedHas = visited.contains(tuple);
                    boolean queueHas = queue.contains(tuple);
                    if (tuple.getMapID() != unit.getMapID() && tuple.getCost() <= distance &&
                            !(visitedHas || queueHas)) {
                        queue.add(tuple);
                    }
                }
            }
        }
        Iterator<TerrainCostTuple> iterator = visited.iterator();
        int[] mapIDs = new int[visited.size()];
        int i = 0;
        while(iterator.hasNext()){
            mapIDs[i] = iterator.next().getMapID();
            i++;
        }
        return mapIDs;
    }

    private class TerrainCostTuple implements Comparable<TerrainCostTuple>{
        private double cost;
        private byte terID;
        private int mapID;

        TerrainCostTuple(int mapID, byte terID){
            this.mapID = mapID;
            this.terID = terID;
            cost = Double.MAX_VALUE;
        }

        void setCost(double newCost){
            cost = newCost;
        }
        double getCost(){
            return cost;
        }
        int getMapID(){
            return mapID;
        }
        @Override
        public int compareTo(@NonNull TerrainCostTuple t2) {
            double comp = cost - t2.getCost();
            if(comp < 0)
                return -1;
            else if(comp > 0)
                return 1;
            else
                return 0;
        }
        @Override
        public int hashCode(){
            return mapID;
        }
        @Override
        public boolean equals(Object o){
            if(!(o instanceof   TerrainCostTuple))
                return false;
            TerrainCostTuple obj = (TerrainCostTuple)o;
            return mapID == obj.getMapID();
        }
    }

    //length is at most 6
    private TerrainCostTuple[] getNeighborTuples(int mapID, byte[] terrain) {
        if(mapID < 0 || mapID >= terrain.length)
            return null;
        ArrayList<TerrainCostTuple> neighbors = new ArrayList<>();
        boolean top = false, bot = false, left = false, right = false;
        int rowLength = (int) Math.sqrt(terrain.length);
        int column = mapID / rowLength;

        if (mapID % rowLength == 0)
            top = true;
        else if (mapID % rowLength == rowLength - 1)
            bot = true;
        if (column == 0)
            left = true;
        else if (column == rowLength - 1)
            right = true;

        //don't check if top and left or right, because they can't be accessed from the hexagon
        if(!top)
            neighbors.add(new TerrainCostTuple(mapID - 1, terrain[mapID - 1]));
        if(!left)
            neighbors.add(new TerrainCostTuple(mapID - rowLength, terrain[mapID - rowLength]));
        if(!right)
            neighbors.add(new TerrainCostTuple(mapID + rowLength, terrain[mapID + rowLength]));
        if(!bot)
            neighbors.add(new TerrainCostTuple(mapID + 1, terrain[mapID + 1]));
        //odd columns
        if((column & 1) == 1){
            if(!bot && !left)
                neighbors.add(new TerrainCostTuple(mapID - rowLength + 1, terrain[mapID - rowLength + 1]));
            if(!bot && !right)
                neighbors.add(new TerrainCostTuple(mapID + rowLength + 1, terrain[mapID + rowLength + 1]));
        }
        //even columns
        else{
            if(!top && !left)
                neighbors.add(new TerrainCostTuple(mapID - rowLength - 1, terrain[mapID - rowLength - 1]));
            if(!top && !right)
                neighbors.add(new TerrainCostTuple(mapID + rowLength - 1, terrain[mapID + rowLength - 1]));
        }
        return neighbors.toArray(new TerrainCostTuple[neighbors.size()]);
    }
}

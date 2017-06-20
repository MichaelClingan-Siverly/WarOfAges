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
    private byte[]terrain;
    private int tileSize;

    terrainCalculations(byte[] terrainMap, int tileSize){
        terrain = terrainMap;
        this.tileSize = tileSize;
    }

    private class Hexagon{
        int row, col;
        Hexagon(int row, int col){
            this.row = row;
            this.col = col;
        }
    }

    //If theres a more efficient way to do this (and I'm sure there is), I'd like to know it
    private Hexagon[] checkLine(int startMapID, int endMapID){
        int rowLength = (int)Math.sqrt(terrain.length);
        int startCol = startMapID / rowLength;
        int startRow = startMapID % rowLength;
        if(startMapID == endMapID)
            return new Hexagon[]{new Hexagon(startRow, startCol)};

        int endCol = endMapID / rowLength;
        int endRow = endMapID % rowLength;
        float heightOfHexagon = (float)Math.sqrt(3/2f) * tileSize;

        //I make the points at the center, hence the half width/height added on each.
        //first column's x = tileSize/2. Each column after that adds 3*tileSize/4
        float startX = startCol * 3 / 4 *tileSize + tileSize / 2;
        float endX = endCol * 3 / 4 * tileSize + tileSize / 2;
        //first row's y = tileSize/2 if x is even or tileSize if x is odd. each row after adds tileSize
        float startY = startRow * heightOfHexagon + heightOfHexagon / (2-(startCol&1));
        float endY = endRow * heightOfHexagon + heightOfHexagon / (2-(endCol&1));

        //I know the line crosses at least (may be on edge between two hexes) this many hexes,
        //so I sample points at this many evenly spaced sections on the line
        int numSamples = findManhattanDistance(startRow, startCol, endRow, endCol);

        //the extra size is because theres a chance the point may be on an edge. I want to check both of them
        Hexagon[] path = new Hexagon[numSamples + 1 + (numSamples)/2];
        Hexagon[] nearestHexagons;
        //distance between the samples; add onto the x and y each time to follow the slope of the line
        float xSampleDist = numSamples / (endX - startX);
        float ySampleDist = numSamples / (endY - startY);
        float x = startX;
        float y = startY;
        //the first hexagon the line hits is the one where it started
        path[0] = new Hexagon(startRow, startCol);

        //sample variable keeps track of what index in path I insert into.
        // can't use i, since sometimes I make two insertions per point
        for(int i = 1, sample = 0; i <= numSamples; i++, sample++){
            x += xSampleDist;
            y += ySampleDist;
            nearestHexagons = getNearestHexToPoint(x,y);
            path[sample] = nearestHexagons[0]; //there will always be at least one nearest hexagon
            //possible to have two nearest ones (point on an edge), happens at most numSamples/2 times
            if(nearestHexagons.length > 1) {
                sample++;
                path[sample] = nearestHexagons[1];
            }
        }
        return path;
    }

    private Hexagon[] getNearestHexToPoint(float x, float y){
        Hexagon[] nearestPoints;
        final float E = .0005f;
        int row;
        int row2 = -1;
        int col;
        int col2 = -1;
        float tentativeCol = 8*x/3*tileSize;
        //if it's close enough to the edge that I consider it to be on it
        if(Math.abs(tentativeCol - ((int)tentativeCol + .5f)) <= E) {
            col = (int)tentativeCol;
            col2 = col+1;
        }
        else
            col = Math.round(tentativeCol);

        //needed to do row after column, because I need to know column for the bitwise operation
        float tentativeRow = (float)((y*(2 - col&1)-100*Math.sqrt(3/2))/((2 - col&1)*100*Math.sqrt(3/2)));
        //col2 will be set anytime row2 will be set, but col2 may be set without setting row2
        if(Math.abs(tentativeRow - (int)(tentativeRow) + .5f) <= E){
            row = (int)tentativeRow;
            row2 = row+1;
        }
        else
            row = Math.round(tentativeRow);

        if(col2 != -1){ //point is on an edge between two hexagons
            nearestPoints = new Hexagon[2];
            if(row2 != -1) //hexagons above/below
                nearestPoints[1] = new Hexagon(row2, col2);
            else //on diagonal edge with one hex top left/right and other bot right/left
                nearestPoints[1] = new Hexagon(row, col2);
        }
        else //point is not on an edge
            nearestPoints = new Hexagon[1];
        nearestPoints[0] = new Hexagon(row, col);

        return nearestPoints;
    }

    private int[] convertToCubeCoordinates(int row, int col){
        int[] cube = new int[3];
        cube[0] = col; //x
        cube[2] = row - (col - (col&1)) / 2; //z
        cube[1] = -cube[0]-cube[2]; //y
        return cube;
    }

    private int findManhattanDistance(int startRow, int startCol, int endRow, int endCol){
        int[] startCube = convertToCubeCoordinates(startRow, startCol);
        int[] endCube = convertToCubeCoordinates(endRow, endCol);
        return (Math.abs(startCube[0] - endCube[0]) + Math.abs(startCube[1] - endCube[1]) + Math.abs(startCube[2] - endCube[2]) / 2);
    }

    /**
     *
     * @param unit unit who is checking the surrounding terrain
     * @return an array of mapIDs for possible moves for a unit, excluding the current position
     */
    int[] checkSurroundingTerrain(Unit unit, Player player, boolean attacking){
        double distance;
        PriorityQueue<TerrainCostTuple> queue = new PriorityQueue<>();
        HashSet<TerrainCostTuple> visited = new HashSet<>(40);

        TerrainCostTuple start = new TerrainCostTuple(unit.getMapID());
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
            visited.add(lowestCostTuple);
            TerrainCostTuple[] neighbors = getNeighborTuples(lowestCostTuple.getMapID());
            for(TerrainCostTuple tuple : neighbors){
                //projectiles move the same speed in the air regardless of terrain: hence the unit cost
                if(attacking)
                    tuple.setCost(lowestCostTuple.getCost() + 1);
                //not attacking, but I don't want units (even friendly units) to move through each other
                else if(player.getFriendlyUnit(tuple.getMapID()) == null
                        && player.getEnemyUnit(tuple.getMapID()) == null)
                    tuple.setCost(lowestCostTuple.getCost() + unit.getMovementCost(tuple.terID));

                //This doesn't care about distance to all mapIDs, only those reachable by the unit
                if(tuple.getCost() <= distance)
                    queue.add(tuple);
            }
        }

        if(attacking && unit instanceof RangedUnit){
            for(TerrainCostTuple t : visited){
                if(t.getCost() < ((RangedUnit)unit).getMinAttackRange())
                    visited.remove(t);
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

        TerrainCostTuple(int mapID){
            this.mapID = mapID;
            terID = terrain[mapID];
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
    }

    //length is 4 to 6
    private TerrainCostTuple[] getNeighborTuples(int mapID){
        Integer[] neighborMapIDs = getNeighborsMapIDs(mapID);
        if(neighborMapIDs == null)
            return new TerrainCostTuple[0];
        TerrainCostTuple[] neighbors = new TerrainCostTuple[neighborMapIDs.length];
        int i = 0;
        for(Integer gridID : neighborMapIDs){
            neighbors[i] = new TerrainCostTuple(gridID);
            i++;
        }
        return neighbors;
    }

    //length is 4 to 6
    private Integer[] getNeighborsMapIDs(int mapID) {
        if(mapID < 0 || mapID >= terrain.length)
            return null;
        ArrayList<Integer> neighbors = new ArrayList<>();
        boolean top = false, bot = false, left = false, right = false;
        int rowLength = (int) Math.sqrt(terrain.length);

        if (mapID % rowLength == 0)
            top = true;
        else if (mapID % rowLength == rowLength - 1)
            bot = true;
        if (mapID / rowLength == 0)
            left = true;
        else if (mapID / rowLength == rowLength - 1)
            right = true;

        if(!top && !left)
            neighbors.add(mapID - rowLength - 1);
        if(!left)
            neighbors.add(mapID-rowLength);
        if(!top)
            neighbors.add(mapID - 1);
        if(!bot)
            neighbors.add(mapID + 1);
        //don't check if bot and left or right, because they can't be accessed from the hexagon
        if(!top && !right)
            neighbors.add(mapID + rowLength-1);
        if(!right)
            neighbors.add(mapID + rowLength);
        return neighbors.toArray(new Integer[neighbors.size()]);
    }
}

package warofages.gamebackend;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;

import coms309.mike.units.Unit;

/**
 * Implementation of Dijkstra's algorithm to find which mapIDs may be moved to or attacked
 * Does not check whether a mapID is valid for a move (unit on the space) or attack (no unit on the space)
 * Created by mike on 6/16/2017.
 */
//TODO I'm thinking about having certain terrain such as forests and mountains block line of sight for attacks, but Dijkstra's alone wouldn't work for that
class TerrainDijkstra {
    private int[]terrain;

    TerrainDijkstra(int[] terrainMap){
        terrain = terrainMap;
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

        if(attacking)
            distance = unit.getMaxAttackRange();
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

                if(tuple.getCost() <= distance)
                    queue.add(tuple);
            }
        }

        if(attacking){
            for(TerrainCostTuple t : visited){
                if(t.getCost() < unit.getMinAttackRange())
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
        private int terID;
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

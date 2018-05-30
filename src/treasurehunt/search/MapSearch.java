package treasurehunt.search;

import treasurehunt.constant.Direction;
import treasurehunt.constant.Obstacle;
import treasurehunt.constant.Tool;
import treasurehunt.map.Map;
import treasurehunt.map.MapPosition;
import treasurehunt.map.MapTile;
import treasurehunt.search.find.CoastalFinder;
import treasurehunt.search.find.HarbourFinder;
import treasurehunt.search.find.HomeHarbourFinder;
import treasurehunt.search.find.UnexploredFinder;
import treasurehunt.search.priority.*;

import java.util.*;

/**
 * MapSearch provides methods for finding a variety of different tiles.
 * Each search method finds the valid tiles and then prioritises them before
 * returning the valid priorities tiles as an array with the highest priority
 * tile being first in the array.
 */
public class MapSearch
{
    /**
     * Finds all of the unexplored tiles on the map. The tiles are prioritiesed
     * by how much new information would be seen if the player walk there devided
     * by the approximate number of moves it would take to get there.
     *
     * @param map The map to find all the unexplored tiles on.
     * @return All of the unexplored tiles on the map.
     */
    public static MapTile[] findUnexploredTiles(Map map)
    {
        UnexploredFinder finder = new UnexploredFinder(map);
        HashSet<MapTile> unexplored = finder.find();

        if (unexplored.size() == 0) return null;

        UnexploredPrioritiser pt = new UnexploredPrioritiser(map);
        pt.addTiles(unexplored);
        return pt.toArray();
    }

    /**
     * Finds all the water that is adjacent to the player's current zone.
     *
     * @param map The Map to search.
     * @return All of the MapTile that have WATER as an obstacle and are adjacent
     * to the player's current tile.
     */
    public static MapTile[] findCoastalTiles(Map map)
    {
        CoastalFinder finder = new CoastalFinder(map, map.getPlayer().getTile().getZone());
        HashSet<MapTile> coastalTiles = finder.find();

        if (coastalTiles.size() == 0) return null;

        CoastalPrioritiser pt = new CoastalPrioritiser(map);
        pt.addTiles(coastalTiles);
        return pt.toArray();
    }

    /**
     * Finds all of the land based tiles the player could potentially go to from
     * the given starting tile which must be a water tile.
     *
     * @param map The map to search.
     * @param start The water tile to search from.
     * @param canEscape If the player is able to escape from the zone that the
     *                  harbour tile is part of. Escaping essentially means that
     *                  there is a tree on the harbour's zone that could be cut
     *                  down to use as a raft.
     * @param avoidTrees Avoid any harbour tiles that have a TREE on them.
     * @return
     */
    public static MapTile[] findHabourTiles(Map map, MapTile start, boolean canEscape, boolean avoidTrees)
    {
        if(!start.isWater()) return null;

        HarbourFinder finder = new HarbourFinder(map, avoidTrees);
        HashSet<MapTile> harbourTiles = finder.find(start);

        if(harbourTiles.size() == 0) return null;

        HarbourPrioritiser pt = new HarbourPrioritiser(map, canEscape);
        pt.addTiles(harbourTiles);

        return pt.toArray();
    }
    public static MapTile[] findHabourTiles(Map map, boolean canEscape, boolean avoidTrees)
    {
        return findHabourTiles(map, map.getPlayer().getTile(), canEscape, avoidTrees);
    }
    public static MapTile[] findHabourTiles(Map map)
    {
        return findHabourTiles(map, true, true);
    }

    /**
     * Like findHarbourTiles() but only finds harbour tiles that are a part of the
     * starting zone of the player.
     *
     * @param map The map to search.
     * @return All of the MapTile that are part of the starting zone and are next
     * to water.
     */
    public static MapTile[] findHomeHabourTiles(Map map)
    {
        if(!map.getPlayer().isSailing()) return null;

        HomeHarbourFinder finder = new HomeHarbourFinder(map);
        HashSet<MapTile> harbourTiles = finder.find();

        if(harbourTiles.size() == 0) return null;

        HomeHarbourPrioritiser pt = new HomeHarbourPrioritiser(map);
        pt.addTiles(harbourTiles);
        return pt.toArray();
    }

    /**
     * Finds places where stepping stones could be placed. The places are prioritied
     * by the potential reward the player would recieve by accessing new land with
     * the placement of the stone.
     *
     * @param map The Map to search.
     * @return All of the potential places a stepping stone could be placed.
     */
    public static MapTile[] findStoneTargets(Map map)
    {
        MapTile[] coastalTiles = findCoastalTiles(map);

        if(coastalTiles == null || coastalTiles.length == 0) return null;

        StoneTargetPrioritiser pt = new StoneTargetPrioritiser(map);
        pt.addTiles(coastalTiles);
        return pt.toArray();
    }

    /**
     * Finds the number of unexplored tiles in a radius around the origin. The
     * radius is a square of length radius*2 centered on the origin.
     *
     * @param map The map to search.
     * @param origin The origin of the search.
     * @param radius How far to look.
     * @return The number of unexplored tiles around the origin within the given
     * radius.
     */
    public static int numUnexploredTilesNearby(Map map, MapTile origin, int radius)
    {
        int count = 0;

        int xMin = origin.getPosition().getX() - radius;
        int yMin = origin.getPosition().getY() - radius;

        int xMax = origin.getPosition().getX() + radius;
        int yMax = origin.getPosition().getY() + radius;

        MapTile tile;

        for(int x = xMin; x <= xMax; x++) {
            for(int y = yMin; y <= yMax; y++) {
                tile = map.getTile(new MapPosition(x, y));
                if(tile == null || !tile.isExplored()) count++;
            }
        }

        return count;
    }
    public static int numUnexploredTilesNearby(Map map, MapTile origin)
    {
        int radius = Math.max(Map.VIEW_WIDTH/2, Map.VIEW_HEIGHT/2);
        return numUnexploredTilesNearby(map, origin, radius);
    }

    /**
     * Finds the optimal path from the start MapTile to the finish MapTile based
     * on a set of given restrictions, i.e avoidWater, avoidStones, avoidTrees.
     *
     * The path is returned as an array of tiles with the first tile being the
     * start of the array and the last tile being the end of the array.
     *
     * The path will also not go through any tiles that are impossible to go through
     * do to the lack of an AXE or KEY.
     *
     * @param map The map to plot the path on.
     * @param start The starting MapTile.
     * @param finish The goal MapTile.
     * @param avoidWater If TRUE then the path will not go through any water.
     * @param avoidTrees If TRUE then the path will not go through any trees.
     * @param avoidStones If TRUE then the path will no go into any stepping stone
     *                    tools that have not been picked up yet.
     * @return The path from start to finish MapTiles.
     */
    public static MapTile[] getPath(Map map, MapTile start, MapTile finish, boolean avoidWater, boolean avoidTrees, boolean avoidStones)
    {
        //if(finish.isWater() && avoidWater) return null;

        PriorityQueue<TileNode> queue = new PriorityQueue<>();
        HashMap<MapTile, TileNode> expanded = new HashMap<>();

        TileNode current = new TileNode(start, null, 0);
        MapTile[] neighbours;

        Direction direction;
        int cost;

        while(current.getValue() != finish) {
            neighbours = current.getValue().getNeighbours();
            for(MapTile neighbour : neighbours) {

                if (expanded.containsKey(neighbour)) continue;

                if(map.getPlayer().isSailing()) {
                    if(!neighbour.isWater() && neighbour != finish) continue;
                }
                else {
                    if(neighbour.getObstacle() == Obstacle.WILDERNESS) continue;
                    if (neighbour.getObstacle() == Obstacle.WALL) continue;
                    if (neighbour.getObstacle() == Obstacle.WATER && avoidWater && neighbour != finish) continue;
                    if (neighbour.getObstacle() == Obstacle.TREE && (avoidTrees || !map.getPlayer().hasTool(Tool.AXE))) continue;
                    if (neighbour.getObstacle() == Obstacle.DOOR && !map.getPlayer().hasTool(Tool.KEY)) continue;
                    if (neighbour.getTool() == Tool.STONE && avoidStones) continue;
                }

                if(current.getValue().north() == neighbour) direction = Direction.NORTH;
                else if(current.getValue().east() == neighbour) direction = Direction.EAST;
                else if(current.getValue().south() == neighbour) direction = Direction.SOUTH;
                else direction = Direction.WEST;

                cost = current.getCost() + Heuristic.distance(neighbour, finish, direction);
                queue.add(new TileNode(neighbour, current.getValue(), cost));
            }
            expanded.put(current.getValue(), current);
            current = queue.poll();
            if(current == null) return null;
        }

        ArrayList<MapTile> tiles = new ArrayList<>();

        while(current != null) {
            tiles.add(0, current.getValue());
            current = expanded.get(current.getParent());
        }

        MapTile[] path = new MapTile[tiles.size()];
        tiles.toArray(path);

        return path;
    }
    public static MapTile[] getPath(Map map, MapTile start, MapTile finish)
    {
        return getPath(map, start, finish, false, false, false);
    }
}

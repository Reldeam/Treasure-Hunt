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

public class MapSearch
{
    public static MapTile[] findUnexploredTiles(Map map)
    {
        UnexploredFinder finder = new UnexploredFinder(map);
        HashSet<MapTile> unexplored = finder.find();

        if (unexplored.size() == 0) return null;

        UnexploredPrioritiser pt = new UnexploredPrioritiser(map);
        pt.addTiles(unexplored);
        return pt.toArray();
    }

    public static MapTile[] findCoastalTiles(Map map)
    {
        CoastalFinder finder = new CoastalFinder(map, map.getPlayer().getTile().getZone());
        HashSet<MapTile> coastalTiles = finder.find();

        if (coastalTiles.size() == 0) return null;

        CoastalPrioritiser pt = new CoastalPrioritiser(map);
        pt.addTiles(coastalTiles);
        return pt.toArray();
    }

    public static MapTile[] findHabourTiles(Map map, MapTile start, boolean canEscape, boolean avoidTrees)
    {
        if(!start.isWater()) return null;

        HarbourFinder finder = new HarbourFinder(map, avoidTrees);
        HashSet<MapTile> harbourTiles = finder.find();

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

    public static MapTile[] findStoneTargets(Map map)
    {
        MapTile[] coastalTiles = findCoastalTiles(map);

        if(coastalTiles == null || coastalTiles.length == 0) return null;

        StoneTargetPrioritiser pt = new StoneTargetPrioritiser(map);
        pt.addTiles(coastalTiles);
        return pt.toArray();
    }

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

    public static MapTile[] getPath(Map map, MapTile start, MapTile finish, boolean avoidWater, boolean avoidTrees, boolean avoidStones)
    {
        if(finish.isWater() && avoidWater) return null;

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
                    if (neighbour.getObstacle() == Obstacle.WATER && avoidWater) continue;
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

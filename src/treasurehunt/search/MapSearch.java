package treasurehunt.search;

import treasurehunt.constant.Direction;
import treasurehunt.constant.Obstacle;
import treasurehunt.constant.Tool;
import treasurehunt.map.Map;
import treasurehunt.map.MapPosition;
import treasurehunt.map.MapTile;
import treasurehunt.search.priority.*;

import javax.swing.text.html.HTMLDocument;
import java.util.*;

public class MapSearch
{
    public static MapTile[] findUnexploredTiles(Map map)
    {
        HashSet<MapTile> expanded = new HashSet<MapTile>();
        LinkedList<MapTile> unexpanded = new LinkedList<>();

        HashSet<MapTile> unexplored = new HashSet<MapTile>();

        unexpanded.add(map.getPlayer().getTile());
        expanded.add(map.getPlayer().getTile());

        MapTile tile;

        while((tile = unexpanded.poll()) != null) {
            if(!tile.isExplored()) unexplored.add(tile);
            for(MapTile neighbour : tile.getNeighbours()) {
                if(expanded.contains(neighbour)) continue;
                if(neighbour.getObstacle() == Obstacle.WALL || neighbour.getObstacle() == Obstacle.WILDERNESS) continue;
                if(!map.getPlayer().isSailing() && neighbour.getObstacle() == Obstacle.WATER) continue;
                if(map.getPlayer().isSailing() && neighbour.getObstacle() != Obstacle.WATER) continue;
                expanded.add(neighbour);
                unexpanded.add(neighbour);
            }
        }

        UnexploredPT pt = new UnexploredPT(map);
        pt.addTiles(unexplored);
        return pt.toArray();
    }

    public static MapTile[] findCoastalTiles(Map map)
    {
        // STEP 1: Find all targets.

        LinkedList<MapTile> unexpanded = new LinkedList<>();
        HashSet<MapTile> expanded = new HashSet<>();
        HashSet<MapTile> coastalTiles = new HashSet<>();

        unexpanded.add(map.getPlayer().getTile());
        expanded.add(map.getPlayer().getTile());

        MapTile tile;

        while ((tile = unexpanded.poll()) != null) {
            for (MapTile neighbour : tile.getNeighbours()) {
                if (expanded.contains(neighbour)) continue;
                if (neighbour.isWater()) coastalTiles.add(neighbour);
                else if (neighbour.getZone() != tile.getZone()) continue;
                else {
                    unexpanded.add(neighbour);
                    expanded.add(neighbour);
                }
            }
        }

        if (coastalTiles.size() == 0) return null;

        // STEP 2: Priorities found targets.
        // Reward: (# of water tiles - # of explored tiles) + (# of adjacent zones - 1) / distance to target

        CoastalPT pt = new CoastalPT(map);
        pt.addTiles(coastalTiles);
        return pt.toArray();
    }

    public static MapTile[] findHabourTiles(Map map, MapTile start, boolean canEscape, boolean avoidTrees)
    {
        // STEP 1: Find all targets.

        if(!start.isWater()) return null;

        LinkedList<MapTile> unexpanded = new LinkedList<>();
        HashSet<MapTile> expanded = new HashSet<>();
        HashSet<MapTile> harbourTiles = new HashSet<>();

        unexpanded.add(start);
        expanded.add(start);

        MapTile tile;

        while((tile = unexpanded.poll()) != null) {
            for(MapTile neighbour : tile.getNeighbours()) {
                if(expanded.contains(neighbour)) continue;
                if(neighbour.isWater()) {
                    expanded.add(neighbour);
                    unexpanded.add(neighbour);
                }
                else if(neighbour.getObstacle() == Obstacle.NONE
                     || (neighbour.getObstacle() == Obstacle.DOOR && map.getPlayer().hasTool(Tool.KEY))
                     || (neighbour.getObstacle() == Obstacle.TREE && map.getPlayer().hasTool(Tool.AXE) && !avoidTrees)) {
                    harbourTiles.add(neighbour);
                }
            }
        }

        if(harbourTiles.size() == 0) return null;

        // STEP 2: Priorities found targets.

        HarbourPT pt = new HarbourPT(map, canEscape);
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
        // STEP 1: Find all targets.

        if(!map.getPlayer().isSailing()) return null;

        int homeZone = map.getTile(new MapPosition(0, 0)).getZone();

        LinkedList<MapTile> unexpanded = new LinkedList<>();
        HashSet<MapTile> expanded = new HashSet<>();
        HashSet<MapTile> harbourTiles = new HashSet<>();

        unexpanded.add(map.getPlayer().getTile());
        expanded.add(map.getPlayer().getTile());

        MapTile tile;

        while((tile = unexpanded.poll()) != null) {
            for(MapTile neighbour : tile.getNeighbours()) {
                if(expanded.contains(neighbour)) continue;
                if(neighbour.isWater()) {
                    expanded.add(neighbour);
                    unexpanded.add(neighbour);
                    continue;
                }
                else if(neighbour.getObstacle() == Obstacle.NONE
                        || (neighbour.getObstacle() == Obstacle.DOOR && map.getPlayer().hasTool(Tool.KEY))
                        || (neighbour.getObstacle() == Obstacle.TREE && map.getPlayer().hasTool(Tool.AXE))) {
                    if(homeZone == neighbour.getZone()) harbourTiles.add(neighbour);
                }
            }
        }

        if(harbourTiles.size() == 0) return null;

        // STEP 2: Priorities found targets.

        HomeHarbourPT pt = new HomeHarbourPT(map);
        pt.addTiles(harbourTiles);
        return pt.toArray();
    }

    public static MapTile[] findStoneTargets(Map map)
    {
        MapTile[] coastalTiles = findCoastalTiles(map);

        // STEP 2: Priorities found targets.

        StoneTargetPT pt = new StoneTargetPT(map);
        pt.addTiles(coastalTiles);
        return pt.toArray();
    }

    /*
    public static MapTile[] findStoneTargets(Map map)
    {
        // STEP 1: Find all targets.

        HashSet<MapTile> expanded = new HashSet<MapTile>();
        LinkedList<MapTile> unexpanded = new LinkedList<>();
        HashSet<MapTile> coastalTiles = new HashSet<MapTile>();
        unexpanded.add(map.getPlayer().getTile());
        MapTile tile;

        while((tile = unexpanded.poll()) != null)
        {
            expanded.add(tile);
            for(MapTile neighbour : tile.getNeighbours()) {
                if(expanded.contains(neighbour)) continue;
                if(neighbour.getObstacle() == Obstacle.WALL) continue;
                if(neighbour.getObstacle() == Obstacle.WATER) {
                    coastalTiles.add(neighbour);
                    continue;
                }
                unexpanded.add(neighbour);
            }
        }

        // STEP 2: Priorities found targets.

        PriorityQueue<TileReward> targetRewards = new PriorityQueue<>();
        Iterator iterator = coastalTiles.iterator();

        while(iterator.hasNext()) {
            tile = (MapTile) iterator.next();

            int coast = 0;
            if(tile.north() != null && tile.north().getObstacle() != Obstacle.WATER && tile.north().getObstacle() != Obstacle.WALL) coast++;
            if(tile.east() != null && tile.east().getObstacle() != Obstacle.WATER && tile.east().getObstacle() != Obstacle.WALL) coast++;
            if(tile.south() != null && tile.south().getObstacle() != Obstacle.WATER && tile.south().getObstacle() != Obstacle.WALL) coast++;
            if(tile.west() != null && tile.west().getObstacle() != Obstacle.WATER && tile.west().getObstacle() != Obstacle.WALL) coast++;

            if(coast < 2) continue;

            int reward = numUnexploredTilesNearby(map, tile) + tile.zoneReward();
            System.out.println(reward);
            targetRewards.add(new TileReward(tile, reward));
        }

        if(targetRewards.size() == 0) return null;

        // STEP 3: Return as array of targets.

        MapTile[] targets = new MapTile[targetRewards.size()];
        iterator = targetRewards.iterator();

        for(int i = 0; i < targets.length; i++) {
            targets[i] = ((TileReward) iterator.next()).getTile();
        }

        return targets;
    }
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

package treasurehunt.search.priority;

import treasurehunt.map.Map;
import treasurehunt.map.MapTile;
import treasurehunt.search.TileReward;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;

public abstract class PrioritisedTiles extends PriorityQueue<TileReward>
{
    protected final Map map;

    public PrioritisedTiles(Map map)
    {
        this.map = map;
    }

    public void addTiles(HashSet<MapTile> tiles)
    {
        Iterator iterator = tiles.iterator();
        MapTile tile;

        while(iterator.hasNext()) {
            tile = (MapTile) iterator.next();
            if(!valid(tile)) continue;
            add(new TileReward(tile, reward(tile)));
        }
    }
    public void addTiles(MapTile[] tiles)
    {
        HashSet<MapTile> set = new HashSet<>();
        Collections.addAll(set, tiles);
        addTiles(set);
    }

    public MapTile[] toArray()
    {
        Iterator iterator = iterator();
        MapTile[] tiles = new MapTile[size()];
        int i = 0;

        while(iterator.hasNext()) {
            tiles[i++] = ((TileReward) iterator.next()).getTile();
        }

        return tiles;
    }

    public abstract int reward(MapTile tile);

    public abstract boolean valid(MapTile tile);
}

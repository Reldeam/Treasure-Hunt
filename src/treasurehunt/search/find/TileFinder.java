package treasurehunt.search.find;

import treasurehunt.map.Map;
import treasurehunt.map.MapTile;

import java.util.HashSet;
import java.util.LinkedList;

public abstract class TileFinder
{
    protected final Map map;

    public TileFinder(Map map)
    {
        this.map = map;
    }

    public HashSet<MapTile> find(MapTile start)
    {
        HashSet<MapTile> expanded = new HashSet<>();
        LinkedList<MapTile> unexpanded = new LinkedList<>();

        HashSet<MapTile> matches = new HashSet<>();

        unexpanded.add(start);
        expanded.add(start);

        MapTile tile;

        while((tile = unexpanded.poll()) != null) {

            if(validMatch(tile)) matches.add(tile);

            for(MapTile neighbour : tile.getNeighbours()) {
                if(!validNeighbour(tile, neighbour) || expanded.contains(neighbour)) continue;
                expanded.add(neighbour);
                unexpanded.add(neighbour);
            }
        }

        return matches;
    }

    public HashSet<MapTile> find()
    {
        return find(map.getPlayer().getTile());
    }

    protected abstract boolean validMatch(MapTile tile);
    protected abstract boolean validNeighbour(MapTile tile, MapTile neighbour);
}

package treasurehunt.search.find;

import treasurehunt.constant.Obstacle;
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

    public HashSet<MapTile> find()
    {
        HashSet<MapTile> expanded = new HashSet<>();
        LinkedList<MapTile> unexpanded = new LinkedList<>();

        HashSet<MapTile> matches = new HashSet<>();

        unexpanded.add(map.getPlayer().getTile());
        expanded.add(map.getPlayer().getTile());

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

    protected abstract boolean validMatch(MapTile tile);
    protected abstract boolean validNeighbour(MapTile tile, MapTile neighbour);
}

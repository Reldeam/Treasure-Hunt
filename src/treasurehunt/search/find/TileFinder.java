package treasurehunt.search.find;

import treasurehunt.map.Map;
import treasurehunt.map.MapTile;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * TileFinder is a search tool for finding MapTile(s) on a given Map based on
 * specific requirements depending on the implementation.
 */
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

    /**
     * Whether or not a given MapTile constitutes a tile that the TileFinder is
     * trying to find.
     *
     * @param tile The tile to be checked for validity.
     * @return TRUE if the tile is a valid match, FALSE otherwise.
     */
    protected abstract boolean validMatch(MapTile tile);

    /**
     * Whether or not a given tile is a valid neighbour during the searching
     * process. Certain MapTiles may wish to be exluded from checking their
     * match validity or from propogating the search through any of their
     * neighbours. These MapTiles can be set to FALSE as a validNeighbour() to
     * remove them and their neighbours from the search as potential candidates.
     *
     * @param tile The origin tile.
     * @param neighbour The neighbour to check the validity of.
     * @return TRUE if the neighbour is valid, FALSE otherwise.
     */
    protected abstract boolean validNeighbour(MapTile tile, MapTile neighbour);
}

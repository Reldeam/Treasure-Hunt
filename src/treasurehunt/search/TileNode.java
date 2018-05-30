package treasurehunt.search;

import treasurehunt.map.MapTile;

/**
 * An implementation of the SearchNode that uses MapTile as values.
 */
public class TileNode extends SearchNode<MapTile>
{
    public TileNode(MapTile tile, MapTile parent, int cost)
    {
        super(tile, parent, cost);
    }
}

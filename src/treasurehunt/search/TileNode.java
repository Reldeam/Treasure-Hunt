package treasurehunt.search;

import treasurehunt.map.MapTile;

public class TileNode extends SearchNode<MapTile>
{
    public TileNode(MapTile tile, MapTile parent, int cost)
    {
        super(tile, parent, cost);
    }
}

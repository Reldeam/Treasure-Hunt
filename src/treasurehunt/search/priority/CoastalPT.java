package treasurehunt.search.priority;

import treasurehunt.map.Map;
import treasurehunt.map.MapTile;
import treasurehunt.search.Heuristic;

import java.util.HashSet;

public class CoastalPT extends PrioritisedTiles
{
    public CoastalPT(Map map)
    {
        super(map);
    }

    @Override
    public int reward(MapTile tile)
    {
        MapTile[] zoneTiles = tile.zoneTiles();
        int numExplored = 0;

        for (MapTile zoneTile : zoneTiles) {
            if (zoneTile.isExplored()) numExplored++;
        }

        return (zoneTiles.length - numExplored) + (tile.numAdjacentZones() - 1) / Heuristic.distance(map.getPlayer().getTile(), tile, map.getPlayer().getDirection());
    }

    @Override
    public boolean valid(MapTile tile)
    {
        return true;
    }
}

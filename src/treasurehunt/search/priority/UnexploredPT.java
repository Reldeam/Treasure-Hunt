package treasurehunt.search.priority;

import treasurehunt.map.Map;
import treasurehunt.map.MapTile;
import treasurehunt.search.Heuristic;
import treasurehunt.search.MapSearch;

import java.util.HashSet;

public class UnexploredPT extends PrioritisedTiles
{
    public UnexploredPT(Map map)
    {
        super(map);
    }

    @Override
    public int reward(MapTile tile)
    {
        return MapSearch.numUnexploredTilesNearby(map, tile) / Heuristic.distance(map.getPlayer().getTile(), tile, map.getPlayer().getDirection());
    }

    @Override
    public boolean valid(MapTile tile)
    {
        return true;
    }
}

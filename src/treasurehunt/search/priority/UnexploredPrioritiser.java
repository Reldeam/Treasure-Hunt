package treasurehunt.search.priority;

import treasurehunt.map.Map;
import treasurehunt.map.MapTile;
import treasurehunt.search.Heuristic;
import treasurehunt.search.MapSearch;

public class UnexploredPrioritiser extends TilePrioritiser
{
    public UnexploredPrioritiser(Map map)
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

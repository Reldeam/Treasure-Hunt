package treasurehunt.search.priority;

import treasurehunt.map.Map;
import treasurehunt.map.MapTile;
import treasurehunt.search.Heuristic;

public class HomeHarbourPrioritiser extends TilePrioritiser
{
    public HomeHarbourPrioritiser(Map map)
    {
        super(map);
    }

    @Override
    public int reward(MapTile tile)
    {
        return 1 / Heuristic.distance(map.getPlayer().getTile(), tile, map.getPlayer().getDirection());
    }

    @Override
    public boolean valid(MapTile tile)
    {
        return true;
    }
}

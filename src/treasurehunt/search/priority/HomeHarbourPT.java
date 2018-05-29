package treasurehunt.search.priority;

import treasurehunt.map.Map;
import treasurehunt.map.MapTile;
import treasurehunt.search.Heuristic;

import java.util.HashSet;

public class HomeHarbourPT extends PrioritisedTiles
{
    public HomeHarbourPT(Map map)
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

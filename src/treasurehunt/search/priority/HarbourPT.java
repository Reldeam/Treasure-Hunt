package treasurehunt.search.priority;

import treasurehunt.map.Map;
import treasurehunt.map.MapTile;
import treasurehunt.search.MapSearch;

import java.util.HashSet;

public class HarbourPT extends PrioritisedTiles
{
    private final boolean canEscape;

    public HarbourPT(Map map, boolean canEscape)
    {
        super(map);
        this.canEscape = canEscape;
    }

    @Override
    public int reward(MapTile tile)
    {
        return MapSearch.numUnexploredTilesNearby(map, tile);
    }

    @Override
    public boolean valid(MapTile tile)
    {
        return !canEscape || tile.zoneContainsTree();
    }
}

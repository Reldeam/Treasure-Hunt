package treasurehunt.search.priority;

import treasurehunt.map.Map;
import treasurehunt.map.MapTile;
import treasurehunt.search.MapSearch;

public class HarbourPrioritiser extends TilePrioritiser
{
    private final boolean canEscape;

    public HarbourPrioritiser(Map map, boolean canEscape)
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

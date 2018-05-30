package treasurehunt.search.priority;

import treasurehunt.constant.Obstacle;
import treasurehunt.constant.Tool;
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
        int reward = (MapSearch.numUnexploredTilesNearby(map, tile) + tile.zoneReward());
        if(tile.getObstacle() == Obstacle.TREE) reward -= Map.MAX_WIDTH * Map.MAX_HEIGHT;
        return reward;
    }

    @Override
    public boolean valid(MapTile tile)
    {
        return (!canEscape
        || (tile.getObstacle() == Obstacle.TREE && tile.numObstaclesInZone(Obstacle.TREE) > 1)
        || (tile.getObstacle() != Obstacle.TREE && tile.zoneContainsTree())
        || (tile.zoneContainsTool(Tool.STONE)));
    }
}

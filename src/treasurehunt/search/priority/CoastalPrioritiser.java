package treasurehunt.search.priority;

import treasurehunt.constant.Tool;
import treasurehunt.map.Map;
import treasurehunt.map.MapPosition;
import treasurehunt.map.MapTile;
import treasurehunt.search.Heuristic;

public class CoastalPrioritiser extends TilePrioritiser
{
    public CoastalPrioritiser(Map map)
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

        int reward = (zoneTiles.length - numExplored) + (tile.numAdjacentZones() - 1) / Heuristic.distance(map.getPlayer().getTile(), tile, map.getPlayer().getDirection());

        int homeZone = map.getTile(new MapPosition(0, 0)).getZone();

        if(map.getPlayer().hasTool(Tool.GOLD) && tile.isAdjacentTo(homeZone))
            reward += Map.MAX_WIDTH * Map.MAX_HEIGHT;

        return reward;
    }

    @Override
    public boolean valid(MapTile tile)
    {
        return true;
    }
}

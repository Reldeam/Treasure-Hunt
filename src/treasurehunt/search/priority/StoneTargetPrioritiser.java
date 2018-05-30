package treasurehunt.search.priority;

import treasurehunt.constant.Tool;
import treasurehunt.map.Map;
import treasurehunt.map.MapTile;
import treasurehunt.search.MapSearch;

public class StoneTargetPrioritiser extends TilePrioritiser
{
    public StoneTargetPrioritiser(Map map)
    {
        super(map);
    }

    @Override
    public int reward(MapTile tile)
    {
        int reward;
        int bestReward = 0;

        MapTile[] harbourTiles = MapSearch.findHabourTiles(map, tile, false, true);

        for(MapTile harbour : harbourTiles) {
            if(harbour.getZone() == map.getPlayer().getTile().getZone()) continue;

            MapTile[] path = MapSearch.getPath(map, tile, harbour, false, true, true);
            if(path == null || (path.length - 1) > map.getPlayer().numOfTool(Tool.STONE)) continue;

            reward = (harbour.zoneReward() + harbour.zoneSize()) / path.length;
            if(reward > bestReward) bestReward = reward;
        }

        return bestReward;
    }

    @Override
    public boolean valid(MapTile tile)
    {
        MapTile[] harbourTiles = MapSearch.findHabourTiles(map, tile, false, true);

        for(MapTile harbour : harbourTiles) {
            if(harbour.getZone() == map.getPlayer().getTile().getZone()) continue;
            MapTile[] path = MapSearch.getPath(map, tile, harbour, false, true, true);
            if(path == null || (path.length - 1) > map.getPlayer().numOfTool(Tool.STONE)) continue;
            return true;
        }

        return false;
    }
}

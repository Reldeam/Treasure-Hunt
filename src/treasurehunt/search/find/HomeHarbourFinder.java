package treasurehunt.search.find;

import treasurehunt.constant.Obstacle;
import treasurehunt.constant.Tool;
import treasurehunt.map.Map;
import treasurehunt.map.MapPosition;
import treasurehunt.map.MapTile;

public class HomeHarbourFinder extends TileFinder
{
    private final int home;

    public HomeHarbourFinder(Map map)
    {
        super(map);
        home = map.getTile(new MapPosition(0, 0)).getZone();
    }

    @Override
    protected boolean validMatch(MapTile tile)
    {
        return(home == tile.getZone()
        &&(tile.getObstacle() == Obstacle.NONE
        ||(tile.getObstacle() == Obstacle.DOOR && map.getPlayer().hasTool(Tool.KEY))
        ||(tile.getObstacle() == Obstacle.TREE && map.getPlayer().hasTool(Tool.AXE))));
    }

    @Override
    protected boolean validNeighbour(MapTile tile, MapTile neighbour)
    {
        return tile.isWater() || neighbour.isWater();
    }
}

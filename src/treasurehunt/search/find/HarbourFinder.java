package treasurehunt.search.find;

import treasurehunt.constant.Obstacle;
import treasurehunt.constant.Tool;
import treasurehunt.map.Map;
import treasurehunt.map.MapTile;

public class HarbourFinder extends TileFinder
{
    private final boolean avoidTrees;

    public HarbourFinder(Map map, boolean avoidTrees)
    {
        super(map);
        this.avoidTrees = avoidTrees;
    }

    @Override
    protected boolean validMatch(MapTile tile)
    {
        return(tile.getObstacle() == Obstacle.NONE
        ||(tile.getObstacle() == Obstacle.DOOR && map.getPlayer().hasTool(Tool.KEY))
        ||(tile.getObstacle() == Obstacle.TREE && map.getPlayer().hasTool(Tool.AXE) && !avoidTrees)
        ||(tile.getObstacle() == Obstacle.TREE && map.getPlayer().hasTool(Tool.AXE) && avoidTrees && tile.numObstaclesInZone(Obstacle.TREE) > 1));
    }

    @Override
    protected boolean validNeighbour(MapTile tile, MapTile neighbour)
    {
        return tile.isWater() || neighbour.isWater();
    }
}

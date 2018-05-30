package treasurehunt.search.find;

import treasurehunt.constant.Obstacle;
import treasurehunt.map.Map;
import treasurehunt.map.MapTile;

public class UnexploredFinder extends TileFinder {

    public UnexploredFinder(Map map)
    {
        super(map);
    }

    @Override
    protected boolean validMatch(MapTile tile)
    {
        return !tile.isExplored();
    }

    @Override
    protected boolean validNeighbour(MapTile tile, MapTile neighbour)
    {
        return !((neighbour.getObstacle() == Obstacle.WALL || neighbour.getObstacle() == Obstacle.WILDERNESS)
        || (!map.getPlayer().isSailing() && neighbour.getObstacle() == Obstacle.WATER)
        || (map.getPlayer().isSailing() && neighbour.getObstacle() != Obstacle.WATER));
    }
}

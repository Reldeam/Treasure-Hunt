package treasurehunt.search.find;

import treasurehunt.map.Map;
import treasurehunt.map.MapTile;

public class CoastalFinder extends TileFinder
{
    private final int zone;

    public CoastalFinder(Map map, int zone)
    {
        super(map);
        this.zone = zone;
    }

    @Override
    protected boolean validMatch(MapTile tile)
    {
        return tile.isWater();
    }

    @Override
    protected boolean validNeighbour(MapTile tile, MapTile neighbour)
    {
        return (tile.getZone() == zone && (neighbour.getZone() == zone || neighbour.isWater()));
    }
}

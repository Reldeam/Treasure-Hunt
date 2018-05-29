package treasurehunt.search;

import treasurehunt.map.MapTile;

public class TileReward implements Comparable<TileReward>
{
    private final MapTile tile;
    private final int reward;

    public TileReward(MapTile tile, int reward)
    {
        this.tile = tile;
        this.reward = reward;
    }

    public MapTile getTile() { return tile; }
    public int getReward() { return reward; }

    @Override
    public int compareTo(TileReward tile) {
        return tile.getReward() - reward;
    }
}

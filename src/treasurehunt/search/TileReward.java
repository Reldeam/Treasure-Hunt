package treasurehunt.search;

import treasurehunt.map.MapTile;

/**
 * A way to compare MapTile based on arbitrary rewards. TileReward can then be
 * easily sorted into any Collection that is ordered.
 */
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

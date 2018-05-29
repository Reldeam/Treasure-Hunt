package treasurehunt.map;

/**
 * The MapPosition is used by Map to record the x, y coordinates of each Tile as
 * well as to generate a unique key to store the Tiles for reference.
 *
 * MapPosition may be used as a comparator to find matching Tiles that have the
 * same MapPosition.
 */
public class MapPosition
{
	private final int x;
	private final int y;
	
	public MapPosition(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
	public int getX() { return x; }
	public int getY() { return y; }
	public int[] toArray() { return new int[] {x, y}; };
	
	public boolean equals(Object object)
	{
		MapPosition position = (MapPosition) object;
		return x == position.getX() && y == position.getY();
	}
	
	public int hashCode()
	{
		return x*100 + y;
	}
}

package treasurehunt.map;

import treasurehunt.constant.Direction;
import treasurehunt.constant.Obstacle;
import treasurehunt.constant.Tool;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A MapTile represents one position on the Map. Each MapTile holds all the
 * properties of that position such as the type of obstacle on the tile, as well
 * as if the tile contains a tool.
 *
 * The MapTile also is able to store neighbours (adjacent tiles).
 *
 * Each MapTile is part of a zone. A zone is denoted by a group of joined tiles
 * that each share the same obstacle.
 *
 * MapTile can be set to explored and will automatically find all other tiles
 * in its vicinity that are equivalently explored.
 */
public class MapTile 
{
	private final MapPosition position;
	
	private Obstacle obstacle = Obstacle.NONE;
	private Tool tool = Tool.NONE;
	private boolean explored = false;
	private int zone = -1;
	
	private final HashMap<Direction, MapTile> neighbours = new HashMap<>();
	
	public MapTile(MapPosition position, Obstacle obstacle, Tool tool)
	{
		this.position = position;
		this.obstacle = obstacle;
		this.tool = tool;
	}
	public MapTile(MapPosition position, char type)
	{
		this(position, Obstacle.fromKey(type), Tool.fromKey(type));
	}
	public MapTile(MapPosition position, Obstacle obstacle) { this(position, obstacle, Tool.NONE); }
	public MapTile(MapPosition position, Tool tool) { this(position, Obstacle.NONE, tool); }
	public MapTile(MapPosition position) { this(position, Obstacle.NONE, Tool.NONE); }

	public boolean isExplored() { return explored; }

	/**
	 An explored tile is one that either the player has stood in or that if
	 the player were to stand on the tile they would not see any new tiles that
	 they have never seen before.

	 Sets this tile to be explored and then automatically finds all other tiles
	 around this tile that are equivalently explored (that is, even if the
	 player has not stood in that tile before, if they were to they would not
	 gain any new information for doing so.
	 */
	public void explored()
	{
		explored = true;
		updateExplored(Direction.NORTH, neighbours.get(Direction.NORTH), Map.VIEW_HEIGHT);
		updateExplored(Direction.EAST, neighbours.get(Direction.EAST), Map.VIEW_WIDTH);
		updateExplored(Direction.SOUTH, neighbours.get(Direction.SOUTH), Map.VIEW_HEIGHT);
		updateExplored(Direction.WEST, neighbours.get(Direction.WEST), Map.VIEW_WIDTH);
	}

	/**
	 * Helper function for explored() used to recursively find all tiles that are
	 * considered explored based on the definition given in the explored()
	 * description.
	 */
	private boolean updateExplored(Direction direction, MapTile tile, int distance)
	{
		if(distance <= 0 || tile == null) return false;
		if(tile.isExplored() || tile.getObstacle() == Obstacle.WILDERNESS) return true;
		if(updateExplored(direction, tile.getNeighbour(direction), distance - 1)) {
			tile.explored();
			return true;
		}
		else return false;
	}

	public int getZone() { return zone; }
	public void setZone(int zone) { this.zone = zone; }

	/**
	 * Finds the number of tiles that are part of the same zone.
	 *
	 * @return The number of MapTile that are part of the same zone.
	 */
	public int zoneSize()
	{
		int count = 0;

		LinkedList<MapTile> unexpanded = new LinkedList<>();
		HashSet<MapTile> expanded = new HashSet<>();

		unexpanded.add(this);
		expanded.add(this);

		MapTile tile;

		while((tile = unexpanded.poll()) != null) {
			for(MapTile neighbour : tile.getNeighbours()) {
				if(expanded.contains(neighbour) || zone != neighbour.getZone()) continue;
				unexpanded.add(neighbour);
				expanded.add(neighbour);
			}
			count++;
		}

		return count;
	}

	/**
	 * Gets all of the tiles that are part of the same zone.
	 *
	 * @return All of the MapTile that share the same zone as this tile.
	 */
	public MapTile[] zoneTiles()
	{
		LinkedList<MapTile> unexpanded = new LinkedList<>();
		HashSet<MapTile> expanded = new HashSet<>();

		unexpanded.add(this);
		expanded.add(this);

		MapTile tile;

		while((tile = unexpanded.poll()) != null) {
			for(MapTile neighbour : tile.getNeighbours()) {
				if(expanded.contains(neighbour) || zone != neighbour.getZone()) continue;
				unexpanded.add(neighbour);
				expanded.add(neighbour);
			}
		}

		MapTile[] targets = new MapTile[expanded.size()];
		Iterator iterator = expanded.iterator();

		for(int i = 0; i < targets.length; i++) {
			targets[i] = (MapTile) iterator.next();
		}

		return targets;
	}

	/**
	 * The expected reward obtained if a player were to explore this zone and
	 * collect all of the tools on this zone.
	 *
	 * @return The reward value of all of the items on the zone that this tile
	 * is a part of.
	 */
	public int zoneReward()
	{
		int reward = 0;

		LinkedList<MapTile> unexpanded = new LinkedList<>();
		HashSet<MapTile> expanded = new HashSet<>();

		unexpanded.add(this);
		expanded.add(this);

		MapTile tile;

		while((tile = unexpanded.poll()) != null) {
			for(MapTile neighbour : tile.getNeighbours()) {
				if(expanded.contains(neighbour) || zone != neighbour.getZone()) continue;
				unexpanded.add(neighbour);
				expanded.add(neighbour);
			}
			reward += tile.getTool().reward();
		}

		return reward;
	}

	/**
	 * Finds the number of different zones that are connected to this zone.
	 *
	 * @return The number of different zones that border the zone that this
	 * MapTile is a part of.
	 */
	public int numAdjacentZones()
	{
		LinkedList<MapTile> unexpanded = new LinkedList<>();
		HashSet<MapTile> expanded = new HashSet<>();
		HashSet<Integer> adjacentZones = new HashSet<>();

		unexpanded.add(this);
		expanded.add(this);

		MapTile tile;

		while((tile = unexpanded.poll()) != null) {
			for(MapTile neighbour : tile.getNeighbours()) {
				if(expanded.contains(neighbour)) continue;
				if(zone != neighbour.getZone()) {
					adjacentZones.add(neighbour.getZone());
					continue;
				}
				unexpanded.add(neighbour);
				expanded.add(neighbour);
			}
		}

		return adjacentZones.size();
	}

	/**
	 * Finds whether the zone contains a tree.
	 *
	 * @return TRUE if the zone that this tile is a part of contains a tree. FALSE
	 * otherwise.
	 */
	public boolean zoneContainsTree()
	{
		return numObstaclesInZone(Obstacle.TREE) > 0;
	}

	public int numObstaclesInZone(Obstacle obstacle)
	{
		int count = 0;

		LinkedList<MapTile> unexpanded = new LinkedList<>();
		HashSet<MapTile> expanded = new HashSet<MapTile>();

		unexpanded.add(this);
		expanded.add(this);

		MapTile tile;

		while((tile = unexpanded.poll()) != null) {
			for(MapTile neighbour : tile.getNeighbours()) {
				if(zone != neighbour.getZone()) continue;
				if(neighbour.getObstacle() == obstacle) count++;
				if(expanded.contains(neighbour)) continue;
				expanded.add(neighbour);
				unexpanded.add(neighbour);
			}
		}

		return count;
	}

	public boolean zoneContainsTool(Tool tool)
	{
		return numToolsInZone(tool) > 0;
	}

	public int numToolsInZone(Tool tool)
	{
		int count = 0;

		LinkedList<MapTile> unexpanded = new LinkedList<>();
		HashSet<MapTile> expanded = new HashSet<MapTile>();

		unexpanded.add(this);
		expanded.add(this);

		MapTile tile;

		while((tile = unexpanded.poll()) != null) {
			for(MapTile neighbour : tile.getNeighbours()) {
				if(zone != neighbour.getZone()) continue;
				if(neighbour.getTool() == tool) count++;
				if(expanded.contains(neighbour)) continue;
				expanded.add(neighbour);
				unexpanded.add(neighbour);
			}
		}

		return count;
	}

	/**
	 * Checks if the given MapTile is part of the same zone as this MapTile. If
	 * so then it is possible to travel between the two tiles without the use
	 * of tools.
	 *
	 * @param tile The MapTile to check if this MapTile is connected to
	 * @return TRUE if the given tile is in the same zone, FALSE otherwise.
	 */
	public boolean connected(MapTile tile)
	{
		if(zone == -1) return false;
		return zone == tile.getZone();
	}

	/**
	 * Adds the given tile as a neighbour to the given direction. Will replace
	 * any previous neighbour in that direction.
	 *
	 * NOTE: This is a one way connection and does not guarantee that the given
	 * tile has set this tile as a neighbour.
	 *
	 * @param direction The direction of the neighbour.
	 * @param tile The new neighbour to be added.
	 */
	public void addNeighbour(Direction direction, MapTile tile) { neighbours.put(direction, tile); }

	/**
	 * Gets the neighbour in the given direction.
	 *
	 * @param direction The direction to get the neighbour from.
	 * @return Returns the MapTile neighbour in the given direction. If no
	 * neighbour exists in that direction the null is returned.
	 */
	public MapTile getNeighbour(Direction direction) { return neighbours.get(direction); }
	public MapTile[] getNeighbours()
	{
		MapTile[] tiles = new MapTile[neighbours.size()];
		Iterator iterator = neighbours.values().iterator();
		for(int i = 0; i < tiles.length; i++) tiles[i] = (MapTile) iterator.next();
		return tiles;
	}
	
	public MapPosition getPosition() { return position; }

	public MapTile north() { return neighbours.get(Direction.NORTH); }
	public MapTile east() { return neighbours.get(Direction.EAST); }
	public MapTile south() { return neighbours.get(Direction.SOUTH); }
	public MapTile west() { return neighbours.get(Direction.WEST); }
	
	public void setTool(Tool tool) { this.tool = tool; }
	public Tool getTool() { return tool; }
	
	public void setObstacle(Obstacle obstacle) { this.obstacle = obstacle; }
	public Obstacle getObstacle() { return this.obstacle; }

	public String toString()
	{
		String string = "Map Tile [" + position.getX() + "," + position.getY() + "]";
		string += " Obstacle: " + obstacle;
		string += ", Tool: " + tool;
		return string;
	}

	/**
	 * Checks whether it would be possible to walk in this tile if the player
	 * had the right tool.
	 *
	 * @return TRUE if the player could potentially walk in this tile with the
	 * right tool (regardless of the player's current inventory).
	 */
	public boolean isWalkable()
	{
		return obstacle == Obstacle.NONE || obstacle == Obstacle.DOOR || obstacle == Obstacle.TREE;
	}

	public boolean isWater() { return obstacle == Obstacle.WATER; }
	public boolean isWall() { return obstacle == Obstacle.WALL; }
	public boolean isWilderness() { return obstacle == Obstacle.WILDERNESS; }
}

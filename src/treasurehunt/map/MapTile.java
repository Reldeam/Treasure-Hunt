package treasurehunt.map;

import treasurehunt.constant.Direction;
import treasurehunt.constant.Obstacle;
import treasurehunt.constant.Tool;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

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
			reward = tile.getTool().reward();
		}

		return reward;
	}

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

	public boolean zoneContainsTree()
	{
		LinkedList<MapTile> unexpanded = new LinkedList<>();
		HashSet<MapTile> expanded = new HashSet<MapTile>();

		unexpanded.add(this);
		expanded.add(this);

		MapTile tile;

		while((tile = unexpanded.poll()) != null) {
			for(MapTile neighbour : tile.getNeighbours()) {
				if(zone != neighbour.getZone()) continue;
				if(neighbour.getObstacle() == Obstacle.TREE) return true;
				if(expanded.contains(neighbour)) continue;
				expanded.add(neighbour);
				unexpanded.add(neighbour);
			}
		}

		return false;
	}

	public boolean connected(MapTile tile)
	{
		if(zone == -1) return false;
		return zone == tile.getZone();
	}

	public void addNeighbour(Direction direction, MapTile tile) { neighbours.put(direction, tile); }
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

	public boolean isWalkable()
	{
		return obstacle == Obstacle.NONE || obstacle == Obstacle.DOOR || obstacle == Obstacle.TREE;
	}

	public boolean isWater() { return obstacle == Obstacle.WATER; }
	public boolean isWall() { return obstacle == Obstacle.WALL; }
	public boolean isWilderness() { return obstacle == Obstacle.WILDERNESS; }
}

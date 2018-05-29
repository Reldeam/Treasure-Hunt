package treasurehunt.map;

import treasurehunt.constant.Action;
import treasurehunt.util.Matrix;
import treasurehunt.constant.Direction;
import treasurehunt.constant.Obstacle;
import treasurehunt.constant.Tool;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * The Map maintains a persistent model of the map that the agent is currently
 * playing in. As the player moves around the map the map generates based on
 * the agents view when the maps is updated using update().
 *
 * The Map takes into account the player's current direction when updating and
 * also maintains the current state of each tile depending on the player's actions.
 * The state of each tile is updated internally, therefore to ensure consistency
 * between the client and the server, the Map and the Player are prevented from
 * making any illegal moves that would place the client and server out of sync.
 */
public class Map
{
	public static final int VIEW_WIDTH = 5;
	public static final int VIEW_HEIGHT = 5;
	public static final int MAX_WIDTH = 81;
	public static final int MAX_HEIGHT = 81;

	private int minX = 0; private int minY = 0;
	private int maxX = 0; private int maxY = 0;
	private int zones = 32;

	private final HashMap<MapPosition, MapTile> tiles = new HashMap<>();
	private final HashMap<Tool, HashSet<MapPosition>> tools = new HashMap<>();
	private final HashMap<Obstacle, HashSet<MapPosition>> obstacles = new HashMap<>();

	private final Player player = new Player();

	public Map()
	{
		tiles.put(player.getTile().getPosition(), player.getTile());
		tools.put(Tool.GOLD, new HashSet<MapPosition>());
		tools.put(Tool.KEY, new HashSet<MapPosition>());
		tools.put(Tool.AXE, new HashSet<MapPosition>());
		tools.put(Tool.STONE, new HashSet<MapPosition>());
		obstacles.put(Obstacle.TREE, new HashSet<MapPosition>());
		obstacles.put(Obstacle.DOOR, new HashSet<MapPosition>());
	}

	public Player getPlayer() { return player; }

	/**
	 * Add a tile to a given position in the map. If a tile was already at that
	 * position the previous tile is overwritten. Neighbours are automatically
	 * updated when the tile is added.
	 *
	 * @param position Position to add the tile to.
	 * @param tile The tile to add to the map.
	 */
	public void addTile(MapPosition position, MapTile tile)
	{
		if(position.getX() < minX) minX = position.getX();
		else if(position.getX() > maxX) maxX = position.getX();
		if(position.getY() < minY) minY = position.getY();
		else if(position.getY() > maxY) maxY = position.getY();

		if(tile.getTool() != Tool.NONE) tools.get(tile.getTool()).add(position);
		else if(tile.getObstacle() == Obstacle.TREE || tile.getObstacle() == Obstacle.DOOR)
			obstacles.get(tile.getObstacle()).add(position);

		MapPosition north = new MapPosition(position.getX(), position.getY()-1);
		MapPosition east = new MapPosition(position.getX()+1, position.getY());
		MapPosition south = new MapPosition(position.getX(), position.getY()+1);
		MapPosition west = new MapPosition(position.getX()-1, position.getY());

		if(tiles.containsKey(north)) {
			tile.addNeighbour(Direction.NORTH, tiles.get(north));
			tiles.get(north).addNeighbour(Direction.SOUTH, tile);
		}

		if(tiles.containsKey(east)) {
			tile.addNeighbour(Direction.EAST, tiles.get(east));
			tiles.get(east).addNeighbour(Direction.WEST, tile);
		}

		if(tiles.containsKey(south)) {
			tile.addNeighbour(Direction.SOUTH, tiles.get(south));
			tiles.get(south).addNeighbour(Direction.NORTH, tile);
		}

		if(tiles.containsKey(west)) {
			tile.addNeighbour(Direction.WEST, tiles.get(west));
			tiles.get(west).addNeighbour(Direction.EAST, tile);
		}

		for(MapTile neighbour : tile.getNeighbours()) {
			if((tile.isWalkable() && neighbour.isWalkable())
					|| (tile.isWater() && neighbour.isWater())
					|| (tile.isWall() && neighbour.isWall())
					|| (tile.isWilderness() && neighbour.isWilderness())) {
				tile.setZone(neighbour.getZone());
				break;
			}
		}

		if(tile.getZone() == -1) tile.setZone(zones++);

		// Finds and merges and zone discrepancies between like neighbours.
		for(MapTile neighbour : tile.getNeighbours()) {
			if(((tile.isWalkable() && neighbour.isWalkable())
					|| (tile.isWater() && neighbour.isWater())
					|| (tile.isWall() && neighbour.isWall())
					|| (tile.isWilderness() && neighbour.isWilderness()))
					&& (tile.getZone() != neighbour.getZone())) {
				mergeZones(tile, neighbour);
			}
		}

		tiles.put(position, tile);
	}

	/**
	 * Combines zones that were discovered independantly but have been later found
	 * to be the same zone.
	 */
	private void mergeZones(MapTile zone1, MapTile zone2)
	{
		HashSet<MapTile> expanded = new HashSet<>();
		LinkedList<MapTile> unexpanded = new LinkedList<>();

		expanded.add(zone1);
		expanded.add(zone2);
		unexpanded.add(zone2);

		MapTile tile;

		while((tile = unexpanded.poll()) != null) {
			tile.setZone(zone1.getZone());
			for(MapTile neighbour : tile.getNeighbours()) {
				if(!expanded.contains(neighbour)) {
					if(((neighbour.isWalkable() && zone1.isWalkable())
							|| (neighbour.isWater() && zone1.isWater())
							|| (neighbour.isWall() && zone1.isWall())
							|| (tile.isWilderness() && neighbour.isWilderness()))
							&& (neighbour.getZone() != zone1.getZone())) {
						expanded.add(neighbour);
						unexpanded.add(neighbour);
					}
				}
			}
		}
	}

	/**
	 * Updates the map with the new view assuming the view is orientated in
	 * the direction that the player is currently facing and that the player
	 * is in the center of the view.
	 *
	 * @param view A 2D array of characters that represent the current
	 *             view of the map with respect to the player's direction they
	 *             are facing and their current position.
	 */
	public void update(char[][] view)
	{
		tools.get(Tool.GOLD).remove(player.getTile().getPosition());
		tools.get(Tool.KEY).remove(player.getTile().getPosition());
		tools.get(Tool.AXE).remove(player.getTile().getPosition());
		tools.get(Tool.STONE).remove(player.getTile().getPosition());

		switch(player.getDirection()) {
			case NORTH:
				view = Matrix.transpose(view);
				break;
			case EAST:
				view = Matrix.horizontalFlip(view);
				break;
			case SOUTH:
				view = Matrix.transpose(view);
				view = Matrix.horizontalFlip(view);
				view = Matrix.verticalFlip(view);
				break;
			case WEST:
				view = Matrix.verticalFlip(view);
				break;
		}

		updateTile(player.getTile(), view, view.length/2, view[0].length/2);

		// Finds and merges and zone discrepancies between like neighbours.
		for(MapTile neighbour : player.getTile().getNeighbours()) {
			if(((player.getTile().isWalkable() && neighbour.isWalkable())
					|| (player.getTile().isWater() && neighbour.isWater())
					|| (player.getTile().isWall() && neighbour.isWall())
					|| (player.getTile().isWilderness() && neighbour.isWilderness()))
					&& (player.getTile().getZone() != neighbour.getZone())) {
				mergeZones(player.getTile(), neighbour);
			}
		}
	}
	public boolean update(Action action)
	{
		switch(action) {
			case FORWARD:
				if(!player.forward()) return false;
				break;
			case LEFT:
				player.turnLeft();
				break;
			case RIGHT:
				getPlayer().turnRight();
				break;
			case UNLOCK:
				player.useTool(Tool.KEY);
				break;
			case CHOP:
				player.useTool(Tool.AXE);
		}

		return true;
	}

	/**
	 * Helper function for update(). Recursively updates out from the player's
	 * position adding any new tiles that have not been viewed before. Adding new
	 * tiles is the only thing that needs to be done as the current state of each
	 * tile is maintained on the client side once the tile has been added to the
	 * map.
	 * @param tile Current tile to have it's neighbours inspected for possible
	 *             updates.
	 * @param updated A set of all the tiles that have already been updated with
	 *                the new view.
	 */
	private void updateTile(MapTile tile, HashSet<MapTile> updated, char[][] view, int x, int y)
	{
		updated.add(tile);
		MapPosition position = tile.getPosition();

		if(tile.getObstacle() == Obstacle.NONE) {
			if(obstacles.get(Obstacle.TREE).contains(position))
				obstacles.get(Obstacle.TREE).remove(position);
			else if(obstacles.get(Obstacle.DOOR).contains(position))
				obstacles.get(Obstacle.DOOR).remove(position);
		}

		if(y-1 >= 0 && !updated.contains(tile.north())) {
			if(tile.north() == null) {
				MapPosition northPosition = new MapPosition(position.getX(), position.getY()-1);
				MapTile northTile = new MapTile(northPosition, view[x][y-1]);
				addTile(northPosition, northTile);
			}
			updateTile(tile.north(), updated, view, x, y-1);
		}

		if(x+1 < view.length && !updated.contains(tile.east())) {
			if(tile.east() == null) {
				MapPosition eastPosition = new MapPosition(position.getX()+1, position.getY());
				MapTile eastTile = new MapTile(eastPosition, view[x+1][y]);
				addTile(eastPosition, eastTile);
			}

			updateTile(tile.east(), updated, view, x+1, y);
		}

		if(y+1 < view[x].length && !updated.contains(tile.south())) {
			if(tile.south() == null) {
				MapPosition southPosition = new MapPosition(position.getX(), position.getY()+1);
				MapTile southTile = new MapTile(southPosition, view[x][y+1]);
				addTile(southPosition, southTile);
			}

			updateTile(tile.south(), updated, view, x, y+1);
		}

		if(x-1 >= 0 && !updated.contains(tile.west())) {
			if(tile.west() == null) {
				MapPosition westPosition = new MapPosition(position.getX()-1, position.getY());
				MapTile westTile = new MapTile(westPosition, view[x-1][y]);
				addTile(westPosition, westTile);
			}
			updateTile(tile.west(), updated, view, x-1, y);
		}
	}
	private void updateTile(MapTile tile, char[][] view, int x, int y)
	{
		HashSet<MapTile> updated = new HashSet<>();
		updateTile(tile, updated, view, x, y);
	}

	/**
	 * Get's the Tile at a given position.
	 *
	 * @param position The MapPosition to get the Tile from.
	 * @return The Tile that corresponds to the given position or null if a Tile
	 * at the given position does not exist.
	 */
	public MapTile getTile(MapPosition position)
	{
		return tiles.get(position);
	}

	/**
	 public MapTile getTile(MapPosition position) { return tiles.get(position); }

	 /**
	 * Returns all tiles that currently have the given tool on them.
	 *
	 * @param tool treasurehunt.constant.Tool that you are searching for.
	 * @return An array of tiles that have the given tool currently on them.
	 */
	public MapTile[] getTiles(Tool tool)
	{
		MapPosition[] positions = getPositions(tool);
		MapTile[] tiles = new MapTile[positions.length];
		for(int i = 0; i < positions.length; i++) tiles[i] = getTile(positions[i]);
		return tiles;
	}

	/**
	 * Returns all tiles that currently have the given obstacle on them.
	 * Only TREE and DOOR obstacles can be searched for. All other obstacles will
	 * automatically return null.
	 *
	 * @param obstacle The obstacle to search for (either TREE or DOOR).
	 * @return An array of all the tiles that currently have the given obstacle
	 * on them.
	 */
	public MapTile[] getTiles(Obstacle obstacle)
	{
		MapPosition[] positions = getPositions(obstacle);
		if(positions == null) return null;
		MapTile[] tiles = new MapTile[positions.length];
		for(int i = 0; i < positions.length; i++) tiles[i] = getTile(positions[i]);
		return tiles;
	}

	/**
	 * Get the MapPosition(s) of a certain tool. Returns all MapPosition(s) that
	 * have the given tool on the regardless of player position or ability to get
	 * to said locations.
	 *
	 * @param tool The type of tool to get the MapPositions for.
	 * @return An array of MapPosition which have the tool on the respective
	 * MapPosition's Tile.
	 */
	public MapPosition[] getPositions(Tool tool)
	{
		MapPosition[] positions = new MapPosition[tools.get(tool).size()];
		Iterator iterator =  tools.get(tool).iterator();
		for(int i = 0; i < positions.length; i++) positions[i] = (MapPosition) iterator.next();
		return positions;
	}

	/**
	 * Get the MapPosition(s) of a certain Obstacle. Returns all MapPosition(s) that
	 * have the given obstacle on the regardless of player position or ability to get
	 * to said locations.
	 *
	 * NOTE: The only obstacles that are searchable are DOOR and TREE.
	 *
	 * @param obstacle The type of obstacle to get the MapPositions for.
	 * @return An array of MapPosition which have the obstacle on the respective
	 * MapPosition's Tile.
	 */
	public MapPosition[] getPositions(Obstacle obstacle)
	{
		MapPosition[] positions = new MapPosition[obstacles.get(obstacle).size()];
		Iterator iterator =  obstacles.get(obstacle).iterator();
		for(int i = 0; i < positions.length; i++) positions[i] = (MapPosition) iterator.next();
		return positions;
	}

	public int minX() { return minX; }
	public int minY() { return minY; }
	public int maxX() { return maxX; }
	public int maxY() { return maxY; }

	/**
	 * Creates a String representation of the Map.
	 *
	 * @return A String representation of the map.
	 */
	public String toString()
	{
		StringBuilder string = new StringBuilder();
		int width = maxX - minX + 1;

		MapTile tile;

		string.append("+");
		for(int n = 0; n < width; n++) string.append("-");
		string.append("+\n");

		for(int j = minY; j <= maxY; j++) {
			string.append("|");
			for(int i = minX; i <= maxX; i++) {
				tile = tiles.get(new MapPosition(i, j));
				if(tile == null) string.append("?");
				else if(tile.getTool() != Tool.NONE) string.append(Tool.toKey(tile.getTool()));
				else if(tile.getPosition() == player.getTile().getPosition()) {
					switch(player.getDirection()) {
						case NORTH: string.append("^"); break;
						case EAST: string.append(">"); break;
						case SOUTH: string.append("˅"); break;
						case WEST: string.append("<"); break;
					}
				}
				else string.append(Obstacle.toKey(tile.getObstacle()));
				// If you want to see the zones
				//else string.append((char)(tile.getZone()));
			}
			string.append("|\n");
		}

		string.append("+");
		for(int n = 0; n < width; n++) string.append("-");
		string.append("+\n");

		return string.toString();
	}

	/**
	 * Prints the String representation of the map.
	 */
	public void print() { System.out.print(toString()); }
}

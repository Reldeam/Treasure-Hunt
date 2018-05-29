package treasurehunt.map;

import treasurehunt.constant.Direction;
import treasurehunt.constant.Obstacle;
import treasurehunt.constant.Tool;
import treasurehunt.util.Debug;

import java.util.HashMap;

/**
 * The Player acts as the representation of the Agent inside of the game. The
 * agent is able to control the player via the Actions of the game, i.e FOWARD,
 * LEFT, RIGHT, CHOP, and UNLOCK.
 *
 * The Player cannot make an illegal move. An illegal moving being one that is
 * impossible or would kill the player, ending the game.
 */
public class Player 
{
	private Direction direction = Direction.NORTH;
	private MapTile tile = new MapTile(new MapPosition(0, 0));
	private final HashMap<Tool, Integer> inventory = new HashMap<>();
	private boolean sailing = false;

	public Player()
	{
		tile.explored();
	}

	public boolean isSailing() { return sailing; }

	/**
	 * Adds a tool to the player's inventory.
	 *
	 * @param tool Tool to add the the player's inventory.
	 */
	public void addTool(Tool tool)
	{
		Debug.msg("Attempting to pick up tool: " + tool + "...");
		int current = inventory.containsKey(tool) ? inventory.get(tool) : 0;
		if(tool.limit() == -1 || current < tool.limit()) {
			current++;

			Debug.msg("You now have " + current + " " + tool + "(s).");
		}
		else Debug.wrn("You can't carry any more " + tool + "(s), so you throw it away.");

		inventory.put(tool, current);
	}

	/**
	 * Attempts to use the given tool. If the player does not have the tool or
	 * the tool will be used in the wrong way or to no effect then the tool will
	 * not be used and the method will return false. If the tool is used successfully
	 * the the method will return true.
	 *
	 * @param tool Tool to try and use.
	 * @return Whether the tool was successfully used or not.
	 */
	public boolean useTool(Tool tool)
	{
		Debug.msg("Attempting to use tool: " + tool + "...");
		int current = inventory.containsKey(tool) ? inventory.get(tool) : 0;

		if(current == 0) {
			Debug.err("You do not have tool: " + tool + ".");
			return false;
		}
		
		MapTile target = null;
		
		switch(direction) {
		case NORTH:
			target = tile.north();
			break;
		case EAST:
			target = tile.east();
			break;
		case SOUTH:
			target = tile.south();
			break;
		case WEST:
			target = tile.west();
		}
		
		if(target == null) {
			Debug.err("You are not facing a tile.");
			return false;
		}
		
		switch(tool) {
		case AXE:
			if(target.getObstacle() != Obstacle.TREE) {
				Debug.err("AXE can only be used on a TREE.");
				return false;
			}
			target.setObstacle(Obstacle.NONE);
			addTool(Tool.RAFT);
			break;
		case KEY:
			if(target.getObstacle() != Obstacle.DOOR) {
				Debug.error("KEY can only be used on a DOOR.");
				return false;
			}
			target.setObstacle(Obstacle.NONE);
			break;
		case STONE:
			if(target.getObstacle() != Obstacle.WATER) {
				Debug.error("STONE can only be put in WATER.");
				return false;
			}
			target.setObstacle(Obstacle.NONE);
			break;
		case RAFT:
			if(!sailing) {
				Debug.error("You are currently not sailing.");
				return false;
			}
			if(target.getObstacle() == Obstacle.WATER) return false;
			sailing = false;
			break;
		default:
			return false;
		}
		
		Debug.msg("Used tool: " + tool + ".");
		if(tool.isConsumable()) {
			inventory.put(tool, current - 1);
			Debug.msg("You now have " + inventory.get(tool) + " " + tool + "(s).");
		}
		return true;
	}

	/**
	 * Whether or not the player has at least one of the given tool.
	 *
	 * @param tool The tool to check if the player has or not.
	 * @return TRUE if the player has at least one of the given item, FALSE otherwise.
	 */
	public boolean hasTool(Tool tool)
	{
		if(inventory.get(tool) == null) return false;
		int current = inventory.get(tool);
		return current > 0;
	}

	/**
	 * The number of the given tool that the player currently has.
	 *
	 * @param tool The tool to get the number of.
	 * @return The number of the given tool the the player currently has in their
	 * inventory.
	 */
	public int numOfTool(Tool tool)
	{
		if(inventory.get(tool) == null) return 0;
		return inventory.get(tool);
	}

	/**
	 * Manually teleport the player to a given tile.
	 * WARNING: This may desync the map or have other unforeseen ramifications.
	 *
	 * @param tile The tile to teleport the player to.
	 */
	public void setTile(MapTile tile) { this.tile = tile; }

	/**
	 * Gets the tile the player is currently standing on.
	 *
	 * @return The tile the player is currently standing on.
	 */
	public MapTile getTile() { return tile; }

	/**
	 * Gets the direction the player is currently facing.
	 *
	 * @return The Direction the player is currently facing.
	 */
	public Direction getDirection() { return direction; }

	/**
	 * Turns the player left.
	 *
	 * @return Turns the player 90 degrees to the left, e.g. If they were facing
	 * NORTH then after turning left they would be facing WEST.
	 */
	public Direction turnLeft()
	{	
		switch(direction) {
		case NORTH:
			direction = Direction.WEST;
			break;
		case EAST:
			direction = Direction.NORTH;
			break;
		case SOUTH:
			direction = Direction.EAST;
			break;
		case WEST:
			direction = Direction.SOUTH;
			break;
		}

		Debug.msg("You are now facing " + direction + ".");
		return direction;
	}

	/**
	 * Turns the player to the right.
	 *
	 * @return Turns the player 90 degrees to the right, e.g. If they were facing
	 * NORTH then after turning right they would be facing EAST.
	 */
	public Direction turnRight()
	{
		switch(direction) {
		case NORTH:
			direction = Direction.EAST;
			break;
		case EAST:
			direction = Direction.SOUTH;
			break;
		case SOUTH:
			direction = Direction.WEST;
			break;
		case WEST:
			direction = Direction.NORTH;
			break;
		}

		Debug.msg("You are now facing " + direction + ".");
		return direction;
	}

	/**
	 * Attempts to move the player forward. If the player is able to move forward
	 * then the player is moved forward and the mothod returns true. If the player
	 * can not move forward then the player does not move forward and the method
	 * returns false.
	 *
	 * @return Whether or not the player moved forward.
	 */
	public boolean forward()
	{
		MapTile nextTile = tile.getNeighbour(direction);
		if(nextTile == null) return false;
		
		switch(nextTile.getObstacle()) {
		case WATER:
			if(!sailing) {
				if(useTool(Tool.STONE)) break;
				if(!hasTool(Tool.RAFT)) {
					Debug.err("You need a STONE or RAFT to move into water.");
					return false;
				}
				sailing = true;
			}
			break;
		case TREE:
			Debug.err("You cannot walk into a TREE.");
			return false;
		case WALL:
			Debug.err("You cannot walk into a WALL.");
			return false;
		case DOOR:
			Debug.err("You cannot walk into a DOOR.");
			return false;
		case WILDERNESS:
			Debug.err("You cannot walk into the WILDERNESS.");
			return false;
		case NONE:
			if(sailing) {
				useTool(Tool.RAFT);
				sailing = false;
			}
		}
		
		// Pick up item if there is one.
		if(nextTile.getTool() != Tool.NONE) {
			addTool(nextTile.getTool());
			nextTile.setTool(Tool.NONE);
		}

		tile = nextTile;
		tile.explored();
		Debug.message("You moved forward to [" + tile.getPosition().getX() + ", " + tile.getPosition().getY() + "].");
		return true;
	}
}

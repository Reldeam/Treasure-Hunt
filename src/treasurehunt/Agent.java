package treasurehunt;

/*********************************************
 *  treasurehunt.Agent.java
 *  Sample treasurehunt.Agent for Text-Based Adventure Game
 *  COMP3411/9414/9814 Artificial Intelligence
 *  UNSW Session 1, 2018
*/

import treasurehunt.constant.Action;
import treasurehunt.constant.Behaviour;
import treasurehunt.constant.Obstacle;
import treasurehunt.constant.Tool;
import treasurehunt.exception.InvalidTaskException;
import treasurehunt.map.Map;
import treasurehunt.map.MapPosition;
import treasurehunt.map.MapTile;
import treasurehunt.search.MapSearch;
import treasurehunt.util.Debug;
import treasurehunt.util.Task;

import java.io.*;
import java.net.*;

public class Agent 
{
	private final Map map = new Map();
	private Task task = null;
	
	public char get_action(char view[][])
	{
		map.update(view);
		Debug.print(map.toString());

		return get_automatic_action();
		//return get_manual_action();
	}

	/**
	 * Will return the next action the agent will make based on AI.
	 *
	 * @return The next action in the form of a character, i.e. 'f', 'l', 'r',
	 * 'c', or 'u'.
	 */
	private char get_automatic_action()
	{
		if(task == null || task.isFinished()) getTask();
		if(task == null) return 0;

		Action action = task.poll();
		if(map.update(action)) return action.getChar();
		return 0;
	}

	private char get_manual_action()
	{
		int ch = 0;

		System.out.print("Enter Action(s): ");

		try {
			while (ch != -1) {
				// read character from keyboard
				ch = System.in.read();

				Action action = Action.getAction((char) ch);
				if(!map.update(action)) continue;

				switch (ch) { // if character is a valid action, return it
					case 'F':
					case 'L':
					case 'R':
					case 'C':
					case 'U':
					case 'f':
					case 'l':
					case 'r':
					case 'c':
					case 'u':
						return ((char) ch);
				}
			}
		} catch (IOException e) {
			System.out.println("IO error:" + e);
		}
		return 0;
	}

	/**
	 * Gets the next task that the AI agent will perform. The tasks that the
	 * agent can perform are based on the behaviours listed in Behaviour enum.
	 *
	 * This method is the main logic algorithm that decides which task to perform
	 * depending on the agent's current situation and world model.
	 */
	private void getTask()
	{
		task = null;

		if(!map.getPlayer().hasTool(Tool.GOLD)) {

			if(map.getPlayer().isSailing()) {
				Debug.msg("Player is currently sailing.");
				if((task = explore(false, true, true)) != null) return;
				if((task = dock(true)) != null) return;
				if((task = dock(false)) != null) return;
			}

			if((task = getTool(Tool.GOLD)) != null) return;
			if(!map.getPlayer().hasTool(Tool.KEY)) {
				if((task = getTool(Tool.KEY)) != null) return;
			}
			if(!map.getPlayer().hasTool(Tool.AXE)) {
				if((task = getTool(Tool.AXE)) != null) return;
			}

			if(map.getPlayer().hasTool(Tool.KEY) || map.getPlayer().hasTool(Tool.STONE)) {
				if((task = getTool(Tool.STONE)) != null) return;
				if((task = unlockDoor()) != null) return;
			}

			if(!map.getPlayer().hasTool(Tool.KEY)) {
				if((task = explore(true, true, true)) != null) return;
			}
			else if(map.getPlayer().hasTool(Tool.KEY)) {
				if ((task = explore(true, true, false)) != null) return;
			}

			if(map.getPlayer().hasTool(Tool.AXE)) {
				if(map.getPlayer().hasTool(Tool.KEY) && (task = explore(true, false, false)) != null) {
					return;
				}
				else if((task = explore(true, false, true)) != null) {
					return;
				}
			}

			if(!map.getPlayer().hasTool(Tool.RAFT) && (task = chopTree()) != null) return;
			if(map.getPlayer().numOfTool(Tool.STONE) > 0 && (task = placeStone()) != null) return;

			if(map.getPlayer().hasTool(Tool.RAFT) && (task = setSail()) != null) return;
			if(map.getPlayer().hasTool(Tool.AXE) &&  (task = chopTree()) != null) return;
			task = getTool(Tool.STONE);

		}
		else {
			if((task = goHome()) != null) return;
			if(map.getPlayer().isSailing() && (task = dock(true)) != null) return;
			if(!map.getPlayer().isSailing() && (task = chopTree()) != null) return;
			task = explore(false, false, false);

		}
	}

	/**
	 * Attempts to construct a Task based on the given choice of Behaviour as
	 * well as several contraints that will alter how the Behaviour is carried
	 * out.
	 *
	 * @param behaviour The behaviour to attempt to do.
	 * @param tool One of the constraints specifically related to the GET_TOOL
	 *             behaviour.
	 * @param avoidWater Whether to avoid water while carrying out this task.
	 *                   This constraint has no effect if the agent is already
	 *                   on the water.
	 * @param avoidTrees Whether to avoid cutting down trees while performing
	 *                   the behaviour.
	 * @param avoidStones Whether to avoid picking up stepping stones while
	 *                    performing the behaviour.
	 * @return
	 */
	private Task buildTask(Behaviour behaviour, Tool tool, boolean avoidWater, boolean avoidTrees, boolean avoidStones)
	{
		MapTile[] targets;

		switch(behaviour) {
			case EXPLORE:
				Debug.msg("Attempting to explore...");
				targets = MapSearch.findUnexploredTiles(map);
				break;
			case PLACE_STONE:
				Debug.msg("Attempting to place stone...");
				targets = MapSearch.findStoneTargets(map);
				break;
			case GET_TOOL:
				Debug.msg("Attempting to get tool, " + tool + "...");
				targets = map.getTiles(tool);
				break;
			case CHOP_TREE:
				Debug.msg("Attempting to chop down tree...");
				targets = map.getTiles(Obstacle.TREE);
				break;
			case UNLOCK_DOOR:
				Debug.msg("Attempting to unlock door...");
				targets = map.getTiles(Obstacle.DOOR);
				break;
			case SET_SAIL:
				Debug.msg("Attempting to set sail...");
				targets = MapSearch.findCoastalTiles(map);
				break;
			case DOCK:
				Debug.msg("Attempting to dock...");
				targets = MapSearch.findHabourTiles(map, true, avoidTrees);
				if(targets == null || targets.length == 0) targets = MapSearch.findHabourTiles(map);
				if(targets == null || targets.length == 0) targets = MapSearch.findHabourTiles(map, false, avoidTrees);
				break;
			case GO_HOME:
				Debug.msg("Attempting to go home...");
				if(map.getPlayer().isSailing()) targets = MapSearch.findHomeHabourTiles(map);
				else targets = new MapTile[] {map.getTile(new MapPosition(0, 0))};
				break;
			default: targets = null;
		}

		if(targets == null || targets.length == 0) {
			Debug.err("No valid targets found.");
			return null;
		}

		MapTile[] path = buildPath(targets, avoidWater, avoidTrees, avoidStones);
		if(path == null) return null;

		try {
			return new Task(path, map.getPlayer().getDirection());
		} catch (InvalidTaskException e) {
			Debug.err("Could not build a Task from the given path.");
			return null;
		}
	}
	private Task buildTask(Behaviour behaviour, boolean avoidWater, boolean avoidTrees, boolean avoidStones)
	{
		return buildTask(behaviour, Tool.NONE, avoidWater, avoidTrees, avoidStones);
	}
	private Task buildTask(Behaviour behaviour)
	{
		return buildTask(behaviour, false, false, false);
	}

	/**
	 * Attempts to construct a path from the player to one of the target's given
	 * in the list of targets. The path that will be returned will be the first
	 * valid path to be formed to a target while iterating through all of the
	 * targets.
	 *
	 * NOTE: Since the first valid path will be returned it is advised to give
	 * the targets in order of priority from highest to lowest.
	 *
	 * @param targets The target MapTile(s) to attempt to form a path to.
	 * @param avoidWater Whether to avoid water while forming the path.
	 * @param avoidTrees Whether to avoid cutting down trees while forming the
	 *                    path.
	 * @param avoidStones Whether to avoid picking up stones while forming the
	 *                    path.
	 * @return The first valid path to be formed or null if no valid path could
	 * be found.
	 */
	private MapTile[] buildPath(MapTile[] targets, boolean avoidWater, boolean avoidTrees, boolean avoidStones)
	{
		MapTile[] path = null;

		for(MapTile target : targets) {
			Debug.msg("Attempting to build path to: [" + target.getPosition().getX() + ", " + target.getPosition().getY() + "]...");
			path = MapSearch.getPath(map, map.getPlayer().getTile(), target, avoidWater, avoidTrees, avoidStones);
			if(path != null) {
				Debug.msg("Found path!");
				break;
			}
			Debug.wrn("Could not find path.");
		}

		return path;
	}

	/**
	 * Attempts to create an exploring task where the goal is to expand the map
	 * by stepping in tiles that will allow the player to see new tiles.
	 */
	private Task explore(boolean avoidWater, boolean avoidTrees, boolean avoidStones)
	{
		return buildTask(Behaviour.EXPLORE, avoidWater, avoidTrees, avoidStones);
	}

	/**
	 * Attempts to create a task where the aim to to place a stepping stone
	 * towards another island that will give the biggest reward to the player.
	 */
	private Task placeStone()
	{
		return buildTask(Behaviour.PLACE_STONE, true, true, false);
	}

	/**
	 * Attempts to create a task where the aim is to pick up a tool that is lying
	 * on the ground, such as treasure, keys, axes, or stones.
	 */
	private Task getTool(Tool tool)
	{
		if(map.getPlayer().hasTool(Tool.KEY) || tool == Tool.STONE)
			return buildTask(Behaviour.GET_TOOL, tool, true, false, false);
		return buildTask(Behaviour.GET_TOOL, tool, true, false, true);
	}

	/**
	 * Attempts to create a task where the aim is to chop down a tree.
	 */
	private Task chopTree()
	{
		if(map.getPlayer().hasTool(Tool.KEY)) return buildTask(Behaviour.CHOP_TREE, true, false, false);
		return buildTask(Behaviour.CHOP_TREE, true, false, true);
	}

	/**
	 * Attempts to create a task where the aim is to unlock a door.
	 */
	private Task unlockDoor()
	{
		if(!map.getPlayer().hasTool(Tool.KEY)) return null;
		if(map.getPlayer().isSailing()) return buildTask(Behaviour.UNLOCK_DOOR, false, true, true);
		return buildTask(Behaviour.UNLOCK_DOOR, true, true, true);
	}

	/**
	 * Attempts to create a task where the aim is to set sail on the high seas,
	 * choosing the body of water that looks the biggest and most unexplored.
	 */
	private Task setSail()
	{
		return buildTask(Behaviour.SET_SAIL, true, true, true);
	}

	/**
	 * Attempts to find a harbour to dock at which will provide the player with
	 * the most potential reward by taking into account the potential rewards on
	 * the new land as well as how unexplored it is or if it is possible to escape
	 * from the land once docked there.
	 */
	private Task dock(boolean avoidTree)
	{
		return buildTask(Behaviour.DOCK, false, avoidTree, false);
	}

	/**
	 * Attempts to go home.
	 */
	private Task goHome()
	{
		Task task = buildTask(Behaviour.GO_HOME, true, false, false);
		if(task != null) return task;

		if(!map.getPlayer().isSailing() && map.getPlayer().hasTool(Tool.RAFT)) return buildTask(Behaviour.SET_SAIL, true, false, false);
		if(map.getPlayer().isSailing()) return buildTask(Behaviour.GO_HOME);
		return null;
	}

	void print_view(char view[][]) 
	{
		int i, j;

		System.out.println("\n+-----+");
		for (i = 0; i < 5; i++) {
			System.out.print("|");
			for (j = 0; j < 5; j++) {
				if ((i == 2) && (j == 2)) {
					System.out.print('^');
				} else {
					System.out.print(view[i][j]);
				}
			}
			System.out.println("|");
		}
		System.out.println("+-----+");
	}

	public static void main(String[] args) 
	{
		InputStream in = null;
		OutputStream out = null;
		Socket socket = null;
		Agent agent = new Agent();
		char view[][] = new char[5][5];
		char action = 'F';
		int port;
		int ch;
		int i, j;

		if (args.length < 2) {
			System.out.println("Usage: java treasurehunt.Agent -p <port>\n");
			System.exit(-1);
		}

		port = Integer.parseInt(args[1]);

		// -debug parameter to show/hide debugging messages.
		if(args.length == 4) {
			switch(args[3]) {
				case "t" :
				case "T" :
				case "true" :
				case "True" :
				case "TRUE" :
				case "on" :
				case "ON" : Debug.on();
			}
		}

		// open socket to Game Engine
		try {
			socket = new Socket("localhost", port);
			in = socket.getInputStream();
			out = socket.getOutputStream();
		} catch (IOException e) {
			System.out.println("Could not bind to port: " + port);
			System.exit(-1);
		}

		// scan 5-by-5 wintow around current location
		try { 
			while (true) {
				for (i = 0; i < 5; i++) {
					for (j = 0; j < 5; j++) {
						if (!((i == 2) && (j == 2))) {
							ch = in.read();
							if (ch == -1) System.exit(-1);
							view[i][j] = (char) ch;
						}
					}
				}
				//agent.print_view(view); // COMMENT THIS OUT BEFORE SUBMISSION
				action = agent.get_action(view);
				out.write(action);
			}
		} catch (IOException e) {
			System.out.println("Lost connection to port: " + port);
			System.exit(-1);
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
			}
		}
	}
}

package treasurehunt.constant;

/**
 * The types of obstacles that can be encountered on the Map. The rules for each
 * obstacle can be found on the assignment spec expect for WILDERNESS which is
 * the name given to the region outside of the map.
 */
public enum Obstacle
{
	TREE,		// Unwalkable, can be chopped down with an axe to make a raft.
	DOOR,		// Unwalkable, can be opened with a key.
	WATER,		// Unwalkable, can be removed with a stone or sailed on with a raft.
	WALL,		// Unwalkable.
	WILDERNESS,	// Unwalkable.
	NONE;		// Walkable.

	/**
	 * Gets the Obstacle associated with the given character. The server uses
	 * characters to represent Obstacles and uses these characters to communicate
	 * to the client.
	 *
	 * @param c The character that is to be checked if it has an associated
	 *          Obstacle.
	 * @return The Obstacle associated with the given character. If no Obstacle
	 * is associated with the given character than the NONE Obstacle is returned.
	 */
	public static Obstacle fromKey(char c)
	{
		switch(c) {
			case 'T' : return TREE;
			case '-' : return DOOR;
			case '~' : return WATER;
			case '*' : return WALL;
			case '.' : return WILDERNESS;
			default : return NONE;
		}
	}

	/**
	 * Gets the Obstacle associated with the given character. The server uses
	 * characters to represent Obstacles and uses these characters to communicate
	 * to the client.
	 *
	 * @param obstacle The obstacle that is to be checked if it has an associated
	 *          character.
	 * @return The character associated with the given Obstacle. If no character
	 * is associated with the given Obstacle than the character associated with
	 * the NONE Obstacle is returned.
	 */
	public static char toKey(Obstacle obstacle)
	{
		switch(obstacle) {
			case TREE : return 'T';
			case DOOR : return '-';
			case WATER : return '~';
			case WALL : return '*';
			case WILDERNESS : return '.';
			default : return ' ';
		}
	}
}

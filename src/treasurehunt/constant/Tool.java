package treasurehunt.constant;

/**
 * The tools that can be found on the Map. Each tool has certain rules such as
 * carry limit and whether or not the tool is consumed on use.
 *
 * There is also an abitrary reward associated with each tool that can help
 * in prioritising when searching for tools to pick up.
 */
public enum Tool
{
	AXE(1, false, 1000),	// Chops down trees.
	KEY(1, false, 1000),	// Opens doors.
	STONE(-1, true, 100),	// Removes one water tile.
	RAFT(1, true, 0),		// Sails on a single body of water indefinitely.
	GOLD(1, true, 10000),	// Must be acquired to to win game (see rules in assignment spec).
	NONE(0, false, 0);

	private final int limit;
	private final boolean consumable;
	private final int reward;
	
	private Tool(int limit, boolean consumable, int reward)
	{
		this.limit = limit;
		this.consumable = consumable;
		this.reward = reward;
	}
	
	public int limit() { return this.limit; }
	public boolean isConsumable() { return this.consumable; }
	public int reward() { return this.reward; }

	/**
	 * Gets the Tool associated with the given character. The server uses characters
	 * that represent tools to communicate with the client and visa versa.
	 *
	 * @param c The character to get the associated Tool of.
	 * @return The Tool that is associated with the character or NONE if no
	 * association is found.
	 */
	public static Tool fromKey(char c)
	{
		switch(c) {
			case 'a' : return AXE;
			case 'k' : return KEY;
			case 'o' : return STONE;
			case '$' : return GOLD;
			default : return NONE;
		}
	}

	/**
	 * Gets the character associated with the given Tool. The server uses characters
	 * that represent tools to communicate with the client and visa versa.
	 *
	 * @param tool The Tool to get the associated character of.
	 * @return The character that is associated with the Tool or ' ' if no
	 * association can be found.
	 */
	public static char toKey(Tool tool)
	{
		switch(tool) {
			case AXE : return 'a';
			case KEY : return 'k';
			case STONE : return 'o';
			case GOLD : return '$';
			default : return ' ';
		}
	}
}

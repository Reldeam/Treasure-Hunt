package treasurehunt.constant;

/**
 * The possible actions that the agent can take in the game. Each action has
 * a corresponding character that the client and server use to communicate with.
 */
public enum Action
{
	LEFT('l'), 
	RIGHT('r'), 
	FORWARD('f'), 
	CHOP('c'), 
	UNLOCK('u'),
	UNKNOWN('?');
	
	private final char key;
	
	private Action(char key)
	{
		this.key = key;
	}
	
	public char getChar() { return key; }
	
	public static Action getAction(char key)
	{
		switch(key) {
		case 'F':
		case 'f':
			return Action.FORWARD;
		case 'L':
		case 'l':
			return Action.LEFT;
		case 'R':
		case 'r':
			return Action.RIGHT;
		case 'C':
		case 'c':
			return Action.CHOP;
		case 'U':
		case 'u':
			return Action.UNLOCK;
		default:
			return Action.UNKNOWN;
		}
	}
}

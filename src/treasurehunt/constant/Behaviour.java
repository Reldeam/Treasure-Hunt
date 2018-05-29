package treasurehunt.constant;

/**
 * The possible behaviours of the agent. These behaviours mirror all the possible
 * types of tasks that the agent completes.
 */
public enum Behaviour
{
    EXPLORE,        // Try and see more of the map.
    GET_TOOL,       // Try and pick up a tool.
    CHOP_TREE,      // Try and chop down a nearby tree.
    UNLOCK_DOOR,    // Try and unlock a door.
    SET_SAIL,       // Try to get on the water with a raft.
    PLACE_STONE,    // Try and work out the best way to place stepping stones.
    DOCK,           // Try to find the best place to dock when sailing.
    GO_HOME         // Try and get home when the player has the gold.
}

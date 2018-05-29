package treasurehunt.util;

import treasurehunt.constant.Action;
import treasurehunt.constant.Direction;
import treasurehunt.constant.Obstacle;
import treasurehunt.exception.InvalidTaskException;
import treasurehunt.map.Map;
import treasurehunt.map.MapPosition;
import treasurehunt.map.MapTile;

import java.util.EmptyStackException;
import java.util.LinkedList;

/**
 * A Task takes an array of MapTile[] (a path) and attempts to construct a
 * LinkedList of Action that the agent can perform in order to move through the
 * given path.
 *
 * For the Task to construct a valid path the given path must be in order with
 * each sequential tile being adjacent to the previous tile. If this condition is
 * not met then Task will fail to construct a path and will throw an InvalidTaskException.
 */
public class Task extends LinkedList<Action>
{
    public Task(MapTile[] tiles, Direction startDirection) throws InvalidTaskException
    {
        constructTask(tiles, startDirection);
    }

    public Task(Map map, MapPosition[] positions) throws InvalidTaskException
    {
        MapTile[] tiles = new MapTile[positions.length];

        for(int i = 0; i < positions.length; i++) {
            tiles[i] = map.getTile(positions[i]);
        }

        constructTask(tiles, map.getPlayer().getDirection());
    }
    public Task() {}

    private void constructTask(MapTile[] tiles, Direction startDirection) throws InvalidTaskException
    {
        MapTile currentTile, nextTile;
        Direction currentDirection = startDirection;

        for(int i = 0; i < tiles.length - 1; i++) {
            currentTile = tiles[i];
            nextTile = tiles[i+1];

            if(currentTile.north() == nextTile) {
                switch(currentDirection) {
                    case EAST : add(Action.LEFT); break;
                    case SOUTH : add(Action.LEFT); add(Action.LEFT); break;
                    case WEST : add(Action.RIGHT); break;
                }
                currentDirection = Direction.NORTH;
            }
            else if(currentTile.east() == nextTile) {
                switch(currentDirection) {
                    case NORTH : add(Action.RIGHT); break;
                    case SOUTH : add(Action.LEFT); break;
                    case WEST : add(Action.LEFT); add(Action.LEFT); break;
                }
                currentDirection = Direction.EAST;
            }
            else if(currentTile.south() == nextTile) {
                switch(currentDirection) {
                    case NORTH : add(Action.LEFT); add(Action.LEFT); break;
                    case EAST : add(Action.RIGHT); break;
                    case WEST : add(Action.LEFT); break;
                }
                currentDirection = Direction.SOUTH;
            }
            else if(currentTile.west() == nextTile) {
                switch (currentDirection) {
                    case NORTH : add(Action.LEFT); break;
                    case EAST : add(Action.LEFT); add(Action.LEFT); break;
                    case SOUTH : add(Action.RIGHT); break;
                }
                currentDirection = Direction.WEST;
            }
            else throw new InvalidTaskException();

            if(nextTile.getObstacle() == Obstacle.TREE) add(Action.CHOP);
            if(nextTile.getObstacle() == Obstacle.DOOR) add(Action.UNLOCK);

            add(Action.FORWARD);
        }
    }

    public char nextChar() throws EmptyStackException
    {
        Action action = pop();
        return action.getChar();
    }

    public boolean isFinished() { return isEmpty(); }
}

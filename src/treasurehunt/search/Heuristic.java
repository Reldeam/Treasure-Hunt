package treasurehunt.search;

import treasurehunt.constant.Direction;
import treasurehunt.map.MapPosition;
import treasurehunt.map.MapTile;

/**
 * A collection of search heuristics for use with search algorithms that require
 * heuristics.
 */
public class Heuristic
{
    /**
     * The distance heuristic attempts to find the minimum number of moves
     * (including LEFT and RIGHT) that would be required to go from the start
     * MapTile to the finish MapTile.
     *
     * @param start The starting position.
     * @param finish The goal position.
     * @param startDirection The direction the player is facing on the start
     *                       position.
     * @return The minimum number of move to get the the target destination.
     */
    public static int distance(MapPosition start, MapPosition finish, Direction startDirection)
    {
        int xDis = finish.getX() - start.getX();
        int yDis = finish.getY() - start.getY();

        int turns = 0;

        switch(startDirection) {
            case NORTH:
                if(xDis != 0) turns++;
                if(yDis > 0) turns += 2;
                break;
            case SOUTH:
                if(xDis != 0) turns++;
                if(yDis < 0) turns += 2;
                break;
            case EAST:
                if(yDis != 0) turns++;
                if(xDis < 0) turns += 2;
                break;
            case WEST:
                if(yDis != 0) turns++;
                if(xDis > 0) turns += 2;
                break;
        }

        if(xDis != 0 && yDis != 0) turns++;

        return Math.abs(xDis) + Math.abs(yDis) + turns;
    }
    public static int distance(MapTile start, MapTile finish, Direction startDirection)
    {
        return distance(start.getPosition(), finish.getPosition(), startDirection);
    }
}

package karataiev.dmytro.connectfour.players;
import java.util.Random;

import karataiev.dmytro.connectfour.Connect4Game;

/**
 * Randomly moving player. No system or algorithm at all.
 */
public class RandomAgent extends Agent
{

    /**
     * Constructs a new agent, giving it the game and telling it whether it is Red or Yellow.
     *
     * @param game The game the agent will be playing.
     * @param iAmRed True if the agent is Red, False if the agent is Yellow.
     */
    public RandomAgent(Connect4Game game, boolean iAmRed)
    {
        super(game, iAmRed);
        r = new Random();
    }

    /**
     * Actual random move if it is allowed
     */
    public void move()
    {
		int rand = randomMove();
		moveOnColumn(rand);
	}

    /**
     * Returns a random valid move. If your agent doesn't know what to do, making a random move
     * can allow the game to go on anyway.
     *
     * @return a random valid move.
     */
    public int randomMove()
    {
        int i = r.nextInt(myGame.getColumnCount());
        while (getLowestEmptyIndex(myGame.getColumn(i)) == -1)
        {
            i = r.nextInt(myGame.getColumnCount());
        }
        return i;
    }
    
}

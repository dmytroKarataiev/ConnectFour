package karataiev.dmytro.connectfour.players;
import java.util.Random;

import karataiev.dmytro.connectfour.Connect4Game;

/**
 *  user controlled class of Agent
 *
 */
public class PlayerAgent extends Agent
{
    Random r;

    /**
     * Constructs a new agent, giving it the game and telling it whether it is Red or Yellow.
     *
     * @param game The game the agent will be playing.
     * @param iAmRed True if the agent is Red, False if the agent is Yellow.
     */
    public PlayerAgent(Connect4Game game, boolean iAmRed)
    {
        super(game, iAmRed);
        r = new Random();
    }

    /**
     * Move by player
     *
     * @param move column to place the circle
     */
    public void playerMove(int move)
    {
        moveOnColumn(move);
    }


    public void move()
    {

    };

    /**
     * Returns the name of this agent.
     *
     * @return the agent's name
     */
    public String getName()
    {
        return "Player";
    }



}

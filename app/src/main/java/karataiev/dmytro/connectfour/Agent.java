package karataiev.dmytro.connectfour;

import java.util.Random;

/**
 * The abstract class for an Agent that plays Connect 4.
 * 
 * All Agents must have three things: a constructor that initializes the agent with a game
 * and whether the agent is the red player, a name, and the ability to move.
 */
public abstract class Agent
{
    // A protected variable means that only the current Class as well as Classes that inherit from the superclass
    // will have access to the variable.
    protected Connect4Game myGame;
    protected boolean iAmRed;
    protected Random r;

    /**
     * Constructs a new agent.*
     *  super(game, iAmRed);
     *
     * @param game the game for the agent to play.
     * @param iAmRed whether the agent is the red player.
     */
    public Agent(Connect4Game game, boolean iAmRed)
    {
        this.myGame = game;
        this.iAmRed = iAmRed;
    }

    /**
     * A name for the agent.
     *
     * @return the agent's name.
     */
    public abstract String getName();

    /**
     * The way the agent's name is displayed in the game, with its color.
     *
     * @return the agent's name to display in the game.
     */
    public String toString()
    {
        if (iAmRed)
        {
            return getName() + " (Red)";
        }
        else
        {
            return getName() + " (Yellow)";
        }
    }

    /**
     * Drops a token into a particular column so that it will fall to the bottom of the column.
     * If the column is already full, nothing will change.
     *
     * @param columnNumber The column into which to drop the token.
     */
    public void moveOnColumn(int columnNumber)
    {
        int lowestEmptySlotIndex = getLowestEmptyIndex(myGame.getColumn(columnNumber));   // Find the top empty slot in the column
        // If the column is full, lowestEmptySlot will be -1
        if (lowestEmptySlotIndex > -1)  // if the column is not full
        {
            Connect4Slot lowestEmptySlot = myGame.getColumn(columnNumber).getSlot(lowestEmptySlotIndex);  // get the slot in this column at this index
            if (iAmRed) // If the current agent is the Red player...
            {
                lowestEmptySlot.addRed(); // Place a red token into the empty slot
            }
            else // If the current agent is the Yellow player (not the Red player)...
            {
                lowestEmptySlot.addYellow(); // Place a yellow token into the empty slot
            }
        }
    }

    /**
     * Returns the index of the top empty slot in a particular column.
     *
     * @param column The column to check.
     * @return the index of the top empty slot in a particular column; -1 if the column is already full.
     */
    public int getLowestEmptyIndex(Connect4Column column) {
        int lowestEmptySlot = -1;
        for  (int i = 0; i < column.getRowCount(); i++)
        {
            if (!column.getSlot(i).getIsFilled())
            {
                lowestEmptySlot = i;
            }
        }
        return lowestEmptySlot;
    }
}

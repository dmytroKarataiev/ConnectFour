package karataiev.dmytro.connectfour;
/**
 * A single slot in a Connect 4 board. A slot can be either empty or filled, and it can
 * be filled with either a red token or a yellow token.
 * 
 */
public class Connect4Slot
{
    private boolean isFilled;
    private boolean isRed;
    private boolean isHighlighted;
    
    /**
     * Creates a new Connect4Slot, initially unfilled.
     */
    public Connect4Slot()
    {
        this.isFilled = false;
        this.isRed = false;
    }

    /**
     * Copies the given slot.
     *
     * @param slot the slot to copy.
     */
    public Connect4Slot(Connect4Slot slot)
    {
        this.isFilled = slot.getIsFilled();
        this.isRed = slot.getIsRed();
    }

    /**
     * Checks if the slot is currently filled.
     *
     * @return true if filled, false if not.
     */
    public boolean getIsFilled()
    {
        return isFilled;
    }

    /**
     * If the slot is filled, checks if the token in the slot is red.
     * 
     * If the slot is not filled, this will still return false; so, this should only
     * be checked after checking getIsFilled().
     *
     * @return true if the token in the slot is red, false if it is yellow.
     */
    public boolean getIsRed()
    {
        return isRed;
    }

    /**
     * If the slot is currently empty, adds a red token to it.
     */
    public void addRed()
    {
        if (!isFilled)
        {
            this.isFilled = true;
            this.isRed = true;
        }
    }

    /**
     * If the slot is currently empty, adds a yellow token to it.
     */
    public void addYellow()
    {
        if (!isFilled)
        {
            this.isFilled = true;
            this.isRed = false;
        }
    }
    
    /**
     * Checks if the slot should be highlighted because it is part of a winning move.
     *
     * @return true if the slot is highlighted, false if not.
     */
    public boolean getIsHighlighted()
    {
        return isHighlighted;
    }

    /**
     * Highlights the slot.
     */
    public void highlight()
    {
        this.isHighlighted = true;
    }

    /**
     * Clears the slot.
     */
    public void clear()
    {
        this.isFilled = false;
        this.isRed = false;
        this.isHighlighted = false;
    }
}

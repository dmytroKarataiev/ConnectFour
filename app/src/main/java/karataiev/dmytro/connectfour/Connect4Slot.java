/*
 * MIT License
 *
 * Copyright (c) 2016. Dmytro Karataiev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package karataiev.dmytro.connectfour;

/**
 * A single slot in a Connect 4 board. A slot can be either empty or filled, and it can
 * be filled with either a red token or a yellow token.
 */
public class Connect4Slot {
    private boolean isFilled;
    private boolean isRed;
    private boolean isHighlighted;
    private boolean isLastFilled;

    /**
     * Creates a new Connect4Slot, initially unfilled.
     */
    public Connect4Slot() {
        this.isFilled = false;
        this.isRed = false;
    }

    /**
     * Copies the given slot.
     *
     * @param slot the slot to copy.
     */
    public Connect4Slot(Connect4Slot slot) {
        this.isFilled = slot.getIsFilled();
        this.isRed = slot.getIsRed();
    }

    /**
     * Checks if the slot is currently filled.
     *
     * @return true if filled, false if not.
     */
    public boolean getIsFilled() {
        return isFilled;
    }

    /**
     * If the slot is filled, checks if the token in the slot is red.
     * <p/>
     * If the slot is not filled, this will still return false; so, this should only
     * be checked after checking getIsFilled().
     *
     * @return true if the token in the slot is red, false if it is yellow.
     */
    public boolean getIsRed() {
        return isRed;
    }

    /**
     * If the slot is currently empty, adds a red token to it.
     */
    public void addRed() {
        if (!isFilled) {
            this.isFilled = true;
            this.isRed = true;
        }
    }

    /**
     * If the slot is currently empty, adds a yellow token to it.
     */
    public void addYellow() {
        if (!isFilled) {
            this.isFilled = true;
            this.isRed = false;
        }
    }

    /**
     * Checks if the slot should be highlighted because it is part of a winning move.
     *
     * @return true if the slot is highlighted, false if not.
     */
    public boolean getIsHighlighted() {
        return isHighlighted;
    }

    /**
     * Highlights the slot.
     */
    public void highlight() {
        this.isHighlighted = true;
    }

    /**
     * Clears the slot.
     */
    public void clear() {
        this.isFilled = false;
        this.isRed = false;
        this.isHighlighted = false;
    }

    /**
     * Set the slot as filled last
     */
    public void addLastFilled() {
        this.isLastFilled = true;
    }

    /**
     * Set the slot as "usual" (do not highlight)
     */
    public void setLastFilled() {
        this.isLastFilled = false;
    }

    /**
     * Check if it was the last filled slot and then highlight
     *
     * @return true if it was last filled
     */
    public boolean getLastFilled() {
        return isLastFilled;
    }
}

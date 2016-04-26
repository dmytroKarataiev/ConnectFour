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

import android.app.Activity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

import karataiev.dmytro.connectfour.players.Agent;
import karataiev.dmytro.connectfour.players.PlayerAgent;

/**
 * The main driver of the Connect4Game
 */
public class Connect4Frame extends MainActivity {

    private final String LOG_TAG = Connect4Frame.class.getSimpleName();

    Connect4Game myGame;    // the game itself
    Connect4Panel myPanel; // the panel (draw & paint)

    Agent redPlayer, yellowPlayer;   // the two players playing the game
    boolean redPlayerturn, gameActive;  // booleans controlling whose turn it is and whether a game is ongoing
    Button playToEndButton;
    TextView status;
    Activity current;
    Random r;   // a random number generator to randomly decide who plays first

    // Images glow on move
    ImageView yellowPlayerImage, redPlayerImage;
    TextView yellowPlayerName, redPlayerName;

    /**
     * Creates a new Connect4Frame with a given game and pair of players.
     *
     * @param game the game itself.
     * @param redPlayer the agent playing as the red tokens.
     * @param yellowPlayer the agent playing as the yellow tokens.
     * @param current current activity
     */
    public Connect4Frame(Connect4Game game, Agent redPlayer, Agent yellowPlayer, Activity current)
    {
        this.myGame = game;   // stores the game itself
        this.redPlayer = redPlayer;   // stores the red player
        this.yellowPlayer = yellowPlayer; //stores the yellow player
        gameActive = false;   // initially sets that no game is active
        r = new Random();   // creates the random number generator

        this.current = current;

        playToEndButton = (Button) current.findViewById(R.id.end);

        status = (TextView) current.findViewById(R.id.status);

        // Colorful balls
        yellowPlayerImage = (ImageView) current.findViewById(R.id.image_player_left);
        redPlayerImage = (ImageView) current.findViewById(R.id.image_player_right);

        // Player names
        yellowPlayerName = (TextView) current.findViewById(R.id.text_player_left);
        redPlayerName = (TextView) current.findViewById(R.id.text_player_right);
        yellowPlayerName.setText(yellowPlayer.getName());
        redPlayerName.setText(redPlayer.getName());

        myPanel = new Connect4Panel(game, current);  // creates the panel for displaying the game
    }


    /**
     * Changes the text of the update label.
     *
     * @param text the next text for the update label.
     */
    public void alert(String text)
    {
        status.setText(text);
    }


    /**
     * Runs the next move of the game.
     */
    private void nextMove(int move)
    {
        Connect4Game oldBoard = new Connect4Game(myGame);   // store the old board for validation

        colorPlayerBall(redPlayerturn);

        if (redPlayerturn && redPlayer instanceof PlayerAgent)
        {
            alert(yellowPlayer.toString() + " plays next...");
            ((PlayerAgent) redPlayer).playerMove(move);
        }
        else if (!redPlayerturn && yellowPlayer instanceof PlayerAgent)
        {
            alert(redPlayer.toString() + " plays next...");
            ((PlayerAgent) yellowPlayer).playerMove(move);
        }
        else if(redPlayerturn) // if it's the red player's turn, run their move
        {
            redPlayer.move();
            alert(yellowPlayer.toString() + " plays next...");
        }
        else // if it's the yellow player's turn, run their move
        {
            yellowPlayer.move();
            alert(redPlayer.toString() + " plays next...");
        }

        String validateResult = oldBoard.validate(myGame); // check and make sure this is a valid next move for this board
        if(validateResult.length() > 0) // if there was a validation error, show it and cancel the game
        {
            alert(validateResult);  // show the error
            gameActive = false;
        }
        redPlayerturn = !redPlayerturn;   // switch whose turn it is
        char won = myGame.gameWon();    // check if the game has been won
        if (won != 'N') // if the game has been won...
        {
            gameActive = false;
            if (myGame.gameWon() == 'R') // if red won, say so
            {
                alert(redPlayer.toString() + " wins!");
            }
            else if (myGame.gameWon() == 'Y') // if yellow won, say so
            {
                alert(yellowPlayer.toString() + " wins!");
            }
        }
        else if (myGame.boardFull()) // if the board is full...
        {
            alert("The game ended in a draw!"); // announce the draw
            gameActive = false;
        }

        myPanel.paint();
    }


    /**
     * Clear the board and start a new game.
     */
    private void newGame()
    {
        myGame.clearBoard();
        gameActive = true;
        redPlayerturn = r.nextBoolean();

        if (redPlayerturn)
        {
            alert(redPlayer.toString() + " plays first!");
            myGame.setRedPlayedFirst(true);
        }
        else
        {
            alert(yellowPlayer.toString() + " plays first!");
            myGame.setRedPlayedFirst(false);
        }

        colorPlayerBall(!redPlayerturn);

        myPanel.paint();

        if (gameActive && (!(redPlayer instanceof PlayerAgent) && redPlayerturn ||
                !(yellowPlayer instanceof PlayerAgent) && !redPlayerturn))
        {
            nextMoveButtonPressed(-1);
        }
    }

    /**
     * Reacts to the new game button being pressed.
     */
    public void newGameButtonPressed()
    {
        newGame();
    }

    /**
     * Reacts to the next move button being pressed.
     */
    public void nextMoveButtonPressed(int move)
    {
        nextMove(move);
    }

    /**
     * Method to color payer ball in correct color
     */
    private void colorPlayerBall(Boolean firstMove) {
        // Glow on turn
        if (firstMove) {
            redPlayerImage.setImageResource(R.drawable.red);
            yellowPlayerImage.setImageResource(R.drawable.yellow_glow);
        } else {
            redPlayerImage.setImageResource(R.drawable.red_glow);
            yellowPlayerImage.setImageResource(R.drawable.yellow);
        }
    }

}


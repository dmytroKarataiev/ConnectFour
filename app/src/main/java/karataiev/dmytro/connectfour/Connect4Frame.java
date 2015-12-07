package karataiev.dmytro.connectfour;

import android.app.Activity;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;

/**
 * The main driver of the Connect4Game
 */
public class Connect4Frame extends MainActivity {

    Connect4Game myGame;    // the game itself
    Connect4Panel myPanel; // the panel (draw & paint)

    Agent redPlayer, yellowPlayer;   // the two players playing the game
    boolean redPlayerturn, gameActive;  // booleans controlling whose turn it is and whether a game is ongoing
    Button playToEndButton;
    TextView status;
    Activity current;
    Random r;   // a random number generator to randomly decide who plays first

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

        this.playToEndButton = (Button) current.findViewById(R.id.end);

        this.status = (TextView) current.findViewById(R.id.status);

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
            if (redPlayer instanceof MyAgent)
            {
                ((MyAgent) redPlayer).move();
            }
            else
            {
                redPlayer.move();
            }
            alert(yellowPlayer.toString() + " plays next...");
        }
        else // if it's the yellow player's turn, run their move
        {
            if (yellowPlayer instanceof MyAgent)
            {
                ((MyAgent) yellowPlayer).move();
            }
            else
            {
                yellowPlayer.move();
            }
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

}


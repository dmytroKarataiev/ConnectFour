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

import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

import karataiev.dmytro.connectfour.players.AdvancedAgent;
import karataiev.dmytro.connectfour.players.Agent;
import karataiev.dmytro.connectfour.players.BrilliantAgent;
import karataiev.dmytro.connectfour.players.MyAgent;
import karataiev.dmytro.connectfour.players.PlayerAgent;
import karataiev.dmytro.connectfour.players.RandomAgent;

public class GamefieldFragment extends Fragment {

    private static final String TAG = GamefieldFragment.class.getSimpleName();

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private Connect4Game game;
    private int[] output;

    int mGameType;
    String mPlayerName;

    Connect4Game myGame;    // the game itself
    Connect4Panel myPanel; // the panel (draw & paint)

    Agent mRedPlayer, mYellowPlayer;   // the two players playing the game
    boolean redPlayerturn, mGameActive;  // booleans controlling whose turn it is and whether a game is ongoing
    Button playToEndButton;
    TextView status;
    Random mRandom;   // a random number generator to randomly decide who plays first

    // Images glow on move
    ImageView yellowPlayerImage, redPlayerImage;
    TextView yellowPlayerName, redPlayerName;

    public GamefieldFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param name Parameter 1.
     * @param id Parameter 2.
     * @return A new instance of fragment NewGameFragment.
     */
    public static GamefieldFragment newInstance(String name, int id) {
        GamefieldFragment fragment = new GamefieldFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, name);
        args.putInt(ARG_PARAM2, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPlayerName = getArguments().getString(ARG_PARAM1);
            mGameType = getArguments().getInt(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_game, container, false);

        if (savedInstanceState != null) {
            Log.d(TAG, "onCreateView: not null ");
        }

        // gets the dimensions of image view and passes them to paint() function to create gamefield on whole screen
        output = imageViewSize(rootView);

        // gets parameter (type of player) from new game screen
        game = new Connect4Game(7, 6); // create the game; these sizes can be altered for larger or smaller games

        // standard player (user)
        mRedPlayer = new PlayerAgent(game, true);
        mRedPlayer.setName(Utility.getPlayerNameFromPref(getContext()));

        // depending on mGameType variable from new game screen chooses needed player
        switch (mGameType) {
            case R.id.newGameScreen:
                mYellowPlayer = new MyAgent(game, false); // simple connect four algorithm, tries to block you from winning
                mYellowPlayer.setName("Easy");
                break;
            case R.id.vsRandom:
                mYellowPlayer = new RandomAgent(game, false); // moves completely randomly
                mYellowPlayer.setName("Random");
                break;
            case R.id.vsPlayer:
                mYellowPlayer = new PlayerAgent(game, false); // second user controlled player
                mYellowPlayer.setName("Player");
                break;
            case R.id.vsAdvanced:
                mYellowPlayer = new AdvancedAgent(game, false); // AdvancedPlayer player
                mYellowPlayer.setName("Advanced");
                break;
            case R.id.vsHard:
                mYellowPlayer = new BrilliantAgent(game, false); // BrilliantPlayer player
                mYellowPlayer.setName("Brilliant");
                break;
        }

        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getActionMasked();

                // TODO: 4/27/16 refactor
                // gets screen size
                Display display = getActivity().getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;

                // different touch activities
                switch (action) {
                    case MotionEvent.ACTION_DOWN:

                        int move = (int) event.getX() / (width / 7);

                        if (mGameActive) {
                            if (redPlayerturn && mRedPlayer instanceof PlayerAgent && (mRedPlayer)
                                    .getLowestEmptyIndex(game.getColumn(move)) > -1) {

                                nextMoveButtonPressed(move);
                                if (mGameActive && !(mYellowPlayer instanceof PlayerAgent)) {
                                    nextMoveButtonPressed(-1);
                                }
                            } else if (!redPlayerturn && mYellowPlayer instanceof PlayerAgent && (mYellowPlayer)
                                    .getLowestEmptyIndex(game.getColumn(move)) > -1) {

                                nextMoveButtonPressed(move);
                                if (mGameActive && !(mRedPlayer instanceof PlayerAgent)) {
                                    nextMoveButtonPressed(-1);
                                }
                            }
                        }
                        break;
                }
                return true;
            }
        });

        return rootView;
    }


    /**
     * Method to get game field imageview size to draw the field correctly
     *
     * @return dimensions of the field
     */
    public int[] imageViewSize(final View rootView) {

        output = new int[2];

        final View iv = rootView.findViewById(R.id.gameField);

        iv.post(new Runnable() {
            @Override
            public void run() {
                output[0] = iv.getMeasuredWidth();
                output[1] = iv.getMeasuredHeight();
                Log.d(TAG, "run: " + output[0] + " " + output[1]);
                Connect4Frame(game, mRedPlayer, mYellowPlayer, rootView);
            }
        });

        return output;
    }

    /**
     * Creates a new Connect4Frame with a given game and pair of players.
     *
     * @param game the game itself.
     * @param redPlayer the agent playing as the red tokens.
     * @param yellowPlayer the agent playing as the yellow tokens.
     */
    public void Connect4Frame(Connect4Game game, Agent redPlayer, Agent yellowPlayer, View current) {

        myGame = game;   // stores the game itself
        mRedPlayer = redPlayer;   // stores the red player
        mYellowPlayer = yellowPlayer; //stores the yellow player
        mGameActive = false;   // initially sets that no game is active
        mRandom = new Random();   // creates the random number generator

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

        myPanel = new Connect4Panel(game, current, output);  // creates the panel for displaying the game

        newGame();
    }


    /**
     * Changes the text of the update label.
     *
     * @param text the next text for the update label.
     */
    public void alert(String text) {
        status.setText(text);
    }


    /**
     * Runs the next move of the game.
     */
    private void nextMove(int move) {
        Connect4Game oldBoard = new Connect4Game(myGame);   // store the old board for validation

        colorPlayerBall(redPlayerturn);

        if (redPlayerturn && mRedPlayer instanceof PlayerAgent) {
            alert(mYellowPlayer.toString() + " plays next...");
            ((PlayerAgent) mRedPlayer).playerMove(move);
        } else if (!redPlayerturn && mYellowPlayer instanceof PlayerAgent) {
            alert(mRedPlayer.toString() + " plays next...");
            ((PlayerAgent) mYellowPlayer).playerMove(move);
        } else if(redPlayerturn) {  // if it's the red player's turn, run their move
            mRedPlayer.move();
            alert(mYellowPlayer.toString() + " plays next...");
        } else {  // if it's the yellow player's turn, run their move
            mYellowPlayer.move();
            alert(mRedPlayer.toString() + " plays next...");
        }

        String validateResult = oldBoard.validate(myGame); // check and make sure this is a valid next move for this board
        if(validateResult.length() > 0) { // if there was a validation error, show it and cancel the game
            alert(validateResult);  // show the error
            mGameActive = false;
        }
        redPlayerturn = !redPlayerturn;   // switch whose turn it is
        char won = myGame.gameWon();    // check if the game has been won

        if (won != 'N') { // if the game has been won...
            mGameActive = false;

            if (myGame.gameWon() == 'R') { // if red won, say so
                alert(mRedPlayer.toString() + " wins!");
            } else if (myGame.gameWon() == 'Y') { // if yellow won, say so
                alert(mYellowPlayer.toString() + " wins!");
            }
        }
        else if (myGame.boardFull()) { // if the board is full...
            alert("The game ended in a draw!"); // announce the draw
            mGameActive = false;
        }

        myPanel.paint();
    }


    /**
     * Clear the board and start a new game.
     */
    private void newGame() {

        myGame.clearBoard();
        mGameActive = true;
        redPlayerturn = mRandom.nextBoolean();

        if (redPlayerturn) {
            alert(mRedPlayer.toString() + " plays first!");
            myGame.setRedPlayedFirst(true);
        } else {
            alert(mYellowPlayer.toString() + " plays first!");
            myGame.setRedPlayedFirst(false);
        }

        colorPlayerBall(!redPlayerturn);

        myPanel.paint();

        if (mGameActive && (!(mRedPlayer instanceof PlayerAgent) && redPlayerturn ||
                !(mYellowPlayer instanceof PlayerAgent) && !redPlayerturn)) {
            nextMoveButtonPressed(-1);
        }
    }

    /**
     * Reacts to the new game button being pressed.
     */
    public void newGameButtonPressed() {
        newGame();
    }

    /**
     * Reacts to the next move button being pressed.
     */
    public void nextMoveButtonPressed(int move) {
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.d(TAG, "onSaveInstanceState: ");

    }
}

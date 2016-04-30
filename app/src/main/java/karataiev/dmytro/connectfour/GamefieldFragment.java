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
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
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
    private int[] mGamefieldDimensions;

    int mGameType;
    String mPlayerName;

    Connect4Game mGame;    // the game itself
    Connect4Panel mPanel; // the panel (draw & paint)

    Agent mRedPlayer, mYellowPlayer;   // the two players playing the game
    boolean redPlayerturn, mGameActive, mMultiplayer;  // booleans controlling whose turn it is and whether a game is ongoing

    // Views and Unbinder
    @BindView(R.id.image_player_left) ImageView yellowPlayerImage;
    @BindView(R.id.image_player_right) ImageView redPlayerImage;
    @BindView(R.id.text_player_left) TextView yellowPlayerName;
    @BindView(R.id.text_player_right) TextView redPlayerName;
    @BindView(R.id.status) TextView status;
    Unbinder mUnbinder;

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

        mUnbinder = ButterKnife.bind(this, rootView);

        // gets the dimensions of image view and passes them to paint() function to create gamefield on whole screen
        mGamefieldDimensions = imageViewSize(rootView);

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
            case R.id.button_multiplayer:
                mYellowPlayer = new PlayerAgent(game, false);
                mYellowPlayer.setName("Multiplayer");
                mMultiplayer = true;
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
                            if (mMultiplayer) {

                                if (redPlayerturn && mRedPlayer
                                        .getLowestEmptyIndex(game.getColumn(move)) > -1 ||
                                        !redPlayerturn && mYellowPlayer
                                                .getLowestEmptyIndex(game.getColumn(move)) > -1) {
                                    if (((MainActivity) getActivity()).broadcastScore(move)) {
                                        nextMove(move);
                                    }
                                }

                            } else if (redPlayerturn && mRedPlayer instanceof PlayerAgent && (mRedPlayer)
                                    .getLowestEmptyIndex(game.getColumn(move)) > -1) {

                                nextMove(move);
                                if (mGameActive && !(mYellowPlayer instanceof PlayerAgent)) {
                                    nextMove(-1);
                                }
                            } else if (!redPlayerturn && mYellowPlayer instanceof PlayerAgent && (mYellowPlayer)
                                    .getLowestEmptyIndex(game.getColumn(move)) > -1) {

                                nextMove(move);
                                if (mGameActive && !(mRedPlayer instanceof PlayerAgent)) {
                                    nextMove(-1);
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

        mGamefieldDimensions = new int[2];

        final View iv = rootView.findViewById(R.id.gameField);

        iv.post(new Runnable() {
            @Override
            public void run() {
                mGamefieldDimensions[0] = iv.getMeasuredWidth();
                mGamefieldDimensions[1] = iv.getMeasuredHeight();
                Log.d(TAG, "run: " + mGamefieldDimensions[0] + " " + mGamefieldDimensions[1]);
                Connect4Frame(game, mRedPlayer, mYellowPlayer, rootView, redPlayerturn);
            }
        });

        return mGamefieldDimensions;
    }

    /**
     * Creates a new Connect4Frame with a given game and pair of players.
     *
     * @param game the game itself.
     * @param redPlayer the agent playing as the red tokens.
     * @param yellowPlayer the agent playing as the yellow tokens.
     */
    public void Connect4Frame(Connect4Game game, Agent redPlayer, Agent yellowPlayer, View current, boolean firstMove) {

        mGame = game;   // stores the game itself
        mRedPlayer = redPlayer;   // stores the red player
        mYellowPlayer = yellowPlayer; //stores the yellow player
        mGameActive = false;   // initially sets that no game is active

        // Player names
        yellowPlayerName.setText(yellowPlayer.getName());
        redPlayerName.setText(redPlayer.getName());

        mPanel = new Connect4Panel(game, current, mGamefieldDimensions);  // creates the panel for displaying the game

        newGame(firstMove);
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
    public void nextMove(int move) {
        Log.d(TAG, "move:" + move);
        Connect4Game oldBoard = new Connect4Game(mGame);   // store the old board for validation

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

        String validateResult = oldBoard.validate(mGame); // check and make sure this is a valid next move for this board
        if (validateResult.length() > 0) { // if there was a validation error, show it and cancel the game
            alert(validateResult);  // show the error
            mGameActive = false;
            Log.d(TAG, "nextMove: unvalidated");
        }
        redPlayerturn = !redPlayerturn;   // switch whose turn it is
        char won = mGame.gameWon();    // check if the game has been won

        if (won != 'N') { // if the game has been won...
            mGameActive = false;
            Log.d(TAG, "nextMove: game has been won");

            if (mGame.gameWon() == 'R') { // if red won, say so
                alert(mRedPlayer.toString() + " wins!");
            } else if (mGame.gameWon() == 'Y') { // if yellow won, say so
                alert(mYellowPlayer.toString() + " wins!");
            }
        }
        else if (mGame.boardFull()) { // if the board is full...
            Log.d(TAG, "nextMove: board full");
            alert("The game ended in a draw!"); // announce the draw
            mGameActive = false;
        }

        Log.d(TAG, "mGameActive:" + mGameActive);

        mPanel.paint();
    }

    /**
     * Clear the board and start a new game.
     */
    public void newGame(boolean firstMove) {

        mGame.clearBoard();
        mGameActive = true;
        redPlayerturn = firstMove;

        if (redPlayerturn) {
            alert(mRedPlayer.toString() + " plays first!");
            mGame.setRedPlayedFirst(true);
        } else {
            alert(mYellowPlayer.toString() + " plays first!");
            mGame.setRedPlayedFirst(false);
        }

        colorPlayerBall(!redPlayerturn);

        mPanel.paint();

        if (mGameActive && (!(mRedPlayer instanceof PlayerAgent) && redPlayerturn ||
                !(mYellowPlayer instanceof PlayerAgent) && !redPlayerturn)) {
            nextMove(-1);
        }
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
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}

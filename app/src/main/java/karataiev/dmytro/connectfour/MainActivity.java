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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import karataiev.dmytro.connectfour.players.AdvancedAgent;
import karataiev.dmytro.connectfour.players.Agent;
import karataiev.dmytro.connectfour.players.BrilliantAgent;
import karataiev.dmytro.connectfour.players.MyAgent;
import karataiev.dmytro.connectfour.players.PlayerAgent;
import karataiev.dmytro.connectfour.players.RandomAgent;

public class MainActivity extends AppCompatActivity {

    private float initialX, initialY;
    private Agent yellowPlayer, redPlayer;
    private Connect4Frame mainframe;
    private Connect4Game game;
    private int[] output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // gets the dimensions of image view and passes them to paint() function to create gamefield on whole screen
        output = imageViewSize();

        // gets parameter (type of player) from new game screen
        Bundle b = getIntent().getExtras();
        int id = b.getInt(getString(R.string.name_default));

        game = new Connect4Game(7, 6); // create the game; these sizes can be altered for larger or smaller games

        // standard player (user)
        redPlayer = new PlayerAgent(game, true);
        redPlayer.setName(Utility.getPlayerNameFromPref(getApplicationContext()));

        // depending on id variable from new game screen chooses needed player
        if (id == R.id.newGameScreen) {
            yellowPlayer = new MyAgent(game, false); // simple connect four algorithm, tries to block you from winning
            yellowPlayer.setName("Easy");
        } else if (id == R.id.vsRandom) {
            yellowPlayer = new RandomAgent(game, false); // moves completely randomly
            yellowPlayer.setName("Random");
        } else if (id == R.id.vsPlayer) {
            yellowPlayer = new PlayerAgent(game, false); // second user controlled player
            yellowPlayer.setName("Player");
        } else if (id == R.id.vsAdvanced) {
            yellowPlayer = new AdvancedAgent(game, false); // AdvancedPlayer player
            yellowPlayer.setName("Advanced");
        } else if (id == R.id.vsHard) {
            yellowPlayer = new BrilliantAgent(game, false); // BrilliantPlayer player
            yellowPlayer.setName("Brilliant");
        }

        // passing of activity to game frame
        Activity toPass = this;
        mainframe = new Connect4Frame(game, redPlayer, yellowPlayer, toPass);

    }


    // starts the game on focus (to be able to get dimensions of the imageview
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        // starts the game when users focuses on the screen (to be able to get dimensions correctly)
        if (!mainframe.gameActive) {
            mainframe.newGameButtonPressed();
        }
    }


    /**
     * Method to get game field imageview size to draw the field correctly
     *
     * @return dimensions of the field
     */
    public int[] imageViewSize() {

        output = new int[2];

        final ImageView iv = (ImageView) findViewById(R.id.gameField);

        ViewTreeObserver vto = iv.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                iv.getViewTreeObserver().removeOnPreDrawListener(this);
                output[1] = iv.getMeasuredHeight();
                output[0] = iv.getMeasuredWidth();

                return true;
            }
        });

        return output;
    }

    /**
     * method to capture different touch activities and if player is a user - make a move, if it's valid
     *
     * @return touch event
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();

        // gets screen size
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        // different touch activities
        switch (action) {

            case MotionEvent.ACTION_DOWN:
                initialX = event.getX();
                initialY = event.getY();

                int move = (int) initialX / (width / 7);

                if (mainframe.gameActive) {
                    if (mainframe.redPlayerturn && redPlayer instanceof PlayerAgent && (redPlayer)
                            .getLowestEmptyIndex(game.getColumn(move)) > -1) {

                        mainframe.nextMoveButtonPressed(move);
                        if (mainframe.gameActive && !(yellowPlayer instanceof PlayerAgent)) {
                            mainframe.nextMoveButtonPressed(-1);
                        }
                    } else if (!mainframe.redPlayerturn && yellowPlayer instanceof PlayerAgent && (yellowPlayer)
                            .getLowestEmptyIndex(game.getColumn(move)) > -1) {

                        mainframe.nextMoveButtonPressed(move);
                        if (mainframe.gameActive && !(redPlayer instanceof PlayerAgent)) {
                            mainframe.nextMoveButtonPressed(-1);
                        }
                    }
                }

                break;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Finish the game?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }})
                .setNegativeButton("No", null)
                .show();
    }

}

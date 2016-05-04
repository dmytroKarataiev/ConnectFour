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

package karataiev.dmytro.connectfour.managers;

import android.app.Activity;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;

import java.util.List;
import java.util.Random;

import karataiev.dmytro.connectfour.GamefieldFragment;
import karataiev.dmytro.connectfour.MainActivity;
import karataiev.dmytro.connectfour.R;

/**
 * Created by karataev on 4/30/16.
 */
public class RoomManager implements RoomUpdateListener,
        RealTimeMessageReceivedListener,
        RoomStatusUpdateListener {

    private static final String TAG = RoomManager.class.getSimpleName();

    private MainActivity mActivity;

    public RoomConfig.Builder mRoomConfig;

    public RoomConfig.Builder getRoomConfig() {
        return mRoomConfig;
    }

    public void init(Activity context) {

        mActivity = (MainActivity) context;

        mRoomConfig = RoomConfig.builder(this)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
    }

    @Override
    public void onRoomCreated(int statusCode, Room room) {
        Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");

        if (statusCode != GamesStatusCodes.STATUS_OK) {
            // let screen go to sleep
            mActivity.keepScreenOn();
            Log.d(TAG, "onRoomCreated Error (" + statusCode + ", " + room + ")");

            // show error message, return to main screen.
        } else {
            // save room ID so we can leave cleanly before the game starts.
            mActivity.mRoomId = room.getRoomId();
            mActivity.mYourMove = new Random().nextBoolean();
            mActivity.mMsgBuf[2] = (byte) (mActivity.mYourMove ? 'R' : 'Y');
            Log.d(TAG, "On Room Created: " + mActivity.mMsgBuf[2] + " yourMove: " + mActivity.mYourMove);
        }

    }

    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");

        if (statusCode != GamesStatusCodes.STATUS_OK) {
            // let screen go to sleep
            mActivity.keepScreenOn();
            // show error message, return to main screen.
        }
    }

    @Override
    public void onLeftRoom(int statusCode, String s) {
        Log.d(TAG, "onLeftRoom, code " + statusCode);
    }

    @Override
    public void onRoomConnected(int statusCode, Room room) {
        Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");

        if (statusCode != GamesStatusCodes.STATUS_OK) {
            // let screen go to sleep
            mActivity.keepScreenOn();
            // show error message, return to main screen.
        }

        mActivity.updateRoom(room);
        // TODO: 4/29/16 fix
        mActivity.mGamefieldFragment = GamefieldFragment.newInstance("Multi", R.id.button_multiplayer);

        new Handler().post(new Runnable() {
            public void run() {
                mActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, mActivity.mGamefieldFragment, MainActivity.FRAGMENT_GAMEFIELD)
                        .commit();
                mActivity.mGamefieldFragment.mMultiplayer = true;
                mActivity.mMultiplayer = true;
                mActivity.broadcastScore(-1);
            }
        });

    }

    @Override
    public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {

        byte[] buf = realTimeMessage.getMessageData();
        String sender = realTimeMessage.getSenderParticipantId();
        Log.d(TAG, "Message received: " + (char) buf[0] + "/" + (int) buf[1] + "/" + (char) buf[2]);

        FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
        mActivity.mGamefieldFragment = (GamefieldFragment) fragmentManager.findFragmentByTag(MainActivity.FRAGMENT_GAMEFIELD);

        if (mActivity.mGamefieldFragment == null) {
            mActivity.mGamefieldFragment = GamefieldFragment.newInstance("user", R.id.button_multiplayer);
        }

        if (buf[1] == -1 && ((char) buf[2] == 'R' || (char) buf[2] == 'Y')) {
            Log.d(TAG, "First launch: " + buf[2] + " " + (buf[2] == 'R'));
            if (buf[2] == 'R') {
                mActivity.mYourMove = false;
                mActivity.mGamefieldFragment.newGame(false);
            } else if (buf[2] == 'Y') {
                mActivity.mYourMove = true;
                mActivity.mGamefieldFragment.newGame(true);
            }
        } else if (buf[1] != -1) {
            mActivity.mYourMove = !mActivity.mYourMove;
            Log.d(TAG, "mYourMove:" + mActivity.mYourMove);
        }

        int thisScore = (int) buf[1];
        if (thisScore != -1) {
            mActivity.mParticipantScore.put(sender, thisScore);

            // update the scores on the screen
            mActivity.updatePeerScoresDisplay();

            if (mActivity.mGamefieldFragment != null) {
                mActivity.mGamefieldFragment.nextMove(thisScore);
            }

            // if it's a final score, mark this participant as having finished the game
            mActivity.mFinishedParticipants.add(realTimeMessage.getSenderParticipantId());
        }
        
    }

    @Override
    public void onRoomAutoMatching(Room room) {
        updateRoom(room);
    }

    @Override
    public void onRoomConnecting(Room room) {
        updateRoom(room);
    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onPeerDeclined(Room room, List<String> peers) {
        // peer declined invitation -- see if game should be canceled
        if (!mActivity.mPlaying && mActivity.shouldCancelGame(room)) {
            Games.RealTimeMultiplayer.leave(mActivity.mGoogleApiClient, null, mActivity.mRoomId);
            mActivity.keepScreenOn();
        }
    }

    @Override
    public void onPeerJoined(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onPeerLeft(Room room, List<String> peers) {
        // peer left -- see if game should be canceled
        if (!mActivity.mPlaying && mActivity.shouldCancelGame(room)) {
            Games.RealTimeMultiplayer.leave(mActivity.mGoogleApiClient, null, mActivity.mRoomId);
            mActivity.keepScreenOn();
        }
    }

    @Override
    public void onConnectedToRoom(Room room) {
        //get participants and my ID:
        mActivity.mParticipants = room.getParticipants();
        mActivity.mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(mActivity.mGoogleApiClient));

        // save room ID if its not initialized in onRoomCreated() so we can leave cleanly before the game starts.
        if(mActivity.mRoomId==null) {
            mActivity.mRoomId = room.getRoomId();
        }

        // print out the list of participants (for debug purposes)
        Log.d(TAG, "Room ID: " + mActivity.mRoomId);
        Log.d(TAG, "My ID " + mActivity.mMyId);
        Log.d(TAG, "<< CONNECTED TO ROOM>>");
    }

    @Override
    public void onDisconnectedFromRoom(Room room) {
        mActivity.mRoomId = null;
    }

    @Override
    public void onPeersConnected(Room room, List<String> peers) {
        if (mActivity.mPlaying) {
            // add new player to an ongoing game
        } else if (mActivity.shouldStartGame(room)) {
            // start game!
        }
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> peers) {
        if (mActivity.mPlaying) {
            // do game-specific handling of this -- remove player's avatar
            // from the screen, etc. If not enough players are left for
            // the game to go on, end the game and leave the room.
        } else if (mActivity.shouldCancelGame(room)) {
            // cancel the game
            Games.RealTimeMultiplayer.leave(mActivity.mGoogleApiClient, null, mActivity.mRoomId);
            mActivity.keepScreenOn();
        }
    }

    @Override
    public void onP2PConnected(String s) {

    }

    @Override
    public void onP2PDisconnected(String s) {

    }

    private void updateRoom(Room room) {
        mActivity.updateRoom(room);
    }
}

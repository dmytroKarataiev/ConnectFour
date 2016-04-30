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
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import butterknife.ButterKnife;
import karataiev.dmytro.connectfour.gameutils.BaseGameUtils;
import karataiev.dmytro.connectfour.interfaces.OnFragmentInteraction;
import karataiev.dmytro.connectfour.interfaces.OnGoogleApiChange;
import karataiev.dmytro.connectfour.managers.MultiplayerManager;

/**
 * Main starting class which signs in to Google Account
 */
public class MainActivity extends AppCompatActivity implements
        RoomUpdateListener,
        RoomStatusUpdateListener,
        RealTimeMessageReceivedListener,
        OnInvitationReceivedListener, OnGoogleApiChange,
        OnFragmentInteraction {

    private static final String TAG = MainActivity.class.getSimpleName();

    private MultiplayerManager mMultiplayerManager;

    final static int RC_SIGN_IN = 9001;

    // request code for the "select players" UI
    // can be any number as long as it's unique
    final static int RC_SELECT_PLAYERS = 10000;

    // Client used to interact with Google APIs.
    private GoogleApiClient mGoogleApiClient;

    // Are we playing in multiplayer mode?
    boolean mMultiplayer = false;

    // Room ID where the currently active game is taking place; null if we're
    // not playing.
    String mRoomId = null;

    // My participant ID in the currently active game
    String mMyId = null;

    // Message buffer for sending messages
    byte[] mMsgBuf = new byte[4];
    private boolean mYourMove;
    int mScore = 0; // user's current score

    // Fragments and corresponding tags
    private NewGameFragment mNewGameFragment;
    private GamefieldFragment mGamefieldFragment;
    private MultiplayerFragment mMultiplayerFragment;

    public static final String FRAGMENT_NEWGAME = "newgame";
    public static final String FRAGMENT_GAMEFIELD = "gamefield";
    public static final String FRAGMENT_MULTIPLAYER = "multiplayer";
    public static final String FRAGMENT_BACKSTACK = "backstack";

    public static boolean isContinueVisible = false;

    // The participants in the currently active game
    ArrayList<Participant> mParticipants = null;

    // If non-null, this is the mGameType of the invitation we received via the
    // invitation listener
    String mIncomingInvitationId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        if (savedInstanceState == null) {
            mNewGameFragment = NewGameFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mNewGameFragment, FRAGMENT_NEWGAME)
                    .commit();
        }

        // Create the Google Api Client with access to Games
        mMultiplayerManager = App.getGoogleApiManager(this, this);
        mGoogleApiClient = mMultiplayerManager.getGoogleApiClient();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnectedApi(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected() called. Sign in successful!");
        // register listener so we are notified if we receive an invitation to play
        // while we are in the game
        Games.Invitations.registerInvitationListener(mGoogleApiClient, this);

        if (bundle != null) {
            Log.d(TAG, "onConnected: connection hint provided. Checking for invite.");
            Invitation inv = bundle
                    .getParcelable(Multiplayer.EXTRA_INVITATION);
            if (inv != null && inv.getInvitationId() != null) {
                // retrieve and cache the invitation ID
                Log.d(TAG,"onConnected: connection hint has a room invite!");
                acceptInviteToRoom(inv.getInvitationId());
                return;
            }
        }
        //mSignIn.setVisibility(View.GONE);
        //mSignOut.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        switch (requestCode) {
            case RC_SIGN_IN:
                mMultiplayerManager.setSignInClicked(false);
                mMultiplayerManager.setResolvingConnectionFailure(false);
                if (resultCode == RESULT_OK) {
                    mGoogleApiClient.connect();
                } else {
                    // Bring up an error dialog to alert the user that sign-in
                    // failed. The R.string.signin_failure should reference an error
                    // string in your strings.xml file that tells the user they
                    // could not be signed in, such as "Unable to sign in."
                    BaseGameUtils.showActivityResultError(this,
                            requestCode, resultCode, R.string.sign_in_failed);
                }
                break;
            case RC_SELECT_PLAYERS:
                handleSelectPlayersResult(resultCode, intent);
                break;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    // Handle the result of the "Select players UI" we launched when the user clicked the
    // "Invite friends" button. We react by creating a room with those players.
    private void handleSelectPlayersResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** select players UI cancelled, " + response);
            return;
        }

        Log.d(TAG, "Select players UI succeeded.");
        mMultiplayer = true;

        // get the invitee list
        final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
        Log.d(TAG, "Invitee count: " + invitees.size());

        // get the automatch criteria
        Bundle autoMatchCriteria = null;
        int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
        if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                    minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            Log.d(TAG, "Automatch criteria: " + autoMatchCriteria);
        }

        // create the room
        Log.d(TAG, "Creating room...");
        RoomConfig.Builder rtmConfigBuilder = makeBasicRoomConfigBuilder();
        rtmConfigBuilder.addPlayersToInvite(invitees);
        if (autoMatchCriteria != null) {
            rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        }
        RoomConfig roomConfig = rtmConfigBuilder.build();
        Games.RealTimeMultiplayer.create(mGoogleApiClient, roomConfig);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.d(TAG, "Room created, waiting for it to be ready...");
    }

    // create a RoomConfigBuilder that's appropriate for your implementation
    private RoomConfig.Builder makeBasicRoomConfigBuilder() {
        return RoomConfig.builder(this)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
    }

    @Override
    public void onRoomCreated(int statusCode, Room room) {
        Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");

        if (statusCode != GamesStatusCodes.STATUS_OK) {
            // let screen go to sleep
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            // show error message, return to main screen.
        }
        // save room ID so we can leave cleanly before the game starts.
        mRoomId = room.getRoomId();

        mYourMove = new Random().nextBoolean();
        mMsgBuf[2] = (byte) (mYourMove ? 'R' : 'Y');
        Log.d(TAG, "On Room Created: " + mMsgBuf[2] + " yourMove: " + mYourMove);
    }

    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");

        if (statusCode != GamesStatusCodes.STATUS_OK) {
            // let screen go to sleep
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            // show error message, return to main screen.
        }
    }

    @Override
    public void onLeftRoom(int i, String s) {
        Log.d(TAG, "onLeftRoom, code " + i);
    }

    @Override
    public void onRoomConnected(int statusCode, Room room) {
        Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");

        if (statusCode != GamesStatusCodes.STATUS_OK) {
            // let screen go to sleep
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            // show error message, return to main screen.
        }
        updateRoom(room);
        // TODO: 4/29/16 fix
        mGamefieldFragment = GamefieldFragment.newInstance("Multi", R.id.button_multiplayer);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, mGamefieldFragment, FRAGMENT_GAMEFIELD)
                .commit();
        mGamefieldFragment.mMultiplayer = true;
        mMultiplayer = true;
        broadcastScore(-1);
    }

    // are we already playing?
    boolean mPlaying = false;

    // at least 2 players required for our game
    final static int MIN_PLAYERS = 2;

    // returns whether there are enough players to start the game
    boolean shouldStartGame(Room room) {
        int connectedPlayers = 0;
        for (Participant p : room.getParticipants()) {
            if (p.isConnectedToRoom()) ++connectedPlayers;
        }
        return connectedPlayers >= MIN_PLAYERS;
    }

    // Returns whether the room is in a state where the game should be canceled.
    boolean shouldCancelGame(Room room) {
        // TODO: Your game-specific cancellation logic here. For example, you might decide to
        // cancel the game if enough people have declined the invitation or left the room.
        // You can check a participant's status with Participant.getStatus().
        // (Also, your UI should have a Cancel button that cancels the game too)
        mMultiplayer = false;
        return false;
    }

    @Override
    public void onPeersConnected(Room room, List<String> peers) {
        if (mPlaying) {
            // add new player to an ongoing game
        } else if (shouldStartGame(room)) {
            // start game!
        }
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> peers) {
        if (mPlaying) {
            // do game-specific handling of this -- remove player's avatar
            // from the screen, etc. If not enough players are left for
            // the game to go on, end the game and leave the room.
        } else if (shouldCancelGame(room)) {
            // cancel the game
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, null, mRoomId);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onPeerLeft(Room room, List<String> peers) {
        // peer left -- see if game should be canceled
        if (!mPlaying && shouldCancelGame(room)) {
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, null, mRoomId);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onConnectedToRoom(Room room) {
        //get participants and my ID:
        mParticipants = room.getParticipants();
        mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(mGoogleApiClient));

        // save room ID if its not initialized in onRoomCreated() so we can leave cleanly before the game starts.
        if(mRoomId==null) {
            mRoomId = room.getRoomId();
        }

        // print out the list of participants (for debug purposes)
        Log.d(TAG, "Room ID: " + mRoomId);
        Log.d(TAG, "My ID " + mMyId);
        Log.d(TAG, "<< CONNECTED TO ROOM>>");
    }

    @Override
    public void onDisconnectedFromRoom(Room room) {
        mRoomId = null;
    }

    @Override
    public void onPeerDeclined(Room room, List<String> peers) {
        // peer declined invitation -- see if game should be canceled
        if (!mPlaying && shouldCancelGame(room)) {
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, null, mRoomId);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    // We treat most of the room update callbacks in the same way: we update our list of
    // participants and update the display. In a real game we would also have to check if that
    // change requires some action like removing the corresponding player avatar from the screen,
    // etc.
    @Override
    public void onPeerInvitedToRoom(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onP2PDisconnected(String participant) {
    }

    @Override
    public void onP2PConnected(String participant) {
    }

    @Override
    public void onPeerJoined(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onRoomAutoMatching(Room room) {
        updateRoom(room);
    }

    @Override
    public void onRoomConnecting(Room room) {
        updateRoom(room);
    }

    void updateRoom(Room room) {
        if (room != null) {
            mParticipants = room.getParticipants();
        }
        if (mParticipants != null) {
        }
    }

    /*
     * COMMUNICATIONS SECTION. Methods that implement the game's network
     * protocol.
     */

    // Score of other participants. We update this as we receive their scores
    // from the network.
    Map<String, Integer> mParticipantScore = new HashMap<String, Integer>();

    // Participants who sent us their final score.
    Set<String> mFinishedParticipants = new HashSet<String>();

    // Called when we receive a real-time message from the network.
    // Messages in our game are made up of 2 bytes: the first one is 'F' or 'U'
    // indicating
    // whether it's a final or interim score. The second byte is the score.
    // There is also the
    // 'S' message, which indicates that the game should start.
    @Override
    public void onRealTimeMessageReceived(RealTimeMessage rtm) {
        byte[] buf = rtm.getMessageData();
        String sender = rtm.getSenderParticipantId();
        Log.d(TAG, "Message received: " + (char) buf[0] + "/" + (int) buf[1] + "/" + (char) buf[2]);

        FragmentManager fragmentManager = getSupportFragmentManager();
        mGamefieldFragment = (GamefieldFragment) fragmentManager.findFragmentByTag(FRAGMENT_GAMEFIELD);

        if (buf[1] == -1 && ((char) buf[2] == 'R' || (char) buf[2] == 'Y')) {
            Log.d(TAG, "First launch: " + buf[2] + " " + (buf[2] == 'R'));
            if (buf[2] == 'R') {
                mYourMove = false;
                mGamefieldFragment.newGame(false);
            } else if (buf[2] == 'Y') {
                mYourMove = true;
                mGamefieldFragment.newGame(true);
            }
        } else if (buf[1] != -1) {
            mYourMove = !mYourMove;
            Log.d(TAG, "mYourMove:" + mYourMove);
        }

        int thisScore = (int) buf[1];
        if (thisScore != -1) {
            mParticipantScore.put(sender, thisScore);

            // update the scores on the screen
            updatePeerScoresDisplay();

            if (mGamefieldFragment != null) {
                mGamefieldFragment.nextMove(thisScore);
            }

            // if it's a final score, mark this participant as having finished the game
            mFinishedParticipants.add(rtm.getSenderParticipantId());
        }
    }

    // Broadcast my score to everybody else.
    public boolean broadcastScore(int finalScore) {

        Log.d(TAG, "mMultiplayer:" + mMultiplayer + " move: " + mYourMove + " " +
                (!mYourMove && finalScore > -1) + " score: " + finalScore);
        if (!mMultiplayer || (!mYourMove && finalScore > -1)) { return false; } // playing single-player mode

        // First byte in message indicates whether it's a final score or not
        mMsgBuf[0] = (byte) 'F';

        // Second byte is the score.
        mMsgBuf[1] = (byte) finalScore;

        Log.d(TAG, "mParticipants.size():" + mParticipants.size());

        // Send to every other participant.
        for (Participant p : mParticipants) {
            if (p.getParticipantId().equals(mMyId)) { continue; }
            if (p.getStatus() != Participant.STATUS_JOINED) { continue; }

            Log.d(TAG, "send reliable");
            // final score notification must be sent via reliable message
            Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, mMsgBuf,
                    mRoomId, p.getParticipantId());
        }

        if (finalScore == -1) {
            mMsgBuf[2] = 0;
        } else {
            mYourMove = !mYourMove;
        }

        return true;
    }

    // updates the screen with the scores from our peers
    void updatePeerScoresDisplay() {

        if (mRoomId != null) {
            for (Participant p : mParticipants) {
                String pid = p.getParticipantId();
                if (pid.equals(mMyId)) { continue; }
                if (p.getStatus() != Participant.STATUS_JOINED) { continue; }
                int score = mParticipantScore.containsKey(pid) ? mParticipantScore.get(pid) : 0;
                Log.d(TAG, "score: " + score);
            }
        }
    }

    // Called when we get an invitation to play a game. We react by showing that to the user.
    @Override
    public void onInvitationReceived(Invitation invitation) {
        // We got an invitation to play a game! So, store it in
        // mIncomingInvitationId
        // and show the popup on the screen.
        mIncomingInvitationId = invitation.getInvitationId();
        ((TextView) findViewById(R.id.incoming_invitation_text)).setText(
                invitation.getInviter().getDisplayName() + " " +
                        getString(R.string.is_inviting_you));
    }

    @Override
    public void onInvitationRemoved(String invitationId) {
        if (mIncomingInvitationId != null && mIncomingInvitationId.equals(invitationId)) {
            mIncomingInvitationId = null;
        }
    }

    // Handle the result of the invitation inbox UI, where the player can pick an invitation
    // to accept. We react by accepting the selected invitation, if any.
    private void handleInvitationInboxResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** invitation inbox UI cancelled, " + response);
            return;
        }

        Log.d(TAG, "Invitation inbox UI succeeded.");
        Invitation inv = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);

        // accept invitation
        acceptInviteToRoom(inv.getInvitationId());
    }

    // Accept the given invitation.
    void acceptInviteToRoom(String invId) {
        // accept the invitation
        Log.d(TAG, "Accepting invitation: " + invId);
        RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(this);
        roomConfigBuilder.setInvitationIdToAccept(invId)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
        keepScreenOn();
        Games.RealTimeMultiplayer.join(mGoogleApiClient, roomConfigBuilder.build());
    }

    // Sets the flag to keep this screen on. It's recommended to do that during
    // the
    // handshake when setting up a game, because if the screen turns off, the
    // game will be
    // cancelled.
    void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // TODO: 4/29/16 add multiplayer fragment onBackPressed
    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(FRAGMENT_GAMEFIELD) != null &&
                fragmentManager.findFragmentByTag(FRAGMENT_GAMEFIELD).isVisible()) {

            fragmentManager.beginTransaction()
                    .add(R.id.container, mNewGameFragment, FRAGMENT_NEWGAME)
                    .hide(mGamefieldFragment)
                    .commit();

            isContinueVisible = true;
        } else {
            isContinueVisible = false;
            finish();
        }
    }

    @Override
    public void onFragmentClick(int click) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        switch (click) {
            case R.id.button_multiplayer:
                mMultiplayerFragment = MultiplayerFragment.newInstance();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, mMultiplayerFragment, FRAGMENT_MULTIPLAYER)
                        .commit();
                break;
            case R.id.button_continue:
                if (mGamefieldFragment != null) {
                    fragmentManager.beginTransaction()
                            .remove(mNewGameFragment)
                            .show(mGamefieldFragment)
                            .commit();
                }
                break;
            case R.id.sign_in_button:
                // start the asynchronous sign in flow
                mMultiplayerManager.setSignInClicked(true);
                mGoogleApiClient.connect();
                break;
            case R.id.sign_out_button:
                // sign out.
                mMultiplayerManager.setSignInClicked(false);
                mGoogleApiClient.disconnect();

                Games.signOut(mGoogleApiClient);

                //mSignOut.setVisibility(View.GONE);
                //mSignIn.setVisibility(View.VISIBLE);
                break;
            case R.id.button_invitation:
                Intent intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(mGoogleApiClient, 1, 1);
                startActivityForResult(intent, RC_SELECT_PLAYERS);
                break;
            case R.id.broadcastScore:
                mScore++;
                broadcastScore(mScore);
                break;
            case R.id.button_accept_popup_invitation:
                // user wants to accept the invitation shown on the invitation popup
                // (the one we got through the OnInvitationReceivedListener).
                acceptInviteToRoom(mIncomingInvitationId);
                mIncomingInvitationId = null;
                mMultiplayer = true;
                break;
            default:
                mGamefieldFragment = GamefieldFragment.newInstance(getString(R.string.name_default), click);
                fragmentManager.beginTransaction()
                        .replace(R.id.container, mGamefieldFragment, FRAGMENT_GAMEFIELD)
                        .commit();
                break;
        }
    }

}

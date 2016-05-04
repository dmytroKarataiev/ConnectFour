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
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import butterknife.ButterKnife;
import karataiev.dmytro.connectfour.gameutils.BaseGameUtils;
import karataiev.dmytro.connectfour.interfaces.OnFragmentInteraction;
import karataiev.dmytro.connectfour.interfaces.OnGoogleApiChange;
import karataiev.dmytro.connectfour.managers.MultiplayerManager;
import karataiev.dmytro.connectfour.managers.RoomManager;

/**
 * Main starting class which signs in to Google Account
 */
public class MainActivity extends AppCompatActivity implements
        OnInvitationReceivedListener,
        OnGoogleApiChange,
        OnFragmentInteraction {

    private static final String TAG = MainActivity.class.getSimpleName();

    private MultiplayerManager mMultiplayerManager;
    private RoomManager mRoomManager;

    final static int RC_SIGN_IN = 9001;
    final static int RC_SELECT_PLAYERS = 10000;

    // Client used to interact with Google APIs.
    public GoogleApiClient mGoogleApiClient;

    // Are we playing in multiplayer mode?
    public boolean mMultiplayer = false;

    // Room ID where the currently active game is taking place; null if we're not playing.
    public String mRoomId = null;

    // My participant ID in the currently active game
    public String mMyId = null;

    // Message buffer for sending messages
    public byte[] mMsgBuf = new byte[4];
    public boolean mYourMove;
    int mScore = 0; // user's current score

    // Fragments and corresponding tags
    public NewGameFragment mNewGameFragment;
    public GamefieldFragment mGamefieldFragment;
    public MultiplayerFragment mMultiplayerFragment;

    public static final String FRAGMENT_NEWGAME = "newgame";
    public static final String FRAGMENT_GAMEFIELD = "gamefield";
    public static final String FRAGMENT_MULTIPLAYER = "multiplayer";

    public static boolean isContinueVisible = false;

    // The participants in the currently active game
    public ArrayList<Participant> mParticipants = null;

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
        mRoomManager = App.getRoomManager(this);
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
            }
        }
        switchToScreen(SCREEN_LOGGED);
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

    // are we already playing?
    public boolean mPlaying = false;

    // at least 2 players required for our game
    final static int MIN_PLAYERS = 2;

    // returns whether there are enough players to start the game
    public boolean shouldStartGame(Room room) {
        int connectedPlayers = 0;
        for (Participant p : room.getParticipants()) {
            if (p.isConnectedToRoom()) ++connectedPlayers;
        }
        return connectedPlayers >= MIN_PLAYERS;
    }

    // Returns whether the room is in a state where the game should be canceled.
    public boolean shouldCancelGame(Room room) {
        // TODO: Your game-specific cancellation logic here. For example, you might decide to
        // cancel the game if enough people have declined the invitation or left the room.
        // You can check a participant's status with Participant.getStatus().
        // (Also, your UI should have a Cancel button that cancels the game too)
        mMultiplayer = false;
        return false;
    }

    public void updateRoom(Room room) {
        if (room != null) {
            mParticipants = room.getParticipants();
        }
        if (mParticipants != null) {
        }
    }

    // Score of other participants. We update this as we receive their scores
    // from the network.
    public Map<String, Integer> mParticipantScore = new HashMap<String, Integer>();

    // Participants who sent us their final score.
    public Set<String> mFinishedParticipants = new HashSet<String>();

    // Broadcast my score to everybody else.
    public boolean broadcastScore(int finalScore) {

        if (!mMultiplayer || (!mYourMove && finalScore > -1)) {
            return false; // playing single-player mode
        }

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
    public void updatePeerScoresDisplay() {

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

        keepScreenOn();
        Log.d(TAG, "Room created, waiting for it to be ready...");
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
        if (invId != null) {
            // accept the invitation
            Log.d(TAG, "Accepting invitation: " + invId);
            RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
            roomConfigBuilder.setInvitationIdToAccept(invId);
            keepScreenOn();
            Games.RealTimeMultiplayer.join(mGoogleApiClient, roomConfigBuilder.build());
        } else {
            Log.d(TAG, "null:" + null);
        }
    }

    // create a RoomConfigBuilder that's appropriate for your implementation
    private RoomConfig.Builder makeBasicRoomConfigBuilder() {

        mRoomManager.init(this);
        return mRoomManager.getRoomConfig();
    }

    // Sets the flag to keep this screen on. It's recommended to do that during
    // the handshake when setting up a game, because if the screen turns off, the
    // game will be cancelled.
    public void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Clears the flag that keeps the screen on.
    void stopKeepingScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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

    @Override
    protected void onStart() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Log.w(TAG,
                    "GameHelper: client was already connected on onStart()");
        } else {
            Log.d(TAG, "Connecting client.");
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "**** got onStop");

        // if we're in a room, leave it.
        leaveRoom();

        // stop trying to keep the screen on
        stopKeepingScreenOn();

        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            switchToScreen(SCREEN_INITIAL);
        } else {
            switchToScreen(SCREEN_LOGGED);
        }
        super.onStop();
    }

    public static final int SCREEN_INITIAL = 0;
    public static final int SCREEN_LOGGED = 1;


    // Leave the room.
    void leaveRoom() {
        Log.d(TAG, "Leaving room.");
        stopKeepingScreenOn();
        if (mRoomId != null) {
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, mRoomManager, mRoomId);
            mRoomId = null;
            switchToScreen(SCREEN_INITIAL);
        } else {
            switchToScreen(SCREEN_INITIAL);
        }
    }

    public void switchToScreen(int screenId) {
        switch (screenId) {
            case SCREEN_INITIAL:
                //hide invite, accept
                break;
            case SCREEN_LOGGED:
                // show controls
                break;
        }
    }
}

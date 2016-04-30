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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;

import karataiev.dmytro.connectfour.R;
import karataiev.dmytro.connectfour.interfaces.OnGoogleApiChange;
import karataiev.dmytro.connectfour.gameutils.BaseGameUtils;

/**
 * Created by karataev on 4/27/16.
 */
public class MultiplayerManager implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MultiplayerManager.class.getSimpleName();

    final static int RC_SIGN_IN = 9001;

    private GoogleApiClient mGoogleApiClient;
    private Activity mContext;
    private OnGoogleApiChange onGoogleApiChange;

    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = false;
    private boolean mSignInClicked = false;

    public boolean ismSignInClicked() {
        return mSignInClicked;
    }

    public void setSignInClicked(boolean mSignInClicked) {
        this.mSignInClicked = mSignInClicked;
    }

    public boolean isResolvingConnectionFailure() {
        return mResolvingConnectionFailure;
    }

    public void setResolvingConnectionFailure(boolean mResolvingConnectionFailure) {
        this.mResolvingConnectionFailure = mResolvingConnectionFailure;
    }

    public boolean isAutoStartSignInFlow() {
        return mAutoStartSignInFlow;
    }

    public void setAutoStartSignInFlow(boolean mAutoStartSignInFlow) {
        this.mAutoStartSignInFlow = mAutoStartSignInFlow;
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public void init(Activity context, OnGoogleApiChange listener) {

        onGoogleApiChange = listener;

        mContext = context;

        // Create the Google Api Client with access to Games
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected() called. Sign in successful!");
        onGoogleApiChange.onConnectedApi(bundle);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended() called. Trying to reconnect.");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed() called, result: " + connectionResult);

        if (mResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed() ignoring connection failure; already resolving.");
            return;
        }

        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = BaseGameUtils.resolveConnectionFailure(mContext, mGoogleApiClient,
                    connectionResult, RC_SIGN_IN, mContext.getString(R.string.signin_other_error));
        }
    }

    public void clear() {
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.disconnect();
            }
            mGoogleApiClient = null;
        }
    }

}

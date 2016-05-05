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

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.common.SignInButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import karataiev.dmytro.connectfour.interfaces.OnFragmentInteraction;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteraction} interface
 * to handle interaction events.
 * Use the {@link MultiplayerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MultiplayerFragment extends Fragment {

    private static final String TAG = MultiplayerFragment.class.getSimpleName();

    @BindView(R.id.sign_in_button) SignInButton mSignInButton;
    @BindView(R.id.sign_out_button) Button mSignOutButton;
    @BindView(R.id.button_invitation) Button mInviteButton;

    @OnClick({R.id.sign_in_button,
            R.id.sign_out_button,
            R.id.button_invitation })
    public void onClick(View view) {
        mListener.onFragmentClick(view.getId());
    }

    private Unbinder mUnbinder;

    private OnFragmentInteraction mListener;

    public MultiplayerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment MultiplayerFragment.
     */
    public static MultiplayerFragment newInstance() {
        return new MultiplayerFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_multiplayer, container, false);

        mUnbinder = ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (((MainActivity) getActivity()).mGoogleApiClient.isConnected() ||
                ((MainActivity) getActivity()).mGoogleApiClient.isConnecting()) {
            showUi(true);
        } else {
            showUi(false);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteraction) {
            mListener = (OnFragmentInteraction) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement onMultiplayerFragmentInteraction");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    public void showUi(boolean connected) {
        if (this.isVisible()) {
            if (connected) {
                mInviteButton.setVisibility(View.VISIBLE);
                mSignInButton.setVisibility(View.GONE);
                mSignOutButton.setVisibility(View.VISIBLE);
            } else {
                mInviteButton.setVisibility(View.GONE);
                mSignOutButton.setVisibility(View.GONE);
                mSignInButton.setVisibility(View.VISIBLE);
            }
        }
    }
}

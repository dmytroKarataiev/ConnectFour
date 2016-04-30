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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import karataiev.dmytro.connectfour.interfaces.OnFragmentInteraction;

/**
 * Fragment to start a new game,
 * presents a screen with different buttons.
 */
public class NewGameFragment extends Fragment {

    private static final String TAG = NewGameFragment.class.getSimpleName();

    private OnFragmentInteraction mListener;

    @BindView(R.id.button_continue) Button mButtonContinue;

    @OnClick({ R.id.newGameScreen,
            R.id.vsRandom,
            R.id.vsPlayer,
            R.id.vsAdvanced,
            R.id.vsHard,
            R.id.button_multiplayer,
            R.id.button_continue })
    public void startGame(View view) {
        mListener.onFragmentClick(view.getId());
    }

    private Unbinder mUnbinder;

    public NewGameFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment NewGameFragment.
     */
    public static NewGameFragment newInstance() {
        return new NewGameFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_new_game, container, false);

        mUnbinder = ButterKnife.bind(this, rootView);

        mButtonContinue.setVisibility(MainActivity.isContinueVisible ? View.VISIBLE : View.GONE);

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteraction) {
            mListener = (OnFragmentInteraction) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnNewGameFragmentInteraction");
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

}

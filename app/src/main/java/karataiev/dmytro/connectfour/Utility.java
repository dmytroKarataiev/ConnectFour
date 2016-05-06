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
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.preference.PreferenceManager;
import android.view.Display;

/**
 * Class with additional methods
 * Created by karataev on 1/27/16.
 */
public class Utility {

    /**
     * Method to get player mPlayerName from SharedPreferences
     * @param context from which call is being made
     * @return String mPlayerName of a player
     */
    public static String getPlayerNameFromPref(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_key), "Player");
    }

    /**
     * Returns numbed of played games
     * @param context from which call was made
     * @return number of played games since the installation
     */
    public static int getPlayedGames(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(context.getString(R.string.sharedprefs_played_games), 0);
    }

    /**
     * Returns achievement according to the progress of the game
     * @param context from which call was made
     * @param playedGames number of played games since the beginning
     * @return correct id of an achievement
     */
    public static String getAchievement(Context context, int playedGames) {

        if (playedGames >= 10 && playedGames < 100) {
            return context.getString(R.string.achievement_10_played_games);
        } else if (playedGames >= 100 && playedGames < 200) {
            return context.getString(R.string.achievement_100_played_games);
        } else if (playedGames >= 200 && playedGames < 500) {
            return context.getString(R.string.achievement_200_played_games);
        } else if (playedGames >= 500 && playedGames < 1000) {
            return context.getString(R.string.achievement_500_played_games);
        } else if (playedGames >= 1000) {
            return context.getString(R.string.achievement_1000_played_games);
        }

        return null;
    }

    /**
     * Increments number of played games
     * @param context from which call was made
     */
    public static void incrementPlayedGames(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int playedToNow = sharedPreferences.getInt(context.getString(R.string.sharedprefs_played_games), 0);
        sharedPreferences.edit().putInt(context.getString(R.string.sharedprefs_played_games), ++playedToNow).apply();
    }

    /**
     * Returns a move according to the screen resolution
     * @param activity from which call was made
     * @return place to make a move
     */
    public static int getMove(Activity activity, float eventX) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        return (int) eventX / (size.x / 7);
    }
}

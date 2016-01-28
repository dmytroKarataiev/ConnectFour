package karataiev.dmytro.connectfour;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Class with additional methods
 * Created by karataev on 1/27/16.
 */
public class Utility {

    /**
     * Method to get player name from SharedPreferences
     * @param context from which call is being made
     * @return String name of a player
     */
    public static String getPlayerNameFromPref(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_key), "Player");
    }
}

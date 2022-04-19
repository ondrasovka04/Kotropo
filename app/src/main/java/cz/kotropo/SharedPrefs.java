package cz.kotropo;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

/**
 * Utilities for shared preferences
 */
public class SharedPrefs {

    public static final String URL = "url";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String GROUP_AJ = "skupina_aj";
    public static final String GROUP_NJ = "skupina_nj";
    public static final String SCHOOL = "school";
    public static final String CLASS = "class";
    public static final String TOKEN = "token";
    public static final String DB_USERNAME = "KopacTrousilPopelka";
    public static final String DB_PASSWORD = "KoTroPoGyArab2021/2022";
    public static boolean UKRAINE = false;

    public static String getString(Context context, String key) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, "");
    }

    public static void setString(Context context, String key, String value) {
        SharedPreferences.Editor preferenceManager = PreferenceManager.getDefaultSharedPreferences(context).edit();
        preferenceManager.putString(key, value);
        preferenceManager.apply();
    }
}

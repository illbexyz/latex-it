package me.albertonicoletti.latex;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import me.albertonicoletti.latex.activities.SettingsActivity;

/**
 * Created by alberto on 16/02/16.
 */
public class PreferenceHelper {

    public static String getOutputFolder(Context ctx) {
        SharedPreferences s = PreferenceManager.getDefaultSharedPreferences(ctx);
        return s.getString(SettingsActivity.OUTPUT_FOLDER, "");
    }

    public static String getImageFolder(Context ctx) {
        SharedPreferences s = PreferenceManager.getDefaultSharedPreferences(ctx);
        return s.getString(SettingsActivity.IMAGES_FOLDER, "");
    }

}

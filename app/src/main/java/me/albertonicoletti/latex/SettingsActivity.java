package me.albertonicoletti.latex;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;

/**
 * Created by alberto on 09/08/15.
 */
public class SettingsActivity extends Activity {

    public static final String IMAGES_FOLDER = "images_folder";
    public static final String OUTPUT_FOLDER = "output_folder";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);


    }
}

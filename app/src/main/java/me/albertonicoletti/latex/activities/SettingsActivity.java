package me.albertonicoletti.latex.activities;

import android.app.Activity;
import android.os.Bundle;

import me.albertonicoletti.latex.SettingsFragment;
import me.albertonicoletti.utils.FilesUtils;

/**
 * Settings activity.
 *
 * @author Alberto Nicoletti    albyx.n@gmail.com    https://github.com/albyxyz
 */
public class SettingsActivity extends Activity  {

    public static final String FONT_SIZE = "font_size";
    public static final String IMAGES_FOLDER = "images_folder";
    public static final String OUTPUT_FOLDER = "output_folder";
    public static final String EXE = "executable";
    public static final String TAB_SIZE = "tab_size";

    public static final String DEFAULT_IMAGES_FOLDER =
            FilesUtils.getDocumentsDir().getPath() + "/Images/";

    public static final String DEFAULT_OUTPUT_FOLDER =
            FilesUtils.getDocumentsDir().getPath() + "/LatexOutput/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

    }

}

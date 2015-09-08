package me.albertonicoletti.latex.activities;

import android.app.Activity;
import android.os.Bundle;

import me.albertonicoletti.latex.R;

/**
 * Activity used to show the licenses of the open source libraries used.
 *
 * @author Alberto Nicoletti    albyx.n@gmail.com    https://github.com/albyxyz
 */
public class OpenSourceLicencesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_source);
    }

}

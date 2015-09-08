package me.albertonicoletti.latex.activities;

import android.app.Activity;
import android.os.Bundle;

import me.albertonicoletti.latex.R;

/**
 * Activity used to show information about me and the project.
 *
 * @author Alberto Nicoletti    albyx.n@gmail.com    https://github.com/albyxyz
 */
public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

}

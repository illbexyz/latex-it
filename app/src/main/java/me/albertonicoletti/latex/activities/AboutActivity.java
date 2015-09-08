package me.albertonicoletti.latex.activities;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

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
        TextView profile = (TextView) findViewById(R.id.github_profile);
        TextView editor = (TextView) findViewById(R.id.github_editor);
        TextView server = (TextView) findViewById(R.id.github_server);
        profile.setMovementMethod(LinkMovementMethod.getInstance());
        editor.setMovementMethod(LinkMovementMethod.getInstance());
        server.setMovementMethod(LinkMovementMethod.getInstance());
    }

}

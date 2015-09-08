package me.albertonicoletti.latex;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;

import java.io.File;

/**
 * Provides utilities for showing dialogs.
 *
 * @author Alberto Nicoletti    albyx.n@gmail.com    https://github.com/albyxyz
 */
public class DialogsUtil {

    /**
     * Shows a dialog asking which name to use for renaming a file.
     * @param activity Activity
     * @param file File to rename
     */
    public static void showRenameDialog(Activity activity, File file) {
        Bundle args = new Bundle();
        args.putString("path", file.getPath());
        DialogFragment renameDialog = new RenameDialog();
        renameDialog.setArguments(args);
        renameDialog.show(activity.getFragmentManager(), "rename_dialog");
    }

}

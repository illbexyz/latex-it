package me.albertonicoletti.latex;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;

import java.io.File;

/**
 * Created by Alberto on 05/08/2015.
 */
public class DialogsUtil {

    public static void showRenameDialog(Activity activity, File file) {
        Bundle args = new Bundle();
        args.putString("path", file.getPath());
        DialogFragment renameDialog = new RenameDialog();
        renameDialog.setArguments(args);
        renameDialog.show(activity.getFragmentManager(), "rename_dialog");
    }

}

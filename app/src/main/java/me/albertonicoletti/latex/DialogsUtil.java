package me.albertonicoletti.latex;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
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
     * @param activity Activity where to show the dialog
     * @param file File to rename
     */
    public static void showRenameDialog(Activity activity, File file) {
        Bundle args = new Bundle();
        args.putString("path", file.getPath());
        DialogFragment renameDialog = new RenameDialog();
        renameDialog.setArguments(args);
        renameDialog.show(activity.getFragmentManager(), "rename_dialog");
    }

    /**
     * Shows a generic confirm dialog.
     * @param activity Activity where to show the dialog
     * @param title Dialog's title
     * @param message Dialogs's message
     * @param no Negative response listener
     * @param ok Positive response listener
     */
    public static Dialog showConfirmDialog(Activity activity,
                                           String title,
                                           String message,
                                           DialogInterface.OnClickListener no,
                                           DialogInterface.OnClickListener ok) {

        final AlertDialog dialog = new AlertDialog.Builder(activity).create();
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", no);
        if(ok != null) {
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", ok);
        }
        dialog.show();
        return dialog;
    }

}

package me.albertonicoletti.latex;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import java.io.File;

/**
 * Dialog showing an EditText for renaming a file.
 */
public class RenameDialog extends DialogFragment {

    public interface RenameDialogListener {
        void onRenameDialogConfirmClick(DialogFragment dialog, String path, String newFilename);
    }

    /** Click listener */
    RenameDialogListener mListener;
    /** File path */
    String path;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (RenameDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement RenameDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        path = getArguments().getString("path");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View view = inflater.inflate(R.layout.rename_dialog, null);
        builder.setView(view)
                // On positive answer will execute the listener's onClick
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText editText = (EditText) view.findViewById(R.id.rename_document_title);
                        String newFilename = editText.getText().toString();
                        mListener.onRenameDialogConfirmClick(RenameDialog.this, path, newFilename);
                    }
                })
                // On negative answer it will close the dialog
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        RenameDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

}

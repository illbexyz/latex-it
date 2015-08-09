package me.albertonicoletti.latex;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Dialog showing actions to do on the documents
 */
public class DocumentDialog extends DialogFragment {

    public interface DocumentDialogListener {
        void onDialogDeleteClick(DialogFragment dialog, String path);
        void onDialogRenameClick(DialogFragment dialog, String path);
    }

    /** Object listening to the dialog's events */
    DocumentDialogListener mListener;

    /** Document's filename */
    String path;

    @Override
    public void onAttach(Activity activity) {
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (DocumentDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement DocumentDialogListener");
        }
        super.onAttach(activity);
    }

    /**
     * Creates the dialog. Bundle must contain a "filename" string.
     * @param savedInstanceState Must contain a "filename" string.
     * @return
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        path = getArguments().getString("filepath");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.document_dialog_title)
                .setItems(R.array.document_dialog, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        switch(which) {
                            case 0:
                                mListener.onDialogRenameClick(DocumentDialog.this, path);
                            case 1:
                                mListener.onDialogDeleteClick(DocumentDialog.this, path);
                        }
                    }
                });
        return builder.create();
    }

}

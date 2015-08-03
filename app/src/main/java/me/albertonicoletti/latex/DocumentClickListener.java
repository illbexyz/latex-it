package me.albertonicoletti.latex;

import android.app.Activity;
import android.view.View;

/**
 * Listener for onClick and onLongClick events over a Document' view
 */
public class DocumentClickListener implements View.OnClickListener, View.OnLongClickListener {

    public interface DocumentClickInterface {
        void onDocumentClickListener(View v);
        void onDocumentLongClickListener(View v);
    }

    private DocumentClickInterface mListener;

    /**
     * The listener can be constructed only by an Activity that implements the DocumentClickInterface
     * @param activity An activity implementing DocumentClickInterface
     */
    public DocumentClickListener(Activity activity){
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (DocumentClickInterface) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    /**
     * On document click event
     * @param v Document's view
     */
    @Override
    public void onClick(View v) {
        mListener.onDocumentClickListener(v);
    }

    /**
     * On document long click event
     * @param v Document's view
     * @return true
     */
    @Override
    public boolean onLongClick(View v) {
        mListener.onDocumentLongClickListener(v);
        return true;
    }

}

package me.albertonicoletti.latex;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.util.LinkedList;


public class MainActivity extends Activity
        implements  DocumentDialog.DocumentDialogListener,
                    RenameDialog.RenameDialogListener,
                    DocumentClickListener.DocumentClickInterface {

    private RecyclerView documentsListView;
    private DocumentsAdapter documentsAdapter;
    private DocumentClickListener documentClickListener;
    private RecyclerView.LayoutManager documentsLayoutManager;
    private FilesManager filesManager;
    private LinkedList<Document> documents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        filesManager = new FilesManager();
        //documents = filesManager.getExistingFiles();

        documentsListView = (RecyclerView) findViewById(R.id.documents_list);
        // Every elements of the list will have a fixed size
        documentsListView.setHasFixedSize(true);
        // Sets the layout manager
        documentsLayoutManager = new LinearLayoutManager(this);
        documentsListView.setLayoutManager(documentsLayoutManager);
        // Sets an adapter
        documentClickListener = new DocumentClickListener(this);
        documentsAdapter = new DocumentsAdapter(documents, documentClickListener);
        documentsListView.setAdapter(documentsAdapter);
    }

    @Override
    protected void onResume(){
        super.onResume();
        this.refreshDocuments();
    }

    /**
     * Refresh the dataset and the view
     */
    private void refreshDocuments() {
        documents = filesManager.getExistingFiles();
        documentsAdapter.refresh(documents);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * On new document click (MenuBar)
     * @param v
     */
    public void newDocumentClick(MenuItem v){
        // Launch the editor creating a new file
        this.launchEditor();
    }

    /**
     * Creates a new file and launch the editor activity using the new file
     */
    private void launchEditor() {
        File file = filesManager.newFile();
        this.launchEditor(Uri.fromFile(file));
    }

    /**
     * Starts the editor activity using an existing file
     * @param fileUri File to open's uri
     */
    private void launchEditor(Uri fileUri){
        Intent editorIntent = new Intent(this, EditorActivity.class);
        editorIntent.setData(fileUri);
        Log.v("NEW_ACTIVITY", "Starting editor activity with " + fileUri.toString());
        startActivity(editorIntent);
    }

    /**
     * Triggered by selecting to rename a file
     * @param dialog
     * @param filename Filename to rename
     */
    @Override
    public void onDialogRenameClick(DialogFragment dialog, String filename) {
        Bundle args = new Bundle();
        args.putString("old_filename", filename);
        DialogFragment renameDialog = new RenameDialog();
        renameDialog.setArguments(args);
        renameDialog.show(getFragmentManager(), "rename_dialog");
    }

    /**
     * Triggered by confirming to rename a document
     * @param dialog
     * @param oldFilename Old filename
     * @param newFilename New filename
     */
    @Override
    public void onRenameDialogConfirmClick(DialogFragment dialog, String oldFilename, String newFilename) {
        // Rename the document
        filesManager.renameDocument(oldFilename, newFilename);
        // Refresh the view
        this.refreshDocuments();
    }

    /**
     * Triggered by tapping a document
     * @param v
     */
    @Override
    public void onDocumentClickListener(View v) {
        TextView m = (TextView) v.findViewById(R.id.documentTitle);
        Uri uri = filesManager.getUriFromFilename(m.getText().toString());
        //
        launchEditor(uri);
    }

    /**
     * Triggered by long-clicking a document
     * @param v
     */
    @Override
    public void onDocumentLongClickListener(View v) {
        TextView m = (TextView) v.findViewById(R.id.documentTitle);
        Bundle args = new Bundle();
        // Adds a "filename" parameter containing the filename
        args.putString("filename", m.getText().toString());
        // Creates the dialog and adds the parameter
        DialogFragment dialog = new DocumentDialog();
        dialog.setArguments(args);
        // Opens thee dialog
        dialog.show(getFragmentManager(), "document_dialog");
    }

}

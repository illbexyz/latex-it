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


public class FileChooserActivity extends Activity
        implements  DocumentDialog.DocumentDialogListener,
                    RenameDialog.RenameDialogListener,
                    DocumentClickListener.DocumentClickInterface {

    private RecyclerView documentsListView;
    private DocumentsAdapter documentsAdapter;
    private DocumentClickListener documentClickListener;
    private RecyclerView.LayoutManager documentsLayoutManager;
    private LinkedList<File> directories;
    private LinkedList<File> files;
    private File currentDirectory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_chooser);
        currentDirectory = FilesManager.getDocumentsDir();
        documentsListView = (RecyclerView) findViewById(R.id.file_chooser_files);
        // Every elements of the list will have a fixed size
        documentsListView.setHasFixedSize(true);
        // Sets the layout manager
        documentsLayoutManager = new LinearLayoutManager(this);
        documentsListView.setLayoutManager(documentsLayoutManager);
        // Sets an adapter
        documentClickListener = new DocumentClickListener(this);
        documentsAdapter = new DocumentsAdapter(files, documentClickListener, DocumentsAdapter.FILE_CHOOSER);
        documentsListView.setAdapter(documentsAdapter);
        setTitle("Choose a file");
    }

    @Override
    protected void onResume(){
        super.onResume();
        this.refreshDocuments(currentDirectory);
    }

    /**
     * Refresh the dataset and the view
     */
    private void refreshDocuments(File directory) {
        this.currentDirectory = directory;
        files = FilesManager.getExistingFiles(directory);
        directories = FilesManager.getDirectories(directory);
        directories.addAll(files);
        documentsAdapter.refresh(directories);
    }

    private void parentDirectory(){
        refreshDocuments(currentDirectory.getParentFile());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_file_chooser, menu);
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
     * @param path File path
     * @param newFilename New filename
     */
    @Override
    public void onRenameDialogConfirmClick(DialogFragment dialog, String path, String newFilename) {
        // Rename the document
        FilesManager.renameFile(new File(path), newFilename);
        // Refresh the view
        this.refreshDocuments(currentDirectory);
    }

    @Override
    public void onBackPressed() {
        if(currentDirectory.getParentFile() != null) {
            parentDirectory();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Triggered by tapping a document
     * @param v
     */
    @Override
    public void onDocumentClickListener(View v) {
        TextView m = (TextView) v.findViewById(R.id.documentTitle);
        Uri uri = FilesManager.getUriFromFilename(currentDirectory, m.getText().toString());
        File file = new File(uri.getPath());
        if(file.isDirectory()){
            refreshDocuments(file);
        } else {
            Intent intent = new Intent();
            intent.setData(uri);
            setResult(1, intent);
            finish();
        }
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

    public void onBackClick(MenuItem item) {
        parentDirectory();
    }
}

package me.albertonicoletti.latex;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Stack;


public class FileChooserActivity extends Activity
        implements  DocumentOptionsDialog.DocumentDialogListener,
                    RenameDialog.RenameDialogListener,
                    DocumentClickListener.DocumentClickInterface,
                    Comparator<File> {

    private RecyclerView documentsListView;
    private DocumentsAdapter documentsAdapter;
    private DocumentClickListener documentClickListener;
    private RecyclerView.LayoutManager documentsLayoutManager;

    private LinkedList<File> directories;
    private LinkedList<File> files;
    private File currentDirectory;

    String rootDirectoryPath = Environment.getExternalStorageDirectory().getPath();
    private long backPressed = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_chooser);
        currentDirectory = FilesUtils.getDocumentsDir();
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
        setTitle(getString(R.string.choose_file));
    }

    @Override
    protected void onResume(){
        super.onResume();
        refreshPathSubtitle();
        this.refreshDocuments(currentDirectory);
    }

    private void refreshPathSubtitle(){
        TextView currentPathView = (TextView) findViewById(R.id.current_path);
        Stack<String> stack = new Stack<>();
        File dir = currentDirectory;
        stack.push(dir.getName());
        if(!dir.getPath().equals(rootDirectoryPath)) {
            while (!dir.getParent().equals(rootDirectoryPath)) {
                dir = dir.getParentFile();
                stack.push(dir.getName());
            }
            stack.push(dir.getParentFile().getName());
        }
        String title = stack.pop();
        while(!stack.empty()){
            title += "/" + stack.pop();
        }
        currentPathView.setText(title);
    }

    /**
     * Refresh the dataset and the view
     */
    private void refreshDocuments(File directory) {
        this.currentDirectory = directory;
        files = FilesUtils.getTexDocuments(directory);
        Collections.sort(files, this);
        directories = FilesUtils.getDirectories(directory);
        Collections.sort(directories, this);
        directories.addAll(files);
        documentsAdapter.refresh(directories);
        refreshPathSubtitle();
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

    @Override
    public void onDialogDeleteClick(DialogFragment dialog, String path) {
        FilesUtils.deleteFile(new File(path));
        refreshDocuments(currentDirectory);
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
        FilesUtils.renameFile(new File(path), newFilename);
        // Refresh the view
        this.refreshDocuments(currentDirectory);
    }

    @Override
    public void onBackPressed() {
        if(!currentDirectory.getPath().equals(rootDirectoryPath)) {
            parentDirectory();
        } else {
            if (backPressed + 2000 > System.currentTimeMillis()){
                super.onBackPressed();
            } else {
                Toast.makeText(getBaseContext(), "Press once again to exit!", Toast.LENGTH_SHORT).show();
                backPressed = System.currentTimeMillis();
            }
        }
    }

    /**
     * Triggered by tapping a document
     * @param v
     */
    @Override
    public void onDocumentClickListener(View v) {
        TextView m = (TextView) v.findViewById(R.id.documentTitle);
        Uri uri = FilesUtils.getUriFromFilename(currentDirectory, m.getText().toString());
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
        String filepath = currentDirectory.getPath() + "/" + m.getText().toString();
        // Adds a "filename" parameter containing the filename
        args.putString("filepath", filepath);
        // Creates the dialog and adds the parameter
        DialogFragment dialog = new DocumentOptionsDialog();
        dialog.setArguments(args);
        // Opens thee dialog
        dialog.show(getFragmentManager(), "file_chooser_longclick_dialog");
    }

    public void onBackClick(MenuItem item) {
        parentDirectory();
    }

    @Override
    public int compare(File lhs, File rhs) {
        return lhs.getName().compareToIgnoreCase(rhs.getName());
    }
}

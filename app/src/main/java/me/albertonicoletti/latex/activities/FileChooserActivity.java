package me.albertonicoletti.latex.activities;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

import me.albertonicoletti.latex.DocumentClickListener;
import me.albertonicoletti.latex.DocumentOptionsDialog;
import me.albertonicoletti.latex.DocumentsAdapter;
import me.albertonicoletti.latex.R;
import me.albertonicoletti.latex.RenameDialog;
import me.albertonicoletti.utils.FilesUtils;

/**
 * Activity used to pick a file to open.
 *
 * @author Alberto Nicoletti    albyx.n@gmail.com    https://github.com/albyxyz
 */

public class FileChooserActivity extends Activity
        implements DocumentOptionsDialog.DocumentDialogListener,
        RenameDialog.RenameDialogListener,
        DocumentClickListener.DocumentClickInterface {

    /** Files' adapter */
    private DocumentsAdapter documentsAdapter;
    /** Current showing directory */
    private File currentDirectory;
    /** The files in the current directory */
    private LinkedList<File> files;
    /** Root directory */
    private String rootDirectoryPath = Environment.getExternalStorageDirectory().getPath();
    /** Used to remember when back button is pressed */
    private long backPressed = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_chooser);
        currentDirectory = FilesUtils.getDocumentsDir();
        RecyclerView documentsListView = (RecyclerView) findViewById(R.id.file_chooser_files);
        // Every elements of the list will have a fixed size
        documentsListView.setHasFixedSize(true);
        // Sets the layout manager
        RecyclerView.LayoutManager documentsLayoutManager = new LinearLayoutManager(this);
        documentsListView.setLayoutManager(documentsLayoutManager);
        // Sets an adapter
        DocumentClickListener documentClickListener = new DocumentClickListener(this);
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

    /**
     * Refresh the activity subtitle, showing the current directory path
     */
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
     * Refresh the dataset and the view.
     */
    private void refreshDocuments(File directory) {
        this.currentDirectory = directory;
        files = FilesUtils.getTexDocuments(directory);
        Collections.sort(files, new FileComparator());
        LinkedList<File> directories = FilesUtils.getDirectories(directory);
        Collections.sort(directories, new FileComparator());
        directories.addAll(files);
        documentsAdapter.refresh(directories);
        refreshPathSubtitle();
    }

    /**
     * Routine to show the parent of the current directory.
     */
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
     * Triggered by selection to delete a file in the dialog.
     * @param dialog Calling dialog
     * @param path File path
     */
    @Override
    public void onDialogRemoveClick(DialogFragment dialog, String path) {
        FilesUtils.deleteFile(new File(path));
        refreshDocuments(currentDirectory);
    }

    /**
     * Triggered by selecting to rename a file in the dialog.
     * @param dialog Calling dialog
     * @param path File path to rename
     */
    @Override
    public void onDialogRenameClick(DialogFragment dialog, String path) {
        Bundle args = new Bundle();
        args.putString("old_filename", path);
        DialogFragment renameDialog = new RenameDialog();
        renameDialog.setArguments(args);
        renameDialog.show(getFragmentManager(), "rename_dialog");
    }

    /**
     * Triggered by confirming to rename a document
     * @param dialog Dialog
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
     * Triggered by tapping a document.
     * Will send the selected file to the calling activity.
     * @param v View
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
     * Triggered by long-clicking a document.
     * Will show a dialog showing the possible actions on the file.
     * @param v View
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

    /**
     * Class used to compare 2 files.
     */
    public class FileComparator implements Comparator<File> {
        @Override
        public int compare(File lhs, File rhs) {
            return lhs.getName().compareToIgnoreCase(rhs.getName());
        }
    }

}

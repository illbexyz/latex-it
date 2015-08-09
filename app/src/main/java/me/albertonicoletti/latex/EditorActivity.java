package me.albertonicoletti.latex;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.io.File;
import java.util.LinkedList;


public class EditorActivity extends Activity implements DocumentClickListener.DocumentClickInterface,
                                                        RenameDialog.RenameDialogListener,
                                                        DocumentDialog.DocumentDialogListener {

    /** Left Drawer Layout */
    private DrawerLayout mDrawerLayout;
    /** Recycler View containing the open documents */
    private RecyclerView mDrawerList;
    /** Recycler View layout manager */
    private RecyclerView.LayoutManager documentsLayoutManager;
    /** Adapter for the Recycler View */
    private DocumentsAdapter documentsAdapter;
    /** A custom EditText */
    private Editor editor;
    /** A custom scrollView, used to intercept when a scroll is stopped */
    private VerticalScrollView scrollView;
    /** List of the open documents */
    private LinkedList<File> documents = new LinkedList<>();
    /** List of the images */
    //private LinkedList<LinkedList<File>> images = new LinkedList<>();
    /** The document the editor is showing */
    private File document;

    /** Set to true if the text has been modified and not saved */
    private boolean textModified = false;

    private boolean initialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Log.v("ACTIVITY", "Editor activity started");
        initDrawer();
        initEditor();

        final Button button = (Button) findViewById(R.id.maths_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(EditorActivity.this, button);
                popup.getMenuInflater().inflate(R.menu.menu_maths, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        return false;
                    }
                });
                popup.show();
            }
        });

        Uri fileUri = getIntent().getData();
        // If a URI is passed
        if (fileUri != null) {
            document = new File(fileUri.getPath());
        } else {
            // No URI is passed
            documents = DataPersistenceUtil.readSavedOpenFiles(getApplicationContext());
            if(documents.isEmpty()) {
                // If there's no open document it opens a new untitled file
                document = FilesUtils.newFile();
            } else {
                // Else it gets the first open document
                document = documents.get(0);
            }
        }

        openDocumentInEditor(document);
    }

    private void initDrawer(){
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (RecyclerView) findViewById(R.id.left_drawer);
        mDrawerList.setHasFixedSize(true);
        // Sets the layout manager
        documentsLayoutManager = new LinearLayoutManager(this);
        mDrawerList.setLayoutManager(documentsLayoutManager);
        documentsAdapter = new DocumentsAdapter(documents,
                new DocumentClickListener(this),
                DocumentsAdapter.DRAWER);
        mDrawerList.setAdapter(documentsAdapter);
        mDrawerLayout.openDrawer(Gravity.LEFT);
    }

    private void initEditor(){
        scrollView = (VerticalScrollView) findViewById(R.id.editor_scroll_view);
        editor = (Editor) findViewById(R.id.editor);

        editor.setVerticalScrollBarEnabled(true);
        editor.setMovementMethod(new ScrollingMovementMethod());
        scrollView.setScrollStoppedListener(new VerticalScrollView.ScrollStoppedListener() {
            @Override
            public void onStopped() {
                highlightEditor();
            }
        });
        editor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (initialized) {
                    highlightEditor();
                    textModified = true;
                }
            }
        });
        initialized = true;
    }

    @Override
    protected void onStop() {
        DataPersistenceUtil.saveFilesPath(getApplicationContext(), documents);
        super.onStop();
    }

    private void highlightEditor(){
        int scrollY = scrollView.getScrollY();
        if (scrollY == -1) scrollY = 0;
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int start = Math.max(0, editor.getOffsetForPosition(0, scrollY));
        int end = Math.max(0, editor.getOffsetForPosition(size.x, scrollY + size.y));
        editor.highlightText(start, end);
    }

    private void changeFile(String path){
        File file = new File(path);
        openDocumentInEditor(file);
    }

    private void openDocumentInEditor(File document){
        this.document = document;
        String fileContent = FilesUtils.readTextFile(document);

        if(!documents.contains(document)){
            documents.add(document);
        }
        documentsAdapter.refresh(documents);
        setTitle(document.getName());
        editor.setText(fileContent);
        mDrawerLayout.closeDrawer(Gravity.LEFT);
    }

    @Override
    public void onBackPressed() {
        if(textModified){
            AlertDialog dialog = new AlertDialog.Builder(this).create();
            dialog.setTitle("Unsaved File");
            dialog.setMessage("You have unsaved changes in your file.\nDo you want to save them?");
            dialog.setCancelable(false);
            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    EditorActivity.this.finish();
                }
            });
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    saveFile(document);
                    EditorActivity.this.finish();
                }
            });
            dialog.show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        highlightEditor();
    }


    /**
     * Routine to save a specific document
     */
    private void saveFile(File document){
        FilesUtils.writeFile(document, editor.getTextString());
        textModified = false;
    }

    /**
     * Routine to update the dataset
     */
    private void refreshTitleAndDrawer() {
        setTitle(this.document.getName());
        documentsAdapter.refresh(documents);
    }

    private void generatePDF(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultOutput = sharedPref.getString(SettingsActivity.OUTPUT_FOLDER, "");

        /*
        RequestParams params = new RequestParams();
        params.put("file", editor.getTextString());

        LatexNetClient.post("latex", params, new FileAsyncHttpResponseHandler(this) {
            @Override
            public void onFailure(int i, Header[] headers, Throwable throwable, File file) {
                Log.v("WE", ":(");
            }

            @Override
            public void onSuccess(int i, Header[] headers, File file) {

                byte[] bytes = FilesUtils.readBinaryFile(file);

                File pdf = FilesUtils.newFile("nuovo.pdf");
                FilesUtils.writeBinaryFile(pdf, bytes);

                Intent pdfIntent = new Intent();
                pdfIntent.setAction(Intent.ACTION_VIEW);
                pdfIntent.setDataAndType(Uri.fromFile(pdf), "application/pdf");

                if (pdfIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(pdfIntent);
                }
            }
        });*/
    }

    public void onOpenClick(View view) {
        //Intent intent = new Intent(this, FileChooserActivity.class);
        //startActivityForResult(intent, 1);
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        startActivityForResult(intent, 1);
    }

    public void onSymbolClick(View view) {
        Button button = (Button) view;
        int selection = Math.max(0, editor.getSelectionStart());
        String symbol = button.getText().toString();
        editor.getText().insert(selection, symbol);
        editor.setSelection(selection + 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data != null) {
            if(document.getName().contains("untitled") && editor.getTextString().length() == 0){
                documents.remove(document);
            }
            Uri fileUri = data.getData();

            openDocumentInEditor(new File(fileUri.getPath()));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.action_save:
                saveCurrentDocument();
                break;
            case R.id.action_pdf:
                generatePDF();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDocumentClickListener(View v) {
        TextView textView = (TextView) v.findViewById(R.id.drawer_file_path);
        String filePath = textView.getText().toString();
        changeFile(filePath);
        mDrawerLayout.closeDrawer(Gravity.LEFT);
    }

    @Override
    public void onDocumentLongClickListener(View v) {
        TextView m = (TextView) v.findViewById(R.id.drawer_file_path);
        Bundle args = new Bundle();
        // Adds a "filename" parameter containing the filename
        args.putString("filepath", m.getText().toString());
        // Creates the dialog and adds the parameter
        DialogFragment dialog = new DocumentDialog();
        dialog.setArguments(args);
        // Opens thee dialog
        dialog.show(getFragmentManager(), "document_dialog");
    }

    public void onNewFileClick(View view) {
        File file = FilesUtils.newFile();
        openDocumentInEditor(file);
    }

    private void saveCurrentDocument(){
        if(!document.exists()){
            DialogsUtil.showRenameDialog(this, document);
        } else {
            saveFile(document);
        }
    }

    @Override
    public void onRenameDialogConfirmClick(DialogFragment dialog, String path, String newFilename) {
        // Recreate the document
        File newDocument = new File(path);
        // Save it on disk
        FilesUtils.saveFileOnDisk(newDocument.getParentFile(), newDocument);
        // Recreate it to change the name (the file won't change his name otherwise)
        newDocument = new File(FilesUtils.renameFile(newDocument, newFilename).getPath());
        // Remove the old document from the dataset and insert the new one in the same position
        int oldDocumentIndex = documents.indexOf(this.document);
        documents.remove(this.document);
        documents.add(oldDocumentIndex, newDocument);
        this.document = newDocument;
        // Update the title and drawer
        refreshTitleAndDrawer();
    }

    @Override
    public void onDialogDeleteClick(DialogFragment dialog, String path) {
        File file = new File(path);
        documents.remove(file);
        if(file == document){
            if(documents.size() > 0){
                document = documents.get(0);
            } else {
                document = FilesUtils.newFile();
            }
        }
        refreshTitleAndDrawer();
    }

    @Override
    public void onDialogRenameClick(DialogFragment dialog, String filename) {

    }
}

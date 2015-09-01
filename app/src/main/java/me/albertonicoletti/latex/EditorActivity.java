package me.albertonicoletti.latex;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class EditorActivity extends Activity implements DocumentClickListener.DocumentClickInterface,
                                                        RenameDialog.RenameDialogListener,
                                                        DocumentOptionsDialog.DocumentDialogListener {

    private ActionBarDrawerToggle mDrawerToggle;
    /** Left Drawer Layout */
    private DrawerLayout mDrawerLayout;
    /** Recycler View containing the open documents */
    private RecyclerView mDrawerList;
    /** Recycler View layout manager */
    private RecyclerView.LayoutManager documentsLayoutManager;
    /** Adapter for the Recycler View */
    private DocumentsAdapter documentsAdapter;
    /** A custom EditText */
    private LatexEditor editor;
    /** A custom scrollView, used to intercept when a scroll is stopped */
    private VerticalScrollView scrollView;
    /** List of the open documents */
    private LinkedList<Document> documents = new LinkedList<>();
    /** List of the images */
    //private LinkedList<LinkedList<File>> images = new LinkedList<>();
    /** The document the editor is showing */
    private Document document;

    private PopupWindow popupWindow;

    private boolean initialized = false;

    private long backPressed = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Log.v("ACTIVITY", "Editor activity started");
        initPreferences();
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
                        insertSymbol(menuItem.getTitle().toString());
                        return false;
                    }
                });
                popup.show();
            }
        });

        Uri fileUri = getIntent().getData();
        // If a URI is passed
        if (fileUri != null) {
            document = new Document(fileUri.getPath());
        } else {
            // No URI is passed
            documents = DataPersistenceUtil.readSavedOpenFiles(getApplicationContext());
            if(documents.isEmpty()) {
                // If there's no open document it opens a new untitled file
                document = new Document(FilesUtils.newFile());
            } else {
                // Else it gets the first open document
                for(Document d : documents){
                    if(d.isOpen()){
                        document = d;
                    }
                }
                if(document == null) {
                    document = documents.get(0);
                }
            }
        }

        openDocumentInEditor(document);
    }

    private void initPreferences(){
        String outputPath = FilesUtils.getDocumentsDir().getPath() + "/LatexOutput/";
        String imagesPath = FilesUtils.getDocumentsDir().getPath() + "/Images/";

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefsEdit = prefs.edit();
        String currentOutputFolder = prefs.getString(SettingsActivity.OUTPUT_FOLDER, null);
        if(currentOutputFolder == null) {
            prefsEdit.putString(SettingsActivity.OUTPUT_FOLDER, outputPath);
        }
        String currentImageFolder = prefs.getString(SettingsActivity.IMAGES_FOLDER, null);
        if(currentImageFolder == null){
            prefsEdit.putString(SettingsActivity.IMAGES_FOLDER, imagesPath);
        }
        prefsEdit.apply();
    }

    private void initDrawer(){
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (RecyclerView) findViewById(R.id.left_drawer);
        mDrawerList.setHasFixedSize(true);
        // Sets the layout manager
        documentsLayoutManager = new LinearLayoutManager(this);
        mDrawerList.setLayoutManager(documentsLayoutManager);
        documentsAdapter = new DocumentsAdapter(documentsToFiles(),
                new DocumentClickListener(this),
                DocumentsAdapter.DRAWER);
        mDrawerList.setAdapter(documentsAdapter);
        mDrawerLayout.openDrawer(Gravity.LEFT);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                ActionBar actionBar = getActionBar();
                if(actionBar!= null) {
                    actionBar.setTitle(document.getName());
                }
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                ActionBar actionBar = getActionBar();
                if(actionBar!= null) {
                    actionBar.setTitle("Choose File");
                }
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        ActionBar actionBar = getActionBar();
        if(actionBar!= null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    private List<File> documentsToFiles() {
        LinkedList<File> files = new LinkedList<>();
        for(Document doc : documents) {
            files.add(doc);
        }
        return files;
    }

    private void initEditor(){
        scrollView = (VerticalScrollView) findViewById(R.id.editor_scroll_view);
        editor = (LatexEditor) findViewById(R.id.editor);

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

    private void openDocumentInEditor(Document document){
        this.document.setOpen(false);
        document.setOpen(true);
        this.document = document;
        editor.setText("");
        new AsyncTask<File, Integer, String>(){
            ProgressDialog asyncDialog = new ProgressDialog(EditorActivity.this);

            @Override
            protected void onPreExecute(){
                asyncDialog.setMessage("Loading...");
                asyncDialog.show();
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(File... params) {
                File file = params[0];
                return FilesUtils.readTextFile(file);
            }

            @Override
            protected void onPostExecute(String s) {
                editor.setText(s);
                asyncDialog.dismiss();
                super.onPostExecute(s);
            }

        }.execute(document);

        if(!documents.contains(document)){
            documents.add(document);
        }
        refreshTitleAndDrawer();
        mDrawerLayout.closeDrawer(Gravity.LEFT);
    }

    @Override
    public void onBackPressed() {
//        if (popupWindow != null && popupWindow.isShowing()) {
//            popupWindow.dismiss();
//        } else {
        if(document.isLog()){
            removeDocument();
            openDocumentInEditor(documents.getFirst());
        } else {
            /*
            if (textModified) {
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
                dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        saveFile(document);
                        EditorActivity.this.finish();
                    }
                });
                dialog.show();
            } else {
                super.onBackPressed();
            }*/
            if (backPressed + 2000 > System.currentTimeMillis()){
                saveFile();
                super.onBackPressed();
            } else {
                Toast.makeText(getBaseContext(), "Press once again to exit!", Toast.LENGTH_SHORT).show();
                backPressed = System.currentTimeMillis();
            }
        }
//        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        highlightEditor();
    }


    /**
     * Routine to save a specific document
     */
    private void saveFile(){
        if(!document.exists()) {
            DialogsUtil.showRenameDialog(this, document);
        } else {
            FilesUtils.writeFile(document, editor.getTextString());
        }
    }

    private void renameFile(String oldPath, String newFilename){
        String name = ensureTexExtension(newFilename);
        Document oldDocument = new Document(oldPath);
        Document newDocument = new Document(FilesUtils.saveFileRenaming(oldPath, name));
        // Remove the old document from the dataset and insert the new one in the same position
        if(documents.contains(oldDocument)) {
            int oldDocumentIndex = documents.indexOf(oldDocument);
            documents.remove(oldDocument);
            documents.add(oldDocumentIndex, newDocument);
            FilesUtils.deleteFile(oldDocument);
        } else {
            documents.add(newDocument);
        }
        if(document.getPath().equals(oldDocument.getPath())){
            document = newDocument;
        }
        // Update the title and drawer
        refreshTitleAndDrawer();
    }

    private String ensureTexExtension(String name){
        if(!name.endsWith(".tex")){
            int lastIndex;
            if(name.contains(".")){
                lastIndex = name.lastIndexOf(".");
            } else {
                lastIndex = name.length();
            }
            name = name.substring(0, lastIndex) + ".tex";
        }
        return name;
    }

    /**
     * Routine to update the dataset
     */
    private void refreshTitleAndDrawer() {
        String title = document.getName();
        ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            actionBar.setTitle(title);
        }
        documentsAdapter.refresh(documentsToFiles());
    }

    private void generatePDF(){
        saveFile();
        if(!editor.getTextString().equals("")) {
            Toast.makeText(this, "Compressing and sending files...", Toast.LENGTH_SHORT).show();
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            final String imagesFolderPath = sharedPref.getString(SettingsActivity.IMAGES_FOLDER, "");
            final String outputFolderPath = sharedPref.getString(SettingsActivity.OUTPUT_FOLDER, "");
            File imagesFolder = new File(imagesFolderPath);
            if(!imagesFolder.exists()){
                FilesUtils.newDirectory(imagesFolderPath);
            }
            final File outputFolder = new File(outputFolderPath);
            if(!outputFolder.exists()){
                FilesUtils.newDirectory(outputFolderPath);
            }

            final LinkedList<String> imagesFilenames = new LinkedList<>();
            String editorText = editor.getTextString();

            Pattern pattern = Pattern.compile("(?<=\\\\includegraphics).*\\{.*\\}", Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(editorText);

            while (matcher.find()) {
                if (matcher.group().length() > 0) {
                    String group = matcher.group();
                    group = group.substring(group.indexOf("{") + 1, group.indexOf("}"));
                    group = group.substring(0, group.indexOf("."));
                    imagesFilenames.add(group);
                }
            }

            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    String filenameNoExt = filename.substring(0, filename.lastIndexOf("."));
                    return imagesFilenames.contains(filenameNoExt);
                }
            };

            File[] images = imagesFolder.listFiles(filter);
            File[] files = Arrays.copyOf(images, images.length + 1);
            files[files.length - 1] = document;
            File zip = ZipUtils.newZipFile(outputFolderPath + document.getName(), files);
            zip.deleteOnExit();
            RequestParams params = new RequestParams();
            try {
                params.put("zip_file", zip, "application/zip");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            LatexNetClient.post("latex", params, new FileAsyncHttpResponseHandler(this) {
                @Override
                public void onFailure(int i, Header[] headers, Throwable throwable, File file) {
                    Toast.makeText(getApplicationContext(), "Something went wrong",
                            Toast.LENGTH_LONG).show();
                    Log.e("NET", throwable.getMessage() + "");
                }

                @Override
                public void onSuccess(int i, Header[] headers, final File file) {
                    final Document receivedDocument = new Document(file);
                    Header header = null;
                    for (Header h : headers) {
                        if (h.getName().equals("Content-Type")) {
                            header = h;
                            break;
                        }
                    }

                    assert header != null;
                    if (header.getValue().equals("application/pdf")) {
                        byte[] bytes = FilesUtils.readBinaryFile(file);
                        String pdfName = document.getName().substring(0, document.getName().lastIndexOf(".")) + ".pdf";
                        File pdf = new File(outputFolderPath, pdfName);
                        FilesUtils.writeBinaryFile(pdf, bytes);
                        Intent pdfIntent = new Intent();
                        pdfIntent.setAction(Intent.ACTION_VIEW);
                        pdfIntent.setDataAndType(Uri.fromFile(pdf), "application/pdf");

                        if (pdfIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(pdfIntent);
                        } else {
                            Toast.makeText(getApplicationContext(), "You don't have any app to show pdf!",
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        final AlertDialog dialog = new AlertDialog.Builder(EditorActivity.this).create();
                        dialog.setTitle("Unsaved File");
                        dialog.setMessage("Latex compiling has failed.\nDo you wish to open the log?");
                        dialog.setCancelable(false);
                        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialog.dismiss();
                            }
                        });
                        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                receivedDocument.setLog();
                                openDocumentInEditor(receivedDocument);
                            }
                        });
                        dialog.show();
                    }
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "Can't compile an empty file!",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void onOpenClick(View view) {
        Intent intent = new Intent(this, FileChooserActivity.class);
        startActivityForResult(intent, 1);
        /*Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        startActivityForResult(intent, 1);*/
    }

    public void onSymbolClick(View view) {
        Button button = (Button) view;
        String symbol = button.getText().toString();
        insertSymbol(symbol);
    }

    private void insertSymbol(String symbol){
        int selection = Math.max(0, editor.getSelectionStart());
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

            openDocumentInEditor(new Document(fileUri.getPath()));
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
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        int id = item.getItemId();

        switch (id){
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.action_save:
                saveFile();
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
        openDocumentInEditor(documents.get(documents.indexOf(new Document(filePath))));
        mDrawerLayout.closeDrawer(Gravity.LEFT);
    }

    @Override
    public void onDocumentLongClickListener(View v) {
        TextView m = (TextView) v.findViewById(R.id.drawer_file_path);
        Bundle args = new Bundle();
        // Adds a "filename" parameter containing the filename
        args.putString("filepath", m.getText().toString());
        // Creates the dialog and adds the parameter
        DialogFragment dialog = new DocumentOptionsDialog();
        dialog.setArguments(args);
        // Opens thee dialog
        dialog.show(getFragmentManager(), "editor_drawer_longclick");
    }

    public void onNewFileClick(View view) {
        Document file = new Document(FilesUtils.newFile());
        openDocumentInEditor(file);
    }

    private void removeDocument(){
        removeDocument(document);
    }

    private void removeDocument(File file){
        documents.remove(file);
        if(file.getPath().equals(document.getPath())){
            if(documents.size() > 0){
                document = documents.getFirst();
            } else {
                document = new Document(FilesUtils.newFile());
            }
        }
        refreshTitleAndDrawer();
        openDocumentInEditor(document);
    }

    /**
     * Called when saving an untitled file or renaming an open one
     * @param dialog Dialog
     * @param oldPath Old file path
     * @param newFilename New filename
     */
    @Override
    public void onRenameDialogConfirmClick(DialogFragment dialog, String oldPath, String newFilename) {
        if(newFilename.length() > 0) {
            renameFile(oldPath, newFilename);
        }
    }

    @Override
    public void onDialogDeleteClick(DialogFragment dialog, String path) {
        File file = new File(path);
        removeDocument(file);
    }

    @Override
    public void onDialogRenameClick(DialogFragment dialog, String path) {
        File file = new File(path);
        DialogsUtil.showRenameDialog(this, file);
    }
}
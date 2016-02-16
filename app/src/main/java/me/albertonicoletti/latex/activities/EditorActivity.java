package me.albertonicoletti.latex.activities;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.FileAsyncHttpResponseHandler;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import me.albertonicoletti.latex.DataPersistenceUtil;
import me.albertonicoletti.latex.DialogsUtil;
import me.albertonicoletti.latex.Document;
import me.albertonicoletti.latex.DocumentClickListener;
import me.albertonicoletti.latex.DocumentOptionsDialog;
import me.albertonicoletti.latex.DocumentsAdapter;
import me.albertonicoletti.latex.LatexCompiler;
import me.albertonicoletti.latex.LatexEditor;
import me.albertonicoletti.latex.PreferenceHelper;
import me.albertonicoletti.latex.R;
import me.albertonicoletti.latex.RenameDialog;
import me.albertonicoletti.latex.VerticalScrollView;
import me.albertonicoletti.utils.FilesUtils;

/**
 * The main activity, it shows the editor.
 *
 * @author Alberto Nicoletti    albyx.n@gmail.com    https://github.com/albyxyz
 */

public class EditorActivity extends Activity implements DocumentClickListener.DocumentClickInterface,
        RenameDialog.RenameDialogListener,
        DocumentOptionsDialog.DocumentDialogListener {

    private enum Permissions {
        SAVE, OPEN
    }

    public final static int WRITE_EXTERNAL_STORAGE_PERMISSION = 1;

    private ActionBarDrawerToggle mDrawerToggle;
    /** Left Drawer Layout */
    private DrawerLayout mDrawerLayout;
    /** Adapter for the Recycler View */
    private DocumentsAdapter documentsAdapter;
    /** A custom EditText */
    private LatexEditor editor;
    /** A custom scrollView, used to intercept when a scroll is stopped */
    private VerticalScrollView scrollView;
    /** List of the open documents */
    private LinkedList<Document> documents = new LinkedList<>();
    /** The document the editor is showing */
    private Document document;

    private TextWatcher textWatcher;
    private Menu menu;
    private MenuItem saveButton;
    /** Used to remember when back button is pressed */
    private long backPressed = 0;
    private Permissions currentPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPreferences();
        setContentView(R.layout.activity_editor);
        initSymbols();
        initDrawer();
        initEditor();

        Uri fileUri = getIntent().getData();
        // If a URI is passed
        if (fileUri != null) {
            checkStoragePermissions(Permissions.OPEN);
            document = new Document(fileUri.getPath());
        } else {
            // No URI is passed
            documents = DataPersistenceUtil.readSavedOpenFiles(getApplicationContext());
            if(documents.isEmpty()) {
                // If there's no open document it opens a new untitled file
                document = new Document(FilesUtils.newUntitledFile());
            } else {
                // Else it gets the first open document
                for(Document d : documents){
                    if(d.isOpen()){
                        document = d;
                        break;
                    }
                }
                // It should never go here, just in case of an error it opens the first file
                if(document == null) {
                    document = documents.get(0);
                }
            }
        }
        openDocumentInEditor(document);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(!document.isSaved()) {
            document.setSavedText(editor.getTextString());
        }
        outState.putInt("scrollY", scrollView.getScrollY());
        outState.putInt("selectionStart", editor.getSelectionStart());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        scrollView.setScrollY(savedInstanceState.getInt("scrollY"));
        editor.setSelection(savedInstanceState.getInt("selectionStart"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        editor.refreshFontSize();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case WRITE_EXTERNAL_STORAGE_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    switch(currentPermission) {
                        case SAVE:
                            saveFile();
                            break;
                        case OPEN:
                            startFileChooserActivity();
                            break;
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.why_write_permissions),
                            Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    /**
     * Initializes the preferences.
     * Sets the default output directory and images directory the first time the app is launched.
     */
    private void initPreferences(){
        String outputPath = SettingsActivity.DEFAULT_OUTPUT_FOLDER;
        String imagesPath = SettingsActivity.DEFAULT_IMAGES_FOLDER;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences.Editor prefsEdit = prefs.edit();

        String currentOutputFolder = prefs.getString(SettingsActivity.OUTPUT_FOLDER, null);
        if(currentOutputFolder == null || currentOutputFolder.equals("")) {
            prefsEdit.putString(SettingsActivity.OUTPUT_FOLDER, outputPath);
        }
        String currentImageFolder = prefs.getString(SettingsActivity.IMAGES_FOLDER, null);
        if(currentImageFolder == null || currentImageFolder.equals("")){
            prefsEdit.putString(SettingsActivity.IMAGES_FOLDER, imagesPath);
        }
        prefsEdit.apply();
        // Creates the folders if they not exists
        final String imagesFolderPath = PreferenceHelper.getImageFolder(getApplicationContext());
        final String outputFolderPath = PreferenceHelper.getOutputFolder(getApplicationContext());
        File imagesFolder = new File(imagesFolderPath);
        if (!imagesFolder.exists()) {
            FilesUtils.newDirectory(imagesFolderPath);
        }
        final File outputFolder = new File(outputFolderPath);
        if (!outputFolder.exists()) {
            FilesUtils.newDirectory(outputFolderPath);
        }
    }

    /**
     * Initializes the navigation drawer.
     */
    private void initDrawer(){
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        /* Recycler View containing the open documents */
        RecyclerView mDrawerList = (RecyclerView) findViewById(R.id.left_drawer);
        mDrawerList.setHasFixedSize(true);
        // Sets the layout manager
        /* Recycler View layout manager */
        RecyclerView.LayoutManager documentsLayoutManager = new LinearLayoutManager(this);
        mDrawerList.setLayoutManager(documentsLayoutManager);
        documentsAdapter = new DocumentsAdapter(documentsToFiles(),
                new DocumentClickListener(this),
                DocumentsAdapter.DRAWER);
        mDrawerList.setAdapter(documentsAdapter);
        mDrawerLayout.openDrawer(GravityCompat.START);
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
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    /**
     * Initializes the editor.
     */
    private void initEditor(){
        scrollView = (VerticalScrollView) findViewById(R.id.editor_scroll_view);
        editor = (LatexEditor) findViewById(R.id.editor);
        // When the scroll stops, it will highlights the text
        scrollView.setScrollStoppedListener(new VerticalScrollView.ScrollStoppedListener() {
            @Override
            public void onStopped() {
                highlightEditor();
            }
        });
    }

    private void startTextWatcher(){
        textWatcher = new TextWatcher() {

            private RelativeSizeSpan span;
            private SpannableString spannable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // count > 0 means it's not a backspace
                if (count > 0) {
                    // It's a span that does nothing, used to mark where the text has changed
                    span = new RelativeSizeSpan(1.0f);
                    spannable = new SpannableString(s);
                    spannable.setSpan(span, start, start + count, Spanned.SPAN_COMPOSING);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (editor.getLayout() != null && spannable != null) {
                    document.setSaved(false);
                    setSaveButtonEnabled(true);
                    menu.findItem(R.id.action_save).setEnabled(true);
                    autoIndentAndTabEditor(s, spannable, span);
                    highlightEditor();
                    // Cleanup
                    span = null;
                    spannable = null;
                }
            }
        };
        editor.addTextChangedListener(textWatcher);
    }

    private void stopTextWatcher(){
        editor.removeTextChangedListener(textWatcher);
    }

    /**
     * Initializes the symbols shortcut bar.
     */
    private void initSymbols(){
        // It actually only initializes the "+" button, creating a popup menu
        final Button button = (Button) findViewById(R.id.maths_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Shows a popup menu
                PopupMenu popup = new PopupMenu(EditorActivity.this, button);
                popup.getMenuInflater().inflate(R.menu.menu_maths, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        // On symbol click
                        insertSymbol(menuItem.getTitle().toString());
                        return false;
                    }
                });
                popup.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        this.menu = menu;
        saveButton = menu.findItem(R.id.action_save);
        setSaveButtonEnabled(document.hasSavedText());
        return true;
    }

    private void setSaveButtonEnabled(boolean enable){
        if(saveButton != null) {
            Drawable resIcon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_save_white_24dp);
            if (!enable)
                resIcon.mutate().setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
            saveButton.setEnabled(enable);
            saveButton.setIcon(resIcon);
        }
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

    /**
     * When the activity stops it saves the open documents.
     */
    @Override
    protected void onStop() {
        DataPersistenceUtil.saveFilesPath(getApplicationContext(), documents);
        super.onStop();
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
            case R.id.action_about:
                about();
                break;
            case R.id.action_open_source:
                openSource();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Used to maintain the same indentation as the upper line
     * @param editable Text
     * @param spannable Spannable
     * @param span Modified span
     */
    private void autoIndentAndTabEditor(Editable editable, SpannableString spannable, RelativeSizeSpan span){
        int beginIndex = spannable.getSpanStart(span);
        int endIndex = spannable.getSpanEnd(span);
        // If the last written character is a newline
        if(editable.length() > 0) {
            if (editable.charAt(endIndex - 1) == '\n') {
                int lineModified = editor.getLayout().getLineForOffset(beginIndex);
                int modifiedBeginIndex = editor.getLayout().getLineStart(lineModified);
                int modifiedEndIndex = editor.getLayout().getLineEnd(lineModified);
                String str = editable.subSequence(modifiedBeginIndex, modifiedEndIndex).toString();
                // Collects the whitespaces and tabulations in the upper line
                String whitespaces = "";
                int i = 0;
                while (str.charAt(i) == ' ' || str.charAt(i) == '\t') {
                    whitespaces += str.charAt(i);
                    i++;
                }
                // And inserts them in the newline
                editable.insert(beginIndex + 1, whitespaces);
            }
            if (editable.charAt(endIndex - 1) == '\t') {
                int tabSize = Integer.valueOf(
                        PreferenceManager.getDefaultSharedPreferences(this)
                                .getString(SettingsActivity.TAB_SIZE, "")
                );
                String whitespaces = "";
                for (int i=0; i < tabSize; i++) {
                    whitespaces += " ";
                }
                editable.replace(beginIndex, beginIndex+1, whitespaces);
            }
        }
    }

    /**
     * Highlights the text in the screen in the editor.
     */
    private void highlightEditor(){
        int scrollY = scrollView.getScrollY();
        if (scrollY == -1) scrollY = 0;
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int start = Math.max(0, editor.getOffsetForPosition(0, scrollY));
        int end = Math.max(0, editor.getOffsetForPosition(size.x, scrollY + size.y));
        if(!document.isLog())
            editor.highlightText(start, end);
    }

    /**
     * Opens in the editor a document and adds it to the documents list
     * @param document Document to open
     */
    private void openDocumentInEditor(Document document){
        stopTextWatcher();
        this.document.setOpen(false);
        document.setOpen(true);
        this.document = document;
        if(document.hasSavedText()) {
            editor.setText(document.getSavedText());
        } else {
            editor.setText("");
            // Reads the file in a new thread and shows a loading dialog meanwhile
            new AsyncTask<File, Integer, String>() {
                ProgressDialog asyncDialog = new ProgressDialog(EditorActivity.this);

                @Override
                protected void onPreExecute() {
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
                    setSaveButtonEnabled(false);
                    startTextWatcher();
                    super.onPostExecute(s);
                }

            }.execute(document);
        }
        // Adds the document to the document's list if it isn't there yet
        if(documents.contains(document)){
            documents.remove(document);
        }
        documents.addFirst(document);
        refreshTitleAndDrawer();
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    /**
     * Checks the double back pressure to exit
     */
    @Override
    public void onBackPressed() {
        if(document.isLog()){
            removeDocument();
            openDocumentInEditor(documents.getFirst());
        } else {
            if (backPressed + 2000 > System.currentTimeMillis()){
                saveFile();
                super.onBackPressed();
            } else {
                Toast.makeText(getApplicationContext(), "Press once again to exit!", Toast.LENGTH_SHORT).show();
                backPressed = System.currentTimeMillis();
            }
        }
    }

    /**
     * Used to highlight the editor on startup
     * @param hasFocus Has focus
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        highlightEditor();
    }

    private boolean checkStoragePermissions(Permissions type) {
        currentPermission = type;
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_PERMISSION);
            return false;
        }
        return true;
    }

    /**
     * Routine to save the current document
     * @return True if the file existed before this method call.
     */
    private boolean saveFile() {
        boolean exists = false;
        if(checkStoragePermissions(Permissions.SAVE)) {
            if (!document.exists()) {
                DialogsUtil.showRenameDialog(this, document);
            } else {
                FilesUtils.writeFile(document, editor.getTextString());
                exists = true;
                document.setSaved(true);
                setSaveButtonEnabled(false);
                menu.findItem(R.id.action_save).setEnabled(false);
            }
        }
        return exists;
    }

    /**
     * Renames a document
     * @param oldPath Old path
     * @param newFilename New filename
     */
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

    /**
     * Returns the filename having a .tex suffix.
     * @param name Filename
     * @return .tex filename
     */
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

    /**
     * Launches the about activity
     */
    private void about(){
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    /**
     * Launches the open source licenses activity
     */
    private void openSource(){
        Intent intent = new Intent(this, OpenSourceLicencesActivity.class);
        startActivity(intent);
    }

    /**
     * Launches the file chooser activity
     */
    private void startFileChooserActivity(){
        if(checkStoragePermissions(Permissions.OPEN)) {
            Intent intent = new Intent(this, FileChooserActivity.class);
            startActivityForResult(intent, 1);
        }
    }

    /**
     * Routine that searches for the images used in the current document, zips them with the file
     * and sends it to the server.
     * It will show the response pdf or log.
     */
    private void generatePDF(){
        boolean fileNeedsToBeSaved = !saveFile();
        if(!fileNeedsToBeSaved) {
            if(!editor.getTextString().equals("")){
                final String imagesFolderPath = PreferenceHelper.getImageFolder(getApplicationContext());
                final String outputFolderPath = PreferenceHelper.getOutputFolder(getApplicationContext());
                final File imagesFolder = new File(imagesFolderPath);
                final File outputFolder = new File(outputFolderPath);
                final ProgressDialog asyncDialog = new ProgressDialog(EditorActivity.this);
                asyncDialog.setMessage("Compressing and sending files...");
                asyncDialog.show();
                LatexCompiler.generatePDF(getApplicationContext(), editor, imagesFolder, outputFolder, document, new FileAsyncHttpResponseHandler(this) {
                    @Override
                    public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, Throwable throwable, File file) {
                        asyncDialog.dismiss();
                        // On failure shows an error toast
                        Toast.makeText(getApplicationContext(), "Server Error.",
                                Toast.LENGTH_LONG).show();
                        Log.e("LATEX_NET", throwable.getMessage() + "");
                    }

                    @Override
                    public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, File file) {
                        asyncDialog.dismiss();
                        Header header = null;
                        // Retrieves the content-type header
                        for (Header h : headers) {
                            if (h.getName().equals("Content-Type")) {
                                header = h;
                                break;
                            }
                        }
                        openResultingFile(header, file);
                    }
                });
                asyncDialog.setMessage("Waiting for the server to compile...");
            } else {
                // Empty file
                Toast.makeText(getApplicationContext(), "Can't compile an empty file!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void openResultingFile(Header header, File file) {
        final String outputFolderPath = PreferenceHelper.getOutputFolder(getApplicationContext());
        final String headerType = header.getValue();
        // If it's a PDF, the compile succeeded
        if (headerType.equals("application/pdf") || headerType.equals("application/x-dvi")) {
            String ext = headerType.substring(headerType.length()-3, headerType.length());
            // Saves the file in the output directory and tries to open it
            byte[] bytes = FilesUtils.readBinaryFile(file);
            String pdfName = document.getName().substring(0, document.getName().lastIndexOf(".")+1) + ext;
            File pdf = new File(outputFolderPath, pdfName);
            FilesUtils.writeBinaryFile(pdf, bytes);
            Intent pdfIntent = new Intent();
            pdfIntent.setAction(Intent.ACTION_VIEW);
            pdfIntent.setDataAndType(Uri.fromFile(pdf), headerType);

            if (pdfIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(pdfIntent);
            } else {
                Toast.makeText(getApplicationContext(), "You don't have any " + ext + " reader!",
                        Toast.LENGTH_LONG).show();
            }
        } else {
            final Document receivedDocument = new Document(file);
            // Asks the user if he wishes to open the log.
            DialogsUtil.showConfirmDialog(EditorActivity.this,
                    getString(R.string.compiling_error_title),
                    getString(R.string.compiling_error_message),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    },new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            receivedDocument.setLog();
                            openDocumentInEditor(receivedDocument);
                        }
                    }
            );
        }
    }

    /**
     * On a symbol click, inserts it in the editor
     * @param view View
     */
    public void onSymbolClick(View view) {
        Button button = (Button) view;
        String symbol = button.getText().toString();
        insertSymbol(symbol);
    }

    /**
     * Inserts a symbol in the editor
     * @param symbol Symbol
     */
    private void insertSymbol(String symbol){
        int selection = Math.max(0, editor.getSelectionStart());
        editor.getText().insert(selection, symbol);
        editor.setSelection(selection + 1);
    }

    /**
     * Called when returning from the file picker activity, it opens in the editor the returned file
     * @param requestCode Request code
     * @param resultCode Result code
     * @param data Data
     */
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

    /**
     * On new file click, it opens an empty file in the editor
     * @param view View
     */
    public void onNewFileClick(View view) {
        Document file = new Document(FilesUtils.newUntitledFile());
        openDocumentInEditor(file);
    }

    /**
     * On open file click, starts the file picker activity
     * @param view View
     */
    public void onOpenClick(View view) {
        startFileChooserActivity();
    }

    /**
     * Removes the current document from the documents list
     */
    private void removeDocument(){
        removeDocument(document);
    }

    /**
     * Removes a document from the documents list
     * @param document Document to remove
     */
    private void removeDocument(Document document){
        documents.remove(document);
        if(document.getPath().equals(document.getPath())){
            // If the current document is removed, it opens a new file in the editor
            if(documents.size() > 0){
                document = documents.getFirst();
            } else {
                document = new Document(FilesUtils.newUntitledFile());
            }
        }
        openDocumentInEditor(document);
    }

    /**
     * Routine to convert the Documents list to an equivalent File list
     * @return File list
     */
    private List<File> documentsToFiles() {
        LinkedList<File> files = new LinkedList<>();
        for(Document doc : documents) {
            files.add(doc);
        }
        return files;
    }

    private void showSaveDialog(final String filePath){
        DialogsUtil.showConfirmDialog(EditorActivity.this,
                getString(R.string.confirm_save_title),
                getString(R.string.confirm_save_message),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switchDocument(filePath);
                    }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        saveFile();
                        switchDocument(filePath);
                    }
                }
        );
    }

    private void askUserIfFileHasToBeSaved(String filePath) {
        if(!document.isSaved()) {
            showSaveDialog(filePath);
        } else {
            switchDocument(filePath);
        }
    }

    private void switchDocument(String filePath){
        openDocumentInEditor(documents.get(documents.indexOf(new Document(filePath))));
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    /**
     * On clicking a document in the drawer, it opens it in the editor
     * @param view View
     */
    @Override
    public void onDocumentClickListener(View view) {
        TextView textView = (TextView) view.findViewById(R.id.drawer_file_path);
        String filePath = textView.getText().toString();
        askUserIfFileHasToBeSaved(filePath);
    }

    /**
     * On long clicking a document in the drawer, it shows a dialog asking what to do
     * @param view View
     */
    @Override
    public void onDocumentLongClickListener(View view) {
        TextView m = (TextView) view.findViewById(R.id.drawer_file_path);
        Bundle args = new Bundle();
        // Adds a "filename" parameter containing the filename
        args.putString("filepath", m.getText().toString());
        // Creates the dialog and adds the parameter
        DialogFragment dialog = new DocumentOptionsDialog();
        dialog.setArguments(args);
        // Opens thee dialog
        dialog.show(getFragmentManager(), "editor_drawer_longclick");
    }

    /**
     * On confirming to remove a file
     * @param dialog Dialog
     * @param path File to rename
     */
    @Override
    public void onDialogRemoveClick(DialogFragment dialog, String path) {
        Document document = new Document(path);
        removeDocument(document);
    }

    /**
     * Shows a renaming dialog
     * @param dialog Dialog
     * @param path File to rename
     */
    @Override
    public void onDialogRenameClick(DialogFragment dialog, String path) {
        Document document = new Document(path);
        DialogsUtil.showRenameDialog(this, document);
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
}
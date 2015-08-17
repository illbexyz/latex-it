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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class EditorActivity extends Activity implements DocumentClickListener.DocumentClickInterface,
                                                        RenameDialog.RenameDialogListener,
                                                        DocumentOptionsDialog.DocumentDialogListener {

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
    private LinkedList<File> documents = new LinkedList<>();
    /** List of the images */
    //private LinkedList<LinkedList<File>> images = new LinkedList<>();
    /** The document the editor is showing */
    private File document;

    private PopupWindow popupWindow;

    /** Set to true if the text has been modified and not saved */
    private boolean textModified = false;

    private boolean initialized = false;

    private boolean isLog = false;

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
        documentsAdapter = new DocumentsAdapter(documents,
                new DocumentClickListener(this),
                DocumentsAdapter.DRAWER);
        mDrawerList.setAdapter(documentsAdapter);
        mDrawerLayout.openDrawer(Gravity.LEFT);
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
//        if (popupWindow != null && popupWindow.isShowing()) {
//            popupWindow.dismiss();
//        } else {
        if(isLog){
            removeDocument();
            openDocumentInEditor(documents.getFirst());
            isLog = false;
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
    private void saveFile(File document){
        String name = document.getName();
        if(!name.endsWith(".tex")){
            int lastIndex;
            if(name.contains(".")){
                lastIndex = name.lastIndexOf(".");
            } else {
                lastIndex = name.length();
            }
            name = name.substring(0, lastIndex) + ".tex";

            File newDocument = FilesUtils.saveFileRenaming(name, editor.getTextString());
            textModified = false;
            // Remove the old document from the dataset and insert the new one in the same position
            int oldDocumentIndex = documents.indexOf(this.document);
            documents.remove(this.document);
            documents.add(oldDocumentIndex, newDocument);
            FilesUtils.deleteFile(document);
            this.document = newDocument;
            // Update the title and drawer
            refreshTitleAndDrawer();
        }
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
        saveFile(document);
        if(!editor.getTextString().equals("")) {
            Toast.makeText(this, "Compressing and sending files...", Toast.LENGTH_SHORT).show();
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            final String imagesFolderPath = sharedPref.getString(SettingsActivity.IMAGES_FOLDER, "");
            final String outputFolderPath = sharedPref.getString(SettingsActivity.OUTPUT_FOLDER, "");
            File imagesFolder = new File(imagesFolderPath);
            final File outputFolder = new File(outputFolderPath);

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
                    if (imagesFilenames.contains(filenameNoExt)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            };

            File[] images = imagesFolder.listFiles(filter);
            File[] files = Arrays.copyOf(images, images.length + 1);
            files[files.length - 1] = document;
            File zip = ZipUtils.newZipFile(outputFolderPath + document.getName(), files);
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
                                isLog = true;
                                openDocumentInEditor(file);
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
        DialogFragment dialog = new DocumentOptionsDialog();
        dialog.setArguments(args);
        // Opens thee dialog
        dialog.show(getFragmentManager(), "editor_drawer_longclick");
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

    private void removeDocument(){
        removeDocument(document);
    }

    private void removeDocument(File file){
        documents.remove(file);
        if(file == document){
            if(documents.size() > 0){
                document = documents.getFirst();
            } else {
                document = FilesUtils.newFile();
            }
        }
        refreshTitleAndDrawer();
        openDocumentInEditor(document);
    }

    /**
     * Called when saving an untitled file or renaming an open one
     * @param dialog
     * @param path
     * @param newFilename
     */
    @Override
    public void onRenameDialogConfirmClick(DialogFragment dialog, String path, String newFilename) {
        saveFile(new File(newFilename));
    }

    @Override
    public void onDialogDeleteClick(DialogFragment dialog, String path) {
        File file = new File(path);
        removeDocument(file);
        refreshTitleAndDrawer();
    }

    @Override
    public void onDialogRenameClick(DialogFragment dialog, String path) {
        File file = new File(path);
        DialogsUtil.showRenameDialog(this, file);
    }
}
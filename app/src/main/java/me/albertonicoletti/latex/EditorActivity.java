package me.albertonicoletti.latex;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class EditorActivity extends Activity implements DocumentClickListener.DocumentClickInterface,
                                                        RenameDialog.RenameDialogListener{

    public final Pattern commandsPattern = Pattern.compile("([\\\\])\\w+(\\*)*", Pattern.MULTILINE);
    public final Pattern keywordsPattern = Pattern.compile("([{]).+([}])", Pattern.MULTILINE);
    public final Pattern thirdPattern = Pattern.compile("([\\[]).+([\\]])", Pattern.MULTILINE);
    public final Pattern commentsPattern = Pattern.compile("(%).*$", Pattern.MULTILINE);

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
        if(fileUri == null) {
            fileUri = Uri.fromFile(FilesManager.newFile());
        }
        document = new File(fileUri.getPath());
        openDocumentInEditor(document);
        initialized = true;
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
        scrollView.setScrollStoppedListener(new VerticalScrollView.ScrollStoppedListener() {
            @Override
            public void onStopped() {
                highlightText(editor.getText());
            }
        });
        editor = (Editor) findViewById(R.id.editor);
        editor.setVerticalScrollBarEnabled(true);
        editor.setMovementMethod(new ScrollingMovementMethod());
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
                    highlightText(s);
                    textModified = true;
                }
            }
        });
    }

    private void changeFile(String filename){
        File dir = FilesManager.getDocumentsDir();
        File file = new File(dir, filename);
        openDocumentInEditor(file);
    }

    private void openDocumentInEditor(File document){
        this.document = document;
        String fileContent = "";
        Log.v("FILE", "Trying to read file: " + document.getPath());
        try {
            Scanner s = new Scanner(document);
            while(s.hasNextLine()){
                fileContent += s.nextLine() + "\n";
            }
        } catch (FileNotFoundException e) {
            Log.e("FILE", "Can't read file: " + e.getMessage());
        }

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
        highlightText(editor.getText());
    }


    public void highlightText(Editable editable){

        clearSpans(editable);

        int scrollY = scrollView.getScrollY();
        if(scrollY == -1) scrollY = 0;
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int start = Math.max(0, editor.getOffsetForPosition(0, scrollY));
        int end = Math.max(0, editor.getOffsetForPosition(size.x, scrollY + size.y));
        CharSequence s = editor.getText().subSequence(start, end);

        Matcher matcher = keywordsPattern.matcher(s);
        while (matcher.find()) {
            editable.setSpan(new ForegroundColorSpan(getResources()
                            .getColor(R.color.latex_class)),
                    start + matcher.start(), start + matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        matcher = commandsPattern.matcher(s);
        while (matcher.find()) {
            editable.setSpan(new ForegroundColorSpan(getResources()
                            .getColor(R.color.latex_keyword)),
                    start + matcher.start(), start + matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        matcher = thirdPattern.matcher(s);
        while (matcher.find()) {
            editable.setSpan(new ForegroundColorSpan(getResources()
                            .getColor(R.color.latex_third)),
                    start + matcher.start(), start + matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        matcher = commentsPattern.matcher(s);
        while (matcher.find()) {
            editable.setSpan(new ForegroundColorSpan(getResources()
                            .getColor(R.color.text_grey)),
                    start + matcher.start(), start + matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

    }

    /**
     * Routine to save a specific document
     */
    private void saveFile(File document){
        FilesManager.writeFile(document, editor.getTextString());
        textModified = false;
    }

    /**
     * Routine to update the dataset
     */
    private void refreshTitleAndDrawer() {
        setTitle(this.document.getName());
        documentsAdapter.refresh(documents);
    }

    /**
     * Routine to remove the colored spans.
     * @param e Editable string
     */
    private void clearSpans( Editable e ){
        // remove foreground color spans
        ForegroundColorSpan spans[] = e.getSpans(
                0,
                e.length(),
                ForegroundColorSpan.class );

        for( int n = spans.length; n-- > 0; )
            e.removeSpan( spans[n] );

        // remove background color spans
        /*
        BackgroundColorSpan spans[] = e.getSpans(
                0,
                e.length(),
                BackgroundColorSpan.class );

        for( int n = spans.length; n-- > 0; )
            e.removeSpan( spans[n] );
        */
    }

    private void generatePDF(){
        File zip = FilesManager.zipFiles(documents);
        FilesManager.saveFileOnDisk(zip.getParentFile(), zip);
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

                byte[] bytes = FilesManager.readBinaryFile(file);

                File pdf = FilesManager.newFile("nuovo.pdf");
                FilesManager.writeBinaryFile(pdf, bytes);

                Intent pdfIntent = new Intent();
                pdfIntent.setAction(Intent.ACTION_VIEW);
                pdfIntent.setDataAndType(Uri.fromFile(pdf), "application/pdf");

                if (pdfIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(pdfIntent);
                }
            }
        });*/
    }

    public void onPDFClick(MenuItem item) {
        this.generatePDF();
    }

    public void onOpenClick(View view) {
        Intent intent = new Intent(this, FileChooserActivity.class);
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
    public void onDocumentClickListener(View v) {
        TextView textView = (TextView) v.findViewById(R.id.drawer_document_name);
        String filename = textView.getText().toString();
        changeFile(filename);
        mDrawerLayout.closeDrawer(Gravity.LEFT);
    }

    @Override
    public void onDocumentLongClickListener(View v) {

    }

    public void onNewFileClick(View view) {
        File file = FilesManager.newFile();
        openDocumentInEditor(file);
    }

    public void onSaveOpenDocumentClick(MenuItem item) {
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
        FilesManager.saveFileOnDisk(newDocument.getParentFile(), newDocument);
        // Recreate it to change the name (the file won't change his name otherwise)
        newDocument = new File(FilesManager.renameFile(newDocument, newFilename).getPath());
        // Remove the old document from the dataset and insert the new one in the same position
        int oldDocumentIndex = documents.indexOf(this.document);
        documents.remove(this.document);
        documents.add(oldDocumentIndex, newDocument);
        this.document = newDocument;
        // Update the title and drawer
        refreshTitleAndDrawer();
    }

}

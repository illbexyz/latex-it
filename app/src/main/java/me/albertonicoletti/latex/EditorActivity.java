package me.albertonicoletti.latex;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.loopj.android.http.*;

import org.apache.http.Header;


public class EditorActivity extends Activity {

    EditText editor;
    VerticalScrollView scrollView;
    File file;

    public final Pattern commandsPattern = Pattern.compile("([\\\\])\\w+(\\*)*", Pattern.MULTILINE);
    public final Pattern keywordsPattern = Pattern.compile("([{]).+([}])", Pattern.MULTILINE);
    public final Pattern thirdPattern = Pattern.compile("([\\[]).+([\\]])", Pattern.MULTILINE);
    public final Pattern commentsPattern = Pattern.compile("(%).*$", Pattern.MULTILINE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Log.v("ACTIVITY", "Editor activity started");
        editor = (EditText) findViewById(R.id.editor);
        editor.setVerticalScrollBarEnabled(true);
        editor.setMovementMethod(new ScrollingMovementMethod());
        scrollView = (VerticalScrollView) findViewById(R.id.editor_scroll_view);
        scrollView.setScrollStoppedListener(new VerticalScrollView.ScrollStoppedListener() {
            @Override
            public void onStopped() {
                highlightText(editor.getText());
            }
        });

        Uri fileUri = getIntent().getData();
        file = new File(fileUri.getPath());
        String fileContent = "";
        Log.v("FILE", "Trying to read file: " + file.getPath());
        try {
            Scanner s = new Scanner(file);
            while(s.hasNextLine()){
                fileContent += s.nextLine() + "\n";
            }
        } catch (FileNotFoundException e) {
            Log.e("FILE", "Can't read file: " + e.getMessage());
        }
        //Log.v("FILE", "File content: " + fileContent);

        editor.setText(fileContent);

        editor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                highlightText(s);
            }
        });

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
        int start = editor.getOffsetForPosition(0, scrollY);
        int end = editor.getOffsetForPosition(0, scrollY + size.y);
        CharSequence s = editor.getText().subSequence(start, end);
        //Log.v("PAPER_CHANGE", s.toString());


        Matcher matcher = keywordsPattern.matcher(s);
        while (matcher.find()) {
            editable.setSpan(new ForegroundColorSpan(getResources()
                            .getColor(R.color.latex_class)),
                    start + matcher.start(), start + matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            Log.v("PATTERN", "Found pattern at " + matcher.start());
        }

        matcher = commandsPattern.matcher(s);
        while (matcher.find()) {
            editable.setSpan(new ForegroundColorSpan(getResources()
                            .getColor(R.color.latex_keyword)),
                    start + matcher.start(), start + matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            Log.v("PATTERN", "Found pattern at " + matcher.start() + "to " + matcher.end());
        }



        matcher = thirdPattern.matcher(s);
        while (matcher.find()) {
            editable.setSpan(new ForegroundColorSpan(getResources()
                            .getColor(R.color.latex_third)),
                    start + matcher.start(), start + matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            Log.v("PATTERN", "Found pattern at " + matcher.start());
        }

        matcher = commentsPattern.matcher(s);
        while (matcher.find()) {
            editable.setSpan(new ForegroundColorSpan(getResources()
                            .getColor(R.color.blue)),
                    start + matcher.start(), start + matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            Log.v("PATTERN", "Found pattern at " + matcher.start());
        }

    }

    @Override
    protected void onPause(){
        super.onPause();
        saveFile();
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

    private void saveFile(){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            String stringToSave = editor.getText().toString();
            writer.write(stringToSave, 0, stringToSave.length());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Log.e("FILE", "Can't write in the file: " + e.getMessage());
        }
    }

    private void clearSpans( Editable e )
    {
        // remove foreground color spans
        {
            ForegroundColorSpan spans[] = e.getSpans(
                    0,
                    e.length(),
                    ForegroundColorSpan.class );

            for( int n = spans.length; n-- > 0; )
                e.removeSpan( spans[n] );
        }

        // remove background color spans
        /*
        {
            BackgroundColorSpan spans[] = e.getSpans(
                    0,
                    e.length(),
                    BackgroundColorSpan.class );

            for( int n = spans.length; n-- > 0; )
                e.removeSpan( spans[n] );
        }
        */
    }

    public void generatePDF(){

    }

    public void generatePDF(MenuItem item) {
        RequestParams params = new RequestParams();
        try {
            params.put("file", file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        LatexNetClient.post("latex", params, new FileAsyncHttpResponseHandler(this) {
            @Override
            public void onFailure(int i, Header[] headers, Throwable throwable, File file) {
                Log.v("WE", ":(");
            }

            @Override
            public void onSuccess(int i, Header[] headers, File file) {
                Log.v("WE", ":)");
                Uri pdfURL = Uri.parse(file.getAbsolutePath());
                Log.v("YO", pdfURL.toString());
                FilesManager filesManager = new FilesManager();

                String fileContent = "";
                Log.v("FILE", "Trying to read file: " + file.getPath());
                byte[] bytes = filesManager.readBinaryFile(file);

                File pdf = filesManager.newFile("nuovo.pdf");
                filesManager.writeBinaryFile(bytes, pdf);

                Intent pdfIntent = new Intent();
                pdfIntent.setAction(Intent.ACTION_VIEW);
                pdfIntent.setDataAndType(Uri.fromFile(pdf), "application/pdf");
                //pdfIntent.setType("application/pdf");
                //pdfIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                if (pdfIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(pdfIntent);
                }
            }
        });

    }
}

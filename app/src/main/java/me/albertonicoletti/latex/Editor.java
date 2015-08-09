package me.albertonicoletti.latex;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The editor, an EditText with extended functionality:
 * - Line count
 */
public class Editor extends EditText {

    public final Pattern commandsPattern = Pattern.compile("([\\\\])\\w+(\\*)*", Pattern.MULTILINE);
    public final Pattern keywordsPattern = Pattern.compile("([{]).+([}])", Pattern.MULTILINE);
    public final Pattern thirdPattern = Pattern.compile("([\\[]).+([\\]])", Pattern.MULTILINE);
    public final Pattern commentsPattern = Pattern.compile("(%).*$", Pattern.MULTILINE);

    /** Painter used to draw numbers */
    private static final TextPaint numberPainter = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
    /** Painter used to draw the line counter's background */
    private static final Paint backgroundPainter = new Paint();

    /** Line height */
    private int lineHeight;
    /** Line counter's padding top (it starts a little before the actual lines) */
    private int lineCountPaddingTop;
    /** Line counter's column width */
    private int lineCounterColumnWidth;
    /** Line counter's column right margin (the margin before the text starts) */
    private int lineCounterColumnMargin;

    public Editor(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Number's color
        numberPainter.setColor(getResources().getColor(R.color.text_grey));
        lineHeight = getLineHeight();
        // The line counter's size is 30% less than the text one
        numberPainter.setTextSize(lineHeight * 0.70f);
        // Given a point, the numbers are drawn starting from the right
        numberPainter.setTextAlign(Paint.Align.RIGHT);
        lineCountPaddingTop = getPaddingTop() - (lineHeight/7);
        int marginBeforeText = (int) (getPaddingLeft() * 0.20f);
        // The column width is given by the total padding left less the margin before text
        lineCounterColumnWidth =  getPaddingLeft() - marginBeforeText;
        lineCounterColumnMargin = lineCounterColumnWidth/6;
    }

    public void highlightText(int start, int end){
        Editable editable = getText();
        clearSpans(editable);

        CharSequence s = getText().subSequence(start, end);

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

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        backgroundPainter.setColor(getResources().getColor(R.color.editor_column));
        // Draws a colored background to the line counter column
        canvas.drawRect(0, 0, lineCounterColumnWidth, this.getBottom(), backgroundPainter);

        backgroundPainter.setColor(getResources().getColor(R.color.text_darkgrey));
        // Draws a right-border to the line counter column
        canvas.drawLine(lineCounterColumnWidth, 0, lineCounterColumnWidth, getBottom(), backgroundPainter);

        // The number that will be drawn
        int lineToDraw = 1;
        // Set to true if a line hasn't got a newline character
        boolean previousLineNoNewline = false;
        // Will draw a number aside each line in the EditText
        // The number won't be drawn if the previous line hasn't got a newline character
        for (int i = 0; i < getLineCount(); i++) {
            int currentLineStart = getLayout().getLineStart(i);
            int currentLineEnd = getLayout().getLineEnd(i);
            String currentLine = getText().subSequence(currentLineStart, currentLineEnd).toString();
            boolean containsNewLine = currentLine.contains("\n");
            // If the previous line contains a newline character the number it will draw the number
            if (!previousLineNoNewline) {
                canvas.drawText(String.valueOf(lineToDraw),
                        lineCounterColumnWidth - lineCounterColumnMargin,
                        lineCountPaddingTop + ((i+1) * lineHeight),
                        numberPainter);
                if (!containsNewLine) {
                    previousLineNoNewline = true;
                }
                lineToDraw++;
            // When it finds a line containing a newline charcater, the next line will be drawn
            } else {
                if (containsNewLine) {
                    previousLineNoNewline = false;
                }
            }
        }
        super.onDraw(canvas);
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

    /**
     * Returns the String representation of the text
     * @return the String representation of the text
     */
    public String getTextString(){
        return this.getText().toString();
    }

}

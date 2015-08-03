package me.albertonicoletti.latex;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * The editor, an EditText with extended functionality:
 * - Line count
 */
public class Editor extends EditText {

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


    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        backgroundPainter.setColor(getResources().getColor(R.color.light_grey));
        // Draws a colored background to the line counter column
        canvas.drawRect(0, 0, lineCounterColumnWidth, this.getBottom(), backgroundPainter);

        backgroundPainter.setColor(getResources().getColor(R.color.text_darkgrey));
        // Draws a right-border to the line counter column
        canvas.drawLine(lineCounterColumnWidth, 0, lineCounterColumnWidth, getBottom(), backgroundPainter);

        //TODO: linee finte newline
        for(int i=0; i<getLineCount(); i++){
            canvas.drawText(String.valueOf(i + 1),
                    lineCounterColumnWidth - lineCounterColumnMargin,
                    lineCountPaddingTop + ((i+1) * lineHeight),
                    numberPainter);
        }

        super.onDraw(canvas);
    }

}

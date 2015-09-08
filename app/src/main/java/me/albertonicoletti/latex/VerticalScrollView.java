package me.albertonicoletti.latex;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

import java.lang.reflect.Field;

/**
 * Custom ScrollView primarily used to detect when the scroll view "finishes to scroll"
 *
 * @author Alberto Nicoletti    albyx.n@gmail.com    https://github.com/albyxyz
 */
public class VerticalScrollView extends ScrollView {

    /** How much time passes from stop detection */
    private static final int DELAY = 250;

    /** Object listening to the stop of the scroll */
    private ScrollStoppedListener mScrollStoppedListener;

    /** A field from OverScroller that can check if the scroller has finished */
    private Field mFieldFinished;

    /** Y scroller from the super class */
    private Object mScrollerY;

    /** Set to true if there's a Runnable checking if the scroll has finished */
    private boolean mChecking;

    /**
     *
     */
    private final Runnable mCheckIfFinishedRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                // If has finished scrolling, it will call the onStopped method.
                if (mFieldFinished.getBoolean(mScrollerY)) {
                    mScrollStoppedListener.onStopped();
                }
                mChecking = false;
            } catch (Exception ignored) {
            }
        }

    };

    // Constructors: they always call init()
    public VerticalScrollView(Context context) {
        super(context);
        init();
    }

    public VerticalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VerticalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Initialize the ScrollView
     */
    private void init() {
        try {
            // Get the OverScroller field from the ScrollView class.
            final Field fScroller = ScrollView.class.getDeclaredField("mScroller");
            fScroller.setAccessible(true);
            final Object mScroller = fScroller.get(this);
            // Get the SplineOverScroller from the OverScroller class
            Field fScrollerY = mScroller.getClass().getDeclaredField("mScrollerY");
            fScrollerY.setAccessible(true);
            mScrollerY = fScrollerY.get(mScroller);
            // Get OverScroller.SplineOverScroller#mFinished field for OverScroller#mScrollerY
            mFieldFinished = mScrollerY.getClass().getDeclaredField("mFinished");
            mFieldFinished.setAccessible(true);
        } catch (Exception ignored) {
        }
    }

    /**
     * Sets a listener for the stop event
     * @param listener Listener
     */
    public void setScrollStoppedListener(ScrollStoppedListener listener) {
        mScrollStoppedListener = listener;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        // Whenever there's a new scroll if there's a listener it will check if has stopped.
        if (mScrollStoppedListener != null) {
            checkIfStopped();
        }
    }

    /**
     * Checks if the scroll has been stopped.
     */
    private void checkIfStopped() {
        if (mChecking) {
            // Make sure it is only running one check at a time.
            return;
        }
        mChecking = true;
        // Checks if has finished scrolling after DELAY milliseconds
        postDelayed(mCheckIfFinishedRunnable, DELAY);
    }

    public interface ScrollStoppedListener {
        void onStopped();
    }

}

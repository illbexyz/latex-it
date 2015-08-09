package me.albertonicoletti.latex;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

import java.lang.reflect.Field;

/**
 * Custom ScrollView primarily used to detect when the scroll view "finishes to scroll"
 */
public class VerticalScrollView extends ScrollView {

    /** How much time passes from stop detection */
    private static final int DELAY = 250;

    private ScrollStoppedListener mScrollStoppedListener;

    private Field mFieldFinished;

    private Object mScrollerY;

    private boolean mChecking;

    private final Runnable mCheckIfFinishedRunnable = new Runnable() {

        @Override
        public void run() {
            try {
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

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if (mScrollStoppedListener != null) {
            checkIfStopped();
        }
    }

    /**
     * Sets a listener for the stop event
     * @param listener Listener
     */
    public void setScrollStoppedListener(ScrollStoppedListener listener) {
        mScrollStoppedListener = listener;
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

    private void checkIfStopped() {
        if (mChecking) {
            // Make sure we are only running one check at a time.
            return;
        }
        mChecking = true;
        postDelayed(mCheckIfFinishedRunnable, DELAY);
    }

    public interface ScrollStoppedListener {
        void onStopped();
    }

}

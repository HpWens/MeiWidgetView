package com.meis.widget.photodrag;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * Created by wenshi on 2018/5/18.
 * Description
 */
public class PhotoDragDelegate {

    private float mLastY;
    private boolean mIsBeingDragged;
    private int mTouchSlop;

    private OnPhotoDragListener mDragListener;

    public PhotoDragDelegate(Context context) {
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
    }

    public PhotoDragDelegate setDragListener(OnPhotoDragListener listener) {
        mDragListener = listener;
        return this;
    }

    /**
     * @param ev
     * @return
     */
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mDragListener == null || mDragListener.isAnimationRunning() || mDragListener.getDragView() == null) {
            return false;
        }
        final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
        float y = ev.getRawY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                final float yDiff = Math.abs(y - mLastY);
                if (yDiff > mTouchSlop) {
                    mIsBeingDragged = true;
                } else {
                    mIsBeingDragged = false;
                }
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                break;
        }
        return mIsBeingDragged;
    }

    /**
     * @param ev
     */
    public void onTouchEvent(MotionEvent ev) {
        if (mDragListener == null) return;
        float y = ev.getRawY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = y;
                break;
            case MotionEvent.ACTION_MOVE: {
                float dy = y - mLastY;
                mDragListener.onDrag(dy);
                mLastY = y;
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mDragListener.onRelease();
                break;
        }
    }
}

package com.meis.widget.photodrag;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.meis.widget.R;

/**
 * Created by wenshi on 2018/5/22.
 * Description 仿头条视频拖拽控件 委托的方式拓展支持线性布局 帧布局 约束布局
 * Github https://github.com/HpWens/MeiWidgetView
 */
public class VideoDragRelativeLayout extends RelativeLayout {

    /**
     * touch x y point
     */
    private float mTouchLastY;
    private float mTouchLastX;

    /**
     * y move offset
     */
    private float mMoveDy;

    /**
     * x move offset
     */
    private float mMoveDx;

    /**
     * child view intercept touch events , false not intercept , true intercept
     */
    private boolean mChildIntercept = false;

    /**
     * view can drag , true can drag
     */
    private boolean mDragEnable = true;

    /**
     * animation duration , default 800
     */
    private long mDuration;

    /**
     * compress animator (scale and translation and alpha)
     */
    private ObjectAnimator mCompressAnimator;

    /**
     * exit whether execute transition animator , default true
     */
    private boolean mExitTransitionEnable;

    /**
     * handler parent conflict e.g viewpager
     */
    private boolean mParentConflictEnable = true;

    private static final String TAG_DISPATCH = "dispatch";

    public VideoDragRelativeLayout(Context context) {
        this(context, null);
    }

    public VideoDragRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoDragRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //parse xml attribute
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.VideoDragRelativeLayout);
        mDuration = ta.getInt(R.styleable.VideoDragRelativeLayout_video_drag_duration, 400);
        mExitTransitionEnable = ta.getBoolean(R.styleable.VideoDragRelativeLayout_video_drag_transition, true);
        ta.recycle();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //whether intercept touch event
                return mChildIntercept = childDispatchEvent((int) ev.getRawX(), (int) ev.getRawY());
        }
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * @param touchX touch x point
     * @param touchY touch y point
     * @return true intercept event , false transfer event
     */
    private boolean childDispatchEvent(int touchX, int touchY) {
        return !dispatchChildView(this, touchX, touchY);
    }


    /**
     * @param parentView
     * @param touchX
     * @param touchY
     * @return
     */
    private boolean dispatchChildView(ViewGroup parentView, int touchX, int touchY) {
        boolean isDispatch = false;
        for (int i = parentView.getChildCount() - 1; i >= 0; i--) {
            View childView = parentView.getChildAt(i);
            if (!childView.isShown()) {
                continue;
            }
            boolean isTouchView = isTouchView(touchX, touchY, childView);
            if (isTouchView && childView.getTag() != null && TAG_DISPATCH.equals(childView.getTag().toString())) {
                isDispatch = true;
                break;
            }
            if (childView instanceof ViewGroup) {
                ViewGroup itemView = (ViewGroup) childView;
                if (!isTouchView) {
                    continue;
                } else {
                    isDispatch |= dispatchChildView(itemView, touchX, touchY);
                    break;
                }
            }
        }
        return isDispatch;
    }

    /**
     * @param touchX
     * @param touchY
     * @param view
     * @return
     */
    private boolean isTouchView(int touchX, int touchY, View view) {
        Rect rect = new Rect();
        view.getGlobalVisibleRect(rect);
        return rect.contains(touchX, touchY);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float y = event.getRawY();
        float x = event.getRawX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDragEnable = true;
                mMoveDy = 0;
                mTouchLastX = x;
                mTouchLastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                mDragEnable = true;
                float dy = y - mTouchLastY;
                float dx = x - mTouchLastX;

                mMoveDy += dy;
                mMoveDx += dx;
                mMoveDy = mMoveDy <= 0 ? 0 : mMoveDy;

                //fix parent view sliding conflict
                if (Math.abs(mMoveDx) > Math.abs(mMoveDy)) {
                    if (mParentConflictEnable) {
                        mParentConflictEnable = true;
                        return super.onTouchEvent(event);
                    }
                } else {
                    mParentConflictEnable = false;
                }

                //last two point x , y absolute more than 0
                if (mListener != null && (Math.abs(dy) > 0 || Math.abs(dx) > 0)) {
                    mListener.onStartDrag();
                }

                //1、drag x translation
                setX(getX() + dx);

                //2、set scale pivot (current viewGroup bottom of the middle)
                setPivotX(getWidth() / 2F);
                setPivotY(getHeight());

                //3、set scale
                float scale = 1.0F - mMoveDy / getHeight();
                setScaleX(scale);
                setScaleY(scale);

                //4、drag y translation
                if (scale < 0.5F) {
                    setY(getY() + dy / 2);
                }

                mTouchLastX = x;
                mTouchLastY = y;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mDragEnable = false;
                //prevent multi-click call onRelease
                if (mMoveDy == 0 && mMoveDx == 0) {
                    break;
                }

                final boolean mDismiss = (mMoveDy / getHeight()) > 0.1F;

                //transitions animation
                if (mDismiss && mExitTransitionEnable) {
                    if (mListener != null) {
                        mListener.onRelease(mDismiss);
                    }
                    break;
                }

                //compress animation is running
                if (mCompressAnimator != null && mCompressAnimator.isRunning()) {
                    break;
                }

                //scale animation translation animation alpha animation
                PropertyValuesHolder propertyScaleX = PropertyValuesHolder.ofFloat("scaleX", getScaleX(), mDismiss ? 0.1F : 1.0F);
                PropertyValuesHolder propertyScaleY = PropertyValuesHolder.ofFloat("scaleY", getScaleY(), mDismiss ? 0.1F : 1.0F);
                PropertyValuesHolder propertyTranslationX = PropertyValuesHolder.ofFloat("X", getX(), 0);
                PropertyValuesHolder propertyTranslationY = PropertyValuesHolder.ofFloat("Y", getY(), mDismiss ? -getHeight() / 2 : 0);
                PropertyValuesHolder propertyAlpha = PropertyValuesHolder.ofFloat("alpha", 1.0F, mDismiss ? 0F : 1.0F);

                mCompressAnimator = ObjectAnimator.ofPropertyValuesHolder(this, propertyScaleX, propertyScaleY, propertyTranslationX, propertyTranslationY, propertyAlpha)
                        .setDuration(mDuration);
                mCompressAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (mListener != null) {
                            mListener.onRelease(mDismiss);
                        }
                        if (!mDismiss) {
                            mDragEnable = true;
                        }
                    }
                });
                mCompressAnimator.start();

                mMoveDy = 0;
                mMoveDx = 0;
                break;
        }
        return mDragEnable ? mChildIntercept : super.onTouchEvent(event);
    }

    /**
     * @param exitTransitionEnable
     * @return
     */
    public VideoDragRelativeLayout setExitTransitionEnable(boolean exitTransitionEnable) {
        mExitTransitionEnable = exitTransitionEnable;
        return this;
    }

    /**
     * @param duration
     * @return
     */
    public VideoDragRelativeLayout setDuration(long duration) {
        mDuration = duration;
        return this;
    }

    public long getDuration() {
        return mDuration;
    }

    public boolean getExitTransitionEnable() {
        return mExitTransitionEnable;
    }

    private OnVideoDragListener mListener;

    public VideoDragRelativeLayout setOnVideoDragListener(OnVideoDragListener listener) {
        mListener = listener;
        return this;
    }

    public interface OnVideoDragListener {

        /**
         * start drag
         */
        void onStartDrag();

        /**
         * @param dismiss false start current viewGroup default animation
         *                true  {@link #mExitTransitionEnable}
         */
        void onRelease(boolean dismiss);
    }
}

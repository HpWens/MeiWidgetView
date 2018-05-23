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
        mDuration = ta.getInt(R.styleable.VideoDragRelativeLayout_video_drag_duration, 800);
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
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i).getTag() != null &&
                    getChildAt(i).getTag().toString().equals(TAG_DISPATCH)) {
                Rect rect = new Rect();
                getChildAt(i).getGlobalVisibleRect(rect);
                //current point whether contains current view
                if (rect.contains(touchX, touchY)) {
                    return false;
                }
            }
        }
        return true;
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

                //last two point x , y absolute more than 0
                if (mListener != null && (Math.abs(dy) > 0 || Math.abs(dx) > 0)) {
                    mListener.onStartDrag();
                }

                //1、drag x translation
                setX(getX() + dx);

                mMoveDy += dy;
                mMoveDx += dx;
                mMoveDy = mMoveDy <= 0 ? 0 : mMoveDy;

                //2、set scale pivot (current viewGroup bottom of the middle)
                setPivotX(getWidth() / 2F);
                setPivotY(getHeight());

                //3、set scale
                float scaleX = 1.0F - mMoveDy / getHeight() * 9F / 10F;
                float scaleY = 1.0F - mMoveDy / getHeight() * 6F / 5F;
                setScaleX(scaleX);
                //比例判断缩放 平移
                if (scaleY < 0.2F) {
                    setY(getY() + dy);
                } else {
                    setScaleY(scaleY);
                }

                mTouchLastX = x;
                mTouchLastY = y;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mDragEnable = false;
                //根据垂直比例来实现动画
                if (mMoveDy == 0 && mMoveDx == 0) {
                    break;
                }

                final boolean mDismiss = (mMoveDy / getHeight()) > 0.1F;

                if (mDismiss && mExitTransitionEnable) {
                    if (mListener != null) {
                        mListener.onRelease(mDismiss);
                    }
                    break;
                }

                //判定当前动画是否执行
                if (mCompressAnimator != null && mCompressAnimator.isRunning()) {
                    break;
                }

                //组合动画 x缩放 x平移 y缩放 y平移
                PropertyValuesHolder propertyScaleX = PropertyValuesHolder.ofFloat("scaleX", getScaleX(), mDismiss ? 0.1F : 1.0F);
                PropertyValuesHolder propertyScaleY = PropertyValuesHolder.ofFloat("scaleY", getScaleY(), mDismiss ? 0.1F : 1.0F);
                PropertyValuesHolder propertyTranslationX = PropertyValuesHolder.ofFloat("X", getX(), 0);
                PropertyValuesHolder propertyTranslationY = PropertyValuesHolder.ofFloat("Y", getY(), mDismiss ? -getHeight() / 2 : 0);
                PropertyValuesHolder propertyAlpha = PropertyValuesHolder.ofFloat("alpha", 1.0F, mDismiss ? 0F : 1.0F);

                mCompressAnimator = ObjectAnimator.ofPropertyValuesHolder(this, propertyScaleX, propertyScaleY, propertyTranslationX, propertyTranslationY, propertyAlpha).setDuration(mDuration);
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
         *                true  {@link #mExitTransitionEnable true 执行转场动画  false执行内部消失动画}
         */
        void onRelease(boolean dismiss);
    }
}

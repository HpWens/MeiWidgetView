package com.meis.widget.photodrag;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.meis.widget.R;
import com.meis.widget.utils.DensityUtil;

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
     * self intercept event default false
     */
    private boolean mSelfIntercept = false;

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
     * [0~1]
     */
    private float mAutoDismissRatio;

    /**
     * handler parent conflict e.g viewpager
     */
    private boolean mParentConflictEnable = true;

    private int mOriginX;
    private int mOriginY;
    private int mOriginWidth;
    private int mOriginHeight;

    //improve user experience
    private int mOriginMaxVisibleHeight;

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
        mDuration = ta.getInt(R.styleable.VideoDragRelativeLayout_video_drag_duration, 1000);
        mExitTransitionEnable = ta.getBoolean(R.styleable.VideoDragRelativeLayout_video_drag_transition, true);
        mSelfIntercept = ta.getBoolean(R.styleable.VideoDragRelativeLayout_video_drag_self_intercept, false);
        mAutoDismissRatio = ta.getFloat(R.styleable.VideoDragRelativeLayout_video_drag_auto_dismiss_ratio, 0.1F);
        mAutoDismissRatio = mAutoDismissRatio < 0F ? 0F : (mAutoDismissRatio > 1F ? 1F : mAutoDismissRatio);
        ta.recycle();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //whether intercept touch event
                if (mSelfIntercept) {
                    return super.onInterceptTouchEvent(ev);
                }
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
                mMoveDx = 0;
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
                if (Math.abs(mMoveDx) >= Math.abs(mMoveDy)) {
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
                setTranslationX(getTranslationX() + dx);

                //2、set scale pivot (current viewGroup bottom of the middle)
                setPivotX(getWidth() / 2F);
                setPivotY(getHeight());

                //3、set scale
                float scale = 1.0F - mMoveDy / getHeight();
                setScaleX(scale);
                setScaleY(scale);

                //4、drag y translation
                if (scale < 0.5F) {
                    setTranslationY(getTranslationY() + dy / 2);
                }

                mTouchLastX = x;
                mTouchLastY = y;

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mParentConflictEnable) {
                    return super.onTouchEvent(event);
                }
                mDragEnable = false;
                //prevent the second drag
                mParentConflictEnable = true;
                //prevent multi-click call onRelease
                if (mMoveDy == 0 && mMoveDx == 0) {
                    break;
                }

                final boolean mDismiss = (mMoveDy / getHeight()) > mAutoDismissRatio;

                //transitions animation
                if (mDismiss && mExitTransitionEnable) {
                    endTransitionAnimator();
                    break;
                }

                //compress animation is running
                if (mCompressAnimator != null && mCompressAnimator.isRunning()) {
                    break;
                }

                //scale animation translation animation alpha animation
                PropertyValuesHolder propertyScaleX = PropertyValuesHolder.ofFloat("scaleX", getScaleX(), mDismiss ? mAutoDismissRatio : 1.0F);
                PropertyValuesHolder propertyScaleY = PropertyValuesHolder.ofFloat("scaleY", getScaleY(), mDismiss ? mAutoDismissRatio : 1.0F);
                PropertyValuesHolder propertyTranslationX = PropertyValuesHolder.ofFloat("translationX", getTranslationX(), 0);
                PropertyValuesHolder propertyTranslationY = PropertyValuesHolder.ofFloat("translationY", getTranslationY(), mDismiss ? -getHeight() / 2 : 0);
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

    public void setOriginData(Rect rect, int maxVisibleHeight) {
        if (rect != null) {
            mOriginX = rect.left;
            mOriginY = rect.top;

            mOriginWidth = rect.right - mOriginX;
            mOriginHeight = rect.bottom - mOriginY;

            mOriginMaxVisibleHeight = maxVisibleHeight;
        }
    }

    public void setOriginData(int[] origins) {
        if (origins != null && origins.length == 5) {
            setOriginData(new Rect(origins[0], origins[1], origins[2], origins[3]), origins[4]);
        }
    }

    public void setOriginData(int left, int top, int right, int bottom, int maxVisibleHeight) {
        setOriginData(new Rect(left, top, right, bottom), maxVisibleHeight);
    }

    ValueAnimator mEndAnimator;

    private void endTransitionAnimator() {
        if (mEndAnimator != null && mEndAnimator.isRunning()) {
            return;
        }
        if (mOriginHeight != 0 && mOriginWidth != 0) {
            final float startTransitionX = getTranslationX();
            final float startTransitionY = getTranslationY();
            final float startScaleX = getScaleX();
            final float startScaleY = getScaleY();
            final float endScaleX = (float) mOriginWidth / getWidth();
            final float endScaleY = (float) mOriginMaxVisibleHeight / getHeight();
            //DensityUtil.getStatusBarHeight((Activity) getContext());
            final int statusHeight = 0;

            boolean upperOutOfBound = false;
            // +1 prevent errors
            if ((mOriginHeight + 1) < mOriginMaxVisibleHeight) {
                if ((mOriginY + mOriginMaxVisibleHeight) > getHeight()) {
                    //下边界越界
                } else {
                    //上边界越界
                    upperOutOfBound = true;
                }
            }

            final boolean outOfBound = upperOutOfBound;
            mEndAnimator = ValueAnimator.ofFloat(0F, 1.0F).setDuration(mDuration);
            mEndAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();

                    setScaleX(startScaleX + value * (endScaleX - startScaleX));
                    setScaleY(startScaleY + value * (endScaleY - startScaleY));

                    setTranslationX(startTransitionX + value * (mOriginX - startTransitionX) - value * (getWidth() - mOriginWidth) / 2.0F);
                    //注意状态栏的高度 前一个界面无状态栏则去掉 + statusHeight
                    setTranslationY(startTransitionY - value * (startTransitionY - mOriginY) - value * (getHeight() - mOriginMaxVisibleHeight + statusHeight) - (outOfBound ? value * (mOriginMaxVisibleHeight - mOriginHeight) : 0));
                }
            });
            mEndAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mSelfIntercept = false;
                    mChildIntercept = true;
                    if (mListener != null) {
                        mListener.onRelease(true);
                    }
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    mSelfIntercept = true;
                    mChildIntercept = false;
                }
            });
            mEndAnimator.start();
        }
    }

    public void onBackPressed() {
        startTransitionAnimator(true);
    }

    public void startTransitionAnimator() {
        startTransitionAnimator(false);
    }

    ValueAnimator mStartAnimator;

    private void startTransitionAnimator(final boolean exitEnable) {
        if (mStartAnimator != null && mStartAnimator.isRunning()) {
            return;
        }
        if (mEndAnimator != null && mEndAnimator.isRunning()) {
            return;
        }
        if (mOriginHeight != 0 && mOriginWidth != 0) {
            setPivotX(0);
            setPivotY(0);
            //DensityUtil.getStatusBarHeight((Activity) getContext());
            final int statusHeight = 0;

            //具体场景 可以替换成  getWidth()  getHeight()
            boolean upperOutOfBound = false;
            int screenHeight = DensityUtil.getScreenSize(getContext()).y;
            final float startScaleX = (float) mOriginWidth / DensityUtil.getScreenSize(getContext()).x;
            final float startScaleY = (float) mOriginMaxVisibleHeight / screenHeight;

            // +1 prevent errors
            if ((mOriginHeight + 1) < mOriginMaxVisibleHeight) {
                //上边界越界 或者 下边界越界
                if ((mOriginY + mOriginMaxVisibleHeight) > screenHeight) {
                    //下边界越界
                } else {
                    //上边界越界
                    upperOutOfBound = true;
                }
            }

            Log.e("-------------", "***************aaa" + mOriginX + "***" + mOriginY + "****" + mOriginWidth + "***" + mOriginHeight
                    + "*****" + startScaleX + "*****" + startScaleY + "****" + mOriginMaxVisibleHeight + "****" + DensityUtil.getScreenSize(getContext()).x);

            final boolean outOfBound = upperOutOfBound;
            mStartAnimator = ValueAnimator.ofFloat(exitEnable ? 1.0F : 0F, exitEnable ? 0F : 1.0F).setDuration(mDuration);
            mStartAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    setTranslationX(mOriginX - value * mOriginX);
                    setTranslationY((mOriginY - statusHeight) - value * (mOriginY - statusHeight) - (outOfBound ? (1.0F - value) * (mOriginMaxVisibleHeight - mOriginHeight) : 0));

                    setScaleX(startScaleX + value * (1.0F - startScaleX));
                    setScaleY(startScaleY + value * (1.0F - startScaleY));
                }
            });
            mStartAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mSelfIntercept = false;
                    if (mListener != null && exitEnable) {
                        mListener.onRelease(true);
                    }
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    mSelfIntercept = true;
                }
            });
            mStartAnimator.start();
        }
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

    /**
     * @param autoDismissRatio [0~1]
     * @return
     */
    public VideoDragRelativeLayout setAutoDismissRatio(float autoDismissRatio) {
        autoDismissRatio = autoDismissRatio < 0F ? 0F : (autoDismissRatio > 1F ? 1F : autoDismissRatio);
        mAutoDismissRatio = autoDismissRatio;
        return this;
    }

    /**
     * @param selfIntercept
     */
    public VideoDragRelativeLayout setSelfIntercept(boolean selfIntercept) {
        mSelfIntercept = selfIntercept;
        return this;
    }

    public boolean getSelfIntercept() {
        return mSelfIntercept;
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

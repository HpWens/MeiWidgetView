package com.meis.widget.photodrag;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.support.v4.view.ViewCompat;
import android.view.View;

/**
 * Created by wenshi on 2018/5/18.
 * Description
 */
public class PhotoDragHelper implements OnPhotoDragListener {

    private static final int DURATION = 200;
    private static final int DRAG_SLOP_HEIGHT = 320;

    private int mDragSlopHeight = DRAG_SLOP_HEIGHT;
    private int mDuration = DURATION;

    /**
     * 拖动释放的动画
     */
    private ObjectAnimator mBackOrExitAnimator;

    private OnDragListener mListener;

    @Override
    public boolean isAnimationRunning() {
        return mBackOrExitAnimator == null ? false : mBackOrExitAnimator.isRunning();
    }

    @Override
    public void onDrag(float dy) {
        if (mListener != null) {
            View dragView = mListener.getDragView();
            if (dragView != null) {
                float currentY = ViewCompat.getTranslationY(dragView);
                float finalY = currentY + dy;
                ViewCompat.setTranslationY(dragView, finalY);
                setAlpha(finalY);
            }
        }
    }

    @Override
    public void onRelease() {
        if (mListener != null) {
            View dragView = mListener.getDragView();
            if (dragView != null) {
                float from = ViewCompat.getTranslationY(dragView);
                final boolean isExit = Math.abs(from) > mDragSlopHeight;
                float to = 0;
                to = isExit ? ((from > 0) ? dragView.getHeight() : -dragView.getHeight()) : 0;
                endAnimation();
                mBackOrExitAnimator = ObjectAnimator.ofFloat(dragView, "TranslationY", from, to).setDuration(mDuration);
                mBackOrExitAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float value = (float) animation.getAnimatedValue();
                        setAlpha(value);
                    }
                });
                mBackOrExitAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mListener.onAnimationEnd(isExit);
                    }
                });
                mBackOrExitAnimator.start();
            }
        }
    }

    @Override
    public View getDragView() {
        return mListener == null ? null : mListener.getDragView();
    }

    /**
     * @param offsetY Y移动偏移量
     */
    private void setAlpha(float offsetY) {
        float allFadeY = mDragSlopHeight * 2;
        float offsetTranslationY = Math.abs(offsetY);
        if (offsetTranslationY > allFadeY) {
            offsetTranslationY = allFadeY;
        }
        if (mListener != null) {
            mListener.onAlpha(1 - offsetTranslationY / allFadeY);
        }
    }

    private void endAnimation() {
        if (null != mBackOrExitAnimator && mBackOrExitAnimator.isRunning()) {
            mBackOrExitAnimator.cancel();
        }
    }

    public PhotoDragHelper setDragSlopHeight(int dragSlopHeight) {
        mDragSlopHeight = dragSlopHeight;
        return this;
    }

    public PhotoDragHelper setDuration(int duration) {
        mDuration = duration;
        return this;
    }

    public PhotoDragHelper setOnDragListener(OnDragListener listener) {
        this.mListener = listener;
        return this;
    }

    public interface OnDragListener {
        /**
         * @param alpha (1~0)
         */
        void onAlpha(float alpha);

        /**
         * 获取拖拽view
         */
        View getDragView();

        /**
         * 动画结束
         *
         * @param mSlop true you can finish current activity
         */
        void onAnimationEnd(boolean mSlop);
    }
}

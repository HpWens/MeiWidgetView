package com.meis.widget.horizontal;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.meis.widget.utils.DensityUtil;

/**
 * Created by wenshi on 2018/12/12.
 * Description 豆瓣弹性滑动控件
 */
public class HorizontalScrollView extends RelativeLayout {

    private static final String SCROLL_MORE = "左滑看更多";
    private static final String RELEASE_MORE = "松手看更多";

    private RecyclerView mHorizontalRecyclerView;
    private VerticalTextView mMoreTextView;

    private boolean mShowMore = true;
    private float mHintLeftMargin = 0;
    private int mOffsetWidth = 0;
    private float mLastX;
    private float mLastY;
    private boolean mConsumeMoveEvent = false;
    private int mMoveIndex = 0;

    // 回弹动画
    private ValueAnimator ReboundAnim;

    private static final float RATIO = 0.4f;

    public HorizontalScrollView(Context context) {
        this(context, null);
    }

    public HorizontalScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mOffsetWidth = -DensityUtil.dip2px(context, 65);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mMoreTextView != null) {
            mOffsetWidth = -mMoreTextView.getWidth();
            mOffsetWidth = mOffsetWidth == 0 ? -DensityUtil.dip2px(getContext(), 65) : mOffsetWidth;
        }
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        child.setLayoutParams(params);
        addView(child);
    }

    @Override
    public void addView(final View child) {
        if (child instanceof RecyclerView) {
            mHorizontalRecyclerView = (RecyclerView) child;
        } else if (child instanceof VerticalTextView) {
            mMoreTextView = (VerticalTextView) child;
        }
        super.addView(child);
    }

    private ViewParent getParentListView(ViewParent viewParent) {
        if (viewParent == null) return null;
        if (viewParent instanceof RecyclerView || viewParent instanceof ListView) {
            return viewParent;
        } else {
            getParentListView(viewParent.getParent());
        }
        return null;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mHorizontalRecyclerView == null) {
            return super.dispatchTouchEvent(ev);
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mHintLeftMargin = 0;
                mMoveIndex = 0;
                mConsumeMoveEvent = false;
                mLastX = ev.getRawX();
                mLastY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                // 释放动画
                if (ReboundAnim != null && ReboundAnim.isRunning()) {
                    break;
                }
                float mDeltaX = (ev.getRawX() - mLastX);
                float mDeltaY = ev.getRawY() - mLastY;

                if (!mConsumeMoveEvent) {
                    // 处理事件冲突
                    if (Math.abs(mDeltaX) > Math.abs(mDeltaY)) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    } else {
                        getParent().requestDisallowInterceptTouchEvent(false);
                    }
                }

                mMoveIndex++;

                if (mMoveIndex > 2) {
                    mConsumeMoveEvent = true;
                }

                mLastX = ev.getRawX();
                mLastY = ev.getRawY();
                mDeltaX = mDeltaX * RATIO;

                // 右滑
                if (mDeltaX > 0) {
                    if (!mHorizontalRecyclerView.canScrollHorizontally(-1) || mHorizontalRecyclerView.getTranslationX() < 0) {
                        float transX = mDeltaX + mHorizontalRecyclerView.getTranslationX();
                        if (mHorizontalRecyclerView.canScrollHorizontally(-1) && transX >= 0) {
                            transX = 0;
                        }
                        mHorizontalRecyclerView.setTranslationX(transX);
                        setHintTextTranslationX(mDeltaX);
                    }
                } else if (mDeltaX < 0) { // 左滑
                    if (!mHorizontalRecyclerView.canScrollHorizontally(1) || mHorizontalRecyclerView.getTranslationX() > 0) {
                        float transX = mDeltaX + mHorizontalRecyclerView.getTranslationX();
                        if (transX <= 0 && mHorizontalRecyclerView.canScrollHorizontally(1)) {
                            transX = 0;
                        }
                        mHorizontalRecyclerView.setTranslationX(transX);
                        setHintTextTranslationX(mDeltaX);
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                getParent().requestDisallowInterceptTouchEvent(false);
                // 释放动画
                if (ReboundAnim != null && ReboundAnim.isRunning()) {
                    break;
                }

                if (mOffsetWidth != 0 && mHintLeftMargin <= mOffsetWidth && mListener != null) {
                    mListener.onRelease();
                }

                ReboundAnim = ValueAnimator.ofFloat(1.0f, 0);
                ReboundAnim.setDuration(300);
                ReboundAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float value = (float) animation.getAnimatedValue();
                        mHorizontalRecyclerView.setTranslationX(value * mHorizontalRecyclerView.getTranslationX());
                        mMoreTextView.setTranslationX(value * mMoreTextView.getTranslationX());
                    }
                });
                ReboundAnim.start();

                break;

        }
        return mHorizontalRecyclerView.getTranslationX() != 0 ? true : super.dispatchTouchEvent(ev);
    }

    private void setHintTextTranslationX(float deltaX) {
        if (mShowMore) {
            float offsetX = 0;
            if (mMoreTextView != null) {
                mHintLeftMargin += deltaX;
                if (mHintLeftMargin <= mOffsetWidth) {
                    offsetX = mOffsetWidth;
                    mMoreTextView.setVerticalText(RELEASE_MORE);
                } else {
                    offsetX = mHintLeftMargin;
                    mMoreTextView.setVerticalText(SCROLL_MORE);
                }
                mMoreTextView.setOffset(offsetX, mOffsetWidth);
                mMoreTextView.setTranslationX(offsetX);
            }
        }
    }

    public boolean getShowMore() {
        return mShowMore;
    }

    public void setShowMore(boolean showMore) {
        mShowMore = showMore;
    }

    public interface OnReleaseListener {
        void onRelease();
    }

    private OnReleaseListener mListener;

    public void setOnReleaseListener(OnReleaseListener listener) {
        this.mListener = listener;
    }
}

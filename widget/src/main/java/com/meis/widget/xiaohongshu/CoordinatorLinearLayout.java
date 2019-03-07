package com.meis.widget.xiaohongshu;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.OverScroller;

/**
 * Created by wenshi on 2019/3/6.
 * Description
 */
public class CoordinatorLinearLayout extends LinearLayout {

    // 是否展开
    private boolean mIsExpand;

    private OverScroller mOverScroller;

    // 快速抛的最小速度
    private int mMinFlingVelocity;

    // 滚动最大距离 = 图片裁剪控件的高度
    private int mScrollRange;

    // 滚动监听接口
    private OnScrollListener mListener;

    // 最大展开因子
    private static final int MAX_EXPAND_FACTOR = 6;
    // 滚动时长
    private static final int SCROLL_DURATION = 500;

    public CoordinatorLinearLayout(Context context) {
        this(context, null);
    }

    public CoordinatorLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoordinatorLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mOverScroller = new OverScroller(context);
        mMinFlingVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
        // 设置默认值 =  图片裁剪控件的宽度
        mScrollRange = context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 触摸点是否view的区域内
     *
     * @param view
     * @param touchX
     * @param touchY
     * @return
     */
    private boolean isTouchView(View view, int touchX, int touchY) {
        Rect rect = new Rect();
        view.getGlobalVisibleRect(rect);
        return rect.contains(touchX, touchY);
    }

    public void setExpand(boolean expand) {
        mIsExpand = expand;
    }

    // 是否处于展开状态
    public boolean isExpanding() {
        return mIsExpand;
    }


    /**
     * @param y                    相对RecyclerView的距离
     * @param deltaY               偏移量
     * @param maxParentScrollRange 最大滚动距离
     */
    public void onScroll(float y, float deltaY, int maxParentScrollRange) {
        int scrollY = getScrollY();
        int currentScrollY = (int) (scrollY + deltaY);

        if (mScrollRange != maxParentScrollRange) {
            mScrollRange = maxParentScrollRange;
        }

        // 越界检测
        if (currentScrollY > maxParentScrollRange) {
            currentScrollY = maxParentScrollRange;
        } else if (currentScrollY < 0) {
            currentScrollY = 0;
        }

        // 处于展开状态
        if (y <= 0) {
            setScrollY(currentScrollY);
        } else if (y > 0 && scrollY != 0) { // 处于收起状态
            setScrollY(currentScrollY);
        }

        if (mListener != null) {
            mListener.onScroll(getScrollY());
        }
    }

    /**
     * @param velocityY y方向速度
     */
    public void onFiling(int velocityY) {
        int scrollY = getScrollY();
        // 判定非临界状态
        if (scrollY != 0 && scrollY != mScrollRange) {

            // y轴速度是否大于最小抛速度
            if (Math.abs(velocityY) > mMinFlingVelocity) {
                if (velocityY > mScrollRange || velocityY < -mScrollRange) {
                    startScroll(velocityY > mScrollRange);
                } else {
                    collapseOrExpand(scrollY);
                }
            } else {
                collapseOrExpand(scrollY);
            }
        }
    }

    /**
     * 展开或收起
     *
     * @param scrollY
     */
    private void collapseOrExpand(int scrollY) {
        // MAX_EXPAND_FACTOR = 6
        int maxExpandY = mScrollRange / MAX_EXPAND_FACTOR;
        if (isExpanding()) {
            startScroll(scrollY < maxExpandY);
        } else {
            startScroll(scrollY < (mScrollRange - maxExpandY));
        }
    }

    /**
     * 开始滚动
     *
     * @param isExpand 是否展开
     */
    private void startScroll(boolean isExpand) {
        mIsExpand = isExpand;

        if (mListener != null) {
            mListener.isExpand(isExpand);
            if (mIsExpand) {
                // 必须保证滚动完成 再触发回调
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mListener.completeExpand();
                    }
                }, SCROLL_DURATION);
            }
        }

        if (!mOverScroller.isFinished()) {
            mOverScroller.abortAnimation();
        }

        int dy = isExpand ? -getScrollY() : mScrollRange - getScrollY();
        // SCROLL_DURATION = 500
        mOverScroller.startScroll(0, getScrollY(), 0, dy, SCROLL_DURATION);
        postInvalidate();
    }

    @Override
    public void computeScroll() {
        // super.computeScroll();
        if (mOverScroller.computeScrollOffset()) {
            setScrollY(mOverScroller.getCurrY());
            postInvalidate();
        }

    }

    public interface OnScrollListener {

        void onScroll(int scrollY);

        /**
         * @param isExpand 是否展开
         */
        void isExpand(boolean isExpand);

        // 完全展开
        void completeExpand();
    }

    public void setOnScrollListener(OnScrollListener listener) {
        mListener = listener;
    }
}

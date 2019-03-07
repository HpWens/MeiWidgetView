package com.meis.widget.xiaohongshu;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

/**
 * Created by wenshi on 2019/3/6.
 * Description
 */
public class CoordinatorRecyclerView extends RecyclerView {

    private int mTouchSlop = -1;
    private VelocityTracker mVelocityTracker;
    // 是否重新测量用于改变RecyclerView的高度
    private boolean mIsAgainMeasure = true;
    // 是否展开 默认为true
    private boolean mIsExpand = true;
    // 父类最大的滚动区域 = 裁剪控件的高度
    private int mMaxParentScrollRange;
    // 父控件在y方向滚动的距离
    private int mCurrentParenScrollY = 0;
    // 最后RawY坐标
    private float mLastRawY = 0;
    private float mDeltaRawY = 0;
    // 是否消费touch事件 true 消费RecyclerView接受不到滚动事件
    private boolean mIsConsumeTouchEvent = false;
    // 回调接口
    private OnCoordinatorListener mListener;

    // 兼容点击事件失效
    private float mDownRawY = 0;

    public CoordinatorRecyclerView(@NonNull Context context) {
        this(context, null);
    }

    public CoordinatorRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoordinatorRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mVelocityTracker = VelocityTracker.obtain();
        mMaxParentScrollRange = context.getResources().getDisplayMetrics().widthPixels;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        if (mListener == null) {
            return super.onTouchEvent(e);
        }

        if (mTouchSlop < 0) {
            mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        }

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 重置数据
                resetData();

                mLastRawY = e.getRawY();
                mDownRawY = e.getRawY();

                if (mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain();
                } else {
                    mVelocityTracker.clear();
                }
                mVelocityTracker.addMovement(e);
                break;
            case MotionEvent.ACTION_MOVE:
                // y 相对于 RecyclerView y坐标
                float y = e.getY();
                measureRecyclerHeight(y);

                if (mLastRawY == 0) {
                    mLastRawY = e.getRawY();
                }

                mDeltaRawY = mLastRawY - e.getRawY();

                if (mIsExpand) {
                    // 展开
                    mListener.onScroll(y, mDeltaRawY, mMaxParentScrollRange);
                } else {
                    // 收起 canScrollVertically 判定是否滑动到底部
                    if (!mIsConsumeTouchEvent && !canScrollVertically(-1)) {
                        mIsConsumeTouchEvent = true;
                    }
                    if (mIsConsumeTouchEvent && mDeltaRawY != 0) {
                        mListener.onScroll(y, mDeltaRawY, mMaxParentScrollRange);
                    }
                }

                // 处于非临界状态
                mIsConsumeTouchEvent = mCurrentParenScrollY > 0 & mCurrentParenScrollY < mMaxParentScrollRange;

                mVelocityTracker.addMovement(e);

                mLastRawY = e.getRawY();

                if (y < 0 || mIsConsumeTouchEvent) {
                    return false;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                // 重置数据
                resetData();

                mLastRawY = 0;

                // 处理滑动速度
                mVelocityTracker.addMovement(e);
                mVelocityTracker.computeCurrentVelocity(1000);

                int velocityY = (int) Math.abs(mVelocityTracker.getYVelocity());
                mListener.onFiling(mDeltaRawY > 0 ? -velocityY : velocityY);
                mDeltaRawY = 0;
                y = e.getY();

                // 处理子view点击事件失效
                final int yDiff = (int) Math.abs(e.getRawY() - mDownRawY);
                if (yDiff < mTouchSlop) {
                    mListener.handlerInvalidClick((int) e.getRawX(), (int) e.getRawY());
                }

                if (y < 0) {
                    return false;
                }
                break;
        }

        return super.onTouchEvent(e);
    }

    private void resetData() {
        // 重置数据
        mIsAgainMeasure = true;
        // 展开为0 不然会最大高度
        mCurrentParenScrollY = mIsExpand ? 0 : mMaxParentScrollRange;
        mIsConsumeTouchEvent = false;
    }

    public void setMaxParentScrollRange(int maxParentScrollRange) {
        mMaxParentScrollRange = maxParentScrollRange;
    }

    /**
     * @param y 手指相对RecyclerView的y轴坐标
     *          y <= 0 表示手指已经滑出RecyclerView顶部
     */
    private void measureRecyclerHeight(float y) {
        if (y <= 0 && mIsAgainMeasure) {
            if (getHeight() < mMaxParentScrollRange && mIsExpand) {
                mIsAgainMeasure = false;
                getLayoutParams().height = getHeight() + mMaxParentScrollRange;
                requestLayout();
            }
        }
    }

    // 重置高度
    public void resetRecyclerHeight() {
        if (getHeight() > mMaxParentScrollRange && mIsExpand && mIsAgainMeasure) {
            getLayoutParams().height = getHeight() - mMaxParentScrollRange;
            requestLayout();
        }
    }


    @Override
    protected void onDetachedFromWindow() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
        super.onDetachedFromWindow();
    }

    // 设置是否展开
    public void setExpand(boolean expand) {
        mIsExpand = expand;
    }

    // 设置当前滚动的y轴距离
    public void setCurrentParenScrollY(int currentParenScrollY) {
        mCurrentParenScrollY = currentParenScrollY;
    }

    public interface OnCoordinatorListener {
        /**
         * @param y                    相对RecyclerView的距离
         * @param deltaY               偏移量
         * @param maxParentScrollRange 最大滚动距离
         */
        void onScroll(float y, float deltaY, int maxParentScrollRange);

        /**
         * @param velocityY y方向速度
         */
        void onFiling(int velocityY);

        // 处理子view的点击失效
        void handlerInvalidClick(int rawX, int rawY);
    }

    public void setOnCoordinatorListener(OnCoordinatorListener listener) {
        this.mListener = listener;
    }

}

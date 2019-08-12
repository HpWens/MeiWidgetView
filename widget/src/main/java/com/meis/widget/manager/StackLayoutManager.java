package com.meis.widget.manager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.List;

/**
 * @author wenshi
 * @github
 * @Description
 * @since 2019/6/26
 */
public class StackLayoutManager extends RecyclerView.LayoutManager {

    /**
     * 一次完整的聚焦滑动所需要的移动距离
     */
    private float onceCompleteScrollLength = -1;

    /**
     * 第一个子view的偏移量
     */
    private float firstChildCompleteScrollLength = -1;

    /**
     * 屏幕可见第一个view的position
     */
    private int mFirstVisiPos;

    /**
     * 屏幕可见的最后一个view的position
     */
    private int mLastVisiPos;

    /**
     * 水平方向累计偏移量
     */
    private long mHorizontalOffset;

    /**
     * 普通view之间的margin
     */
    private float normalViewGap = 30;

    private int childWidth = 0;

    /**
     * 是否自动选中
     */
    private boolean isAutoSelect = true;
    private ValueAnimator selectAnimator;

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    public StackLayoutManager(Context context, int gap) {
        normalViewGap = dp2px(context, gap);
    }

    public StackLayoutManager(Context context) {
        this(context, 0);
    }

    public static float dp2px(Context context, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        //  super.onLayoutChildren(recycler, state);
        if (state.getItemCount() == 0) {
            removeAndRecycleAllViews(recycler);
            return;
        }

        onceCompleteScrollLength = -1;

        // 分离全部已有的view 放入临时缓存  mAttachedScrap 集合中
        detachAndScrapAttachedViews(recycler);

        fill(recycler, state, 0);
    }

    private int fill(RecyclerView.Recycler recycler, RecyclerView.State state, int dx) {
        int resultDelta = dx;
        resultDelta = fillHorizontalLeft(recycler, state, dx);
        recycleChildren(recycler);
        return resultDelta;
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }


    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        // 手指从右向左滑动，dx > 0; 手指从左向右滑动，dx < 0;
        // 位移0、没有子View 当然不移动
        if (dx == 0 || getChildCount() == 0) {
            return 0;
        }

        float realDx = dx / 1.0f;
        if (Math.abs(realDx) < 0.00000001f) {
            return 0;
        }

        mHorizontalOffset += dx;

        dx = fill(recycler, state, dx);

        return dx;
    }

    /**
     * 最大偏移量
     *
     * @return
     */
    private float getMaxOffset() {
        if (childWidth == 0 || getItemCount() == 0) return 0;
        // getWidth() / 2 + childWidth / 2 +
        return (childWidth + normalViewGap) * (getItemCount() - 1);
    }

    /**
     * 获取最小的偏移量
     *
     * @return
     */
    private float getMinOffset() {
        if (childWidth == 0) return 0;
        return (getWidth() - childWidth) / 2;
    }

    private int fillHorizontalLeft(RecyclerView.Recycler recycler, RecyclerView.State state, int dx) {
        //----------------1、边界检测-----------------
        if (dx < 0) {
            // 已到达左边界
            if (mHorizontalOffset < 0) {
                mHorizontalOffset = dx = 0;
            }
        }

        if (dx > 0) {
            if (mHorizontalOffset >= getMaxOffset()) {
                // 因为在因为scrollHorizontallyBy里加了一次dx，现在减回去
                // mHorizontalOffset -= dx;
                mHorizontalOffset = (long) getMaxOffset();
                dx = 0;
            }
        }

        // 分离全部的view，加入到临时缓存
        detachAndScrapAttachedViews(recycler);

        float startX = 0;
        float fraction = 0f;
        boolean isChildLayoutLeft = true;

        View tempView = null;
        int tempPosition = -1;

        if (onceCompleteScrollLength == -1) {
            // 因为mFirstVisiPos在下面可能被改变，所以用tempPosition暂存一下
            tempPosition = mFirstVisiPos;
            tempView = recycler.getViewForPosition(tempPosition);
            measureChildWithMargins(tempView, 0, 0);
            childWidth = getDecoratedMeasurementHorizontal(tempView);
        }

        // 修正第一个可见view mFirstVisiPos 已经滑动了多少个完整的onceCompleteScrollLength就代表滑动了多少个item
        firstChildCompleteScrollLength = getWidth() / 2 + childWidth / 2;
        if (mHorizontalOffset >= firstChildCompleteScrollLength) {
            startX = normalViewGap;
            onceCompleteScrollLength = childWidth + normalViewGap;
            mFirstVisiPos = (int) Math.floor(Math.abs(mHorizontalOffset - firstChildCompleteScrollLength) / onceCompleteScrollLength) + 1;
            fraction = (Math.abs(mHorizontalOffset - firstChildCompleteScrollLength) % onceCompleteScrollLength) / (onceCompleteScrollLength * 1.0f);
        } else {
            mFirstVisiPos = 0;
            startX = getMinOffset();
            onceCompleteScrollLength = firstChildCompleteScrollLength;
            fraction = (Math.abs(mHorizontalOffset) % onceCompleteScrollLength) / (onceCompleteScrollLength * 1.0f);
        }

        // 临时将mLastVisiPos赋值为getItemCount() - 1，放心，下面遍历时会判断view是否已溢出屏幕，并及时修正该值并结束布局
        mLastVisiPos = getItemCount() - 1;

        float normalViewOffset = onceCompleteScrollLength * fraction;
        boolean isNormalViewOffsetSetted = false;

        //----------------3、开始布局-----------------
        for (int i = mFirstVisiPos; i <= mLastVisiPos; i++) {
            View item;
            if (i == tempPosition && tempView != null) {
                // 如果初始化数据时已经取了一个临时view
                item = tempView;
            } else {
                item = recycler.getViewForPosition(i);
            }

            int focusPosition = (int) (Math.abs(mHorizontalOffset) / (childWidth + normalViewGap));
            if (i <= focusPosition) {
                addView(item);
            } else {
                addView(item, 0);
            }
            measureChildWithMargins(item, 0, 0);

            if (!isNormalViewOffsetSetted) {
                startX -= normalViewOffset;
                isNormalViewOffsetSetted = true;
            }

            int l, t, r, b;
            l = (int) startX;
            t = getPaddingTop();
            r = l + getDecoratedMeasurementHorizontal(item);
            b = t + getDecoratedMeasurementVertical(item);

            // 缩放子view
            final float minScale = 0.6f;
            float currentScale = 0f;
            final int childCenterX = (r + l) / 2;
            final int parentCenterX = getWidth() / 2;
            isChildLayoutLeft = childCenterX <= parentCenterX;
            if (isChildLayoutLeft) {
                final float fractionScale = (parentCenterX - childCenterX) / (parentCenterX * 1.0f);
                currentScale = 1.0f - (1.0f - minScale) * fractionScale;
            } else {
                final float fractionScale = (childCenterX - parentCenterX) / (parentCenterX * 1.0f);
                currentScale = 1.0f - (1.0f - minScale) * fractionScale;
            }
            item.setScaleX(currentScale);
            item.setScaleY(currentScale);
            // item.setAlpha(currentScale);

            layoutDecoratedWithMargins(item, l, t, r, b);

            startX += (childWidth + normalViewGap);

            if (startX > getWidth() - getPaddingRight()) {
                mLastVisiPos = i;
                break;
            }
        }

        return dx;
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        switch (state) {
            case RecyclerView.SCROLL_STATE_DRAGGING:
                //当手指按下时，停止当前正在播放的动画
                cancelAnimator();
                break;
            case RecyclerView.SCROLL_STATE_IDLE:
                //当列表滚动停止后，判断一下自动选中是否打开
                if (isAutoSelect) {
                    //找到离目标落点最近的item索引
                    smoothScrollToPosition(findShouldSelectPosition(), null);
                }
                break;
            default:
                break;
        }
    }

    public int findShouldSelectPosition() {
        if (onceCompleteScrollLength == -1 || mFirstVisiPos == -1) {
            return -1;
        }
        int position = (int) (Math.abs(mHorizontalOffset) / (childWidth + normalViewGap));
        int remainder = (int) (Math.abs(mHorizontalOffset) % (childWidth + normalViewGap));
        // 超过一半，应当选中下一项
        if (remainder >= (childWidth + normalViewGap) / 2.0f) {
            if (position + 1 <= getItemCount() - 1) {
                return position + 1;
            }
        }
        return position;
    }

    /**
     * 平滑滚动到某个位置
     *
     * @param position 目标Item索引
     */
    public void smoothScrollToPosition(int position, OnStackListener listener) {
        if (position > -1 && position < getItemCount()) {
            startValueAnimator(position, listener);
        }
    }

    private void startValueAnimator(int position, final OnStackListener listener) {
        cancelAnimator();

        final float distance = getScrollToPositionOffset(position);

        long minDuration = 100;
        long maxDuration = 300;
        long duration;

        float distanceFraction = (Math.abs(distance) / (childWidth + normalViewGap));

        if (distance <= (childWidth + normalViewGap)) {
            duration = (long) (minDuration + (maxDuration - minDuration) * distanceFraction);
        } else {
            duration = (long) (maxDuration * distanceFraction);
        }
        selectAnimator = ValueAnimator.ofFloat(0.0f, distance);
        selectAnimator.setDuration(duration);
        selectAnimator.setInterpolator(new LinearInterpolator());
        final float startedOffset = mHorizontalOffset;
        selectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mHorizontalOffset = (long) (startedOffset + value);
                requestLayout();
            }
        });
        selectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (listener != null) {
                    listener.onFocusAnimEnd();
                }
            }
        });
        selectAnimator.start();
    }

    /**
     * @param position
     * @return
     */
    private float getScrollToPositionOffset(int position) {
        return position * (childWidth + normalViewGap) - Math.abs(mHorizontalOffset);
    }

    /**
     * 取消动画
     */
    public void cancelAnimator() {
        if (selectAnimator != null && (selectAnimator.isStarted() || selectAnimator.isRunning())) {
            selectAnimator.cancel();
        }
    }

    /**
     * 回收需回收的item
     */
    private void recycleChildren(RecyclerView.Recycler recycler) {
        List<RecyclerView.ViewHolder> scrapList = recycler.getScrapList();
        for (int i = 0; i < scrapList.size(); i++) {
            RecyclerView.ViewHolder holder = scrapList.get(i);
            removeAndRecycleView(holder.itemView, recycler);
        }
    }

    /**
     * 获取某个childView在水平方向所占的空间，将margin考虑进去
     *
     * @param view
     * @return
     */
    public int getDecoratedMeasurementHorizontal(View view) {
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                view.getLayoutParams();
        return getDecoratedMeasuredWidth(view) + params.leftMargin
                + params.rightMargin;
    }

    /**
     * 获取某个childView在竖直方向所占的空间,将margin考虑进去
     *
     * @param view
     * @return
     */
    public int getDecoratedMeasurementVertical(View view) {
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                view.getLayoutParams();
        return getDecoratedMeasuredHeight(view) + params.topMargin
                + params.bottomMargin;
    }

    public int getVerticalSpace() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    public int getHorizontalSpace() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    public interface OnStackListener {
        void onFocusAnimEnd();
    }
}

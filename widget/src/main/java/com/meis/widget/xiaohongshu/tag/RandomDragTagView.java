package com.meis.widget.xiaohongshu.tag;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.meis.widget.R;

/**
 * Created by wenshi on 2019/3/12.
 * Description
 */
public class RandomDragTagView extends LinearLayout {

    // 左侧视图
    private LinearLayout mLeftLayout;
    private TextView mLeftText;
    private View mLeftLine;
    // 右侧视图
    private LinearLayout mRightLayout;
    private TextView mRightText;
    private View mRightLine;
    // 中间视图
    private View mBreathingView;
    private FrameLayout mBreathingLayout;

    // 是否显示左侧视图  默认显示左侧视图
    private boolean mIsShowLeftView = true;

    // 呼吸灯动画
    private ValueAnimator mBreathingAnimator;
    // 回弹动画
    private ValueAnimator mReboundAnimator;
    private float mStartReboundX;
    private float mStartReboundY;
    private float mLastMotionRawY;
    private float mLastMotionRawX;

    // 是否多跟手指按下
    private boolean mPointerDown = false;
    private int mTouchSlop = -1;

    // 是否可以拖拽
    private boolean mCanDrag = true;

    // 是否可以拖拽出父控件区域
    private boolean mDragOutParent = true;

    // 父控件最大的高度
    private int mMaxParentHeight = 0;

    // 最大挤压宽度 默认400
    private int mMaxExtrusionWidth = 400;
    // 文本圆角矩形的最大宽度
    private int mMaxTextLayoutWidth = 0;

    // 删除标签区域的高度
    private int mDeleteRegionHeight;

    // 暴露接口
    private boolean mStartDrag = false;
    private OnRandomDragListener mDragListener;

    public RandomDragTagView(Context context) {
        this(context, null);
    }

    public RandomDragTagView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RandomDragTagView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(HORIZONTAL);
        inflate(context, R.layout.random_tag_layout, this);
        initView();
        initListener();
        initData();
        startBreathingAnimator();
    }

    /**
     * 开启呼吸灯动画 注动画无线循环注意回收防止内存泄露
     */
    private void startBreathingAnimator() {
        clearBreathingAnimator();
        mBreathingAnimator = ValueAnimator.ofFloat(0.8F, 1.0F);
        mBreathingAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mBreathingAnimator.setDuration(800);
        mBreathingAnimator.setStartDelay(200);
        mBreathingAnimator.setRepeatCount(-1);
        mBreathingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mBreathingView.setScaleX(value);
                mBreathingView.setScaleY(value);
            }
        });
        mBreathingAnimator.start();
    }

    private void initData() {
        // 默认显示左侧视图 隐藏右侧视图
        visibilityRightLayout();
        getMaxTextLayoutWidth();
        // 删除区域的高度
        mDeleteRegionHeight = dip2px(getContext(), 60);
    }

    // 获取文本最大宽度
    public void getMaxTextLayoutWidth() {
        post(new Runnable() {
            @Override
            public void run() {
                mMaxTextLayoutWidth = isShowLeftView() ? mLeftLayout.getWidth() : mRightLayout.getWidth();
            }
        });
    }

    private void visibilityLeftLayout() {
        mLeftLayout.setVisibility(mIsShowLeftView ? VISIBLE : GONE);
        mLeftText.setVisibility(mIsShowLeftView ? VISIBLE : GONE);
        mLeftLine.setVisibility(mIsShowLeftView ? VISIBLE : GONE);
    }

    private void visibilityRightLayout() {
        mRightLayout.setVisibility(!mIsShowLeftView ? VISIBLE : GONE);
        mRightText.setVisibility(!mIsShowLeftView ? VISIBLE : GONE);
        mRightLine.setVisibility(!mIsShowLeftView ? VISIBLE : GONE);
    }

    private void initListener() {
        if (!canDragView()) return;
        mBreathingLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switchDirection();
            }
        });
    }

    private void initView() {
        mLeftLayout = findViewById(R.id.left_tag_layout);
        mLeftText = findViewById(R.id.left_tv_tag);
        mLeftLine = findViewById(R.id.left_line_view);

        mBreathingView = findViewById(R.id.white_breathing_view);
        mBreathingLayout = findViewById(R.id.fl_breathing);

        mRightLayout = findViewById(R.id.right_tag_layout);
        mRightText = findViewById(R.id.right_tv_tag);
        mRightLine = findViewById(R.id.right_line_view);
    }

    // 切换方向
    public void switchDirection() {
        mIsShowLeftView = !mIsShowLeftView;
        visibilityLeftLayout();
        visibilityRightLayout();

        // 第一步更改 重置 textLayout 的高度
        final int preSwitchWidth = getWidth();
        LinearLayout.LayoutParams lp = (LayoutParams) (isShowLeftView() ?
                mLeftLayout.getLayoutParams() : mRightLayout.getLayoutParams());
        lp.width = LayoutParams.WRAP_CONTENT;
        if (mIsShowLeftView) {
            mLeftText.setText(mRightText.getText());
            mLeftLayout.setLayoutParams(lp);
        } else {
            mRightText.setText(mLeftText.getText());
            mRightLayout.setLayoutParams(lp);
        }

        // 第二步 重新设置setTranslationX的值
        post(new Runnable() {
            @Override
            public void run() {
                float newTranslationX = 0;
                if (!isShowLeftView()) {
                    newTranslationX = getTranslationX() + preSwitchWidth - mBreathingView.getWidth();
                } else {
                    newTranslationX = getTranslationX() - getWidth() + mBreathingView.getWidth();
                }

                // 边界检测
                checkBound(newTranslationX, getTranslationY());

            }
        });
    }

    /**
     * @param newTranslationX
     * @param newTranslationY
     */
    private void checkBound(float newTranslationX, float newTranslationY) {
        setTranslationX(newTranslationX);

        // 越界的情况下 改变textLayout 的高度
        final int parentWidth = ((View) getParent()).getWidth();
        final int parentHeight = ((View) getParent()).getHeight();
        float translationX = getTranslationX();
        if (translationX <= 0) {
            extrusionTextRegion(translationX);
        } else if (getTranslationX() >= (parentWidth - getWidth())) {
            final float offsetX = getWidth() - (parentWidth - getTranslationX());
            extrusionTextRegion(-offsetX);

            // 越界检测
            post(new Runnable() {
                @Override
                public void run() {
                    if (getTranslationX() >= (parentWidth - getWidth())) {
                        setTranslationX(parentWidth - getWidth());
                    }
                }
            });
        }

        // 越界检测
        if (getTranslationX() <= 0) {
            setTranslationX(0);
        }

        if (newTranslationY <= 0) {
            newTranslationY = 0;
        } else if (newTranslationY >= parentHeight - getHeight()) {
            newTranslationY = parentHeight - getHeight();
        }

        setTranslationY(newTranslationY);
    }

    /**
     * 添加标签
     *
     * @param text           标签文本
     * @param translationX   相对于父控件的x坐标
     * @param translationY   相对于父控件的y坐标
     * @param isShowLeftView 是否显示左侧标签
     */
    public void initTagView(String text, final float translationX, final float translationY, boolean isShowLeftView) {
        this.mIsShowLeftView = isShowLeftView;
        visibilityLeftLayout();
        visibilityRightLayout();
        // 不可见
        setVisibility(INVISIBLE);
        // 设置文本控件
        if (mIsShowLeftView) {
            mLeftText.setText(text);
        } else {
            mRightText.setText(text);
        }
        // 获取文本最大宽度
        getMaxTextLayoutWidth();

        post(new Runnable() {
            @Override
            public void run() {
                // 边界检测
                checkBound(translationX, translationY);

                // 设置可见
                setVisibility(VISIBLE);
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mTouchSlop < 0) {
            mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        }
        if (!canDragView()) return super.onTouchEvent(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                final float x = event.getRawX();
                final float y = event.getRawY();
                getParent().requestDisallowInterceptTouchEvent(true);

                mStartDrag = false;
                mPointerDown = false;
                mLastMotionRawX = x;
                mLastMotionRawY = y;
                mStartReboundX = getTranslationX();
                mStartReboundY = getTranslationY();

                // 调整索引 位于其他标签之上
                adjustIndex();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mPointerDown = true;
                break;
            case MotionEvent.ACTION_MOVE:
                final float rawY = event.getRawY();
                final float rawX = event.getRawX();
                if (!mStartDrag) {
                    mStartDrag = true;
                    if (mDragListener != null) {
                        mDragListener.onStartDrag();
                    }
                }
                if (!mPointerDown) {
                    final float yDiff = rawY - mLastMotionRawY;
                    final float xDiff = rawX - mLastMotionRawX;
                    // 处理move事件
                    handlerMoveEvent(yDiff, xDiff);
                    mLastMotionRawY = rawY;
                    mLastMotionRawX = rawX;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mPointerDown = false;
                break;
            // case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mPointerDown = false;
                mStartDrag = false;
                getParent().requestDisallowInterceptTouchEvent(false);

                final float translationY = getTranslationY();
                final int parentHeight = ((View) getParent()).getHeight();
                if (mMaxParentHeight - mDeleteRegionHeight < translationY) {
                    removeTagView();
                } else if (parentHeight - getHeight() < translationY) {
                    startReBoundAnimator();
                }

                if (mDragListener != null) {
                    mDragListener.onStopDrag();
                }

                break;
        }
        return true;
    }

    // 开始回弹动画
    private void startReBoundAnimator() {
        if (mReboundAnimator != null && mReboundAnimator.isRunning()) {
            mReboundAnimator.cancel();
        }
        mReboundAnimator = ValueAnimator.ofFloat(1F, 0F);
        mReboundAnimator.setDuration(400);
        final float startTransX = getTranslationX();
        final float startTransY = getTranslationY();
        mReboundAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                setTranslationX(mStartReboundX + (startTransX - mStartReboundX) * value);
                setTranslationY(mStartReboundY + (startTransY - mStartReboundY) * value);
            }
        });
        mReboundAnimator.start();
    }

    /**
     * 处理手势的move事件
     *
     * @param yDiff y轴方向的偏移量
     * @param xDiff x轴方向的偏移量
     */
    private void handlerMoveEvent(float yDiff, float xDiff) {
        float translationX = getTranslationX() + xDiff;
        float translationY = getTranslationY() + yDiff;

        // 越界处理 最大最小原则
        int parentWidth = ((View) getParent()).getWidth();
        int parentHeight = ((View) getParent()).getHeight();
        if (mMaxParentHeight == 0) {
            int parentParentHeight = ((View) getParent().getParent()).getHeight();
            mMaxParentHeight = (mDragOutParent ? parentParentHeight : parentHeight) - getHeight();
        }
        int maxWidth = parentWidth - getWidth();

        // 分情况处理越界 宽度
        if (translationX <= 0) {
            translationX = 0;
            // 标签文本出现挤压效果
            if (isShowLeftView()) {
                extrusionTextRegion(xDiff);
            }
        } else if (translationX >= maxWidth) {
            translationX = maxWidth;
            // 右侧挤压
            if (!isShowLeftView()) {
                extrusionTextRegion(-xDiff);

                handleWidthError();
            }
        } else {
            int textWidth = isShowLeftView() ? mLeftLayout.getWidth() : mRightLayout.getWidth();
            // 左侧视图
            if (isShowLeftView()) {
                if (getTranslationX() == 0 && textWidth < mMaxTextLayoutWidth) {
                    translationX = 0;
                    extrusionTextRegion(xDiff);
                }
            } else {
                if (textWidth < mMaxTextLayoutWidth) {
                    extrusionTextRegion(-xDiff);
                    handleWidthError();
                }
            }
        }

        // 高度越界处理
        if (translationY <= 0) {
            translationY = 0;
        } else if (translationY >= mMaxParentHeight) {
            translationY = mMaxParentHeight;
        }

        setTranslationX(translationX);
        setTranslationY(translationY);
    }

    // 处理宽度误差
    private void handleWidthError() {
        post(new Runnable() {
            @Override
            public void run() {
                if (getParent() == null) return;
                int parentWidth = ((View) getParent()).getWidth();
                int maxWidth = parentWidth - getWidth();
                setTranslationX(maxWidth);
            }
        });
    }

    /**
     * 挤压拉伸文本区域
     *
     * @param deltaX 偏移量
     */
    private void extrusionTextRegion(float deltaX) {
        int textWidth = isShowLeftView() ? mLeftLayout.getWidth() : mRightLayout.getWidth();
        LinearLayout.LayoutParams lp = (LayoutParams) (isShowLeftView() ?
                mLeftLayout.getLayoutParams() : mRightLayout.getLayoutParams());
        if (textWidth >= mMaxExtrusionWidth) {
            lp.width = (int) (textWidth + deltaX);

            // 越界判定
            if (lp.width <= mMaxExtrusionWidth) {
                lp.width = mMaxExtrusionWidth;
            } else if (lp.width >= mMaxTextLayoutWidth) {
                lp.width = mMaxTextLayoutWidth;
            }

            if (isShowLeftView()) {
                mLeftLayout.setLayoutParams(lp);
            } else {
                mRightLayout.setLayoutParams(lp);
            }
        }
    }


    /**
     * 调整索引 位于其他标签之上
     * moveToTop(View target) 方法的性能更好
     */
    @Deprecated
    private void adjustIndex() {
        ViewParent parent = getParent();
        if (parent != null) {
            if (parent instanceof ViewGroup) {
                ViewGroup parentView = (ViewGroup) parent;
                int childCount = parentView.getChildCount();
                if (childCount > 1 && indexOfChild(this) != (childCount - 1)) {
                    parentView.removeView(this);
                    parentView.addView(this);
                    // 重新开启呼吸灯动画
                    startBreathingAnimator();
                }
            }
        }
    }

    private void moveToTop(View target) {
        //先确定现在在哪个位置
        int startIndex = indexOfChild(target);
        //计算一共需要几次交换，就可到达最上面
        int count = getChildCount() - 1 - startIndex;
        for (int i = 0; i < count; i++) {
            //更新索引
            int fromIndex = indexOfChild(target);
            //目标是它的上层
            int toIndex = fromIndex + 1;
            //获取需要交换位置的两个子View
            View from = target;
            View to = getChildAt(toIndex);

            //先把它们拿出来
            detachViewFromParent(toIndex);
            detachViewFromParent(fromIndex);

            //再放回去，但是放回去的位置(索引)互换了
            attachViewToParent(to, fromIndex, to.getLayoutParams());
            attachViewToParent(from, toIndex, from.getLayoutParams());
        }
        //刷新
        invalidate();
    }

    // 移除标签view
    private void removeTagView() {
        ViewParent parent = getParent();
        if (parent != null) {
            if (parent instanceof ViewGroup) {
                ViewGroup parentView = (ViewGroup) parent;
                parentView.removeView(this);
            }
        }
    }

    // 是否可以拖拽
    public boolean canDragView() {
        return mCanDrag;
    }

    public boolean isShowLeftView() {
        return mIsShowLeftView;
    }

    public String getTagText() {
        if (isShowLeftView()) {
            return mLeftText.getText().toString();
        }
        return mRightText.getText().toString();
    }

    public float getPercentTransX() {
        int parentWidth = ((View) getParent()).getWidth();
        return getTranslationX() / parentWidth;
    }

    public float getPercentTransY() {
        int parentHeight = ((View) getParent()).getHeight();
        return getTranslationY() / parentHeight;
    }

    public void setShowLeftView(boolean showLeftView) {
        mIsShowLeftView = showLeftView;
    }

    public void setMaxExtrusionWidth(int maxExtrusionWidth) {
        mMaxExtrusionWidth = maxExtrusionWidth;
    }

    public int getMaxExtrusionWidth() {
        return mMaxExtrusionWidth;
    }

    @Override
    protected void onDetachedFromWindow() {
        clearBreathingAnimator();
        super.onDetachedFromWindow();
    }

    // 注意清理 防止内存泄露
    private void clearBreathingAnimator() {
        if (mBreathingAnimator != null && mBreathingAnimator.isRunning()) {
            mBreathingAnimator.cancel();
            mBreathingAnimator = null;
        }
    }

    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public interface OnRandomDragListener {
        // 开始拖拽
        void onStartDrag();

        // 停止拖拽
        void onStopDrag();
    }

    public void setOnRandomDragListener(OnRandomDragListener listener) {

    }
}

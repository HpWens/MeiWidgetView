package com.meis.widget.photodrag;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.meis.widget.R;
import com.meis.widget.utils.DensityUtil;

/**
 * Created by wenshi on 2018/5/22.
 * Github https://github.com/HpWens/MeiWidgetView
 */
public class VideoDragRelativeLayout extends RelativeLayout {

    //记录上一次 x y 轴方向的位置
    private float mTouchLastY;
    private float mTouchLastX;
    //x y 轴方向总的偏移量
    private float mTotalMoveDy;
    private float mTotalMoveDx;

    //是否消费touch事件
    private boolean mIsConsumeTouchEvent = true;

    //是否消费拦截事件
    private boolean mIsInterceptTouchEvent;

    //动画时长
    private int mAnimationDuration = 200;

    //下拉未达到临界值  释放执行的动画
    private ObjectAnimator mRestorationAnimation;

    //临界值 恢复系数 默认0.1f
    private float mRestorationRatio = 0.1F;

    //进入的转场动画
    private ValueAnimator mStartAnimation;

    //结束的转场动画
    private ValueAnimator mEndAnimation;

    //滑动事件冲突 如与viewpager的左右滑动冲突
    private boolean mIsScrollClash = true;

    //y 方向平移速率
    private int mOffsetRateY = 2;

    //y 开始平移系数
    private float mStartOffsetRatioY = 0.5F;

    //为了兼容android5.0以下手机 手动实现转场效果
    //来源view的x轴坐标 这里是列表图片的坐标以及图片宽高 坐标取值相对屏幕
    private int mOriginViewX;
    private int mOriginViewY;

    //来源view可见的宽高
    private int mOriginViewVisibleWidth;
    private int mOriginViewVisibleHeight;

    //来源view真实高度 处理上下边界越界问题 头条上下item越界会导致图片挤压变形
    private int mOriginViewRealHeight;

    //开始动画是否进入  默认true
    private boolean mStartAnimationEnable = true;

    //是否可以拖拽
    private boolean mIsDragEnable = true;

    //滚动最小临界值
    private int mMinScaledTouchSlop;

    //回调接口
    private OnVideoDragListener mListener;

    private float mTopNavHeight;
    private float mBottomNavHeight;
    private boolean mIsLastRow = false;

    private static final String TAG_DISPATCH = "dispatch";

    public VideoDragRelativeLayout(Context context) {
        this(context, null);
    }

    public VideoDragRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoDragRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.VideoDragRelativeLayout);
        mRestorationRatio = ta.getFloat(R.styleable.VideoDragRelativeLayout_vdr_restoration_ratio, 0.1F);
        mOffsetRateY = ta.getInt(R.styleable.VideoDragRelativeLayout_vdr_offset_rate_y, 2);
        mStartOffsetRatioY = ta.getFloat(R.styleable.VideoDragRelativeLayout_vdr_start_offset_ratio_y, 0.5F);
        mStartAnimationEnable = ta.getBoolean(R.styleable.VideoDragRelativeLayout_vdr_start_anim_enable, true);
        mIsDragEnable = ta.getBoolean(R.styleable.VideoDragRelativeLayout_vdr_drag_enable, true);

        mAnimationDuration = ta.getInt(R.styleable.VideoDragRelativeLayout_vdr_anim_duration, 400);
        mTopNavHeight = ta.getDimension(R.styleable.VideoDragRelativeLayout_vdr_top_nav_height, 0);
        mBottomNavHeight = ta.getDimension(R.styleable.VideoDragRelativeLayout_vdr_bottom_nav_height, 0);
        ta.recycle();

        mMinScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        float y = ev.getRawY();
        float x = ev.getRawX();
        switch (ev.getAction() & ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mTouchLastX = x;
                mTouchLastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                float dy = y - mTouchLastY;
                float dx = x - mTouchLastX;
                mIsInterceptTouchEvent = Math.abs(dy) > mMinScaledTouchSlop | Math.abs(dx) > mMinScaledTouchSlop;
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
        boolean childIntercept = true;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                childIntercept = !childInterceptEvent(this, (int) ev.getRawX(), (int) ev.getRawY());
                break;
        }
        return mIsDragEnable & mIsInterceptTouchEvent & childIntercept;
    }

    private boolean childInterceptEvent(ViewGroup parentView, int touchX, int touchY) {
        boolean isConsume = false;
        for (int i = parentView.getChildCount() - 1; i >= 0; i--) {
            View childView = parentView.getChildAt(i);
            if (!childView.isShown()) {
                continue;
            }
            boolean isTouchView = isTouchView(touchX, touchY, childView);
            if (isTouchView && childView.getTag() != null && TAG_DISPATCH.equals(childView.getTag().toString())) {
                isConsume = true;
                break;
            }
            if (childView instanceof ViewGroup) {
                ViewGroup itemView = (ViewGroup) childView;
                if (!isTouchView) {
                    continue;
                } else {
                    isConsume |= childInterceptEvent(itemView, touchX, touchY);
                    if (isConsume) {
                        break;
                    }
                }
            }
        }
        return isConsume;
    }

    private boolean isTouchView(int touchX, int touchY, View view) {
        Rect rect = new Rect();
        view.getGlobalVisibleRect(rect);
        return rect.contains(touchX, touchY);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mIsConsumeTouchEvent) {
            return super.onTouchEvent(event);
        }
        float y = event.getRawY();
        float x = event.getRawX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTotalMoveDy = 0;
                mTotalMoveDx = 0;
                mTouchLastX = x;
                mTouchLastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                float dy = y - mTouchLastY;
                float dx = x - mTouchLastX;

                mTotalMoveDy += dy;
                mTotalMoveDx += dx;

                mTotalMoveDy = mTotalMoveDy <= 0 ? 0 : mTotalMoveDy;
                mTotalMoveDy = mTotalMoveDy >= getHeight() ? getHeight() : mTotalMoveDy;

                mTouchLastX = x;
                mTouchLastY = y;

                //第一步 解决与viewpager的左右冲突 若手指拖动的x轴偏移量大于等于y轴偏移量则不消费事件
                if (Math.abs(mTotalMoveDx) >= Math.abs(mTotalMoveDy)) {
                    if (mIsScrollClash) {
                        mIsScrollClash = true;
                        return super.onTouchEvent(event);
                    }
                } else {
                    mIsScrollClash = false;
                }
                //开始拖拽回调
                if (mListener != null) {
                    mListener.onStartDrag();
                }
                //第二步 拖拽
                setTranslationX(getTranslationX() + dx);
                //设置缩放点 根据需求而定 这里缩放中心点是屏幕底部中点
                setPivotX(getWidth() / 2F);
                setPivotY(getHeight());
                //设置缩放
                float scale = 1.0F - mTotalMoveDy / getHeight();

                if (scale > 0.1F) {
                    setScaleX(scale);
                    setScaleY(scale);
                }
                //缩放小于0.5时并平移y
                if (scale < mStartOffsetRatioY) {
                    setTranslationY(getTranslationY() + dy / mOffsetRateY);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mIsScrollClash) {
                    return super.onTouchEvent(event);
                }
                mIsScrollClash = true;

                //防止多次点击屏幕
                if (mTotalMoveDy == 0 && mTotalMoveDx == 0) {
                    break;
                }

                //判定是否执行恢复动画还是结束动画
                final boolean isEnd = ((mTotalMoveDy / getHeight()) > mRestorationRatio);

                //执行相应回调
                if (mListener != null) {
                    mListener.onReleaseDrag(!isEnd);
                }

                //执行相应动画
                if (isEnd) {
                    startEndAnimation();
                } else {
                    startRestorationAnimation();
                }

                mTotalMoveDy = 0;
                mTotalMoveDx = 0;
                break;
        }
        return mIsDragEnable & mIsConsumeTouchEvent;
    }

    //执行恢复动画
    private void startRestorationAnimation() {
        if (mRestorationAnimation != null && mRestorationAnimation.isRunning()) {
            return;
        }
        PropertyValuesHolder propertyScaleX = PropertyValuesHolder.ofFloat("scaleX", getScaleX(), 1.0F);
        PropertyValuesHolder propertyScaleY = PropertyValuesHolder.ofFloat("scaleY", getScaleY(), 1.0F);
        PropertyValuesHolder propertyTranslationX = PropertyValuesHolder.ofFloat("translationX", getTranslationX(), 0);
        PropertyValuesHolder propertyTranslationY = PropertyValuesHolder.ofFloat("translationY", getTranslationY(), 0);

        mRestorationAnimation = ObjectAnimator.ofPropertyValuesHolder(this, propertyScaleX, propertyScaleY, propertyTranslationX, propertyTranslationY)
                .setDuration(mAnimationDuration);

        mRestorationAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mIsConsumeTouchEvent = true;
                if (mListener != null) {
                    mListener.onRestorationAnimationEnd();
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mIsConsumeTouchEvent = false;
            }
        });
        mRestorationAnimation.start();
    }

    //执行结束动画
    private void startEndAnimation() {
        if (mEndAnimation != null && mEndAnimation.isRunning()) {
            return;
        }
        //判定来源view宽高
        if (mOriginViewVisibleHeight != 0 && mOriginViewVisibleWidth != 0) {
            final float startTransitionX = getTranslationX();
            final float startTransitionY = getTranslationY();
            final float startScaleX = getScaleX();
            final float startScaleY = getScaleY();
            final float endScaleX = (float) mOriginViewVisibleWidth / getWidth();
            final float endScaleY = (float) mOriginViewRealHeight / getHeight();
            //状态栏高度 若状态栏隐藏则设置此值 DensityUtil.getStatusBarHeight((Activity) getContext());
            final int statusHeight = DensityUtil.getStatusBarHeight((Activity) getContext());
            //是否越界
            boolean isTopOutOfBound = false;
            //加1是防止精度误差
            if ((mOriginViewVisibleHeight + 1) < mOriginViewRealHeight) {
                if ((mOriginViewY + mOriginViewRealHeight) > getHeight()) {
                    //下边界越界
                    isTopOutOfBound = false;
                    if (mIsLastRow) {
                        //mOriginViewY = getHeight() - (int) mBottomNavHeight - mOriginViewRealHeight;
                        mOriginViewY -= (mOriginViewRealHeight - mOriginViewVisibleHeight);
                    } else {
                        mOriginViewY = statusHeight + (int) mTopNavHeight;
                    }
                } else {
                    //上边界越界
                    isTopOutOfBound = true;
                }
                mOriginViewVisibleHeight = mOriginViewRealHeight;
            }
            final boolean topOutOfBound = isTopOutOfBound;
            mEndAnimation = ValueAnimator.ofFloat(0F, 1.0F).setDuration(mAnimationDuration);
            mEndAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    setScaleX(startScaleX + value * (endScaleX - startScaleX));
                    setScaleY(startScaleY + value * (endScaleY - startScaleY));

                    setTranslationX(startTransitionX + value * (mOriginViewX - startTransitionX) - value * (getWidth() - mOriginViewVisibleWidth) / 2.0F);
                    //setTranslationY(startTransitionY - value * (startTransitionY - mOriginViewY) - value * (getHeight() - mOriginViewRealHeight + statusHeight) - (topOutOfBound ? value * (mOriginViewRealHeight - mOriginViewVisibleHeight) : 0));
                    setTranslationY(startTransitionY - value * (startTransitionY - mOriginViewY) - value * (getHeight() - mOriginViewRealHeight) - (topOutOfBound ? value * (mOriginViewRealHeight - mOriginViewVisibleHeight) : 0));
                }
            });
            mEndAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mIsConsumeTouchEvent = true;
                    if (mListener != null) {
                        mListener.onExitAnimationEnd();
                    }
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    mIsConsumeTouchEvent = false;
                }
            });
            mEndAnimation.start();
        } else {
            if (mListener != null) {
                mListener.onExitAnimationEnd();
            }
        }
    }

    //执行开始动画
    private void startStartAnimation(final boolean isExit) {
        if (mStartAnimation != null && mStartAnimation.isRunning()) {
            return;
        }
        if (mEndAnimation != null && mEndAnimation.isRunning()) {
            return;
        }
        //判定来源view宽高
        if (mOriginViewVisibleHeight != 0 && mOriginViewVisibleWidth != 0 && mStartAnimationEnable) {
            setPivotX(0);
            setPivotY(0);
            final int statusHeight = DensityUtil.getStatusBarHeight((Activity) getContext());
            //具体场景 可以替换成  getWidth()  getHeight() 这里是以屏幕的宽高来计算的
            boolean isTopOutOfBound = false;
            int screenHeight = DensityUtil.getScreenSize(getContext()).y;
            final float startScaleX = (float) mOriginViewVisibleWidth / DensityUtil.getScreenSize(getContext()).x;
            final float startScaleY = (float) mOriginViewRealHeight / screenHeight;
            final boolean outOfBound = (mOriginViewVisibleHeight + 1) < mOriginViewRealHeight;
            if (outOfBound) {
                //上边界越界 或者 下边界越界
                if ((mOriginViewY + mOriginViewRealHeight) > screenHeight) {
                    //下边界越界
                    isTopOutOfBound = false;
                    if (mIsLastRow) {
                        mOriginViewY = isExit ? (screenHeight - (int) mBottomNavHeight - mOriginViewRealHeight) : mOriginViewY;
                    } else {
                        mOriginViewY = isExit ? (statusHeight + (int) mTopNavHeight) : mOriginViewY;
                    }
                } else {
                    //上边界越界
                    isTopOutOfBound = true;
                }
                mOriginViewVisibleHeight = isExit ? mOriginViewRealHeight : mOriginViewVisibleHeight;
            }

            final boolean topOutOfBound = isTopOutOfBound;
            mStartAnimation = ValueAnimator.ofFloat(isExit ? 1.0F : 0F, isExit ? 0F : 1.0F).setDuration(mAnimationDuration);
            mStartAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    setTranslationX(mOriginViewX - value * mOriginViewX);
                    //setTranslationY((mOriginViewY - statusHeight) - value * (mOriginViewY - statusHeight) - (topOutOfBound ? (1.0F - value) * (mOriginViewRealHeight - mOriginViewVisibleHeight) : 0));
                    setTranslationY(mOriginViewY - value * mOriginViewY - (topOutOfBound ? (1.0F - value) * (mOriginViewRealHeight - mOriginViewVisibleHeight) : 0));

                    setScaleX(startScaleX + value * (1.0F - startScaleX));
                    setScaleY(startScaleY + value * (1.0F - startScaleY));
                }
            });
            mStartAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mIsConsumeTouchEvent = true;
                    if (mListener != null) {
                        if (isExit) {
                            mListener.onExitAnimationEnd();
                        } else {
                            mListener.onEnterAnimationEnd(outOfBound);
                        }
                    }
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    mIsConsumeTouchEvent = false;
                }
            });
            mStartAnimation.start();
        } else {
            if (mListener != null) {
                if (isExit) {
                    mListener.onExitAnimationEnd();
                } else {
                    mListener.onEnterAnimationEnd(false);
                }
            }
        }
    }

    //mRecyclerView.canScrollVertically(1) 判定是否滑动到底部
    public void setIsLastRow(boolean lastRow) {
        this.mIsLastRow = lastRow;
    }

    //设置来源数据
    public void setOriginView(int x, int y, int visibleWidth, int visibleHeight, int realHeight) {
        mOriginViewX = x;
        mOriginViewY = y;
        mOriginViewVisibleWidth = visibleWidth;
        mOriginViewVisibleHeight = visibleHeight;
        mOriginViewRealHeight = realHeight;
    }

    //返回键
    public void onBackPressed() {
        if (mListener != null) {
            mListener.onReleaseDrag(false);
        }
        startStartAnimation(true);
    }

    public void startAnimation() {
        post(new Runnable() {
            @Override
            public void run() {
                startStartAnimation(false);
            }
        });
    }

    public VideoDragRelativeLayout setOnVideoDragListener(OnVideoDragListener listener) {
        mListener = listener;
        return this;
    }

    public interface OnVideoDragListener {

        //开始拖拽
        void onStartDrag();

        /**
         * 释放拖拽
         *
         * @param isRestoration 是否恢复 true 则执行恢复动画  false 则执行结束动画
         */
        void onReleaseDrag(boolean isRestoration);

        /**
         * 进入动画结束
         *
         * @param isOutOfBound 是否越界
         */
        void onEnterAnimationEnd(boolean isOutOfBound);


        /**
         * 退出动画结束
         */
        void onExitAnimationEnd();

        /**
         * 恢复动画结束
         */
        void onRestorationAnimationEnd();
    }

    public int getAnimationDuration() {
        return mAnimationDuration;
    }

    public void setAnimationDuration(int animationDuration) {
        mAnimationDuration = animationDuration;
    }

    public float getRestorationRatio() {
        return mRestorationRatio;
    }

    public void setRestorationRatio(float restorationRatio) {
        mRestorationRatio = restorationRatio;
    }

    public int getOffsetRateY() {
        return mOffsetRateY;
    }

    public void setOffsetRateY(int offsetRateY) {
        mOffsetRateY = offsetRateY;
    }

    public float getStartOffsetRatioY() {
        return mStartOffsetRatioY;
    }

    public void setStartOffsetRatioY(float startOffsetRatioY) {
        mStartOffsetRatioY = startOffsetRatioY;
    }

    public boolean getStartAnimationEnable() {
        return mStartAnimationEnable;
    }

    public void setStartAnimationEnable(boolean startAnimationEnable) {
        mStartAnimationEnable = startAnimationEnable;
    }

    public boolean getDragEnable() {
        return mIsDragEnable;
    }

    public void setDragEnable(boolean dragEnable) {
        mIsDragEnable = dragEnable;
    }

    public float getTopNavHeight() {
        return mTopNavHeight;
    }

    public void setTopNavHeight(float topNavHeight) {
        mTopNavHeight = topNavHeight;
    }

    public float getBottomNavHeight() {
        return mBottomNavHeight;
    }

    public void setBottomNavHeight(float bottomNavHeight) {
        mBottomNavHeight = bottomNavHeight;
    }
}

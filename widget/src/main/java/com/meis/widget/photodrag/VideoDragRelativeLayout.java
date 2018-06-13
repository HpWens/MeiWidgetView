package com.meis.widget.photodrag;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
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
 * 请添加到布局的最外层 若子类想消费点击事件请设置tag为dispatch
 * Github https://github.com/HpWens/MeiWidgetView
 */
public class VideoDragRelativeLayout extends RelativeLayout {

    private static final String TAG_DISPATCH = "dispatch";

    //touch x y point
    private float mTouchLastY;
    private float mTouchLastX;

    //x y move offset
    private float mMoveDy;
    private float mMoveDx;

    //子类是否拦截事件 默认子类不拦截触摸事件 子类想消费事件 设置tag为dispatch
    private boolean mChildInterceptEventEnable = false;

    //是否执行动画
    private boolean mRunningAnimationEnable = false;

    //自己是否拦截 拦截事件 默认拦截
    private boolean mSelfInterceptEventEnable = true;

    //是否正在拖拽
    private boolean mDraggingEnable = true;

    //进入动画时长
    private long mStartAnimDuration;
    //结束动画时长
    private long mEndAnimDuration;

    //恢复动画 恢复系数有关[0~1] 恢复系数越大则需要拖动越大的距离
    private ObjectAnimator mRestorationAnimation;
    //恢复系数 默认0.1f
    private float mRestorationRatio = 0.1F;

    //开始动画
    private ValueAnimator mStartAnimation;
    //结束动画
    private ValueAnimator mEndAnimation;

    //控制事件冲突 如与viewpager的左右滑动冲突
    private boolean mOtherViewClashEnable = true;

    //y轴偏移速率 值越大偏移越慢 前提y偏移量大于y轴开始偏移系数
    private int mOffsetRateY = 2;

    //y轴开始偏移系数
    private float mStartOffsetRatioY = 0.5F;

    //为了兼容android5.0以下手机 手动实现转场效果  经过测试某些机型5.0以上转场动画达不到UI效果
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

    //回调接口
    private OnVideoDragListener mListener;

    public VideoDragRelativeLayout(Context context) {
        this(context, null);
    }

    public VideoDragRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoDragRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.VideoDragRelativeLayout);
        mSelfInterceptEventEnable = ta.getBoolean(R.styleable.VideoDragRelativeLayout_mei_self_intercept_event, true);
        mStartAnimDuration = ta.getInt(R.styleable.VideoDragRelativeLayout_mei_start_anim_duration, 1000);
        mEndAnimDuration = ta.getInt(R.styleable.VideoDragRelativeLayout_mei_end_anim_duration, 1000);
        mRestorationRatio = ta.getFloat(R.styleable.VideoDragRelativeLayout_mei_restoration_ratio, 0.1F);
        mOffsetRateY = ta.getInt(R.styleable.VideoDragRelativeLayout_mei_offset_rate_y, 2);
        mStartOffsetRatioY = ta.getFloat(R.styleable.VideoDragRelativeLayout_mei_start_offset_ratio_y, 0.5F);
        mStartAnimationEnable = ta.getBoolean(R.styleable.VideoDragRelativeLayout_mei_start_anim_enable, true);
        ta.recycle();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!mSelfInterceptEventEnable || mRunningAnimationEnable) {
                    return super.onInterceptTouchEvent(ev);
                }
                mChildInterceptEventEnable = childInterceptEvent((int) ev.getRawX(), (int) ev.getRawY());
                break;
        }
        return mChildInterceptEventEnable;
    }

    /**
     * @param touchX
     * @param touchY
     * @return 子view是否消费事件
     */
    private boolean childInterceptEvent(int touchX, int touchY) {
        return !childInterceptEvent(this, touchX, touchY);
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
            } else if (isTouchView) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    isConsume = childView.hasOnClickListeners();
                    if (isConsume) {
                        break;
                    }
                }
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
        float y = event.getRawY();
        float x = event.getRawX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mChildInterceptEventEnable) {
                    return super.onTouchEvent(event);
                }
                resetData(y, x);
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mChildInterceptEventEnable) {
                    return super.onTouchEvent(event);
                }
                if (!mSelfInterceptEventEnable) {
                    return super.onTouchEvent(event);
                }
                float dy = y - mTouchLastY;
                float dx = x - mTouchLastX;

                mMoveDy += dy;
                mMoveDx += dx;

                mMoveDy = mMoveDy <= 0 ? 0 : mMoveDy;

                //第一步 解决与viewpager的左右冲突 若手指拖动的x轴偏移量大于等于y轴偏移量则不消费事件
                if (Math.abs(mMoveDx) >= Math.abs(mMoveDy)) {
                    if (mOtherViewClashEnable) {
                        mOtherViewClashEnable = true;
                        return super.onTouchEvent(event);
                    }
                } else {
                    mOtherViewClashEnable = false;
                }
                //添加开始拖拽回调
                if (mListener != null && (Math.abs(dy) > 0 || Math.abs(dx) > 0)) {
                    mListener.onStartDrag();
                }
                //第二步 拖拽
                setTranslationX(getTranslationX() + dx);
                //设置缩放点 根据需求而定 这里缩放中心点是屏幕底部中点
                setPivotX(getWidth() / 2F);
                setPivotY(getHeight());
                //设置缩放
                float scale = 1.0F - mMoveDy / getHeight();
                setScaleX(scale);
                setScaleY(scale);
                //缩放小于0.5时并平移y
                if (scale < mStartOffsetRatioY) {
                    setTranslationY(getTranslationY() + dy / mOffsetRateY);
                }
                mTouchLastX = x;
                mTouchLastY = y;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mOtherViewClashEnable) {
                    return super.onTouchEvent(event);
                }
                mDraggingEnable = false;
                mOtherViewClashEnable = true;
                //防止多次点击屏幕
                if (mMoveDy == 0 && mMoveDx == 0) {
                    break;
                }
                //判定是否执行恢复动画还是结束动画
                final boolean isEnd = ((mMoveDy / getHeight()) > mRestorationRatio);

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

                mMoveDy = 0;
                mMoveDx = 0;
                break;
        }
        return mDraggingEnable ? mChildInterceptEventEnable : super.onTouchEvent(event);
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
                .setDuration(mStartAnimDuration);

        mRestorationAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mChildInterceptEventEnable = true;
                mRunningAnimationEnable = false;
                mDraggingEnable = true;
                if (mListener != null) {
                    mListener.onRestorationAnimationEnd();
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mChildInterceptEventEnable = false;
                mRunningAnimationEnable = true;
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
            final int statusHeight = 0;
            //是否越界
            boolean isTopOutOfBound = false;
            //加1是防止精度误差
            if ((mOriginViewVisibleHeight + 1) < mOriginViewRealHeight) {
                if ((mOriginViewY + mOriginViewRealHeight) > getHeight()) {
                    //下边界越界
                    isTopOutOfBound = false;
                } else {
                    //上边界越界
                    isTopOutOfBound = true;
                }
            }
            final boolean topOutOfBound = isTopOutOfBound;
            mEndAnimation = ValueAnimator.ofFloat(0F, 1.0F).setDuration(mEndAnimDuration);
            mEndAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();

                    setScaleX(startScaleX + value * (endScaleX - startScaleX));
                    setScaleY(startScaleY + value * (endScaleY - startScaleY));

                    setTranslationX(startTransitionX + value * (mOriginViewX - startTransitionX) - value * (getWidth() - mOriginViewVisibleWidth) / 2.0F);
                    setTranslationY(startTransitionY - value * (startTransitionY - mOriginViewY) - value * (getHeight() - mOriginViewRealHeight + statusHeight) - (topOutOfBound ?
                            value * (mOriginViewRealHeight - mOriginViewVisibleHeight) : 0));
                }
            });
            mEndAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mChildInterceptEventEnable = true;
                    mRunningAnimationEnable = false;
                    if (mListener != null) {
                        mListener.onExitAnimationEnd();
                    }
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    mRunningAnimationEnable = true;
                    mChildInterceptEventEnable = false;
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
            //DensityUtil.getStatusBarHeight((Activity) getContext());
            final int statusHeight = 0;
            //具体场景 可以替换成  getWidth()  getHeight() 这里是以屏幕的宽高来计算的
            boolean isTopOutOfBound = false;
            int screenHeight = DensityUtil.getScreenSize(getContext()).y;
            final float startScaleX = (float) mOriginViewVisibleWidth / DensityUtil.getScreenSize(getContext()).x;
            final float startScaleY = (float) mOriginViewRealHeight / screenHeight;
            if ((mOriginViewVisibleHeight + 1) < mOriginViewRealHeight) {
                //上边界越界 或者 下边界越界
                if ((mOriginViewY + mOriginViewRealHeight) > screenHeight) {
                    //下边界越界
                    isTopOutOfBound = false;
                } else {
                    //上边界越界
                    isTopOutOfBound = true;
                }
            }
            final boolean topOutOfBound = isTopOutOfBound;
            mStartAnimation = ValueAnimator.ofFloat(isExit ? 1.0F : 0F, isExit ? 0F : 1.0F).setDuration(isExit ? mEndAnimDuration : mStartAnimDuration);
            mStartAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    setTranslationX(mOriginViewX - value * mOriginViewX);
                    setTranslationY((mOriginViewY - statusHeight) - value * (mOriginViewY - statusHeight) - (topOutOfBound ? (1.0F - value) * (mOriginViewRealHeight -
                            mOriginViewVisibleHeight) : 0));

                    setScaleX(startScaleX + value * (1.0F - startScaleX));
                    setScaleY(startScaleY + value * (1.0F - startScaleY));
                }
            });
            mStartAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mChildInterceptEventEnable = true;
                    mRunningAnimationEnable = false;
                    if (mListener != null) {
                        if (isExit) {
                            mListener.onExitAnimationEnd();
                        } else {
                            mListener.onEnterAnimationEnd();
                        }
                    }
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    mRunningAnimationEnable = true;
                    mChildInterceptEventEnable = false;
                }
            });
            mStartAnimation.start();
        } else {
            if (mListener != null) {
                if (isExit) {
                    mListener.onExitAnimationEnd();
                } else {
                    mListener.onEnterAnimationEnd();
                }
            }
        }
    }

    /**
     * 重置数据
     *
     * @param y
     * @param x
     */
    private void resetData(float y, float x) {
        mDraggingEnable = true;
        mMoveDy = 0;
        mMoveDx = 0;
        mTouchLastX = x;
        mTouchLastY = y;
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
        startStartAnimation(false);
    }

    public boolean getStartAnimationEnable() {
        return mStartAnimationEnable;
    }

    public void setStartAnimationEnable(boolean startAnimationEnable) {
        mStartAnimationEnable = startAnimationEnable;
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

    public boolean getSelfInterceptEventEnable() {
        return mSelfInterceptEventEnable;
    }

    public void setSelfInterceptEventEnable(boolean selfInterceptEventEnable) {
        mSelfInterceptEventEnable = selfInterceptEventEnable;
    }

    public long getStartAnimDuration() {
        return mStartAnimDuration;
    }

    public void setStartAnimDuration(long startAnimDuration) {
        mStartAnimDuration = startAnimDuration;
    }

    public long getEndAnimDuration() {
        return mEndAnimDuration;
    }

    public void setEndAnimDuration(long endAnimDuration) {
        mEndAnimDuration = endAnimDuration;
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
         */
        void onEnterAnimationEnd();


        /**
         * 退出动画结束
         */
        void onExitAnimationEnd();

        /**
         * 恢复动画结束
         */
        void onRestorationAnimationEnd();
    }


}

package com.meis.widget.photodrag;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.meis.widget.R;

/**
 * Created by wenshi on 2018/11/15.
 * Description 可以拖拽的相对布局（委托的方式扩展其他布局） 实现方式二可以通过扩展共享元素来实现
 * 原理：左右方向拖动，上下缩放控件
 */
public class DragRelativeLayout extends RelativeLayout {


    // 支持配置的属性
    /**
     * 动画时长
     */
    private int mDuration = 400;

    // 恢复比 0~1.0
    private float mResumeRatio = 0.1f;

    // 是否可以拖拽 控制转场与是否可以拖拽 下个版本会分开控制
    private boolean mDragEnable = true;

    // y方向开始平移系数 0~1.0 默认 0.5
    private float mStartTranslationYRatio = 0.5f;

    // y方向平移速率 1~oo 默认 2
    private int mTranslationYRate = 2;

    // 是否是头条样式 默认false
    private boolean mTTStyleEnable = false;


    // 转场相关区域参数 点击的区域
    // 区域的x坐标
    private int mRegionX;
    // 区域y坐标
    private int mRegionY;
    // 区域宽度
    private int mRegionWidth;
    // 高度
    private int mRegionHeight;
    // 当前区域视图的真实高度 （头条区域在屏幕外，点击会变形，为了防止变形，故做优化处理）
    private int mViewRealWidth;
    private int mViewRealHeight;
    // 开始与结束的坐标
    private int mStartAndEndY;
    // 距离屏幕顶部的y方向距离
    private int mDistanceScreenTop = 0;


    // 不支持的配置参数
    // 手持触摸最后的坐标
    private float mTouchLastY;
    private float mTouchLastX;

    // x,y方向偏移量 处理与viewpager左右滑动冲突
    private float mTouchOffsetY;
    private float mTouchOffsetX;

    // 误差处理 手指按下出发ActionMove的次数
    private int mActionMoveCount;

    // 是否开始拖拽 开始执行拖拽动画
    private boolean mIsStartDrag;

    // 防止滑动冲突 是否消费当前事件
    private boolean mIsInterceptEvent;

    // 动画是否在运行
    private boolean mAnimRunning = false;

    // 设置接口
    private com.meis.widget.photodrag.OnDragListener mListener;


    // 动画相关 下个版本可以整合动画
    // 恢复动画
    private ObjectAnimator mResumeAnimation;

    // 转场动画 进入与退出的转场动画
    private ValueAnimator mTransitionsAnimation;

    // 退出动画
    private ValueAnimator mExitAnimation;

    // 转场动画结束快速滑动屏幕 会导致MotionEvent.ACTION_DOWN时间丢失 兼容处理
    private boolean mEventLostEnable = true;

    public DragRelativeLayout(Context context) {
        this(context, null);
    }

    public DragRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 解析xml属性
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DragRelativeLayout);
        mDuration = ta.getInt(R.styleable.DragRelativeLayout_drl_duration, 400);
        mResumeRatio = ta.getFloat(R.styleable.DragRelativeLayout_drl_resume_ratio, 0.1f);
        mDragEnable = ta.getBoolean(R.styleable.DragRelativeLayout_drl_drag_enable, true);
        mStartTranslationYRatio = ta.getFloat(R.styleable.DragRelativeLayout_drl_translationY_ratio, 0.5f);
        mTranslationYRate = ta.getInt(R.styleable.DragRelativeLayout_drl_translationY_rate, 2);
        mTTStyleEnable = ta.getBoolean(R.styleable.DragRelativeLayout_drl_tt_style_enable, false);
        ta.recycle();

        // 为了防止子控件touch消费事件，导致父视图接受不到touch事件，同时如果父视图拦截了事件，会导致子视图消费不了事件
        // 顾最后统一在分发（dispatchTouchEvent）中处理
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        getDistanceScreenTop();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!mDragEnable || mAnimRunning) return super.dispatchTouchEvent(event);
        float y = event.getRawY();
        float x = event.getRawX();
        switch (event.getAction() & event.getActionMasked()) {
            // 并没有进行多手指处理 需要处理 可以集成相应的api
            case MotionEvent.ACTION_DOWN:
                mEventLostEnable = false;
                reset(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mEventLostEnable) {
                    // 兼容处理，ACTION_DOWN事件丢失
                    mEventLostEnable = false;
                    reset(x, y);
                    break;
                }
                if (move(x, y)) return super.dispatchTouchEvent(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mEventLostEnable = true;
                if (up()) return super.dispatchTouchEvent(event);
                break;
        }
        return super.dispatchTouchEvent(event) | true;
    }

    // 手指触摸
    private boolean move(float x, float y) {
        float dy = y - mTouchLastY;
        float dx = x - mTouchLastX;
        // 计算偏移量
        mTouchOffsetY += dy;
        mTouchOffsetX += dx;

        // 顶部越界判定 头条ios是可以拖动出顶部屏幕的
        mTouchOffsetY = mTouchOffsetY <= 0 ? 0 : mTouchOffsetY;
        mTouchOffsetY = mTouchOffsetY >= getHeight() ? getHeight() : mTouchOffsetY;

        mTouchLastX = x;
        mTouchLastY = y;

        // 计数加1
        mActionMoveCount += 1;

        // 处理与viewpager左右滑动的冲突 左右方向的偏移量大于上下的偏移量
        if (Math.abs(mTouchOffsetX) >= Math.abs(mTouchOffsetY)) {
            if (!mIsInterceptEvent) {
                return true;
            }
        } else {
            mIsInterceptEvent = true;
        }

        // 是否消费事件
        if (dy > 0 && Math.abs(dy) > Math.abs(dx) && (mActionMoveCount == 1 || mActionMoveCount == 2)) {
            mIsInterceptEvent = true;
        }

        if (!mIsInterceptEvent) {
            return true;
        }

        // 开始拖拽 事件回调
        if (mListener != null && !mIsStartDrag) {
            mIsStartDrag = true;
            mListener.onStartDrag();
        }

        // x轴方向跟随手指移动
        setTranslationX(getTranslationX() + dx);

        // y方向移动处理
        // 以屏幕底部为缩放中心点
        setPivotX(getWidth() / 2F);
        setPivotY(getHeight());

        // 设置缩放
        float scale = 1.0F - mTouchOffsetY / getHeight();

        if (scale > 0.1F) {
            setScaleX(scale);
            setScaleY(scale);
        }

        if (scale < mStartTranslationYRatio) {
            setTranslationY(getTranslationY() + dy / mTranslationYRate);
        }

        return false;
    }

    private boolean up() {
        // 防止多次点击屏幕
        if ((mTouchOffsetY == 0 && mTouchOffsetX == 0) || !mIsInterceptEvent) {
            return true;
        }

        // 判定是否执行恢复动画还是结束动画
        boolean isEnd = ((mTouchOffsetY / getHeight()) > mResumeRatio);

        if (mListener != null) {
            mListener.onRelease(!isEnd);
        }

        if (isEnd) {
            startExitAnim();
        } else {
            startResumeAnim();
        }

        return false;
    }

    // 执行恢复动画 几个动画可以整合成一个 下个版本优化
    private void startResumeAnim() {
        if (mResumeAnimation != null && mResumeAnimation.isRunning()) {
            return;
        }
        PropertyValuesHolder propertyScaleX = PropertyValuesHolder.ofFloat("scaleX", getScaleX(), 1.0F);
        PropertyValuesHolder propertyScaleY = PropertyValuesHolder.ofFloat("scaleY", getScaleY(), 1.0F);
        PropertyValuesHolder propertyTranslationX = PropertyValuesHolder.ofFloat("translationX", getTranslationX(), 0);
        PropertyValuesHolder propertyTranslationY = PropertyValuesHolder.ofFloat("translationY", getTranslationY(), 0);

        mResumeAnimation = ObjectAnimator.ofPropertyValuesHolder(this, propertyScaleX, propertyScaleY, propertyTranslationX, propertyTranslationY)
                .setDuration(mDuration);

        mResumeAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mListener != null) {
                    mListener.onEndResume();
                }
                mAnimRunning = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                mAnimRunning = false;
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mAnimRunning = true;
            }
        });
        mResumeAnimation.start();
    }

    // 执行退出动画
    private void startExitAnim() {
        if (mExitAnimation != null && mExitAnimation.isRunning()) {
            return;
        }
        //判定来源view宽高
        if (mRegionWidth != 0 && mRegionHeight != 0) {
            final float startTransitionX = getTranslationX();
            final float startTransitionY = getTranslationY();
            final float startScaleX = getScaleX();
            final float startScaleY = getScaleY();
            final float endScaleX = (float) mRegionWidth / getWidth();
            final float endScaleY = (float) (mTTStyleEnable ? mRegionHeight : mViewRealHeight) / getHeight();

            // 判定顶部超出屏幕或者底部超出屏幕 后面版本可以扩展左右超出屏幕
            final boolean overScreen = mRegionHeight != mViewRealHeight;

            mStartAndEndY = mRegionY;

            // 如果超出屏幕需要重新计算
            if (!mTTStyleEnable && overScreen) {
                // 分两种情况 1、顶部超出屏幕 2、底部超出屏幕
                if ((mRegionY + mViewRealHeight) > getHeight()) {
                    // 底部超出屏幕
                    mStartAndEndY = mRegionY - (mViewRealHeight - mRegionHeight);
                }
            }
            if (mStartAndEndY > mDistanceScreenTop) {
                mStartAndEndY -= mDistanceScreenTop;
            }
            mExitAnimation = ValueAnimator.ofFloat(0F, 1.0F).setDuration(mDuration);
            mExitAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    setScaleX(startScaleX + value * (endScaleX - startScaleX));
                    setScaleY(startScaleY + value * (endScaleY - startScaleY));

                    setTranslationX(startTransitionX + value * (mRegionX - startTransitionX) - value * (getWidth() - mRegionWidth) / 2.0F);
                    setTranslationY(startTransitionY - value * (startTransitionY - mStartAndEndY) - value * (getHeight() - (mTTStyleEnable ? mRegionHeight : mViewRealHeight)));

                }
            });
            mExitAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (mListener != null) {
                        mListener.onEndExit();
                    }
                    mAnimRunning = false;
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    mAnimRunning = true;
                    if (mListener != null) {
                        mListener.onStartExit(overScreen);
                    }
                }
            });
            mExitAnimation.start();
        } else {
            if (mListener != null) {
                mListener.onEndExit();
            }
        }
    }

    /**
     * 转场动画
     *
     * @param isExit 是否执行退出转场动画
     */
    private void transitionsAnimation(final boolean isExit) {
        if (mTransitionsAnimation != null && mTransitionsAnimation.isRunning()) {
            return;
        }
        if (mResumeAnimation != null && mResumeAnimation.isRunning()) {
            return;
        }
        if (mExitAnimation != null && mExitAnimation.isRunning()) {
            return;
        }
        if (mRegionWidth != 0 && mRegionHeight != 0 && mDragEnable) {
            // 设置缩放中心点
            setPivotX(0);
            setPivotY(0);

            // 控件的宽高度 Measured 不是很精准
            int height = getHeight();
            int width = getWidth();

            // 处理非0的情况 默认处理成全屏
            if (height == 0 || width == 0) {
                height = getMeasuredHeight();
                width = getMeasuredWidth();
            }

            // 非变形处理 头条的方式 超出屏幕会变形
            final float startScaleX = (float) (mTTStyleEnable ? mRegionWidth : mViewRealWidth) / width;
            final float startScaleY = (float) (mTTStyleEnable ? mRegionHeight : mViewRealHeight) / height;

            // 判定顶部超出屏幕或者底部超出屏幕 后面版本可以扩展左右超出屏幕
            final boolean overScreen = mRegionHeight != mViewRealHeight;

            mStartAndEndY = mRegionY;

            // 如果超出屏幕需要重新计算
            if (!mTTStyleEnable && overScreen) {
                // 分两种情况 1、顶部超出屏幕 2、底部超出屏幕
                if ((mRegionY + mViewRealHeight) > height) {
                    // 底部超出屏幕
                    mStartAndEndY = isExit ? (mRegionY - (mViewRealHeight - mRegionHeight)) : mRegionY;
                } else {
                    // 顶部超出屏幕 进入的转场动画需要重新计算y值
                    mStartAndEndY = isExit ? mRegionY : (mRegionY - (mViewRealHeight - mRegionHeight));
                }
            }
            // 处理距离顶部的距离
            if (mStartAndEndY > mDistanceScreenTop) {
                mStartAndEndY -= mDistanceScreenTop;
            }
            mTransitionsAnimation = ValueAnimator.ofFloat(isExit ? 1.0F : 0F, isExit ? 0F : 1.0F).setDuration(mDuration);
            mTransitionsAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    setTranslationX(mRegionX - value * mRegionX);
                    setTranslationY(mStartAndEndY - value * mStartAndEndY);

                    setScaleX(startScaleX + value * (1.0F - startScaleX));
                    setScaleY(startScaleY + value * (1.0F - startScaleY));
                }
            });
            mTransitionsAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    onEnd(isExit);
                    mAnimRunning = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    mAnimRunning = false;
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    if (getVisibility() == INVISIBLE) {
                        setVisibility(VISIBLE);
                    }
                    mAnimRunning = true;
                    onStart(isExit, overScreen);
                }
            });
            mTransitionsAnimation.start();
        } else {
            if (getVisibility() == INVISIBLE) {
                setVisibility(VISIBLE);
            }
            onEnd(isExit);
        }
    }

    /**
     * 处理距离顶部的距离
     */
    private void getDistanceScreenTop() {
        int loc[] = new int[2];
        getLocationOnScreen(loc);
        if (loc != null) {
            mDistanceScreenTop = loc[1];
        }
    }

    private void onEnd(boolean isExit) {
        if (mListener != null) {
            if (isExit) {
                mListener.onEndExit();
            } else {
                mListener.onEndEnter();
            }
        }
    }

    private void onStart(boolean isExit, boolean overScreen) {
        if (mListener != null) {
            if (isExit) {
                mListener.onStartExit(overScreen);
            } else {
                mListener.onStartEnter(overScreen);
            }
        }
    }

    /**
     * 重置相关数据
     *
     * @param x
     * @param y
     */
    private void reset(float x, float y) {
        mTouchLastX = x;
        mTouchLastY = y;
        mTouchOffsetY = 0;
        mTouchOffsetX = 0;
        mActionMoveCount = 0;
        mIsStartDrag = false;
        mIsInterceptEvent = false;
    }

    // 设置位置信息
    public DragRelativeLayout setTransitionsView(View view) {
        if (view != null) {
            Rect locRect = new Rect();
            view.getGlobalVisibleRect(locRect);
            setTransitionsRegion(locRect.left, locRect.top, locRect.right, locRect.bottom, view.getWidth(), view.getHeight());
        }
        return this;
    }

    public DragRelativeLayout setTransitionsView(Rect rect) {
        if (rect != null) {
            setTransitionsRegion(rect.left, rect.top, rect.right, rect.bottom, rect.width(), rect.height());
        }
        return this;
    }

    public DragRelativeLayout setTransitionsRegion(int l, int t, int r, int b, int w, int h) {
        mRegionX = l;
        mRegionY = t;
        mRegionWidth = r - l;
        mRegionHeight = b - t;
        mViewRealHeight = h;
        mViewRealWidth = w;
        return this;
    }

    // 执行开始转场动画
    public void startTransitions(View view) {
        setTransitionsView(view);
        startTransitions();
    }

    public void startTransitions() {
        // 某些机型会闪烁 兼容处理
        setVisibility(INVISIBLE);
        post(new Runnable() {
            @Override
            public void run() {
                transitionsAnimation(false);
            }
        });
    }

    // 执行结束转场动画
    public void endTransitions() {
        transitionsAnimation(true);
    }

    // 设置回调
    public DragRelativeLayout setOnoDragListener(com.meis.widget.photodrag.OnDragListener listener) {
        mListener = listener;
        return this;
    }

    public int getDuration() {
        return mDuration;
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }

    public float getResumeRatio() {
        return mResumeRatio;
    }

    public void setResumeRatio(float resumeRatio) {
        mResumeRatio = resumeRatio;
    }

    public boolean isDragEnable() {
        return mDragEnable;
    }

    public void setDragEnable(boolean dragEnable) {
        mDragEnable = dragEnable;
    }

    public float getStartTranslationYRatio() {
        return mStartTranslationYRatio;
    }

    public void setStartTranslationYRatio(float startTranslationYRatio) {
        mStartTranslationYRatio = startTranslationYRatio;
    }

    public int getTranslationYRate() {
        return mTranslationYRate;
    }

    public void setTranslationYRate(int translationYRate) {
        mTranslationYRate = translationYRate;
    }

    public boolean isTTStyleEnable() {
        return mTTStyleEnable;
    }

    public void setTTStyleEnable(boolean TTStyleEnable) {
        mTTStyleEnable = TTStyleEnable;
    }

}

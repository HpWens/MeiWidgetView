package com.meis.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by wenshi on 2018/4/27.
 * Description 切换皮肤 转场动画
 */

public class MeiRippleView extends View {

    //屏幕截图
    private Bitmap mBackground;

    private Paint mPaint;
    //最大半径长度
    private int mMaxRadius;
    //开始半径长度
    private int mStartRadius;
    //当前半径长度
    private int mCurrentRadius;
    //动画是否已经开始
    private boolean isStarted = false;
    //运动时长
    private long mDuration = 1000;
    //扩散的起点
    private float mStartX;
    private float mStartY;
    //DecorView
    private ViewGroup mRootView;

    private ValueAnimator mRippleAnimator;

    public MeiRippleView(Context context) {
        this(context, null);
    }

    public MeiRippleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MeiRippleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        //设置为擦除模式
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }


    /**
     * @param startX      被点击view相对屏幕的 view中心点x坐标
     * @param startY      被点击view相对屏幕的 view中心点y坐标
     * @param startRadius 开始扩散的半径
     */
    public void startRipple(int startX, int startY, int startRadius) {
        mRootView = (ViewGroup) ((Activity) getContext()).getWindow().getDecorView();
        //或者((Activity) getContext()).findViewById(android.R.id.content);

        mStartX = startX;
        mStartY = startY;
        mStartRadius = startRadius;
        //计算最大的扩展半径
        calculateMaxRadius();

        if (!isStarted) {
            isStarted = true;
            updateBackground();
            attachToRootView();
            startAnimation();
        }
    }

    //开始动画
    private void startAnimation() {
        mRippleAnimator = ValueAnimator.ofFloat(mStartRadius, mMaxRadius)
                .setDuration(mDuration);
        mRippleAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isStarted = false;
                //动画播放完毕, 移除本View
                detachFromRootView();
            }
        });
        mRippleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //更新圆的半径
                mCurrentRadius = (int) (float) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        mRippleAnimator.start();
    }

    //获取到最大的半径
    private void calculateMaxRadius() {
        float maxRectX = Math.max(mStartX, mRootView.getWidth() - mStartX);
        float maxRectY = Math.max(mStartY, mRootView.getHeight() - mStartY);
        mMaxRadius = (int) Math.sqrt(Math.pow(maxRectX, 2) + Math.pow(maxRectY, 2));
    }

    public MeiRippleView setDuration(long duration) {
        mDuration = duration;
        return this;
    }

    /**
     * 更新屏幕截图
     */
    private void updateBackground() {
        recycleBackground();
        mRootView.setDrawingCacheEnabled(true);
        mBackground = mRootView.getDrawingCache();
        mBackground = Bitmap.createBitmap(mBackground);
        mRootView.setDrawingCacheEnabled(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //在新的图层上面绘制
        int layer = canvas.save();
        canvas.drawBitmap(mBackground, 0, 0, null);
        canvas.drawCircle(mStartX, mStartY, mCurrentRadius, mPaint);
        canvas.restoreToCount(layer);
    }

    /**
     * 添加到根视图
     */
    private void attachToRootView() {
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mRootView.addView(this);
    }

    /**
     * 从根视图中移除
     */
    private void detachFromRootView() {
        recycleBackground();
        mRootView.removeView(this);
    }

    private void recycleBackground() {
        if (mBackground != null && !mBackground.isRecycled()) {
            mBackground.recycle();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mRippleAnimator != null && mRippleAnimator.isRunning()) {
            mRippleAnimator.cancel();
        }
        super.onDetachedFromWindow();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //消费掉事件
        return true;
    }
}

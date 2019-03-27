package com.meis.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.meis.widget.utils.DensityUtil;

/**
 * desc:文字路径控件
 * author: wens
 * date: 2018/4/29.
 */
public class MeiTextPathView extends View {

    private TextPaint mPaint;
    //默认值为5
    private int mTextSize = 64;

    //文本宽度
    private float mTextWidth;
    private float mTextHeight;

    private int mTextColor = Color.RED;

    //文本
    private String mText = "MEI_S";

    private float mStrokeWidth = 5;

    private Path mFontPath;
    private Path mDstPath;

    private PathMeasure mPathMeasure;

    private float mPathLength = 0;
    private float mCurrentLength = 0;
    //绘画部分长度
    protected float mStop = 0;

    private ValueAnimator mAnimation = null;

    //动画是否循环
    private boolean mIsCycle;

    //动画时长
    private int mDuration = 6000;

    //是否自动开始
    private boolean mAutoStart;

    public MeiTextPathView(Context context) {
        this(context, null);
    }

    public MeiTextPathView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MeiTextPathView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关闭硬件加速
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MeiTextPathView);
        mText = ta.getString(R.styleable.MeiTextPathView_text);
        mTextSize = ta.getDimensionPixelSize(R.styleable.MeiTextPathView_textSize, 108);
        mTextColor = ta.getColor(R.styleable.MeiTextPathView_textColor, Color.RED);
        mDuration = ta.getInt(R.styleable.MeiTextPathView_duration, 6000);
        mStrokeWidth = ta.getDimensionPixelOffset(R.styleable.MeiTextPathView_strokeWidth, 5);
        mIsCycle = ta.getBoolean(R.styleable.MeiTextPathView_cycle, false);
        mAutoStart = ta.getBoolean(R.styleable.MeiTextPathView_autoStart, true);
        ta.recycle();

        init();
        initTextPath();
    }

    private void init() {
        mPaint = new TextPaint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(mTextSize);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mTextColor);
        mPaint.setStrokeWidth(mStrokeWidth);

        mFontPath = new Path();
        mDstPath = new Path();

        mPathMeasure = new PathMeasure();
    }

    //初始化文字路径
    private void initTextPath() {
        mPathLength = 0;
        mFontPath.reset();
        mDstPath.reset();

        if (null == mText || mText.equals("")) {
            mText = "mei_s";
        }
        mTextWidth = Layout.getDesiredWidth(mText, mPaint);
        Paint.FontMetrics metrics = mPaint.getFontMetrics();
        mTextHeight = metrics.bottom - metrics.top;

        mPaint.getTextPath(mText, 0, mText.length(), 0, -metrics.ascent, mFontPath);
        mPathMeasure.setPath(mFontPath, false);

        mPathLength = mPathMeasure.getLength();
        while (mPathMeasure.nextContour()) {
            mPathLength += mPathMeasure.getLength();
        }

        if (mAutoStart) {
            post(new Runnable() {
                @Override
                public void run() {
                    startAnimation();
                }
            });
        }
    }

    /**
     * 开始动画
     */
    public void startAnimation() {
        if (mAnimation == null) {
            mAnimation = ValueAnimator.ofFloat(0, mPathLength);
        }
        if (mAnimation.isRunning()) return;
        if (mIsCycle) {
            mAnimation.setRepeatCount(-1);
        } else {
            mAnimation.setRepeatCount(0);
        }
        mAnimation.setDuration(mDuration);
        mAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mCurrentLength = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        mAnimation.start();
    }

    /**
     * 停止动画
     */
    public void stopAnimation() {
        if (mAnimation != null && mAnimation.isRunning())
            mAnimation.cancel();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        //处理包裹内容的情况
        int warpDefaultSize = DensityUtil.dip2px(getContext(), 100);

        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            widthSize = heightSize = warpDefaultSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            widthSize = warpDefaultSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            heightSize = warpDefaultSize;
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        mDstPath.reset();
        mStop = mCurrentLength;

        //在中间绘制
        canvas.translate(getWidth() / 2 - mTextWidth / 2, 0);
        canvas.translate(0, getHeight() / 2 - mTextHeight / 2);

        //重置路径
        mPathMeasure.setPath(mFontPath, false);

        while (mStop > mPathMeasure.getLength()) {
            mStop = mStop - mPathMeasure.getLength();
            mPathMeasure.getSegment(0, mPathMeasure.getLength(), mDstPath, true);
            if (!mPathMeasure.nextContour()) {
                break;
            }
        }
        mPathMeasure.getSegment(0, mStop, mDstPath, true);

        canvas.drawPath(mDstPath, mPaint);
        canvas.restore();
    }

    /**
     * 设置画笔宽度
     * @param size
     * @return
     */
    public MeiTextPathView setTextSize(int size) {
        this.mTextSize = size;
        this.mPaint.setTextSize(mTextSize);
        return this;
    }

    /**
     * 设置画笔颜色
     * @param color
     * @return
     */
    public MeiTextPathView setTextColor(int color) {
        this.mTextColor = color;
        this.mPaint.setColor(mTextColor);
        return this;
    }

    /**
     * 设置绘制的文本
     * @param text
     * @return
     */
    public MeiTextPathView setText(String text) {
        this.mText = text;
        this.initTextPath();
        return this;
    }

    /**
     * 设置动画时长
     * @param duration
     * @return
     */
    public MeiTextPathView setDuration(int duration) {
        this.mDuration = duration;
        return this;
    }

    /**
     * 设置描边宽度
     * @param width
     * @return
     */
    public MeiTextPathView setStrokeWidth(int width) {
        this.mStrokeWidth = width;
        this.mPaint.setStrokeWidth(mStrokeWidth);
        return this;
    }

    //是否循环
    public MeiTextPathView setCycle(boolean cycle) {
        this.mIsCycle = cycle;
        return this;
    }

    //是否自动开始
    public MeiTextPathView setAutoStart(boolean autoStart) {
        this.mAutoStart = autoStart;
        return this;
    }

    public int getTextSize() {
        return mTextSize;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public String getText() {
        return mText;
    }

    public float getStrokeWidth() {
        return mStrokeWidth;
    }

    public int getDuration() {
        return mDuration;
    }

    public boolean isCycle() {
        return mIsCycle;
    }

    public boolean isAutoStart() {
        return mAutoStart;
    }
}

package com.meis.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;

import com.meis.widget.utils.DensityUtil;

/**
 * desc:滚动视差控件
 * author: wens
 * date: 2018/1/21.
 */

public class MeiScrollParallaxView extends AppCompatImageView implements
        ViewTreeObserver.OnScrollChangedListener {

    //圆角外的颜色 手动设置跟图片背景色一致
    private int mOutRoundColor;

    //是否绘制圆形  若为 true 圆角失效
    private boolean mIsCircle;

    //是否显示视差
    private boolean mIsParallax;

    //实际图形矩形
    private RectF mRect;

    //用于绘制圆角路径
    private Path mPath;

    //圆角画笔
    private Paint mPaint;

    //矩阵
    private Matrix mMatrix;

    //屏幕中的位置
    private int[] mScreenLocation = new int[2];

    //圆角半径
    private int mRoundWidth;

    //默认的视差滚动速率 (0~1)
    private float mParallaxRate;
    private static final float DEFAULT_PARALLAX_RATE = 0.3F;

    //屏幕高度
    private int mScreenHeight = 0;

    public MeiScrollParallaxView(Context context) {
        this(context, null);
    }

    public MeiScrollParallaxView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MeiScrollParallaxView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MeiScrollParallaxView);
        mParallaxRate = ta.getFloat(R.styleable.MeiScrollParallaxView_parallaxRate, DEFAULT_PARALLAX_RATE);
        mIsParallax = ta.getBoolean(R.styleable.MeiScrollParallaxView_enableParallax, true);
        mIsCircle = ta.getBoolean(R.styleable.MeiScrollParallaxView_enableCircle, false);
        mRoundWidth = ta.getDimensionPixelSize(R.styleable.MeiScrollParallaxView_roundWidth, 0);
        mOutRoundColor = ta.getColor(R.styleable.MeiScrollParallaxView_outRoundColor, Color.WHITE);
        ta.recycle();

        init();
    }

    private void init() {
        mMatrix = new Matrix();

        mPath = new Path();
        mPath.setFillType(Path.FillType.INVERSE_EVEN_ODD);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mOutRoundColor);

        mScreenHeight = DensityUtil.getScreenSize(getContext()).y;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //圆形从新进行计算
        if (mIsCircle) {
            int width = getMeasuredWidth();
            int height = getMeasuredHeight();
            setMeasuredDimension(Math.min(width, height), Math.min(width, height));
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mPath.reset();
        if (mRoundWidth != 0) {
            //获取圆角大小
            mRoundWidth = mIsCircle ? Math.min(w / 2, h / 2) : mRoundWidth;
            //添加圆角矩形
            mPath.addRoundRect(new RectF(0, 0, w, h), mRoundWidth, mRoundWidth, Path.Direction.CCW);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mIsParallax) {
            int saveCount = canvas.save();
            getLocationInWindow(mScreenLocation);

            //获取到图形矩形框
            if (mRect == null) {
                mRect = new RectF(getDrawable().getBounds());
            }

            float parallaxScale = 0;
            //视图宽度
            float vw = getWidth();
            //视图高度
            float vh = getHeight();
            //图片宽度
            float bw = mRect.width();
            //图片高度
            float bh = mRect.height();
            //视图宽高比
            float vratio = vw / vh;
            //图片宽高比
            float bratio = bw / bh;
            //最大滑动高度
            float ph = (1 + mParallaxRate) * vh;

            if (bratio > vratio) {
                parallaxScale = ph / (vw / bratio);
            } else {
                float _scale = vw / (vh * bratio);
                float _ph = vw / bratio;
                if (_ph < ph) {
                    _scale = ph / vh;
                }
                parallaxScale = _scale;
            }

            //重置矩阵
            mMatrix.reset();
            mMatrix.mapRect(mRect);

            //居中缩放
            mMatrix.postScale(parallaxScale, parallaxScale, vw / 2, vh / 2);

            //增加滑动偏移量，让滑动从图片顶部开始
            float translationY = mParallaxRate / 2f * vh;
            mMatrix.postTranslate(0, translationY);

            //滚动偏移
            mMatrix.postTranslate(0, -(mParallaxRate * vh) * ((float) mScreenLocation[1] / mScreenHeight));

            canvas.concat(mMatrix);

            super.onDraw(canvas);
            canvas.restoreToCount(saveCount);
        } else {
            super.onDraw(canvas);
        }
        canvas.drawPath(mPath, mPaint);
    }

    /**
     * 设置滑动速率
     * @param rate
     * @return
     */
    public MeiScrollParallaxView setParallaxRate(int rate) {
        if (rate < 0) {
            rate = 0;
        }
        this.mParallaxRate = rate;
        return this;
    }

    /**
     * 是否显示视差
     * @param parallax
     * @return
     */
    public MeiScrollParallaxView setParallax(boolean parallax) {
        mIsParallax = parallax;
        return this;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnScrollChangedListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        getViewTreeObserver().removeOnScrollChangedListener(this);
        super.onDetachedFromWindow();
    }

    @Override
    public void onScrollChanged() {
        postInvalidate();
    }
}

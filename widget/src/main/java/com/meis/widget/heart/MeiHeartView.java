package com.meis.widget.heart;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.meis.widget.R;
import com.meis.widget.utils.DensityUtil;

import java.util.Random;


/**
 * desc:直播间送爱心
 * author: wens
 * date: 2017/9/30.
 */

public class MeiHeartView extends View {

    private SparseArray<Bitmap> mBitmapArray = new SparseArray<>();
    private SparseArray<Heart> mHeartArray = new SparseArray<>();

    private Paint mPaint;

    private int mWidth;
    private int mHeight;

    private Matrix mMatrix;

    //是否控制透明度速率
    private boolean mAlphaEnable;

    //是否控制缩放
    private boolean mScaleEnable;

    //动画时长
    private int mDuration;
    private final static int DURATION_TIME = 3000;

    public MeiHeartView(Context context) {
        this(context, null);
    }

    public MeiHeartView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MeiHeartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关闭硬件加速
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MeiHeartView);
        mDuration = ta.getInteger(R.styleable.MeiHeartView_heartDuration, DURATION_TIME);
        mAlphaEnable = ta.getBoolean(R.styleable.MeiHeartView_heartEnableAlpha, true);
        mScaleEnable = ta.getBoolean(R.styleable.MeiHeartView_heartEnableScale, true);
        ta.recycle();

        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mMatrix = new Matrix();
    }

    /**
     *
     * @param type
     * @param bitmap
     */
    public void setHeartBitmap(int type, Bitmap bitmap) {
        if (mBitmapArray.get(type) == null) {
            mBitmapArray.put(type, bitmap);
        }
    }

    /**
     *
     * @param bitmapArray
     */
    public void setHeartBitmap(SparseArray<Bitmap> bitmapArray) {
        if (null != bitmapArray) {
            mBitmapArray = bitmapArray;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

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


    public void addHeart(final int index) {
        if (mBitmapArray.size() == 0 || index < 0 || index > (mBitmapArray.size() - 1)) {
            return;
        }
        final Path path = new Path();
        final PathMeasure pathMeasure = new PathMeasure();
        final Heart heart = new Heart();
        heart.index = index;

        //绘制三阶贝塞尔曲线 起点位置
        PointF start = new PointF();
        //贝塞尔控制点1
        PointF control1 = new PointF();
        //贝塞尔控制点2
        PointF control2 = new PointF();
        //贝塞尔结束点
        PointF end = new PointF();

        initStartAndEnd(start, end);
        initControl(control1, control2);

        path.moveTo(start.x, start.y);
        path.cubicTo(control1.x, control1.y, control2.x, control2.y, end.x, end.y);

        pathMeasure.setPath(path, false);

        final float pathLength = pathMeasure.getLength();

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1.0f);
        //先加速后减速
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        //动画的长短来控制速率
        animator.setDuration(mDuration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fraction = valueAnimator.getAnimatedFraction();

                float[] pos = new float[2];
                pathMeasure.getPosTan(fraction * pathLength, pos, null);
                heart.x = pos[0];
                heart.y = pos[1];
                heart.progress = fraction;

                postInvalidate();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mHeartArray.remove(heart.hashCode());
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mHeartArray.put(heart.hashCode(), heart);
            }
        });
        animator.start();
    }

    public void addHeart() {
        if (mBitmapArray.size() > 0) {
            addHeart(new Random().nextInt(mBitmapArray.size() - 1));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvasHeart(canvas);
    }

    /**
     * 绘制爱心
     * @param canvas
     */
    private void canvasHeart(Canvas canvas) {
        for (int i = 0; i < mHeartArray.size(); i++) {

            Heart heart = mHeartArray.valueAt(i);

            if (null == heart) {
                return;
            }

            //设置画笔透明度
            mPaint.setAlpha(dealToAlpha(heart.progress));

            mMatrix.reset();
            //会覆盖掉之前的x,y数值
            mMatrix.setTranslate(0, 0);
            //位移到x,y
            mMatrix.postTranslate(heart.x, heart.y);

            // float px, float py  缩放中心点
            mMatrix.postScale(dealToScale(heart.progress), dealToScale(heart.progress), mWidth / 2, mHeight);

            canvas.drawBitmap(mBitmapArray.get(heart.index), mMatrix, mPaint);
        }
    }

    /**
     *
     * @param fraction
     * @return (0 ~ 255)
     */
    private int dealToAlpha(float fraction) {
        if (fraction > 0.8f && mAlphaEnable) {
            return (int) (255 - (fraction - 0.8f) / 0.2f * 255);
        }
        return 255;
    }

    /**
     * 获取到缩放比
     * @param fraction
     * @return
     */
    private float dealToScale(float fraction) {
        if (fraction < 0.1f && mScaleEnable) {
            //0.1路径 从0缩放到1
            return 0.5f + fraction / 0.1f * 0.5f;
        }
        return 1.0f;
    }

    /**
     * 设置控制点
     * @param control1
     * @param control2
     */
    private void initControl(PointF control1, PointF control2) {
        control1.x = (float) (Math.random() * mWidth);
        control1.y = (float) (Math.random() * mHeight);

        control2.x = (float) (Math.random() * mWidth);
        control2.y = (float) (Math.random() * mHeight);

        if (control1.x == control2.x && control1.y == control2.y) {
            initControl(control1, control2);
        }
    }

    /**
     * 设置起点和终点
     * @param start
     * @param end
     */
    private void initStartAndEnd(PointF start, PointF end) {
        start.x = mWidth / 2;
        start.y = mHeight;

        end.x = mWidth / 2;
        end.y = 0;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onDetachedFromWindow() {
        bitmapCycle();
        super.onDetachedFromWindow();
    }


    /**
     * 取消已有动画，释放资源
     */
    public void bitmapCycle() {
        //回收bitmap
        for (int i = 0; i < mBitmapArray.size(); i++) {
            if (mBitmapArray.valueAt(i) != null) {
                mBitmapArray.valueAt(i).recycle();
            }
        }
    }
}

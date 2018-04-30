package com.meis.widget.loading;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import com.meis.widget.R;
import com.meis.widget.utils.DensityUtil;

/**
 * Created by ws on 2018/4/25.
 * Description : fan rotation view
 */
public class MeiFanView extends View {

    private Paint mPaint;

    //线条颜色
    private int mLineColor;

    //中心点坐标
    private int mCenterX;
    private int mCenterY;
    //可绘制的圆半径
    private int mRadius;

    //描边宽度
    private float mStrokeWidth = 5;

    //扇的半径比 （0~1）
    private float mFirstFanRadiusRatio;
    private float mSecondFanRadiusRatio = 0.5f;
    private final static float FAN_RADIUS_RATIO = 0.67F;

    //扇扫描的角度 （0~360）
    private int mFanSweepAngle = 80;

    private RotateAnimation mRotateAnimation;

    private int mDuration = 0;

    public MeiFanView(Context context) {
        this(context, null);
    }

    public MeiFanView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MeiFanView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MeiFanView);
        mLineColor = ta.getColor(R.styleable.MeiFanView_lineColor, Color.RED);
        mFirstFanRadiusRatio = ta.getFloat(R.styleable.MeiFanView_fanRadiusRatio, FAN_RADIUS_RATIO);
        mFanSweepAngle = ta.getInt(R.styleable.MeiFanView_fanSweepAngle, 80);
        mDuration = ta.getInt(R.styleable.MeiFanView_fanDuration, 800);
        ta.recycle();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setColor(mLineColor);

        post(new Runnable() {
            @Override
            public void run() {
                startAnimation();
            }
        });
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

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int leftPadding = getPaddingLeft();
        int rightPadding = getPaddingRight();
        int topPadding = getPaddingTop();
        int bottomPadding = getPaddingBottom();
        mCenterX = w / 2;
        mCenterY = h / 2;
        mCenterX += (leftPadding - rightPadding) / 2;
        mCenterY += (topPadding - bottomPadding) / 2;
        //- DensityUtil.dip2px(getContext(), 1) 确保精度
        mRadius = Math.min((w - Math.max(leftPadding, rightPadding)) / 2, (h - Math.max(topPadding, bottomPadding)) / 2) - DensityUtil.dip2px(getContext(), 1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        //推荐使用路径绘制
        canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaint);
        float fanRadius = mRadius * mFirstFanRadiusRatio;
        RectF fanRectF_1 = new RectF(mCenterX - fanRadius, mCenterY - fanRadius, mCenterX + fanRadius, mCenterY + fanRadius);
        //绘制风扇
        canvas.drawArc(fanRectF_1, 0, mFanSweepAngle, false, mPaint);
        canvas.drawArc(fanRectF_1, 180, mFanSweepAngle, false, mPaint);

        fanRadius = mRadius * mSecondFanRadiusRatio;
        RectF fanRectF_2 = new RectF(mCenterX - fanRadius, mCenterY - fanRadius, mCenterX + fanRadius, mCenterY + fanRadius);
        canvas.drawArc(fanRectF_2, 0, mFanSweepAngle, false, mPaint);
        canvas.drawArc(fanRectF_2, 180, mFanSweepAngle, false, mPaint);

        //绘制中心小圆
        canvas.drawCircle(mCenterX, mCenterY, mRadius * 0.05f, mPaint);
        canvas.restore();
    }

    public void startAnimation() {
        if (mRotateAnimation == null) {
            mRotateAnimation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        }
        mRotateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        mRotateAnimation.setRepeatCount(-1);
        mRotateAnimation.setDuration(mDuration);
        mRotateAnimation.setFillAfter(true);
        startAnimation(mRotateAnimation);
    }

    public void stopAnimation() {
        if (mRotateAnimation != null || getAnimation() != null) {
            clearAnimation();
        }
    }

    public int getLineColor() {
        return mLineColor;
    }

    /**
     * 设置风扇颜色
     * @param lineColor
     */
    public void setLineColor(int lineColor) {
        this.mLineColor = lineColor;
        this.mPaint.setColor(mLineColor);
    }

    /**
     *
     * @param ratio (0~1)
     */
    public void setFirstFanRadiusRatio(float ratio) {
        if (ratio < 0) {
            ratio = 0;
        } else if (ratio > 1) {
            ratio = 1;
        }
        this.mFirstFanRadiusRatio = ratio;
    }

    /**
     *
     * @param angle (0~360)
     */
    public void setFanSweepAngle(int angle) {
        if (angle < 0) {
            angle = 0;
        } else if (angle > 360) {
            angle = 360;
        }
        this.mFanSweepAngle = angle;
    }
}

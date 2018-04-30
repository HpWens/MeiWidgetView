package com.meis.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.meis.widget.utils.DensityUtil;

/**
 * Created by wenshi on 2018/4/27.
 * Description 颜色选择器
 */

public class ColorPickerView extends View {
    //wrap_content 情况默认大小为 100dp
    private static int WARP_DEFAULT_SIZE = 100;

    private Paint mPaint;

    //手指的画笔
    private Paint mFingerPaint;

    //宽高一半 的最小值
    private int mPickerRadius;

    private int mCenterX;
    private int mCenterY;

    private int mFingerX;
    private int mFingerY;

    private int[] mGradientColors;

    private float[] colorHSV = new float[]{0f, 1f, 1f};

    public ColorPickerView(Context context) {
        this(context, null);
    }

    public ColorPickerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关闭硬件加速
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        mFingerPaint = new Paint();
        mFingerPaint.setColor(Color.WHITE);
        mFingerPaint.setAntiAlias(true);
        //获取到渐变的颜色数组
        mGradientColors = getGradientColors();
    }

    private int[] getGradientColors() {
        int colorCount = 12;
        int circleAngle = 360;
        int colorAngleStep = 360 / colorCount;

        int colors[] = new int[colorCount];
        float hsv[] = new float[]{0f, 1f, 1f};
        for (int i = 0; i < colors.length; i++) {
            hsv[0] = (i * colorAngleStep + circleAngle / 2) % circleAngle;
            colors[i] = Color.HSVToColor(hsv);
        }
        return colors;
    }

    private ComposeShader getPickerShader() {
        SweepGradient sweepGradient = new SweepGradient(mCenterX, mCenterY, mGradientColors, null);
        RadialGradient radialGradient = new RadialGradient(mCenterX, mCenterY, mPickerRadius,
                0xFFFFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP);
        ComposeShader composeShader = new ComposeShader(sweepGradient, radialGradient, PorterDuff.Mode.SRC_OVER);
        return composeShader;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int warpDefaultSize = DensityUtil.dip2px(getContext(), WARP_DEFAULT_SIZE);

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
        mCenterX = w / 2;
        mCenterY = h / 2;
        mPickerRadius = Math.min(mCenterX, mCenterY);
        //获取选择器的渲染Shader
        mPaint.setShader(getPickerShader());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.drawCircle(mCenterX, mCenterY, mPickerRadius, mPaint);
        canvas.drawCircle(mFingerX, mFingerY, 10, mFingerPaint);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                int x = (int) event.getX();
                int y = (int) event.getY();

                //计算触摸点距离中心点的距离
                int distance = (int) Math.sqrt((x - mCenterX) * (x - mCenterX) + (y - mCenterY) * (y - mCenterY));
                if (distance <= mPickerRadius) {
                    colorHSV[0] = (float) (Math.toDegrees(Math.atan2((y - mCenterY), (x - mCenterX))) + 180f);
                    colorHSV[1] = Math.max(0f, Math.min(1f, (float) (distance / mPickerRadius)));

                    mFingerY = y;
                    mFingerX = x;
                    postInvalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    public int getColor() {
        return Color.HSVToColor(colorHSV);
    }
}

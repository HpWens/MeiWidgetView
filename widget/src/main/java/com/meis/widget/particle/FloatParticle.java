package com.meis.widget.particle;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;

import java.util.Random;

/**
 * Created by wenshi on 2018/7/4.
 * Description 浮点粒子
 */
public class FloatParticle {

    // 三阶贝塞尔曲线
    private Point startPoint;
    private Point endPoint;
    private Point controlPoint1;
    private Point controlPoint2;

    private Paint mPaint;
    private Path mPath;
    private Random mRandom;

    // 圆半径
    private float mRadius = 5;

    // 控件宽度
    private int mWidth;
    // 控件高度
    private int mHeight;

    private float mCurDistance = 0;

    private static final int DISTANCE = 255;

    private static final float MOVE_PER_FRAME = 1f;

    // 火花外侧阴影大小
    private static final float BLUR_SIZE = 5.0F;

    // 路径测量
    private PathMeasure mPathMeasure;

    private float mMeasureLength;

    public FloatParticle(int width, int height) {
        mWidth = width;
        mHeight = height;
        mRandom = new Random();

        startPoint = new Point((int) (mRandom.nextFloat() * mWidth), (int) (mRandom.nextFloat() * mHeight));

        // 抗锯齿
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        // 防抖动
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.FILL);
        // 设置模糊效果 边缘模糊
        mPaint.setMaskFilter(new BlurMaskFilter(BLUR_SIZE, BlurMaskFilter.Blur.SOLID));

        mPath = new Path();
        mPathMeasure = new PathMeasure();

        startPoint.x = (int) (mRandom.nextFloat() * mWidth);
        startPoint.y = (int) (mRandom.nextFloat() * mHeight);
    }

    public void drawParticle(Canvas canvas) {

        // 初始化三阶贝塞尔曲线数据
        if (mCurDistance == 0) {
            endPoint = getRandomPointRange(startPoint.x, startPoint.y, DISTANCE);
            controlPoint1 = getRandomPointRange(startPoint.x, startPoint.y, mRandom.nextInt(Math.min(mWidth, mHeight) / 2));
            controlPoint2 = getRandomPointRange(endPoint.x, endPoint.y, mRandom.nextInt(Math.min(mWidth, mHeight) / 2));
            // 添加贝塞尔曲线路径
            mPath.reset();
            mPath.moveTo(startPoint.x, startPoint.y);
            mPath.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, endPoint.x, endPoint.y);
            mPathMeasure.setPath(mPath, false);
            mMeasureLength = mPathMeasure.getLength();
        }
        //计算当前坐标点
        float[] loc = new float[2];
        mPathMeasure.getPosTan(mCurDistance / DISTANCE * mMeasureLength, loc, null);
        startPoint.x = (int) loc[0];
        startPoint.y = (int) loc[1];

        // 递增1
        mCurDistance += MOVE_PER_FRAME;

        if (mCurDistance >= DISTANCE) {
            mCurDistance = 0;
        }

        canvas.drawCircle(startPoint.x, startPoint.y, mRadius, mPaint);
    }

    /**
     * @param baseX 基准坐标x
     * @param baseY 基准坐标y
     * @param range 指定范围长度
     * @return 根据基准点获取指定范围的随机点
     */
    private Point getRandomPointRange(int baseX, int baseY, int range) {
        int randomX = 0;
        int randomY = 0;
        //range指定长度为255，可以根据实际效果调整
        if (range <= 0) {
            range = 1;
        }
        //我们知道一点(baseX,baseY)求与它距离长度为range的另一点

        //两点x方向的距离(随机产生)
        int distanceX = mRandom.nextInt(range);

        //知道x方向的距离与斜边的距离求y方向的距离
        int distanceY = (int) Math.sqrt(range * range - distanceX * distanceX);

        randomX = baseX + getRandomPNValue(distanceX);
        randomY = baseY + getRandomPNValue(distanceY);

        if (randomX > mWidth) {
            randomX = mWidth - range;
        } else if (randomX < 0) {
            randomX = range;
        } else if (randomY > mHeight) {
            randomY = mHeight - range;
        } else if (randomY < 0) {
            randomY = range;
        }

        return new Point(randomX, randomY);
    }

    /**
     * 获取随机的正负值
     *
     * @return
     */
    private int getRandomPNValue(int value) {
        return mRandom.nextBoolean() ? value : 0 - value;
    }

    /**
     * 设置圆半径
     *
     * @param radius
     */
    public void setRadius(float radius) {
        mRadius = radius;
    }
}

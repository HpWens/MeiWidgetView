package com.meis.widget.spiderweb;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by wenshi on 2019/3/26.
 * Description https://github.com/HpWens/MeiWidgetView
 */
public class SpiderWebView extends View {
    // 控件宽高
    private int mWidth;
    private int mHeight;
    // 画笔
    private Paint mPointPaint;
    private Paint mLinePaint;
    private Paint mTouchPaint;
    // 触摸点坐标
    private float mTouchX = -1;
    private float mTouchY = -1;
    // 数据源
    private List<SpiderPoint> mSpiderPointList;
    // 相关参数配置
    private SpiderConfig mConfig;
    // 随机数
    private Random mRandom;
    // 手势帮助类 用于处理滚动与拖拽
    private GestureDetector mGestureDetector;

    public SpiderWebView(Context context) {
        this(context, null);
    }

    public SpiderWebView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpiderWebView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // setLayerType(LAYER_TYPE_HARDWARE, null);

        mSpiderPointList = new ArrayList<>();
        mConfig = new SpiderConfig();
        mRandom = new Random();
        mGestureDetector = new GestureDetector(context, mSimpleOnGestureListener);

        // 画笔初始化
        initPaint();
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        mPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointPaint.setStrokeCap(Paint.Cap.ROUND);
        mPointPaint.setStrokeWidth(mConfig.pointRadius);
        mPointPaint.setColor(Color.parseColor("#EBFF4081"));

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStrokeWidth(mConfig.lineWidth);
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);
        mLinePaint.setColor(Color.parseColor("#EBFF94B9"));

        mTouchPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTouchPaint.setStrokeWidth(mConfig.touchPointRadius);
        mTouchPaint.setStrokeCap(Paint.Cap.ROUND);
        mTouchPaint.setColor(Color.parseColor("#D8FF7875"));
    }

    /**
     * 初始化小点
     */
    private void initPoint() {
        for (int i = 0; i < mConfig.pointNum; i++) {
            int width = (int) (mRandom.nextFloat() * mWidth);
            int height = (int) (mRandom.nextFloat() * mHeight);

            SpiderPoint point = new SpiderPoint(width, height);

            int aX = 0;
            int aY = 0;

            // 获取加速度
            while (aX == 0) {
                aX = (int) ((mRandom.nextFloat() - 0.5F) * mConfig.pointAcceleration);
            }
            while (aY == 0) {
                aY = (int) ((mRandom.nextFloat() - 0.5F) * mConfig.pointAcceleration);
            }

            point.aX = aX;
            point.aY = aY;

            point.color = randomRGB();

            mSpiderPointList.add(point);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        restart();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        // 绘制触摸点
        if (mTouchY != -1 && mTouchX != -1) {
            canvas.drawPoint(mTouchX, mTouchY, mTouchPaint);
        }

        if (mSpiderPointList == null || mSpiderPointList.size() <= 0) {
            return;
        }

        // 增强遍历
        int index = 0;
        for (SpiderPoint spiderPoint : mSpiderPointList) {

            spiderPoint.x += spiderPoint.aX;
            spiderPoint.y += spiderPoint.aY;

            // 越界反弹
            if (spiderPoint.x <= mConfig.pointRadius) {
                spiderPoint.x = mConfig.pointRadius;
                spiderPoint.aX = -spiderPoint.aX;
            } else if (spiderPoint.x >= (mWidth - mConfig.pointRadius)) {
                spiderPoint.x = (mWidth - mConfig.pointRadius);
                spiderPoint.aX = -spiderPoint.aX;
            }

            if (spiderPoint.y <= mConfig.pointRadius) {
                spiderPoint.y = mConfig.pointRadius;
                spiderPoint.aY = -spiderPoint.aY;
            } else if (spiderPoint.y >= (mHeight - mConfig.pointRadius)) {
                spiderPoint.y = (mHeight - mConfig.pointRadius);
                spiderPoint.aY = -spiderPoint.aY;
            }

            // 绘制触摸点与其他点的连线
            if (mTouchX != -1 && mTouchY != -1) {
                int offsetX = (int) (mTouchX - spiderPoint.x);
                int offsetY = (int) (mTouchY - spiderPoint.y);
                int distance = (int) Math.sqrt(offsetX * offsetX + offsetY * offsetY);
                if (distance < mConfig.maxDistance) {

                    if (distance >= (mConfig.maxDistance - mConfig.gravitation_strength)) {
                        if (spiderPoint.x > mTouchX) {
                            spiderPoint.x -= 0.03F * -offsetX;
                        } else {
                            spiderPoint.x += 0.03F * offsetX;
                        }

                        if (spiderPoint.y > mTouchY) {
                            spiderPoint.y -= 0.03F * -offsetY;
                        } else {
                            spiderPoint.y += 0.03F * offsetY;
                        }
                    }

                    int alpha = (int) ((1.0F - (float) distance / mConfig.maxDistance) * mConfig.lineAlpha);
                    mLinePaint.setColor(spiderPoint.color);
                    mLinePaint.setAlpha(alpha);
                    canvas.drawLine(spiderPoint.x, spiderPoint.y, mTouchX, mTouchY, mLinePaint);
                }
            }

            // 绘制小点
            mPointPaint.setColor(spiderPoint.color);
            canvas.drawCircle(spiderPoint.x, spiderPoint.y, mConfig.pointRadius, mPointPaint);

            // 绘制连线
            for (int i = index; i < mSpiderPointList.size(); i++) {
                SpiderPoint point = mSpiderPointList.get(i);
                // 判定当前点与其他点之间的距离
                if (spiderPoint != point) {
                    int distance = disPos2d(point.x, point.y, spiderPoint.x, spiderPoint.y);
                    if (distance < mConfig.maxDistance) {
                        // 绘制小点间的连线
                        int alpha = (int) ((1.0F - (float) distance / mConfig.maxDistance) * mConfig.lineAlpha);

                        mLinePaint.setColor(point.color);
                        mLinePaint.setAlpha(alpha);
                        canvas.drawLine(spiderPoint.x, spiderPoint.y, point.x, point.y, mLinePaint);
                    }
                }
            }

            index++;
        }
        canvas.restore();

        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP ||
                event.getAction() == MotionEvent.ACTION_CANCEL) {
            resetTouchPoint();
            return true;
        }
        return mGestureDetector.onTouchEvent(event);
    }

    /**
     * 两点间距离函数
     */
    public static int disPos2d(float x1, float y1, float x2, float y2) {
        return (int) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    /**
     * 获取范围随机整数：如 rangeInt(1,9)
     *
     * @param s 前数(包括)
     * @param e 后数(包括)
     * @return 范围随机整数
     */
    public static int rangeInt(int s, int e) {
        int max = Math.max(s, e);
        int min = Math.min(s, e) - 1;
        return (int) (min + Math.ceil(Math.random() * (max - min)));
    }

    /**
     * @return 获取到随机颜色值
     */
    private int randomRGB() {
        Random random = new Random();
        return Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255));
    }


    // 重置数据
    public void restart() {
        resetTouchPoint();
        clearPointList();
        initPoint();
    }

    /**
     * 重置触摸点
     */
    public void resetTouchPoint() {
        mTouchX = -1;
        mTouchY = -1;
    }

    /**
     * 清空数据源
     */
    private void clearPointList() {
        mSpiderPointList.clear();
    }

    // 手势 用于处理滑动与拖拽
    private GestureDetector.SimpleOnGestureListener mSimpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // 单根手指操作
            if (e1.getPointerCount() == e2.getPointerCount() && e1.getPointerCount() == 1) {
                mTouchX = e2.getX();
                mTouchY = e2.getY();
                return true;
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // 单根手指操作
            if (e1.getPointerCount() == e2.getPointerCount() && e1.getPointerCount() == 1) {
                mTouchX = e2.getX();
                mTouchY = e2.getY();
                return true;
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            // 赋值触摸点
            mTouchX = e.getX();
            mTouchY = e.getY();
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }
    };

    // 设置相关配置参数
    public int getPointRadius() {
        return mConfig.pointRadius;
    }

    public void setPointRadius(int pointRadius) {
        mConfig.pointRadius = pointRadius;
        mPointPaint.setStrokeWidth(mConfig.pointRadius);
    }

    public int getLineWidth() {
        return mConfig.lineWidth;
    }

    public void setLineWidth(int lineWidth) {
        mConfig.lineWidth = lineWidth;
        mLinePaint.setStrokeWidth(mConfig.lineWidth);
    }

    public int getLineAlpha() {
        return mConfig.lineAlpha;
    }

    public void setLineAlpha(int lineAlpha) {
        mConfig.lineAlpha = lineAlpha;
    }

    public int getPointNum() {
        return mConfig.pointNum;
    }

    public void setPointNum(int pointNum) {
        mConfig.pointNum = pointNum;
        restart();
    }

    public int getPointAcceleration() {
        return mConfig.pointAcceleration;
    }

    public void setPointAcceleration(int pointAcceleration) {
        mConfig.pointAcceleration = pointAcceleration;
        restart();
    }

    public int getMaxDistance() {
        return mConfig.maxDistance;
    }

    public void setMaxDistance(int maxDistance) {
        mConfig.maxDistance = maxDistance;
    }

    public int getTouchPointRadius() {
        return mConfig.touchPointRadius;
    }

    public void setTouchPointRadius(int touchPointRadius) {
        mConfig.touchPointRadius = touchPointRadius;
        mTouchPaint.setStrokeWidth(mConfig.touchPointRadius);
    }

    public int getGravitation_strength() {
        return mConfig.gravitation_strength;
    }

    public void setGravitation_strength(int gravitation_strength) {
        mConfig.gravitation_strength = gravitation_strength;
    }
}

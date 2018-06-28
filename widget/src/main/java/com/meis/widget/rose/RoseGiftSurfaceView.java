package com.meis.widget.rose;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.meis.widget.R;
import com.meis.widget.utils.PointUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Created by wenshi on 2018/6/26.
 * Description 改变自'那夕阳下的奔跑，是我逝去的青春'
 * 'https://github.com/jenly1314/GiftSurfaceView'
 */
public class RoseGiftSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    //不使用时回收c层内存
    private Bitmap bitmap;

    //画布
    private Canvas canvas;

    //画笔
    private Paint paint;

    //随机数
    private Random random;

    //起点集合
    private Point[] startPoint;

    //终点集合
    private Point[] endPoint;

    //动画期间点集合
    private Point[] currentPoint;

    //点集合
    private List<Point> points = new ArrayList<>();

    //控件宽度
    private float width;

    //控件高度
    private float height;

    /**
     * 缩放比例
     */
    private float scale = 1;

    /**
     * 偏移（非随机情况下有效）
     */
    private int offsetX;

    //y轴偏移量
    private int offsetY;

    //属性动画
    private ValueAnimator animator;

    private int duration = 3000;

    private static final String ASSET_LOVE = "assets/json/love.json";

    public RoseGiftSurfaceView(Context context) {
        this(context, null);
    }

    public RoseGiftSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoseGiftSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData();
    }

    //初始化数据
    private void initData() {
        paint = new Paint();
        //paint抗锯齿
        paint.setAntiAlias(true);

        random = new Random();

        //设置透明
        setZOrderOnTop(true);
        //配合清屏 canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        getHolder().addCallback(this);

        setLayerType(View.LAYER_TYPE_NONE, null);

        //设置图片
        setImageBitmap(getBitmapByResource(R.drawable.mei_rose), 0.5F);

        //设置点集合 性能待优化
        try {
            points = PointUtils.getListPointByResourceJson(RoseGiftSurfaceView.this.getContext(), ASSET_LOVE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param resId
     * @return
     */
    private Bitmap getBitmapByResource(@DrawableRes int resId) {
        return BitmapFactory.decodeResource(getResources(), resId);
    }

    /**
     * @param resId
     */
    public void setImageResource(@DrawableRes int resId) {
        setImageBitmap(getBitmapByResource(resId));
    }

    /**
     * @param resId
     * @param scale 缩放（图片）
     */
    public void setImageResource(@DrawableRes int resId, float scale) {
        setImageBitmap(getBitmapByResource(resId), scale);
    }

    /**
     * @param bitmap
     */
    public void setImageBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    /**
     * @param bitmap
     * @param scale  缩放（图片）
     */
    public void setImageBitmap(Bitmap bitmap, float scale) {
        this.bitmap = scaleBitmap(bitmap, scale);
    }

    /**
     * 图片按比例缩放
     *
     * @param bmp
     * @param scale
     * @return
     */
    private Bitmap scaleBitmap(Bitmap bmp, float scale) {

        int width = (int) (bmp.getWidth() * scale);
        int height = (int) (bmp.getHeight() * scale);

        return Bitmap.createScaledBitmap(bmp, width, height, true);
    }

    /**
     * 拼图的终点坐标的整体缩放
     *
     * @param scale   点坐标整体缩放
     *                1.0f 表示坐标
     * @param offsetX X 轴偏移量
     * @param offsetY Y 轴偏移量
     */
    public void setPointScale(float scale, int offsetX, int offsetY) {
        this.scale = scale;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public void setListPoint(List<Point> points) {
        this.points = points;
    }

    /**
     * 更新点集合
     *
     * @param points
     */
    private void updatePoints(List<Point> points) {
        if (verifyListEmpty(points)) return;
        int number = points.size();
        startPoint = new Point[number];
        endPoint = new Point[number];
        currentPoint = new Point[number];
        for (int i = 0; i < number; i++) {
            endPoint[i] = new Point((int) (points.get(i).x * scale) + offsetX, (int) ((points.get(i).y + offsetY) * scale));
            startPoint[i] = random(endPoint[i].x, endPoint[i].y);
            currentPoint[i] = new Point(0, 0);
        }
    }

    /**
     * 随机一个点
     *
     * @return
     */
    private Point random(int endX, int endY) {
        int x = 0;
        int y = 0;
        if (width >= bitmap.getWidth() && height >= bitmap.getWidth()) {
            x = random.nextInt((int) (width - bitmap.getWidth() * 2)) + bitmap.getWidth();
            y = random.nextInt((int) (height - bitmap.getHeight() * 2)) + bitmap.getHeight();
        }
        if (x == endX && y == endY) {
            random(endX, endY);
        }
        return new Point(x, y);
    }

    private boolean verifyListEmpty(Collection array) {
        if (array == null || array.isEmpty()) {
            return true;
        }
        return false;
    }

    public void updateGiftSurfaceViewParams(int width, int height) {
        this.width = width;
        this.height = height;
        if (width == 0 || height == 0) {
            DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
            this.width = displayMetrics.widthPixels;
            this.height = displayMetrics.heightPixels;
        }
        setPointScale(1, (int) (this.width / 10), (int) (this.height / 3.8f));
        updatePoints(points);
    }

    public void startAnimation() {
        if (animator != null && animator.isRunning()) {
            return;
        }
        animator = ValueAnimator.ofFloat(0, 1).setDuration(duration);
        animator.setRepeatCount(-1);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                getCurrentPoint((Float) animation.getAnimatedValue());
                drawBitmap();
            }
        });
        animator.start();
    }

    public void stopAnimation() {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
    }

    private void getCurrentPoint(float valueAnimator) {
        if (startPoint == null || endPoint == null) return;
        for (int i = 0; i < startPoint.length; i++) {
            currentPoint[i].x = startPoint[i].x - (int) ((startPoint[i].x - endPoint[i].x) * valueAnimator);
            currentPoint[i].y = startPoint[i].y - (int) ((startPoint[i].y - endPoint[i].y) * valueAnimator);
        }
    }

    /**
     * 画图
     */
    private void drawBitmap() {
        canvas = getHolder().lockCanvas();
        if (canvas != null) {
            if (currentPoint != null) {
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                for (int i = 0; i < currentPoint.length; i++) {
                    canvas.drawBitmap(bitmap, currentPoint[i].x - bitmap.getWidth() * .5f, currentPoint[i].y - bitmap.getHeight() * .5f, paint);
                }
            }
            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        updateGiftSurfaceViewParams(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopAnimation();
    }

}

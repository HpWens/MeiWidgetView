package com.meis.widget.xiaohongshu;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;

/**
 * Created by wenshi on 2019/2/26.
 * Description
 */
public class MCropImageView extends AppCompatImageView implements ViewTreeObserver.OnGlobalLayoutListener {

    // 手势帮助类
    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;

    private boolean mFirstLayout;
    private float mBaseScale = 1.0F;
    private float mMaxScale = 3.0F;

    private float mPreScaleFactor = 1.0f;
    private Matrix mMatrix;

    // 缩放手势(两个手指)中点位置
    private float mLastFocusX;
    private float mLastFocusY;

    private int mTouchSlop = -1;

    private int mCurrentScaleAnimCount;
    private ValueAnimator boundAnimator;

    private Paint mLinePaint;
    // 是否绘制线条
    private boolean mIsDragging;

    private static int MAX_SCROLL_FACTOR = 3;
    // 阻尼系数
    private static float DAMP_FACTOR = 9.0F;

    private static int SCALE_ANIM_COUNT = 10;
    private static int ZOOM_OUT_ANIM_WHIT = 0;
    private static int ZOOM_ANIM_WHIT = 1;
    private static int LINE_ROW_NUMBER = 3;
    private static int LINE_COLUMN_NUMBER = 3;

    public MCropImageView(Context context) {
        this(context, null);
    }

    public MCropImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MCropImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mScaleGestureDetector = new ScaleGestureDetector(context, mOnScaleGestureListener);
        mGestureDetector = new GestureDetector(context, mSimpleOnGestureListener);

        mFirstLayout = true;
        mMatrix = new Matrix();
        setScaleType(ScaleType.MATRIX);

        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setColor(Color.WHITE);
        mLinePaint.setStrokeWidth(dip2px(context, 0.5f));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mTouchSlop < 0) {
            mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 防止父类拦截事件
                getParent().requestDisallowInterceptTouchEvent(true);
                mIsDragging = false;
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mCurrentScaleAnimCount = 0;
                mIsDragging = false;
                invalidate();
                float scale = getScale();
                if (scale > mMaxScale) {
                    // 缩小 SCALE_ANIM_COUNT = 10
                    sendScaleMessage(getRelativeValue(mMaxScale / scale, SCALE_ANIM_COUNT), ZOOM_OUT_ANIM_WHIT, 0);
                } else if (scale < mBaseScale) {
                    // 放大
                    sendScaleMessage(getRelativeValue(mBaseScale / scale, SCALE_ANIM_COUNT), ZOOM_ANIM_WHIT, 0);
                } else {
                    // 平移
                    boundCheck();
                }
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
        }
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        mScaleGestureDetector.onTouchEvent(event);
        return true;
    }

    // 边界检测
    private void boundCheck() {
        // 获取图片矩阵
        RectF rectF = getMatrixRectF();

        if (rectF.left >= 0) {
            // 左越界
            startBoundAnimator(rectF.left, 0, true);
        }

        if (rectF.top >= 0) {
            // 上越界
            startBoundAnimator(rectF.top, 0, false);
        }

        if (rectF.right <= getWidth()) {
            // 右越界
            startBoundAnimator(rectF.left, rectF.left + getWidth() - rectF.right, true);
        }

        if (rectF.bottom <= getHeight()) {
            // 下越界
            startBoundAnimator(rectF.top, getHeight() - rectF.bottom + rectF.top, false);
        }
    }

    /**
     * 开始越界动画
     *
     * @param start      开始点坐标
     * @param end        结束点坐标
     * @param horizontal 是否水平动画  true 水平动画 false 垂直动画
     */
    private void startBoundAnimator(float start, float end, final boolean horizontal) {
        boundAnimator = ValueAnimator.ofFloat(start, end);
        boundAnimator.setDuration(200);
        boundAnimator.setInterpolator(new LinearInterpolator());
        boundAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (float) animation.getAnimatedValue();

                float[] values = new float[9];
                mMatrix.getValues(values);
                values[horizontal ? Matrix.MTRANS_X : Matrix.MTRANS_Y] = v;

                mMatrix.setValues(values);
                setImageMatrix(mMatrix);
            }
        });
        boundAnimator.start();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSize > heightSize) {
            super.onMeasure(heightMeasureSpec, heightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        }
    }


    @Override
    public void onGlobalLayout() {
        if (mFirstLayout) {
            mFirstLayout = false;
            mMatrix.reset();
            // 获取控件的宽度和高度
            int viewWidth = getWidth();
            int viewHeight = getHeight();

            // 图片的固定宽度  高度
            // 获取图片的宽度和高度
            Drawable drawable = getDrawable();
            if (null == drawable) {
                return;
            }
            int drawableWidth = drawable.getIntrinsicWidth();
            int drawableHeight = drawable.getIntrinsicHeight();

            // 将图片移动到屏幕的中点位置
            float dx = (viewWidth - drawableWidth) / 2;
            float dy = (viewHeight - drawableHeight) / 2;

            mBaseScale = Math.max((float) viewWidth / drawableWidth, (float) viewHeight / drawableHeight);
            // 平移居中
            mMatrix.postTranslate(dx, dy);
            // 缩放
            mMatrix.postScale(mBaseScale, mBaseScale, viewWidth / 2, viewHeight / 2);
            setImageMatrix(mMatrix);

            if (mBaseScale >= mMaxScale) {
                mMaxScale = (int) Math.floor(mBaseScale) + 2;
            } else if (mBaseScale < 1.0f) {
                mMaxScale = 1.0f;
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mHandler != null) {
            // 防止内存泄露
            mHandler.removeCallbacksAndMessages(null);
        }
        super.onDetachedFromWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getViewTreeObserver().removeOnGlobalLayoutListener(this);
        } else {
            getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }
    }

    // 获取图片矩阵区域
    private RectF getMatrixRectF() {
        RectF rectF = new RectF();
        Drawable drawable = getDrawable();
        if (drawable != null) {
            rectF.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            mMatrix.mapRect(rectF);
        }
        return rectF;
    }

    // 处理双指的缩放
    private ScaleGestureDetector.OnScaleGestureListener mOnScaleGestureListener = new ScaleGestureDetector.OnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (null == getDrawable() || mMatrix == null) {
                // 如果返回true那么detector就会重置缩放事件
                return true;
            }
            mIsDragging = true;
            // 缩放因子,缩小小于1,放大大于1
            float scaleFactor = mScaleGestureDetector.getScaleFactor();

            // 缩放因子偏移量
            float deltaFactor = scaleFactor - mPreScaleFactor;

            if (scaleFactor != 1.0F && deltaFactor != 0F) {
                mMatrix.postScale(deltaFactor + 1F, deltaFactor + 1F, mLastFocusX = mScaleGestureDetector.getFocusX(),
                        mLastFocusY = mScaleGestureDetector.getFocusY());
                setImageMatrix(mMatrix);
            }
            mPreScaleFactor = scaleFactor;
            return false;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            // 注意返回true
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
        }
    };

    private float getScale() {
        float[] values = new float[9];
        mMatrix.getValues(values);
        return values[Matrix.MSCALE_X];
    }

    private float[] getTransition() {
        float[] values = new float[9];
        mMatrix.getValues(values);
        return new float[]{values[Matrix.MTRANS_X], values[Matrix.MTRANS_Y]};
    }

    /**
     * 计算d的1/count次幂
     *
     * @param d
     * @param count 开根的次数
     * @return 相对值
     */
    private static float getRelativeValue(double d, double count) {
        if (count == 0) {
            return 1F;
        }
        count = 1 / count;
        return (float) Math.pow(d, count);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mIsDragging) {
            canvas.save();
            drawLine(canvas);
            canvas.restore();
        }
    }

    // 绘制九宫线条
    private void drawLine(Canvas canvas) {
        // 开始点
        float startX = 0;
        float startY = 0;

        // 结束点
        float endX = 0;
        float endY = 0;

        RectF rectF = getMatrixRectF();

        startX = rectF.left <= 0 ? 0 : rectF.left;
        startY = rectF.top <= 0 ? 0 : rectF.top;

        endX = rectF.right >= getWidth() ? getWidth() : rectF.right;
        endY = rectF.bottom >= getHeight() ? getHeight() : rectF.bottom;

        float lineWidth = 0;
        float lineHeight = 0;

        lineWidth = endX - startX;
        lineHeight = endY - startY;

        // LINE_ROW_NUMBER = 3 表示多少行
        for (int i = 1; i < LINE_ROW_NUMBER; i++) {
            canvas.drawLine(startX + 0, startY + lineHeight / LINE_ROW_NUMBER * i, endX, startY + lineHeight / LINE_ROW_NUMBER * i, mLinePaint);
        }

        // LINE_COLUMN_NUMBER = 3 表示多少列
        for (int i = 1; i < LINE_COLUMN_NUMBER; i++) {
            canvas.drawLine(startX + lineWidth / LINE_COLUMN_NUMBER * i, startY, startX + lineWidth / LINE_COLUMN_NUMBER * i, endY, mLinePaint);
        }

        mHandler.removeCallbacks(lineRunnable);
        mHandler.postDelayed(lineRunnable, 400);
    }


    // 处理手指滑动
    private GestureDetector.SimpleOnGestureListener mSimpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (e1.getPointerCount() == e2.getPointerCount() && e1.getPointerCount() == 1) {
                mIsDragging = true;
                // 获取图片矩阵
                RectF rectF = getMatrixRectF();

                float leftEdgeDistanceLeft = rectF.left;
                float topEdgeDistanceTop = rectF.top;

                float rightEdgeDistanceRight = leftEdgeDistanceLeft + rectF.right - rectF.left - getWidth();
                float bottomEdgeDistanceBottom = topEdgeDistanceTop + rectF.bottom - rectF.top - getHeight();

                // MAX_SCROLL_FACTOR = 3
                int maxOffsetWidth = getWidth() / MAX_SCROLL_FACTOR;
                int maxOffsetHeight = getHeight() / MAX_SCROLL_FACTOR;

                // 图片左侧越界并且图片右侧未越界
                if (leftEdgeDistanceLeft > 0 && rightEdgeDistanceRight > 0) {
                    // distanceX < 0 表示继续向右滑动
                    if (distanceX < 0) {
                        if (leftEdgeDistanceLeft < maxOffsetWidth) {
                            // DAMP_FACTOR = 9 系数越大阻尼越大  +1防止ratio为0
                            int ratio = (int) (DAMP_FACTOR / maxOffsetWidth * leftEdgeDistanceLeft) + 1;
                            distanceX /= ratio;
                        } else {
                            // 图片向右滑动超过了最大偏移量 图片则不平移
                            distanceX = 0;
                        }
                    }
                    // 向左滑动不做处理 默认取值distanceX
                }
                // 图片右侧越界并且图片左侧未越界 （同上处理）
                else if (rightEdgeDistanceRight < 0 && leftEdgeDistanceLeft < 0) {
                    // distanceX > 0 表示继续向左滑动
                    if (distanceX > 0) {
                        if (rightEdgeDistanceRight > -maxOffsetWidth) {
                            int ratio = (int) (DAMP_FACTOR / maxOffsetWidth * -rightEdgeDistanceRight) + 1;
                            distanceX /= ratio;
                        } else {
                            // 图片右侧距离控件右侧超过最大偏移量 图片则不平移
                            distanceX = 0;
                        }
                    }
                }
                // 图片左侧越界并且图片右侧越界
                else if (leftEdgeDistanceLeft > 0 && rightEdgeDistanceRight < 0) {
                    // 控件宽度的一半
                    int halfWidth = getWidth() / 2;
                    // 获取图片中点x坐标
                    float centerX = (rectF.right - rectF.left) / 2 + rectF.left;
                    // 图片中点x坐标是否右侧偏移
                    boolean rightOffsetCenterX = centerX >= halfWidth;
                    // 右侧偏移并且向右滑动
                    if (distanceX < 0 && rightOffsetCenterX) {
                        // centerX - halfWidth 图片右侧偏移量
                        int ratio = (int) (DAMP_FACTOR / maxOffsetWidth * (centerX - halfWidth)) + 1;
                        distanceX /= ratio;
                    }
                    // 左侧偏移并且向左滑动
                    else if (distanceX > 0 && !rightOffsetCenterX) {
                        // halfWidth - centerX 左侧的偏移量
                        int ratio = (int) (DAMP_FACTOR / maxOffsetWidth * (halfWidth - centerX)) + 1;
                        distanceX /= ratio;
                    }
                }

                // 上下越界 处理方式同左右处理方式一样 本可以提成一个方法但为了方便理解先这样了
                // 图片上侧越界并且图片下侧未越界
                if (topEdgeDistanceTop > 0 && bottomEdgeDistanceBottom > 0) {
                    // distanceY < 0 表示图片继续向下滑动
                    if (distanceY < 0) {
                        if (topEdgeDistanceTop < maxOffsetHeight) {
                            // 获取阻尼比例
                            int ratio = (int) (DAMP_FACTOR / maxOffsetHeight * topEdgeDistanceTop) + 1;
                            distanceY /= ratio;
                        } else {
                            // 向下滑动超过了最大偏移量 则图片不滑动
                            distanceY = 0;
                        }
                    }
                }
                // 图片下侧越界并且图片上侧未越界
                else if (bottomEdgeDistanceBottom < 0 && topEdgeDistanceTop < 0) {
                    if (distanceY > 0) {
                        if (bottomEdgeDistanceBottom > -maxOffsetHeight) {
                            int ratio = (int) (DAMP_FACTOR / maxOffsetHeight * -bottomEdgeDistanceBottom) + 1;
                            distanceY /= ratio;
                        } else {
                            // 向上滑动超过了最大偏移量 则图片不滑动
                            distanceY = 0;
                        }
                    }
                } else if (topEdgeDistanceTop > 0 && bottomEdgeDistanceBottom < 0) {
                    int halfHeight = getHeight() / 2;
                    // 获取图片中点y坐标
                    float centerY = (rectF.bottom - rectF.top) / 2 + rectF.top;
                    // 图片中点y坐标是否向下偏移
                    boolean bottomOffsetCenterY = centerY >= halfHeight;
                    // 向下偏移并且向下移动
                    if (distanceY < 0 && bottomOffsetCenterY) {
                        // centerY - halfHeight 图片偏移量
                        int ratio = (int) (DAMP_FACTOR / maxOffsetHeight * (centerY - halfHeight)) + 1;
                        distanceY /= ratio;
                    } else if (distanceY > 0 && !bottomOffsetCenterY) { // 向上偏移并且向上移动
                        int ratio = (int) (DAMP_FACTOR / maxOffsetHeight * (halfHeight - centerY)) + 1;
                        distanceY /= ratio;
                    }
                }

                mMatrix.postTranslate(-distanceX, -distanceY);
                setImageMatrix(mMatrix);
                return true;
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg != null) {
                if (mCurrentScaleAnimCount < SCALE_ANIM_COUNT) {
                    float obj = (float) msg.obj;
                    mMatrix.postScale(obj, obj, mLastFocusX, mLastFocusY);
                    setImageMatrix(mMatrix);
                    mCurrentScaleAnimCount++;
                    // what scale > mMaxScale 取0 不然取 1
                    sendScaleMessage(obj, msg.what, SCALE_ANIM_COUNT);
                } else if (mCurrentScaleAnimCount >= SCALE_ANIM_COUNT) {
                    float[] values = new float[9];
                    mMatrix.getValues(values);
                    if (msg.what == ZOOM_OUT_ANIM_WHIT) {
                        values[Matrix.MSCALE_X] = mMaxScale;
                        values[Matrix.MSCALE_Y] = mMaxScale;
                    } else if (msg.what == ZOOM_ANIM_WHIT) {
                        values[Matrix.MSCALE_X] = mBaseScale;
                        values[Matrix.MSCALE_Y] = mBaseScale;
                    }
                    mMatrix.setValues(values);
                    setImageMatrix(mMatrix);

                    // 边界检测
                    boundCheck();
                }
            }
        }
    };

    /**
     * 发送消息
     *
     * @param relativeScale
     * @param what
     * @param delayMillis
     */
    private void sendScaleMessage(float relativeScale, int what, long delayMillis) {
        Message mes = new Message();
        mes.obj = relativeScale;
        mes.what = what;
        mHandler.sendMessageDelayed(mes, delayMillis);
    }

    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private Runnable lineRunnable = new Runnable() {
        @Override
        public void run() {
            mIsDragging = false;
            invalidate();
        }
    };

}

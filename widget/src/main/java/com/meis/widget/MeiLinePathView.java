package com.meis.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.util.AttributeSet;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

/**
 * Created by wuyr on 17-12-15 下午8:08
 */

public class MeiLinePathView extends View {

    /**
     * 火车模式 TRAIN_MODE
     * 飞机模式 AIRPLANE_MODE
     */
    @IntDef({TRAIN_MODE, AIRPLANE_MODE})
    @IntRange(from = AIRPLANE_MODE, to = TRAIN_MODE)
    @Retention(RetentionPolicy.SOURCE)
    private @interface Mode {
    }

    // 一开始不显示灰色线条，粉红色线条走过后才留下灰色线条
    public static final int AIRPLANE_MODE = 0;

    // 一开始就显示灰色线条，并且一直显示，直到动画结束
    public static final int TRAIN_MODE = 1;

    private int mMode;

    //cpu锁
    private Semaphore mLightLineSemaphore;
    private Semaphore mDarkLineSemaphore;

    //第一种方案点集合来绘制 第二种可以用线段的方式同样可以截取
    private float[] mLightPoints;
    private float[] mDarkPoints;

    //暗 亮 线条的颜色
    private int mLightLineColor;
    private int mDarkLineColor;

    private ValueAnimator mProgressAnimator;
    private ValueAnimator mAlphaAnimator;
    private int mAlpha;

    //动画是否循环
    private boolean mIsCycle = true;
    private long mAnimationDuration;
    private Paint mPaint;

    private Keyframes mKeyframes;

    public MeiLinePathView(Context context) {
        this(context, null);
    }

    public MeiLinePathView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MeiLinePathView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        //初始化画笔
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5);
        mPaint.setAntiAlias(true);

        //默认动画时长
        mAnimationDuration = 6000;

        //默认颜色
        mLightLineColor = Color.parseColor("#F17F94");
        mDarkLineColor = Color.parseColor("#D8D5D7");

        mLightLineSemaphore = new Semaphore(1);
        mDarkLineSemaphore = new Semaphore(1);
    }

    /**
     * @param mode
     */
    public void setMode(@Mode int mode) {
        if ((mAlphaAnimator != null && mAlphaAnimator.isRunning()) || (mAlphaAnimator != null && mAlphaAnimator
                .isRunning()))
            throw new IllegalStateException("animation has been started!");
        mMode = mode;
        if (mode == TRAIN_MODE)
            setDarkLineProgress(1, 0);
        else
            setDarkLineProgress(0, 0);
    }

    public void setPath(Path path) {
        mKeyframes = new Keyframes(path);
        mAlpha = 0;
    }

    public void setAnimationDuration(long duration) {
        mAnimationDuration = duration;
    }

    public void startAnimation() {
        if (mAlphaAnimator != null && mAlphaAnimator.isRunning())
            mAlphaAnimator.cancel();
        if (mProgressAnimator != null && mProgressAnimator.isRunning())
            mProgressAnimator.cancel();

        // 时长是总时长的10%
        mAlphaAnimator = ValueAnimator.ofInt(0, 255).setDuration(mAnimationDuration / 10);
        mAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAlpha = (int) animation.getAnimatedValue();
            }
        });
        mAlphaAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startUpdateProgress();
            }
        });
        mAlphaAnimator.start();
    }

    public void setLineWidth(float width) {
        mPaint.setStrokeWidth(width);
    }

    public void setLightLineColor(@ColorInt int color) {
        mLightLineColor = color;
    }

    public void setDarkLineColor(@ColorInt int color) {
        mDarkLineColor = color;
    }

    private void setLightLineProgress(float start, float end) {
        setLineProgress(start, end, true);
    }

    private void setDarkLineProgress(float start, float end) {
        setLineProgress(start, end, false);
    }

    /**
     * 有相应的注释 理解一定要自己动手画图 动手画图
     */
    private void startUpdateProgress() {
        mAlphaAnimator = null;
        //底部灰色线条向后加长到原Path的60%
        mProgressAnimator = ValueAnimator.ofFloat(-0.6F, 1).setDuration(mAnimationDuration);
        mProgressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                float currentProgress = (float) animation.getAnimatedValue();
                //粉色线头
                float lightLineStartProgress;
                //粉色线尾
                float lightLineEndProgress;
                //灰色线头
                float darkLineStartProgress;
                //灰色线尾
                float darkLineEndProgress;

                darkLineEndProgress = currentProgress;

                //粉色线头从0开始，并且初始速度是灰色线尾的两倍
                darkLineStartProgress = lightLineStartProgress = (0.6F + currentProgress) * 2;

                //粉色线尾从-0.25开始，速度跟灰色线尾速度一样
                lightLineEndProgress = 0.35F + currentProgress;

                //粉色线尾走到30%时，速度变为原来速度的2倍
                if (lightLineEndProgress > 0.3F) {
                    lightLineEndProgress = (0.35F + currentProgress - 0.3F) * 2 + 0.3F;
                }

                //当粉色线头走到65%时，速度变为原来速度的0.35倍
                if (darkLineStartProgress > 0.65F) {
                    darkLineStartProgress = lightLineStartProgress = ((0.6F + currentProgress) * 2 - 0.65F) * 0.35F + 0.65F;
                }
                if (lightLineEndProgress < 0) {
                    lightLineEndProgress = 0;
                }
                if (darkLineEndProgress < 0) {
                    darkLineEndProgress = 0;
                }

                //当粉色线尾走到90%时，播放透明渐变动画
                if (lightLineEndProgress > 0.9F) {
                    if (mAlphaAnimator == null) {
                        mAlphaAnimator = ValueAnimator.ofInt(255, 0).setDuration((long) (mAnimationDuration * .2));//
                        // 时长是总时长的20%
                        mAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                mAlpha = (int) animation.getAnimatedValue();
                            }
                        });
                        mAlphaAnimator.start();
                    }
                }
                if (lightLineStartProgress > 1) {
                    darkLineStartProgress = lightLineStartProgress = 1;
                }

                setLightLineProgress(lightLineStartProgress, lightLineEndProgress);

                //飞机模式才更新灰色线条
                if (mMode == AIRPLANE_MODE)
                    setDarkLineProgress(darkLineStartProgress, darkLineEndProgress);
            }
        });
        mProgressAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mIsCycle) {
                    startAnimation();
                }
            }
        });
        mProgressAnimator.start();
    }

    private void setLineProgress(float start, float end, boolean isLightPoints) {
        if (mKeyframes == null)
            throw new IllegalStateException("path not set yet!");

        if (isLightPoints) {
            try {
                mLightLineSemaphore.acquire();
            } catch (InterruptedException e) {
                return;
            }
            mLightPoints = mKeyframes.getRangeValue(start, end);
            mLightLineSemaphore.release();
        } else {
            try {
                mDarkLineSemaphore.acquire();
            } catch (InterruptedException e) {
                return;
            }
            mDarkPoints = mKeyframes.getRangeValue(start, end);
            mDarkLineSemaphore.release();
        }
        postInvalidate();
    }

    private void startDraw(Canvas canvas) {
        try {
            mDarkLineSemaphore.acquire();
        } catch (InterruptedException e) {
            return;
        }
        if (mDarkPoints != null) {
            mPaint.setColor(mDarkLineColor);
            mPaint.setAlpha(mAlpha);
            canvas.drawPoints(mDarkPoints, mPaint);
        }
        mDarkLineSemaphore.release();
        try {
            mLightLineSemaphore.acquire();
        } catch (InterruptedException e) {
            return;
        }
        if (mLightPoints != null) {
            mPaint.setColor(mLightLineColor);
            mPaint.setAlpha(mAlpha);
            canvas.drawPoints(mLightPoints, mPaint);
        }
        mLightLineSemaphore.release();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        startDraw(canvas);
    }

    public void stopAnimation() {
        try {
            mDarkLineSemaphore.acquire();
        } catch (InterruptedException e) {
            return;
        }
        mDarkPoints = null;
        mDarkLineSemaphore.release();
        try {
            mLightLineSemaphore.acquire();
        } catch (InterruptedException e) {
            return;
        }
        mLightPoints = null;
        mLightLineSemaphore.release();
        if (mAlphaAnimator != null && mAlphaAnimator.isRunning())
            mAlphaAnimator.cancel();
        if (mProgressAnimator != null && mProgressAnimator.isRunning())
            mProgressAnimator.cancel();
    }

    private static class Keyframes {
        //精度我们用1就够了 (数值越少 numPoints 就越大)
        static final float PRECISION = 1f;
        int numPoints;
        float[] mData;

        Keyframes(Path path) {
            init(path);
        }

        void init(Path path) {
            final PathMeasure pathMeasure = new PathMeasure(path, false);
            final float pathLength = pathMeasure.getLength();
            numPoints = (int) (pathLength / PRECISION) + 1;
            mData = new float[numPoints * 2];
            final float[] position = new float[2];
            int index = 0;
            for (int i = 0; i < numPoints; ++i) {
                final float distance = (i * pathLength) / (numPoints - 1);
                pathMeasure.getPosTan(distance, position, null);
                mData[index] = position[0];
                mData[index + 1] = position[1];
                index += 2;
            }
            numPoints = mData.length;
        }

        /**
         * 拿到start和end之间的x,y数据
         *
         * @param start 开始百分比
         * @param end   结束百分比
         * @return 裁剪后的数据
         */
        float[] getRangeValue(float start, float end) {
            int startIndex = (int) (numPoints * start);
            int endIndex = (int) (numPoints * end);

            //必须是偶数，因为需要float[]{x,y}这样x和y要配对的
            if (startIndex % 2 != 0) {
                //直接减，不用担心 < 0  因为0是偶数，哈哈
                --startIndex;
            }
            if (endIndex % 2 != 0) {
                //不用检查越界
                ++endIndex;
            }
            //根据起止点裁剪
            return startIndex > endIndex ? Arrays.copyOfRange(mData, endIndex, startIndex) : null;
        }
    }

    public void setCycle(boolean cycle) {
        mIsCycle = cycle;
    }
}

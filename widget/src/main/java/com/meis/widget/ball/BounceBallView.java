package com.meis.widget.ball;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.meis.widget.R;

/**
 * Created by ccy on 2017-08-08.
 * 自己撸了一个 这里支持原著
 */

public class BounceBallView extends View {

    /**
     * 常量
     */
    private final static String TAG = "BounceBallView";

    private final float DEFAULT_BALL_RADIUS = dp2px(5);
    private static final int DEFAULT_BALL_COLOR = 0xff000000;
    private static final int DEFAULT_BOUNCE_COUNT = 2;
    private static final int DEFAULT_ANIM_DURATION = 2400;
    private static final int DEFAULT_BALL_COUNT = 10;
    private static final int DEFAULT_BALL_DELAY = (int) (DEFAULT_ANIM_DURATION / DEFAULT_BALL_COUNT);

    /**
     * 数据
     */
    private float radius;
    private int ballColor;
    private int bounceCount; //回弹次数  >=0
    private int ballCount;
    private int ballDelay; //当ballCount>1时，相邻小球开始下落的时间间隔
    private float defaultPadding;//默认偏移(left、top、right)
    private float defaultPaddingBottom; //bottom偏移（比默认偏移稍大一点）
    private int defaultWidth;
    private int defaultHeight;
    private int viewWidth;
    private int viewHeight;
    private float skipLength; //起点需无视的路径长

    /**
     * 绘图
     */
    private Paint[] paint;
    private Path path;
    private PathMeasure pathMeasure;
    private float[] pos = new float[2]; //存储某点的坐标值
    private float[] tan = new float[2]; //存储某点正切值
    private float[] segmentLength;
    private boolean isRandomBallPath = true; // 是否开启小球轨迹略微随机偏移
    private boolean isRandomColor = true; //是否开启小球随机颜色
    private boolean isRandomRadius = true; //是否开启小球随机大小（基础大小上下浮动）
    private float[] randomTransRatioX;
    private float[] randomTransRatioY;
    private int[] randomBallColors;
    private float[] randomRadius;

    /**
     * 动画
     */
    private int defaultDuration;
    private boolean isPhysicsMode = true; //是否开启物理效果(下落加速，上弹减速）
    private Interpolator physicInterpolator; //物理效果插值器
    private ValueAnimator[] translateAnim; // 作用与小球位置变换
    private float[] translateFraction; //动画比例 [0,1]
    MultiDecelerateAccelerateInterpolator interCreater;
    private Interpolator defaultInterpolator = new LinearInterpolator();

    /**
     * 动态配置
     */
    //是否已开启动态配置事务
    private boolean isTransaction = false;

    public BounceBallView(Context context) {
        this(context, null);
    }

    public BounceBallView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BounceBallView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.BounceBallView);
        radius = ta.getDimension(R.styleable.BounceBallView_ball_radius, DEFAULT_BALL_RADIUS);
        ballColor = ta.getColor(R.styleable.BounceBallView_ball_color, DEFAULT_BALL_COLOR);
        bounceCount = ta.getInt(R.styleable.BounceBallView_bounce_count, DEFAULT_BOUNCE_COUNT);
        defaultDuration = ta.getInteger(R.styleable.BounceBallView_anim_duration, DEFAULT_ANIM_DURATION);
        ballCount = ta.getInteger(R.styleable.BounceBallView_ball_count, DEFAULT_BALL_COUNT);
        ballDelay = ta.getInteger(R.styleable.BounceBallView_ball_delay, DEFAULT_BALL_DELAY);
        isPhysicsMode = ta.getBoolean(R.styleable.BounceBallView_physic_mode, true);
        isRandomColor = ta.getBoolean(R.styleable.BounceBallView_random_color, true);
        isRandomRadius = ta.getBoolean(R.styleable.BounceBallView_random_radius, true);
        isRandomBallPath = ta.getBoolean(R.styleable.BounceBallView_random_path, true);
        ta.recycle();

        //检查合法性
        checkAttrs();

        initData();
    }

    private void checkAttrs() {
        radius = radius >= 0 ? radius : DEFAULT_BALL_RADIUS;
        //ballColor = ballColor >= 0 ? ballColor : DEFAULT_BALL_COLOR;
        bounceCount = bounceCount >= 0 ? bounceCount : DEFAULT_BOUNCE_COUNT;
        ballCount = ballCount >= 1 ? ballCount : DEFAULT_BALL_COUNT;
        ballDelay = ballDelay >= 0 ? ballDelay : DEFAULT_BALL_DELAY;
        defaultDuration = defaultDuration >= 0 ? defaultDuration : DEFAULT_ANIM_DURATION;
    }


    private void initData() {

        defaultPadding = 2 * radius + dp2px(2);
        defaultPaddingBottom = 2 * radius + dp2px(15);
        defaultWidth = (int) (2 * defaultPadding + dp2px(200));
        defaultHeight = (int) (defaultPadding + defaultPaddingBottom + dp2px(80));

        paint = new Paint[ballCount];
        for (int i = 0; i < paint.length; i++) {
            paint[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint[i].setColor(ballColor);
            paint[i].setStyle(Paint.Style.FILL);
        }

        path = new Path();
        pathMeasure = new PathMeasure();
        randomBallColors = new int[ballCount];
        randomRadius = new float[ballCount];
        randomTransRatioX = new float[ballCount];
        randomTransRatioY = new float[ballCount];

        translateFraction = new float[ballCount];
        translateAnim = new ValueAnimator[ballCount];

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int hSize = MeasureSpec.getSize(heightMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);


        if (wMode == MeasureSpec.EXACTLY) {
            viewWidth = wSize;
        } else {
            viewWidth = Math.min(defaultWidth, wSize);
        }
        if (hMode == MeasureSpec.EXACTLY) {
            viewHeight = hSize;
        } else {
            viewHeight = Math.min(defaultHeight, hSize);
        }

        setMeasuredDimension(viewWidth, viewHeight);

        initPath();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if ((oldw != 0 && oldw != w) || (oldh != 0 && oldh != h)) {
            initData();
            initPath();
        }
    }


    /**
     * 初始化球体弹跳的路径
     */
    private void initPath() {
        path.reset();

        float intervalX = (viewWidth - 2 * defaultPadding) / (bounceCount + 1); //每次弹跳的间距
        PointF start = new PointF();//起点位置
        PointF control = new PointF(); //贝塞尔控制点
        PointF end = new PointF(); //贝塞尔结束点
        start.x = defaultPadding;
        start.y = viewHeight - defaultPaddingBottom;

        float controlOffsetY = viewHeight * 0.6f;  //控制点向上偏移量,0.6为调试值
        float deltaY = (1.2f * viewHeight + controlOffsetY) / (bounceCount + 1); //控制点高度递减值，1.2为调试值

        PathMeasure tempPathMeasure = new PathMeasure();
        segmentLength = new float[bounceCount + 1];

        for (int i = 0; i <= bounceCount; i++) {
            control.x = start.x + intervalX * (i + 0.5f);
            control.y = -controlOffsetY + deltaY * i;
            end.x = start.x + intervalX * (i + 1);
            end.y = start.y;
            if (i == 0) {
                path.moveTo(start.x, start.y);
            }
            if (i == bounceCount) {
                end.y = viewHeight;
            }
            path.quadTo(control.x, control.y, end.x, end.y);

            tempPathMeasure.setPath(path, false);
            if (i == 0) { //第一次弹跳的上升阶段不画，记录弹跳一半长度(为效果更好，实际取值0.45
                skipLength = tempPathMeasure.getLength() * 0.45f;
            }
            segmentLength[i] = tempPathMeasure.getLength();
        }

        pathMeasure.setPath(path, false);

        if (interCreater == null) {
            interCreater = new MultiDecelerateAccelerateInterpolator();
        }
        physicInterpolator = interCreater.createInterpolator(segmentLength);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        drawBounceBall(canvas);
    }


    private void drawBounceBall(Canvas canvas) {
        for (int i = 0; i < ballCount; i++) {
            canvas.save();

            if (translateFraction[i] < (skipLength / pathMeasure.getLength())) {
                continue;
            }
            //根据当前动画进度获取path上对应点的坐标和正切
            pathMeasure.getPosTan(pathMeasure.getLength() * translateFraction[i], pos, tan);

            //路径随机
            if (isRandomBallPath) {
                pos[0] *= randomTransRatioX[i];
                pos[1] *= randomTransRatioY[i];
            }

            //颜色随机已在makeRandom里被应用
            canvas.drawCircle(pos[0],
                    pos[1],
                    isRandomRadius ? randomRadius[i] : radius,
                    paint[i]);
            canvas.restore();
        }
    }


    /**
     * 启动动画
     */
    public void start() {
        start(defaultDuration);
    }

    /**
     * 启动动画
     *
     * @param duration 动画时长
     */
    public void start(final int duration) {
        post(new Runnable() {  //放入队列（保证view已加载完成）
            @Override
            public void run() {
                createAnim(duration); //20170810备注：检查内部有没有重复创建实例
                startAnim();
            }
        });
    }

    private void startAnim() {
        for (int i = 0; i < translateAnim.length; i++) {
            translateAnim[i].start();
        }
    }

    private void createAnim(int duration) {
        for (int i = 0; i < ballCount; i++) {
            createTranslateAnim(i, duration, i * ballDelay);
        }
    }

    private void createTranslateAnim(final int index, int duration, final int delay) {
        if (translateAnim[index] == null) {
            translateAnim[index] = ValueAnimator.ofFloat(0.0f, 1.0f);
            translateAnim[index].setDuration(duration);
            translateAnim[index].setRepeatCount(ValueAnimator.INFINITE);
            translateAnim[index].setStartDelay(delay);
            if (isPhysicsMode) {
                translateAnim[index].setInterpolator(physicInterpolator);
            } else {
                translateAnim[index].setInterpolator(defaultInterpolator);
            }
            translateAnim[index].addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    makeRandom(index);
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    super.onAnimationRepeat(animation);
                    makeRandom(index);
                }
            });

            translateAnim[index].addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    translateFraction[index] = animation.getAnimatedFraction();
                    if (dealFromAlphaAnim(translateFraction[index]) != -1) {
                        paint[index].setAlpha(dealFromAlphaAnim(translateFraction[index]));
                    } else if (dealToAlphaAnim(translateFraction[index]) != -1) {
                        paint[index].setAlpha(dealToAlphaAnim(translateFraction[index]));
                    } else {
                        paint[index].setAlpha(255);
                    }
                    invalidate();
                }
            });
        }
    }

    /**
     * 数据随机化
     *
     * @param index
     */
    private void makeRandom(int index) {

        if (isRandomBallPath) {   //坐标是在ondraw里才获得的，故在ondraw里再去应用
            randomTransRatioX[index] = (float) (0.9f + (0.2f * Math.random())); //[0.9,1.1)
            randomTransRatioY[index] = (float) (0.8f + (0.4f * Math.random())); //[0.8,1.2)
        }

        if (isRandomColor) {  //不要在ondraw里再应用，会同时覆盖透明度通道，透明动画会失效
            randomBallColors[index] = getRandomColor();
            paint[index].setColor(randomBallColors[index]);
        } else {
            paint[index].setColor(ballColor);
        }

        if (isRandomRadius) {
            randomRadius[index] = (float) (radius * (0.7 + (0.6 * Math.random()))); //[0.7,1.3]
        } else {
            randomRadius[index] = radius;
        }
    }

    /**
     * 传入一个值，和这个值的上下限，计算该值当前比例
     *
     * @param start   起点值
     * @param end     终点值
     * @param current 当前值
     * @return
     */
    private float getEvaluatedFraction(float start, float end, float current) {
        if (end - start == 0) {
            throw new RuntimeException("传值错误，分母为0: start = " + start + ";end = " + end);
        } else {
            return (current - start) / (end - start);
        }
    }

    /**
     * “透明-不透明”的透明值
     */
    private int dealFromAlphaAnim(float fraction) {
        float totalLength = pathMeasure.getLength();
        float beginFra = skipLength / totalLength;
        float endFrac = segmentLength[0] / totalLength;
        if (fraction > beginFra &&
                fraction < endFrac) {
            return (int) (255 * getEvaluatedFraction(beginFra, endFrac, fraction));
        }
        return -1;
    }

    /**
     * “不透明-透明”的透明值
     */
    private int dealToAlphaAnim(float fraction) {
        float totalLength = pathMeasure.getLength();
        if (segmentLength.length > 1) {
            float beginFra = segmentLength[segmentLength.length - 2] / totalLength;
            float endFrac = 1.0f;
            if (fraction > beginFra &&
                    fraction < 1.0f) {
                return (int) (255 - 255 * getEvaluatedFraction(beginFra, endFrac, fraction));
            }
        }
        return -1;
    }

    private int getRandomColor() {
        return Color.argb(255,
                (int) (255 * Math.random()),
                (int) (255 * Math.random()),
                (int) (255 * Math.random()));
    }

    /**
     * 取消已有动画，释放资源
     */
    public void cancel() {
        if (translateAnim != null) {
            for (int i = 0; i < translateAnim.length; i++) {
                if (translateAnim[i] != null) {
                    translateAnim[i].cancel();
                    translateAnim[i] = null;
                }
            }
            translateAnim = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        cancel();
        super.onDetachedFromWindow();
    }

    /**
     * 开启配置事务，可连缀配置属性，最后调用{@link #apply()}使配置生效
     * @return
     */
    public BounceBallView config() {
        for (int i = 0; i < translateAnim.length; i++) {
            translateAnim[i].cancel();
            translateAnim[i] = null;
        }
        isTransaction = true;
        return this;
    }

    /**
     * 使应用配置，在这之前先调用{@link #config()}
     */
    public void apply() {
        if (isTransaction == true) {
            Log.w(TAG, "no config() function was called before calling apply()!");
        }
        isTransaction = false;
        cancel();

        checkAttrs();
        initData();
        requestLayout();
        invalidate();
    }

    /**
     * 小球半径
     * @param radius 默认5dp
     * @return
     */
    public BounceBallView radius(float radius) {
        check();
        this.radius = radius;
        return this;
    }

    /**
     * 小球颜色
     * @param ballColor 默认黑色，{@link #isRandomColor} 为true时无效
     * @return
     */
    public BounceBallView ballColor(int ballColor) {
        check();
        this.ballColor = ballColor;
        return this;
    }

    /**
     * 小球数量
     * @param ballCount 默认10
     * @return
     */
    public BounceBallView ballCount(int ballCount) {
        check();
        this.ballCount = ballCount;
        return this;
    }

    /**
     * 小球弹跳次数
     * @param bounceCount  默认2次
     * @return
     */
    public BounceBallView bounceCount(int bounceCount) {
        check();
        this.bounceCount = bounceCount;
        return this;
    }

    /**
     * 相邻小球出现间隔
     * @param ballDelay  默认为（动画时长/小球数量）。单位ms
     * @return
     */
    public BounceBallView ballDelay(int ballDelay) {
        check();
        this.ballDelay = ballDelay;
        return this;
    }

    /**
     * 一个小球一次完整的动画时长
     * @param defaultDuration 默认2400ms。单位ms
     * @return
     */
    public BounceBallView duration(int defaultDuration) {
        check();
        this.defaultDuration = defaultDuration;
        return this;
    }

    /**
     * 是否颜色随机
     * @param isRandomColor 默认true
     * @return
     */
    public BounceBallView isRandomColor(boolean isRandomColor) {
        check();
        this.isRandomColor = isRandomColor;
        return this;
    }

    /**
     * 是否路径稍微随机偏移
     * @param isRandomBallPath 默认true
     * @return
     */
    public BounceBallView isRamdomPath(boolean isRandomBallPath) {
        check();
        this.isRandomBallPath = isRandomBallPath;
        return this;
    }

    /**
     * 小球大小是否稍微随机偏移
     * @param isRandomRadius 默认true
     * @return
     */
    public BounceBallView isRandomRadius(boolean isRandomRadius) {
        check();
        this.isRandomRadius = isRandomRadius;
        return this;
    }

    /**
     * 是否开启仿物理效果（下落加速上弹减速）
     * @param isPhysicsMode 默认true
     * @return
     */
    public BounceBallView isPhysicMode(boolean isPhysicsMode) {
        check();
        this.isPhysicsMode = isPhysicsMode;
        return this;
    }

    private void check() {
        if (isTransaction) {
            return;
        } else {
            throw new RuntimeException("please call config() first to open the configuration and invoke apply() to apply the configuration");
        }
    }

    public float getRadius() {
        return radius;
    }

    public int getBallColor() {
        return ballColor;
    }

    public int getBounceCount() {
        return bounceCount;
    }

    public int getBallCount() {
        return ballCount;
    }

    public int getBallDelay() {
        return ballDelay;
    }

    public boolean isRandomBallPath() {
        return isRandomBallPath;
    }

    public boolean isRandomColor() {
        return isRandomColor;
    }

    public boolean isRandomRadius() {
        return isRandomRadius;
    }

    public int getDefaultDuration() {
        return defaultDuration;
    }

    public boolean isPhysicsMode() {
        return isPhysicsMode;
    }

    private float dp2px(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}

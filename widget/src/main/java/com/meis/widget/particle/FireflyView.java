package com.meis.widget.particle;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.meis.widget.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by wenshi on 2018/7/5.
 * Description 浮点粒子控件
 */
public class FireflyView extends SurfaceView implements SurfaceHolder.Callback {

    // 粒子的最大数量
    private static final int MAX_NUM = 400;
    // 粒子集合
    private List<FloatParticle> mListParticles;
    // 随机数
    private Random mRandom;

    private SurfaceHolder mHolder;

    // 动画线程
    private Handler mHandler;

    // 粒子半径
    private int mParticleMaxRadius;

    // 粒子数量
    private int mParticleNum;

    // 粒子移动速率
    private int mParticleMoveRate;

    private static final int EMPTY_FLAG = 1;

    public FireflyView(Context context) {
        this(context, null);
    }

    public FireflyView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FireflyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 关闭硬件加速
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        init();

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FireflyView);
        mParticleMaxRadius = ta.getInt(R.styleable.FireflyView_firefly_max_radius, 5);
        mParticleNum = ta.getInt(R.styleable.FireflyView_firefly_num, MAX_NUM);
        mParticleMoveRate = ta.getInt(R.styleable.FireflyView_firefly_move_rate, 5);
        ta.recycle();
    }

    private void init() {
        // 设置透明
        setZOrderOnTop(true);
        // 配合清屏 canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        mHolder = getHolder();
        mHolder.setFormat(PixelFormat.TRANSLUCENT);
        mHolder.addCallback(this);
        // 初始化随机数
        mRandom = new Random();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    // 初始化浮点粒子数据
    private void initParticlesData(int width, int height) {
        mListParticles = new ArrayList<>();
        for (int i = 0; i < mParticleNum; i++) {
            FloatParticle fp = new FloatParticle(width, height);
            mParticleMaxRadius = mParticleMaxRadius < 2 ? 2 : mParticleMaxRadius;
            fp.setRadius(mRandom.nextInt(mParticleMaxRadius - 1) + 1);
            mListParticles.add(fp);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        initParticlesData(width, height);
        startAnimation();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopAnimation();
    }

    public void stopAnimation() {
        mHandler.removeCallbacksAndMessages(null);
    }

    public void startAnimation() {
        //if (mHandler != null) return;
        HandlerThread fireThread = new HandlerThread(this.getClass().getName());
        fireThread.start();
        mHandler = new Handler(fireThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Canvas mCanvas = mHolder.lockCanvas(null);
                if (mCanvas != null) {
                    synchronized (mHolder) {
                        // 清屏
                        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                        for (FloatParticle fp : mListParticles) {
                            fp.drawParticle(mCanvas);
                        }
                    }
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (mCanvas != null) {
                    mHolder.unlockCanvasAndPost(mCanvas);
                }
                mHandler.sendEmptyMessageDelayed(EMPTY_FLAG, mParticleMoveRate);
            }
        };
        mHandler.sendEmptyMessage(EMPTY_FLAG);
    }

    public int getParticleMoveRate() {
        return mParticleMoveRate;
    }

    public void setParticleMoveRate(int particleMoveRate) {
        mParticleMoveRate = particleMoveRate;
    }

    public int getParticleMaxRadius() {
        return mParticleMaxRadius;
    }

    public int getParticleNum() {
        return mParticleNum;
    }
}

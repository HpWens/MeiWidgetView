package com.meis.widget.praise;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.Random;

/**
 * Created by wenshi on 2018/7/11.
 * Description 贝塞尔点赞控件
 */
public class BezierPraiseView extends FrameLayout {

    private BezierPraiseAnimator mBezierPraiseAnimator;
    private Random mRandom;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (getWidth() <= 0 || getHeight() <= 0) return;
            mBezierPraiseAnimator.startAnimation(mRandom.nextInt(getWidth()), mRandom.nextInt(getHeight()));
            mHandler.sendEmptyMessageDelayed(1, 200);
        }
    };

    public BezierPraiseView(Context context) {
        this(context, null);
    }

    public BezierPraiseView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BezierPraiseView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mBezierPraiseAnimator = new BezierPraiseAnimator(this);
        mRandom = new Random();
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            mHandler.sendEmptyMessage(1);
        } else {
            mBezierPraiseAnimator.cancelAnimation();
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
}

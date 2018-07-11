package com.meis.widget.praise;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.widget.ImageView;

import com.meis.widget.R;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by wenshi on 2018/7/10.
 * Description 贝塞尔点赞动画
 */
public class BezierPraiseAnimator {

    // 上下文
    private Context mContext;

    // 控件树的顶层视图
    private ViewGroup mRootView;

    // 随机数
    private Random mRandom;

    // 点赞的小图标资源
    private Drawable[] mDrawables;

    // 动画集合
    private ArrayList<Animator> mAnimatorList;

    // 目标控件在窗口中的X坐标
    private int mTargetX;

    // 目标控件在窗口中的Y坐标
    private int mTargetY;

    // 点赞小图标的宽高
    private int mPraiseIconWidth = 50;
    private int mPraiseIconHeight = 50;

    private int mAnimatorDuration = 1000;

    public BezierPraiseAnimator(Context context) {
        mContext = context;
        if (context instanceof Activity) {
            mRootView = (ViewGroup) ((Activity) context).getWindow().getDecorView();
        } else {
            throw new RuntimeException("context is not instanceof for Activity");
        }
        initData();
    }

    public BezierPraiseAnimator(ViewGroup rootView) {
        mContext = rootView.getContext();
        mRootView = rootView;
        initData();
    }

    private void initData() {
        mRandom = new Random();
        mAnimatorList = new ArrayList<>();
        initIcons();
    }

    /**
     * 获取当前targetView在屏幕中的位置
     *
     * @param targetView
     */
    public void startAnimation(View targetView) {
        // 获取targetView在屏幕中的位置
        int loc[] = new int[2];
        targetView.getLocationInWindow(loc);
        int viewHeight = targetView.getHeight();
        int viewWidth = targetView.getWidth();
        // 设置起始坐标，中点坐标
        mTargetX = loc[0] + viewWidth / 2 - mPraiseIconWidth / 2;
        mTargetY = loc[1] + viewHeight / 2 - mPraiseIconHeight / 2;
        // 播放点赞动画
        startPraiseAnimation();
    }

    /**
     * 屏幕坐标
     *
     * @param screenX
     * @param screenY
     */
    public void startAnimation(int screenX, int screenY) {
        mTargetX = screenX;
        mTargetY = screenY;
        startPraiseAnimation();
    }

    /**
     * 取消动画
     */
    public void cancelAnimation() {
        if (mAnimatorList == null || mAnimatorList.isEmpty()) return;
        for (Animator animator : mAnimatorList) {
            if (animator.isRunning()) {
                animator.cancel();
            }
        }
    }

    // 播放点赞动画
    private void startPraiseAnimation() {
        int size = mRandom.nextInt(4) + (mDrawables.length - 4);
        mAnimatorList.clear();
        for (int i = 0; i < size; i++) {
            // 动态添加点赞小图标
            final ImageView praiseIv = new ImageView(mContext);
            praiseIv.setImageDrawable(mDrawables[mRandom.nextInt(mDrawables.length)]);
            praiseIv.setLayoutParams(new ViewGroup.LayoutParams(mPraiseIconWidth, mPraiseIconHeight));
            mRootView.addView(praiseIv);
            // 设置点赞小图标的动画并播放
            Animator praiseAnimator = getPraiseAnimator(praiseIv);
            praiseAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    removePraiseView(praiseIv);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    removePraiseView(praiseIv);
                }
            });
            praiseAnimator.start();
            mAnimatorList.add(praiseAnimator);
        }
    }

    private void removePraiseView(View praiseView) {
        try {
            mRootView.removeView(praiseView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取到点赞小图标动画
     *
     * @param target
     * @return
     */
    private Animator getPraiseAnimator(View target) {
        // 获取贝塞尔曲线动画
        ValueAnimator bezierPraiseAnimator = getBezierPraiseAnimator(target);

        // 组合动画（旋转动画+贝塞尔曲线动画）旋转角度（200~720）
        int rotationAngle = mRandom.nextInt(520) + 200;
        ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(target, "rotation", 0, rotationAngle % 2 == 0 ? rotationAngle : -rotationAngle);
        rotationAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        rotationAnimator.setTarget(target);

        // 组合动画
        AnimatorSet composeAnimator = new AnimatorSet();
        composeAnimator.play(bezierPraiseAnimator).with(rotationAnimator);
        composeAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        composeAnimator.setDuration(mAnimatorDuration);
        composeAnimator.setTarget(target);
        return composeAnimator;
    }

    private ValueAnimator getBezierPraiseAnimator(final View target) {
        // 构建贝塞尔曲线的起点，控制点，终点坐标
        float startX = mTargetX;
        float startY = mTargetY;
        int random = mRandom.nextInt(mPraiseIconWidth);
        float endX;
        float endY;
        float controlX;
        float controlY;

        controlY = startY - mRandom.nextInt(500) - 100;
        // 左右两边
        if (random % 2 == 0) {
            endX = mTargetX - random * 8;
            controlX = mTargetX - random * 2;
        } else {
            endX = mTargetX + random * 8;
            controlX = mTargetX + random * 2;
        }
        endY = mTargetY + random + 400;

        // 构造自定义的贝塞尔估值器
        PraiseEvaluator evaluator = new PraiseEvaluator(new PointF(controlX, controlY));

        ValueAnimator animator = ValueAnimator.ofObject(evaluator, new PointF(startX, startY), new PointF(endX, endY));
        animator.setInterpolator(new AnticipateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                PointF currentPoint = (PointF) animation.getAnimatedValue();
                target.setX(currentPoint.x);
                target.setY(currentPoint.y);

                // 设置透明度 [1~0]
                target.setAlpha(1.0F - animation.getAnimatedFraction());
            }
        });
        animator.setTarget(target);
        return animator;
    }

    // 初始化点赞小图标
    private void initIcons() {
        mDrawables = new Drawable[14];
        mDrawables[0] = mContext.getResources().getDrawable(R.drawable.mei_ic_praise_1);
        mDrawables[1] = mContext.getResources().getDrawable(R.drawable.mei_ic_praise_2);
        mDrawables[2] = mContext.getResources().getDrawable(R.drawable.mei_ic_praise_3);
        mDrawables[3] = mContext.getResources().getDrawable(R.drawable.mei_ic_praise_4);
        mDrawables[4] = mContext.getResources().getDrawable(R.drawable.mei_ic_praise_5);
        mDrawables[5] = mContext.getResources().getDrawable(R.drawable.mei_ic_praise_6);
        mDrawables[6] = mContext.getResources().getDrawable(R.drawable.mei_ic_praise_7);
        mDrawables[7] = mContext.getResources().getDrawable(R.drawable.mei_ic_praise_8);
        mDrawables[8] = mContext.getResources().getDrawable(R.drawable.mei_ic_praise_9);
        mDrawables[9] = mContext.getResources().getDrawable(R.drawable.mei_ic_praise_10);
        mDrawables[10] = mContext.getResources().getDrawable(R.drawable.mei_ic_praise_11);
        mDrawables[11] = mContext.getResources().getDrawable(R.drawable.mei_ic_praise_12);
        mDrawables[12] = mContext.getResources().getDrawable(R.drawable.mei_ic_praise_13);
        mDrawables[13] = mContext.getResources().getDrawable(R.drawable.mei_ic_praise_14);
    }
}

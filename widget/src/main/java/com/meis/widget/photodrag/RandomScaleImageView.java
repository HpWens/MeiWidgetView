package com.meis.widget.photodrag;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by wenshi on 2019/3/18.
 * Description
 */
public class RandomScaleImageView extends android.support.v7.widget.AppCompatImageView {

    // 比例必须是宽/高
    private float ratio = 1.0f;

    // 是否适配
    private boolean adaptiveEnable = false;

    public RandomScaleImageView(Context context) {
        this(context, null);
    }

    public RandomScaleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RandomScaleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (ratio != 1.0f && adaptiveEnable) {
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);

            // 原本的比例
            float r = (float) widthSize / heightSize;

            if (ratio > r) {
                widthSize = (int) (heightSize * ratio);
            } else {
                heightSize = (int) (widthSize / ratio);
            }

            int heightMeasure = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
            int widthMeasure = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);

            super.onMeasure(widthMeasure, heightMeasure);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

    }

    @Override
    public void layout(int l, int t, int r, int b) {
        super.layout(l, t, r, b);
        if (l != 0 && ratio != 1.0f && adaptiveEnable) {
            // centerCrop 方式 居中对齐
            float scaleWidth = Math.abs(l) * 2;
            if (l < 0) {
                // 需要缩小
                setScaleX(1.0f - scaleWidth / (r - l));
            } else {
                // 需要放大
                setScaleX(1.0f + scaleWidth / (r - l));
            }
        }

        if (t != 0 && ratio != 1.0f && adaptiveEnable) {
            float scaleHeight = Math.abs(t) * 2;
            if (t < 0) {
                setScaleY(1.0f - scaleHeight / (b - t));
            } else {
                setScaleY(1.0f + scaleHeight / (b - t));
            }
        }

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // 布局需要居中
        super.onLayout(changed, left, top, right, bottom);
    }

    public float getRatio() {
        return ratio;
    }

    public void setRatio(float ratio) {
        this.ratio = ratio;
        if (ratio != 1.0f) {
            adaptiveEnable = true;
            requestLayout();
        }
    }

}

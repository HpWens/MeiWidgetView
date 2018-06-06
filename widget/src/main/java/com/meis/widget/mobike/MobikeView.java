package com.meis.widget.mobike;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * desc:JBox2D运用-打造摩拜单车贴纸动画效果
 * author: wens
 * date: 2018/6/6.
 */
public class MobikeView extends FrameLayout {

    private Mobike mMobike;

    public MobikeView(@NonNull Context context) {
        this(context, null);
    }

    public MobikeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MobikeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        mMobike = new Mobike(this);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mMobike.onSizeChange(w, h);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mMobike.onLayout(changed);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mMobike.onDraw();
    }

    public Mobike getMobike() {
        return mMobike;
    }
}

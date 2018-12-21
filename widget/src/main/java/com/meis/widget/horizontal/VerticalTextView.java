package com.meis.widget.horizontal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import com.meis.widget.utils.DensityUtil;

/**
 * Created by wenshi on 2018/12/12.
 * Description 垂直排版的文本控件
 */
public class VerticalTextView extends AppCompatTextView {

    protected TextPaint mPaint;

    // 字符间距
    private int mCharSpacing;
    // 默认文本
    private CharSequence mDefaultText = "左滑看更多";

    // 背景阴影画笔
    private Paint mShadowPaint;
    private Path mShadowPath;

    private float mShadowOffset = 0;
    private int mErrorDistance;
    private boolean mIsDrawShadow = true;

    public VerticalTextView(Context context) {
        this(context, null);
    }

    public VerticalTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setAntiAlias(true);
        //　默认为4dp
        mCharSpacing = DensityUtil.dip2px(context, 4);
        mErrorDistance = DensityUtil.dip2px(context, 8);

        mShadowPaint = new Paint();
        mShadowPaint.setColor(Color.parseColor("#4F000000"));
        mShadowPaint.setAntiAlias(true);
        mShadowPaint.setStyle(Paint.Style.FILL);
        mShadowPaint.setStrokeWidth(1);

        mShadowPath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setTextSize(getTextSize());
        mPaint.setColor(getCurrentTextColor());
        mPaint.setTypeface(getTypeface());
        CharSequence text = mDefaultText;
        if (text != null && !text.toString().trim().equals("")) {
            Rect bounds = new Rect();
            mPaint.getTextBounds(text.toString(), 0, text.length(), bounds);

            // 最开始就忘记 + getPaddingLeft 导致绘制的文本偏左
            float startX = getLayout().getLineLeft(0) + getPaddingLeft();

            if (getCompoundDrawables()[0] != null) {
                Rect drawRect = getCompoundDrawables()[0].getBounds();
                startX += (drawRect.right - drawRect.left);
            }

            startX += getCompoundDrawablePadding();

            float startY = getBaseline();

            int cHeight = (bounds.bottom - bounds.top + mCharSpacing);

            // 居中对齐
            startY -= (text.length() - 1) * cHeight / 2;

            for (int i = 0; i < text.length(); i++) {
                String c = String.valueOf(text.charAt(i));

                canvas.drawText(c, startX, startY + i * cHeight, mPaint);
            }

        }
        super.onDraw(canvas);

        // 绘制贝塞尔阴影
        if (mIsDrawShadow) {
            mShadowPath.reset();
            mShadowPath.moveTo(getWidth(), getHeight() / 4);
            mShadowPath.quadTo(mShadowOffset, getHeight() / 2, getWidth(), getHeight() / 4 * 3);
            canvas.drawPath(mShadowPath, mShadowPaint);
        }
    }

//    @Override
//    public void setText(CharSequence text, BufferType type) {
//        super.setText(text, type);
//        setVerticalText(text);
//    }


    @Override
    public void setText(CharSequence text, BufferType type) {
        mDefaultText = text;
        super.setText("", type);
    }

    public void setVerticalText(CharSequence text) {
        if (TextUtils.isEmpty(text)) return;
        mDefaultText = text;
        invalidate();
    }

    public boolean getIsDrawShadow() {
        return mIsDrawShadow;
    }

    public void setDrawShadow(boolean drawShadow) {
        mIsDrawShadow = drawShadow;
    }

    public void setOffset(float offset, float maxOffset) {
        this.mShadowOffset = offset;
        float dis = maxOffset / 2 - mErrorDistance;
        if (mShadowOffset >= dis) {
            mShadowOffset = dis;
        } else {
            mShadowOffset = dis + (offset - dis) / 2;
        }
        invalidate();
    }
}

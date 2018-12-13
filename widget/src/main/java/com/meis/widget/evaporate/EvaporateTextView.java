package com.meis.widget.evaporate;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wenshi on 2018/11/22.
 * Description
 */
public class EvaporateTextView extends android.support.v7.widget.AppCompatTextView {

    protected int mHeight;
    protected int mWidth;
    protected CharSequence mText;
    protected CharSequence mOldText;

    protected TextPaint mPaint;
    protected TextPaint mOldPaint;

    // 每个字符的宽度集合
    protected List<Float> gapList = new ArrayList<>();
    protected List<Float> oldGapList = new ArrayList<>();

    protected float progress; //  0 ~ 1
    protected float mTextSize;

    protected float oldStartX = 0;

    private float charTime = 300;
    private int mostCount = 20;
    private int mTextHeight;

    private List<CharacterDiffResult> differentList = new ArrayList<>();
    private long duration;
    private ValueAnimator animator;

    public EvaporateTextView(Context context) {
        this(context, null);
    }

    public EvaporateTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EvaporateTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mOldText = "";
        mText = getText();

        mPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setAntiAlias(true);
        mOldPaint = new TextPaint(mPaint);
        mOldPaint.setAntiAlias(true);

        post(new Runnable() {
            @Override
            public void run() {
                mTextSize = getTextSize();
                mWidth = getWidth();
                mHeight = getHeight();

                oldStartX = 0;

                getOldStartX();
            }
        });

        setMaxLines(1);
        setEllipsize(TextUtils.TruncateAt.END);

        prepareAnimate();

        // 初始化animator
        animator = new ValueAnimator();
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                progress = (float) animation.getAnimatedValue();
                invalidate();
            }
        });

        int n = mText.length();
        n = n <= 0 ? 1 : n;
        duration = (long) (charTime + charTime / mostCount * (n - 1));

    }

    @Override
    protected void onDraw(Canvas canvas) {
        // super.onDraw(canvas);  注释掉 重新绘制文本
        float startX = getLayout().getLineLeft(0);
        float startY = getBaseline();

        float offset = startX;
        float oldOffset = oldStartX;

        int maxLength = Math.max(mText.length(), mOldText.length());

        for (int i = 0; i < maxLength; i++) {

            // draw old text
            if (i < mOldText.length()) {

                // pp = progress; 0~1  progress * duration / (charTime + charTime / mostCount * (mText.length() - 1))
                float pp = progress;

                mOldPaint.setTextSize(mTextSize);
                int move = CharacterUtils.needMove(i, differentList);

                if (move != -1) {
                    // 需要移动的字符 左右平移运动 视觉欺骗并没有上下的运动
                    mOldPaint.setAlpha(255);
                    // * 2f 平移速度
                    float p = pp * 2f;
                    p = p > 1 ? 1 : p;
                    float distX = CharacterUtils.getOffset(i, move, p, startX, oldStartX, gapList, oldGapList);
                    canvas.drawText(mOldText.charAt(i) + "", 0, 1, distX, startY, mOldPaint);
                } else {
                    mOldPaint.setAlpha((int) ((1 - pp) * 255));
                    float y = startY - pp * mTextHeight;
                    float width = mOldPaint.measureText(mOldText.charAt(i) + "");
                    // (oldGapList.get(i) - width) / 2 值为0   oldOffset + (oldGapList.get(i) - width) / 2
                    canvas.drawText(mOldText.charAt(i) + "", 0, 1, oldOffset, y, mOldPaint);
                }
                oldOffset += oldGapList.get(i);
            }

            if (i < mText.length()) {
                if (!CharacterUtils.stayHere(i, differentList)) {
                    // 渐显效果 延迟
                    int alpha = (int) (255f / charTime * (progress * duration - charTime * i / mostCount));
                    alpha = alpha > 255 ? 255 : alpha;
                    alpha = alpha < 0 ? 0 : alpha;
                    mPaint.setAlpha(alpha);
                    mPaint.setTextSize(mTextSize);

                    //   float pp = progress * duration / (charTime + charTime / mostCount * (mText.length() - 1));
                    float pp = progress;
                    float y = mTextHeight + startY - pp * mTextHeight;

                    float width = mPaint.measureText(mText.charAt(i) + "");
                    canvas.drawText(mText.charAt(i) + "", 0, 1, offset + (gapList.get(i) - width) / 2, y, mPaint);

                }
                offset += gapList.get(i);
            }

        }
    }

    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }

    public void animateText(CharSequence text) {
        setText(text);
        mOldText = mText;
        mText = text;

        prepareAnimate();

        animatePrepare(text);
        animateStart();
    }

    private void animateStart() {
        int n = mText.length();
        n = n <= 0 ? 1 : n;
        duration = (long) (charTime + charTime / mostCount * (n - 1));
        animator.cancel();
        animator.setFloatValues(0, 1);
        animator.setDuration(duration);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                getOldStartX();
            }
        });
        animator.start();
    }

    private void getOldStartX() {
        try {
            int layoutDirection = ViewCompat.getLayoutDirection(EvaporateTextView.this);

            oldStartX = layoutDirection == LAYOUT_DIRECTION_LTR ? getLayout().getLineLeft(0) : getLayout().getLineRight(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void animatePrepare(CharSequence text) {
        differentList.clear();
        differentList.addAll(CharacterUtils.diff(mOldText, mText));

        Rect bounds = new Rect();
        mPaint.getTextBounds(mText.toString(), 0, mText.length(), bounds);
        mTextHeight = bounds.height();
    }

    private void prepareAnimate() {
        // 初始化相关数据
        mTextSize = getTextSize();
        mPaint.setTextSize(mTextSize);
        mPaint.setColor(getCurrentTextColor());
        mPaint.setTypeface(getTypeface());

        gapList.clear();
        for (int i = 0; i < mText.length(); i++) {
            gapList.add(mPaint.measureText(String.valueOf(mText.charAt(i))));
        }

        mOldPaint.setTextSize(mTextSize);
        mOldPaint.setColor(getCurrentTextColor());
        mOldPaint.setTypeface(getTypeface());

        oldGapList.clear();
        for (int i = 0; i < mOldText.length(); i++) {
            oldGapList.add(mOldPaint.measureText(String.valueOf(mOldText.charAt(i))));
        }

    }

}

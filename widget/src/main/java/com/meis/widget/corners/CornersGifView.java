package com.meis.widget.corners;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;

import com.meis.widget.R;

import java.lang.reflect.Field;

/**
 * Created by
 * Description
 */
public class CornersGifView extends android.support.v7.widget.AppCompatImageView {

    private Path mPath;

    private Paint mPaint;

    private int mCornerSize;
    private int mLeftBottomCorner;
    private int mLeftTopCorner;
    private int mRightBottomCorner;
    private int mRightTopCorner;

    // corners array
    private float[] mCorners;

    public CornersGifView(Context context) {
        this(context, null);
    }

    public CornersGifView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CornersGifView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mPath = new Path();
        // set odd mode
        mPath.setFillType(Path.FillType.INVERSE_EVEN_ODD);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CornersGifView);
        mCornerSize = (int) ta.getDimension(R.styleable.CornersGifView_cornerSize, 0);
        mLeftBottomCorner = (int) ta.getDimension(R.styleable.CornersGifView_leftBottomCorner, 0);
        mLeftTopCorner = (int) ta.getDimension(R.styleable.CornersGifView_leftTopCorner, 0);
        mRightBottomCorner = (int) ta.getDimension(R.styleable.CornersGifView_rightBottomCorner, 0);
        mRightTopCorner = (int) ta.getDimension(R.styleable.CornersGifView_rightTopCorner, 0);
        ta.recycle();

        if (mCornerSize == 0) {
            mCorners = new float[]{
                    mLeftTopCorner, mLeftTopCorner,
                    mRightTopCorner, mRightTopCorner,
                    mRightBottomCorner, mRightBottomCorner,
                    mLeftBottomCorner, mLeftBottomCorner};
        } else {
            mCorners = new float[]{
                    mCornerSize, mCornerSize,
                    mCornerSize, mCornerSize,
                    mCornerSize, mCornerSize,
                    mCornerSize, mCornerSize};
        }
    }

    /**
     * 获取父控件颜色
     *
     * @param parent
     * @return
     */
    private int getParentBackGroundColor(ViewParent parent) {
        if (parent == null) {
            return Color.WHITE;
        }
        if (parent instanceof View) {
            View parentView = (View) parent;
            int parentColor = getViewBackGroundColor(parentView);
            if (parentColor != Color.TRANSPARENT) {
                return parentColor;
            } else {
                getParentBackGroundColor(parentView.getParent());
            }
        }
        // 获取主题背景色
        TypedArray array = getContext().getTheme().obtainStyledAttributes(new int[]{
                android.R.attr.colorBackground
        });
        int backgroundColor = array.getColor(0, Color.WHITE);
        array.recycle();
        return backgroundColor;
    }

    /**
     * 获取 View 的背景色
     *
     * @param view
     * @return
     */
    private int getViewBackGroundColor(View view) {
        Drawable drawable = view.getBackground();
        if (drawable != null) {
            Class<Drawable> mDrawable_class = (Class<Drawable>) drawable.getClass();
            try {
                Field mField = mDrawable_class.getDeclaredField("mColorState");
                mField.setAccessible(true);
                Object mColorState = mField.get(drawable);
                Class mColorState_class = mColorState.getClass();
                Field mColorState_field = mColorState_class.getDeclaredField("mUseColor");
                mColorState_field.setAccessible(true);
                int color = (int) mColorState_field.get(mColorState);
                if (color != Color.TRANSPARENT) {
                    return color;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            TypedArray array = getContext().getTheme().obtainStyledAttributes(new int[]{
                    android.R.attr.colorBackground
            });
            int backgroundColor = array.getColor(0, Color.WHITE);
            array.recycle();
            return backgroundColor;
        }
        return Color.WHITE;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // change skin
        canvas.save();
        canvas.drawPath(mPath, mPaint);
        canvas.restore();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (mPaint != null) {
            mPaint.setColor(getParentBackGroundColor(getParent()));
        }

        addRoundRectPath(w, h);
    }

    private void addRoundRectPath(int w, int h) {
        mPath.reset();
        // add round rect
        mPath.addRoundRect(new RectF(0, 0, w, h), mCorners, Path.Direction.CCW);
    }

    private void setCornerSize(int leftTop, int leftBottom, int rightTop, int rightBottom) {
        mCorners = new float[]{
                leftTop, leftTop,
                rightTop, rightTop,
                rightBottom, rightBottom,
                leftBottom, leftBottom};
        addRoundRectPath(getWidth(), getHeight());
        invalidate();
    }

    public int getCornerSize() {
        return mCornerSize;
    }

    public void setCornerSize(int cornerSize) {
        mCornerSize = cornerSize;
        setCornerSize(cornerSize, cornerSize, cornerSize, cornerSize);
    }

    public int getLeftBottomCorner() {
        return mLeftBottomCorner;
    }

    public void setLeftBottomCorner(int leftBottomCorner) {
        mLeftBottomCorner = leftBottomCorner;
        setCornerSize(mLeftTopCorner, mLeftBottomCorner, mRightTopCorner, mRightBottomCorner);
    }

    public int getLeftTopCorner() {
        return mLeftTopCorner;
    }

    public void setLeftTopCorner(int leftTopCorner) {
        mLeftTopCorner = leftTopCorner;
        setCornerSize(mLeftTopCorner, mLeftBottomCorner, mRightTopCorner, mRightBottomCorner);
    }

    public int getRightBottomCorner() {
        return mRightBottomCorner;
    }

    public void setRightBottomCorner(int rightBottomCorner) {
        mRightBottomCorner = rightBottomCorner;
        setCornerSize(mLeftTopCorner, mLeftBottomCorner, mRightTopCorner, mRightBottomCorner);
    }

    public int getRightTopCorner() {
        return mRightTopCorner;
    }

    public void setRightTopCorner(int rightTopCorner) {
        mRightTopCorner = rightTopCorner;
        setCornerSize(mLeftTopCorner, mLeftBottomCorner, mRightTopCorner, mRightBottomCorner);
    }
}

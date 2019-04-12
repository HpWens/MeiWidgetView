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
     * @param vp parent view
     * @return paint color
     */
    private int getPaintColor(ViewParent vp) {
        if (null == vp) {
            return Color.TRANSPARENT;
        }

        if (vp instanceof View) {
            View parentView = (View) vp;
            int color = getViewBackgroundColor(parentView);

            if (Color.TRANSPARENT != color) {
                return color;
            } else {
                getPaintColor(parentView.getParent());
            }
        }

        return Color.TRANSPARENT;
    }

    /**
     * @param view
     * @return
     */
    private int getViewBackgroundColor(View view) {
        Drawable drawable = view.getBackground();

        if (null != drawable) {
            Class<Drawable> drawableClass = (Class<Drawable>) drawable.getClass();
            if (null == drawableClass) {
                return Color.TRANSPARENT;
            }

            try {
                Field field = drawableClass.getDeclaredField("mColorState");
                field.setAccessible(true);
                Object colorState = field.get(drawable);
                Class colorStateClass = colorState.getClass();
                Field colorStateField = colorStateClass.getDeclaredField("mUseColor");
                colorStateField.setAccessible(true);
                int viewColor = (int) colorStateField.get(colorState);
                if (Color.TRANSPARENT != viewColor) {
                    return viewColor;
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return Color.TRANSPARENT;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.drawPath(mPath, mPaint);
        canvas.restore();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        initPaintColor();
        addRoundRectPath(w, h);
    }

    private void initPaintColor() {
        int paintColor = getPaintColor(getParent());
        if (Color.TRANSPARENT == paintColor) {
            // get theme background color
            TypedArray array = getContext().getTheme().obtainStyledAttributes(new int[]{
                    android.R.attr.colorBackground
            });
            paintColor = array.getColor(0, Color.TRANSPARENT);
            array.recycle();
        }

        mPaint.setColor(paintColor);
    }

    private void addRoundRectPath(int w, int h) {
        mPath.reset();

        //add round rect
        mPath.addRoundRect(new RectF(0, 0, w, h), mCorners, Path.Direction.CCW);

//        Path brainPath = new Path();
//        brainPath.addRect(new RectF(0, 0, w, h), Path.Direction.CCW);
//        brainPath.addCircle(w / 2, h / 2, Math.min(w, h) / 2, Path.Direction.CW);
//
//        mPath.setFillType(Path.FillType.WINDING);
//        mPath.addPath(brainPath);
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

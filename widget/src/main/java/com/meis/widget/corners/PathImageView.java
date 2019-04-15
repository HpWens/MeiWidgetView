package com.meis.widget.corners;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.widget.ImageView;

import java.lang.reflect.Field;


/*
 * Copyright 2019 Wen Shi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@SuppressLint("AppCompatCustomView")
public class PathImageView extends ImageView {

    private Path mPath;

    private Paint mPaint;

    public PathImageView(Context context) {
        this(context, null);
    }

    public PathImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PathImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        mPath = new Path();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initPaintColor();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.drawPath(mPath, mPaint);
        canvas.restore();
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

    /**
     * @param path
     */
    private void setPath(Path path) {
        mPath.reset();

        mPath.setFillType(Path.FillType.WINDING);
        mPath.addPath(path);

        invalidate();
    }

    private void addRoundRectPath(int roundSize) {
        addRoundRectPath(roundSize, roundSize, roundSize, roundSize);
    }

    private void addRoundRectPath(int leftTop, int leftBottom, int rightTop, int rightBottom) {
        int w = getWidth();
        int h = getHeight();

        Path addPath = new Path();
        addPath.addRect(new RectF(0, 0, w, h), Path.Direction.CCW);

        float[] radii = new float[]{
                leftTop, leftTop,
                rightTop, rightTop,
                rightBottom, rightBottom,
                leftBottom, leftBottom
        };
        addPath.addRoundRect(new RectF(0, 0, w, h), radii, Path.Direction.CW);

        setPath(addPath);
    }

    private void addCirclePath() {
        int w = getWidth();
        int h = getHeight();

        Path addPath = new Path();
        addPath.addRect(new RectF(0, 0, w, h), Path.Direction.CCW);
        addPath.addCircle(w / 2, h / 2, Math.min(w, h) / 2, Path.Direction.CW);
        setPath(addPath);
    }

    private void addArcPath(float startAngle, float sweepAngle) {
        int w = getWidth();
        int h = getHeight();

        Path addPath = new Path();
        addPath.addRect(new RectF(0, 0, w, h), Path.Direction.CCW);
        addPath.addArc(new RectF(0, 0, w, h), startAngle, sweepAngle);
        setPath(addPath);
    }

    private void addOvalPath() {
        int w = getWidth();
        int h = getHeight();

        Path addPath = new Path();
        addPath.addRect(new RectF(0, 0, w, h), Path.Direction.CCW);
        addPath.addOval(new RectF(0, 0, w, h), Path.Direction.CW);
        setPath(addPath);
    }

    private void addPolygonPath(int edge, int angle) {
        int w = getWidth();
        int h = getHeight();

        Path addPath = new Path();
        addPath.addRect(new RectF(0, 0, w, h), Path.Direction.CCW);

        Path polygonPath = new Path();
        int radius = Math.min(w / 2, h / 2);
        for (int i = 0; i < edge; i++) {
            int dx = (int) (w / 2 + radius * Math.cos(Math.toRadians(i * 360 / edge + angle)));
            int dy = (int) (h / 2 + radius * Math.sin(Math.toRadians(i * 360 / edge + angle)));
            if (i == 0) {
                polygonPath.moveTo(dx, dy);
            } else {
                polygonPath.lineTo(dx, dy);
            }
        }
        polygonPath.close();
        addPath.addPath(polygonPath);
        setPath(addPath);
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

}


package com.meis.widget.radius.delegate;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.meis.widget.R;
import com.meis.widget.utils.DrawableUtil;

/**
 * Created: AriesHoo on 2018/2/6/006 9:47
 * E-Mail: AriesHoo@126.com
 * Function: 设置CompoundButton ButtonDrawable相关代理
 * Description:
 */
public class RadiusCompoundButtonDelegate<T extends RadiusCompoundButtonDelegate> extends RadiusTextViewDelegate<T> {

    private CompoundButton mButton;
    private StateListDrawable mStateButtonDrawable;

    private float mButtonDrawableColorRadius;
    private boolean mButtonDrawableColorCircleEnable;
    private int mButtonDrawableWidth;
    private int mButtonDrawableHeight;
    private Drawable mButtonDrawable;
    private Drawable mButtonPressedDrawable;
    private Drawable mButtonDisabledDrawable;
    private Drawable mButtonSelectedDrawable;
    private Drawable mButtonCheckedDrawable;


    public RadiusCompoundButtonDelegate(TextView view, Context context, AttributeSet attrs) {
        super(view, context, attrs);
    }


    @Override
    protected void initAttributes(Context context, AttributeSet attrs) {
        mButtonDrawableColorRadius = mTypedArray.getDimension(R.styleable.RadiusSwitch_rv_buttonDrawableColorRadius, 0);
        mButtonDrawableColorCircleEnable = mTypedArray.getBoolean(R.styleable.RadiusSwitch_rv_buttonDrawableColorCircleEnable, false);
        mButtonDrawableWidth = mTypedArray.getDimensionPixelSize(R.styleable.RadiusSwitch_rv_buttonDrawableWidth, -1);
        mButtonDrawableHeight = mTypedArray.getDimensionPixelSize(R.styleable.RadiusSwitch_rv_buttonDrawableHeight, -1);
        mButtonDrawable = mTypedArray.getDrawable(R.styleable.RadiusSwitch_rv_buttonDrawable);
        mButtonPressedDrawable = mTypedArray.getDrawable(R.styleable.RadiusSwitch_rv_buttonPressedDrawable);
        mButtonDisabledDrawable = mTypedArray.getDrawable(R.styleable.RadiusSwitch_rv_buttonDisabledDrawable);
        mButtonSelectedDrawable = mTypedArray.getDrawable(R.styleable.RadiusSwitch_rv_buttonSelectedDrawable);
        mButtonCheckedDrawable = mTypedArray.getDrawable(R.styleable.RadiusSwitch_rv_buttonCheckedDrawable);

        mButtonPressedDrawable = mButtonPressedDrawable == null ? mButtonDrawable : mButtonPressedDrawable;
        mButtonDisabledDrawable = mButtonDisabledDrawable == null ? mButtonDrawable : mButtonDisabledDrawable;
        mButtonSelectedDrawable = mButtonSelectedDrawable == null ? mButtonDrawable : mButtonSelectedDrawable;
        mButtonCheckedDrawable = mButtonCheckedDrawable == null ? mButtonDrawable : mButtonCheckedDrawable;
        super.initAttributes(context, attrs);
    }

    @Override
    public void init() {
        setButtonDrawable();
        super.init();
    }

    /**
     * 设置drawable宽度--ColorDrawable有效其它不知为啥失效
     *
     * @param drawableWidth
     * @return
     */
    public T setButtonDrawableWidth(int drawableWidth) {
        mButtonDrawableWidth = drawableWidth;
        return (T) this;
    }

    /**
     * 设置drawable高度--ColorDrawable有效其它不知为啥失效
     *
     * @param drawableHeight
     * @return
     */
    public T setButtonDrawableHeight(int drawableHeight) {
        mButtonDrawableHeight = drawableHeight;
        return (T) this;
    }

    /**
     * 设置默认状态Drawable
     *
     * @param drawable
     */
    public T setButtonDrawable(Drawable drawable) {
        mButtonDrawable = drawable;
        return (T) this;
    }

    public T setButtonDrawable(int resId) {
        return setButtonDrawable(getDrawable(resId));
    }

    /**
     * 设置按下效果Drawable
     *
     * @param drawable
     */
    public T setButtonPressedDrawable(Drawable drawable) {
        mButtonPressedDrawable = drawable;
        return (T) this;
    }

    public T setButtonPressedDrawable(int resId) {
        return setButtonPressedDrawable(getDrawable(resId));
    }

    /**
     * 设置不可操作效果Drawable
     *
     * @param drawable
     */
    public T setButtonDisabledDrawable(Drawable drawable) {
        mButtonDisabledDrawable = drawable;
        return (T) this;
    }

    public T setButtonDisabledDrawable(int resId) {
        return setButtonDisabledDrawable(getDrawable(resId));
    }

    /**
     * 设置选中效果Drawable
     *
     * @param drawable
     * @return
     */
    public T setButtonSelectedDrawable(Drawable drawable) {
        mButtonSelectedDrawable = drawable;
        return (T) this;
    }

    public T setButtonSelectedDrawable(int resId) {
        return setButtonSelectedDrawable(getDrawable(resId));
    }

    /**
     * 设置Checked状态Drawable
     *
     * @param drawable
     * @return
     */
    public T setButtonCheckedDrawable(Drawable drawable) {
        mButtonCheckedDrawable = drawable;
        return (T) this;
    }

    public T setButtonCheckedDrawable(int resId) {
        return setButtonCheckedDrawable(getDrawable(resId));
    }

    /**
     * 设置CompoundButton的setButtonDrawable属性
     */
    private void setButtonDrawable() {
        mButton = (CompoundButton) mView;
        if (mButtonDrawable != null || mButtonPressedDrawable != null
                || mButtonDisabledDrawable != null || mButtonSelectedDrawable != null
                || mButtonCheckedDrawable != null) {
            float radius = mButtonDrawableColorCircleEnable ?
                    mButtonDrawableWidth + mButtonDrawableHeight / 2 : mButtonDrawableColorRadius;
            if (mStateButtonDrawable == null) {
                mStateButtonDrawable = new StateListDrawable();
            }
            mStateButtonDrawable.addState(new int[]{mStateChecked}, getStateDrawable(mButtonCheckedDrawable, radius, mButtonDrawableWidth, mButtonDrawableHeight));
            mStateButtonDrawable.addState(new int[]{mStateSelected}, getStateDrawable(mButtonSelectedDrawable, radius, mButtonDrawableWidth, mButtonDrawableHeight));
            mStateButtonDrawable.addState(new int[]{mStatePressed}, getStateDrawable(mButtonPressedDrawable, radius, mButtonDrawableWidth, mButtonDrawableHeight));
            mStateButtonDrawable.addState(new int[]{mStateDisabled}, getStateDrawable(mButtonDisabledDrawable, radius, mButtonDrawableWidth, mButtonDrawableHeight));
            mStateButtonDrawable.addState(new int[]{}, getStateDrawable(mButtonDrawable, radius, mButtonDrawableWidth, mButtonDrawableHeight));
            DrawableUtil.setDrawableWidthHeight(mStateButtonDrawable, mButtonDrawableWidth, mButtonDrawableHeight);
            mButton.setButtonDrawable(mStateButtonDrawable);
        }
    }
}

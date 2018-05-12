package com.meis.widget.radius.helper;

import android.view.View;

import java.lang.ref.WeakReference;

/**
 * Created: AriesHoo on 2018/3/12/012 12:41
 * E-Mail: AriesHoo@126.com
 * Function: 设置View按下状态透明度变化帮助类
 * Description:
 */
public class AlphaViewHelper {

    private WeakReference<View> mTarget;

    private float mNormalAlpha = 1f;
    private float mPressedAlpha = 1f;
    private float mDisabledAlpha = 1f;

    public AlphaViewHelper(View target) {
        mTarget = new WeakReference<>(target);
    }

    public AlphaViewHelper(View target, float pressedAlpha, float disabledAlpha) {
        mTarget = new WeakReference<>(target);
        mPressedAlpha = pressedAlpha;
        mDisabledAlpha = disabledAlpha;
    }

    /**
     * @param current the view to be handled, maybe not equal to target view
     * @param pressed
     */
    public AlphaViewHelper onPressedChanged(View current, boolean pressed) {
        View target = mTarget.get();
        if (target == null) {
            return this;
        }
        if (current.isEnabled()) {
            target.setAlpha(pressed && current.isClickable() ? mPressedAlpha : mNormalAlpha);
        } else {
            target.setAlpha(mDisabledAlpha);
        }
        return this;
    }

    /**
     * @param current the view to be handled, maybe not  equal to target view
     * @param enabled
     */
    public AlphaViewHelper onEnabledChanged(View current, boolean enabled) {
        View target = mTarget.get();
        if (target == null) {
            return this;
        }
        float alphaForIsEnable = enabled ? mNormalAlpha : mDisabledAlpha;
        if (current != target && target.isEnabled() != enabled) {
            target.setEnabled(enabled);
        }
        target.setAlpha(alphaForIsEnable);
        return this;
    }

    /**
     * 设置各状态下alpha值
     *
     * @param normal
     * @param pressed
     * @param disabled
     * @return
     */
    public AlphaViewHelper setAlpha(float normal, float pressed, float disabled) {
        this.mNormalAlpha = normal;
        this.mPressedAlpha = pressed;
        this.mDisabledAlpha = disabled;
        return this;
    }

    public AlphaViewHelper setNormalAlpha(float normal) {
        this.mNormalAlpha = normal;
        return this;
    }

    public AlphaViewHelper setPressedAlpha(float pressed) {
        this.mPressedAlpha = pressed;
        return this;
    }

    public AlphaViewHelper setDisabledAlpha(float disabled) {
        this.mDisabledAlpha = disabled;
        return this;
    }

}

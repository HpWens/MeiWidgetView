package com.meis.widget.radius.delegate;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.meis.widget.R;
import com.meis.widget.utils.ResourceUtil;


/**
 * Created: AriesHoo on 2017-02-10 14:25
 * E-Mail: AriesHoo@126.com
 * Function:  公共属性解析代理类
 * Description:
 * 1、2017-11-15 09:36:14 添加Java属性设置链式调用支持
 * 2、2018-2-4 10:13:17 修改设置背景时机需调用init方法以设置避免未设置完所有属性不停调用设置背景
 * 3、2018-2-4 17:10:15 增加setSelected方法及对应的监听setOnSelectedChangeListener
 * 4、2018-2-5 13:36:02 修改不可点击属性Enabled对应修改为Disabled xml及java方法同步修改
 * 5、2018-2-6 12:04:02 校正float类型参数xml属性解析方法
 * 6、2018-2-23 10:30:53 新增View在非波纹背景下各个状态切换时延属性
 * 7、2018-3-18 11:17:29 新增泛型返回方便子类继承的链式调用
 */
public class RadiusViewDelegate<T extends RadiusViewDelegate> {

    protected ResourceUtil mResourceUtil;
    protected TypedArray mTypedArray;
    protected View mView;
    private Context mContext;
    private GradientDrawable mBackground = new GradientDrawable();
    private GradientDrawable mBackgroundPressed = new GradientDrawable();
    private GradientDrawable mBackgroundDisabled = new GradientDrawable();
    private GradientDrawable mBackgroundSelected = new GradientDrawable();
    private GradientDrawable mBackgroundChecked = new GradientDrawable();

    //以下为xml属性对应解析参数
    private int mBackgroundColor;
    private int mBackgroundPressedColor;
    private int mBackgroundDisabledColor;
    private int mBackgroundSelectedColor;
    private int mBackgroundCheckedColor;

    private int mStrokeColor;
    private int mStrokePressedColor;
    private int mStrokeDisabledColor;
    private int mStrokeSelectedColor;
    private int mStrokeCheckedColor;

    private int mStrokeWidth;
    private float mStrokeDashWidth;
    private float mStrokeDashGap;

    private boolean mRadiusHalfHeightEnable;
    private boolean mWidthHeightEqualEnable;

    private float mRadius;
    private float mTopLeftRadius;
    private float mTopRightRadius;
    private float mBottomLeftRadius;
    private float mBottomRightRadius;

    private int mRippleColor;
    private boolean mRippleEnable;
    private boolean mSelected;
    private int mEnterFadeDuration = 0;
    private int mExitFadeDuration = 0;
    //以上为xml属性对应解析参数

    protected int mStateChecked = android.R.attr.state_checked;
    protected int mStateSelected = android.R.attr.state_selected;
    protected int mStatePressed = android.R.attr.state_pressed;
    protected int mStateDisabled = -android.R.attr.state_enabled;
    private float[] mRadiusArr = new float[8];

    private OnSelectedChangeListener mOnSelectedChangeListener;

    public interface OnSelectedChangeListener {
        /**
         * Called when the checked state of a View has changed.
         *
         * @param view       The View whose state has changed.
         * @param isSelected The new selected state of buttonView.
         */
        void onSelectedChanged(View view, boolean isSelected);
    }

    public RadiusViewDelegate(View view, Context context, AttributeSet attrs) {
        this.mView = view;
        this.mContext = context;
        this.mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.RadiusSwitch);
        this.mResourceUtil = new ResourceUtil(context);
        initAttributes(context, attrs);
        if (!(view instanceof CompoundButton) && !view.isClickable()) {
            view.setClickable(true);
        }
        view.setSelected(mSelected);
        setSelected(mSelected);
    }

    protected void initAttributes(Context context, AttributeSet attrs) {
        mBackgroundColor = mTypedArray.getColor(R.styleable.RadiusSwitch_rv_backgroundColor, Integer.MAX_VALUE);
        mBackgroundPressedColor = mTypedArray.getColor(R.styleable.RadiusSwitch_rv_backgroundPressedColor, mBackgroundColor);
        mBackgroundDisabledColor = mTypedArray.getColor(R.styleable.RadiusSwitch_rv_backgroundDisabledColor, mBackgroundColor);
        mBackgroundSelectedColor = mTypedArray.getColor(R.styleable.RadiusSwitch_rv_backgroundSelectedColor, mBackgroundColor);
        mBackgroundCheckedColor = mTypedArray.getColor(R.styleable.RadiusSwitch_rv_backgroundCheckedColor, mBackgroundColor);

        mStrokeColor = mTypedArray.getColor(R.styleable.RadiusSwitch_rv_strokeColor, Color.GRAY);
        mStrokePressedColor = mTypedArray.getColor(R.styleable.RadiusSwitch_rv_strokePressedColor, mStrokeColor);
        mStrokeDisabledColor = mTypedArray.getColor(R.styleable.RadiusSwitch_rv_strokeDisabledColor, mStrokeColor);
        mStrokeSelectedColor = mTypedArray.getColor(R.styleable.RadiusSwitch_rv_strokeSelectedColor, mStrokeColor);
        mStrokeCheckedColor = mTypedArray.getColor(R.styleable.RadiusSwitch_rv_strokeCheckedColor, mStrokeColor);

        mStrokeWidth = mTypedArray.getDimensionPixelSize(R.styleable.RadiusSwitch_rv_strokeWidth, 0);
        mStrokeDashWidth = mTypedArray.getDimension(R.styleable.RadiusSwitch_rv_strokeDashWidth, 0);
        mStrokeDashGap = mTypedArray.getDimension(R.styleable.RadiusSwitch_rv_strokeDashGap, 0);

        mRadiusHalfHeightEnable = mTypedArray.getBoolean(R.styleable.RadiusSwitch_rv_radiusHalfHeightEnable, false);
        mWidthHeightEqualEnable = mTypedArray.getBoolean(R.styleable.RadiusSwitch_rv_widthHeightEqualEnable, false);

        mRadius = mTypedArray.getDimension(R.styleable.RadiusSwitch_rv_radius, 0);
        mTopLeftRadius = mTypedArray.getDimension(R.styleable.RadiusSwitch_rv_topLeftRadius, 0);
        mTopRightRadius = mTypedArray.getDimension(R.styleable.RadiusSwitch_rv_topRightRadius, 0);
        mBottomLeftRadius = mTypedArray.getDimension(R.styleable.RadiusSwitch_rv_bottomLeftRadius, 0);
        mBottomRightRadius = mTypedArray.getDimension(R.styleable.RadiusSwitch_rv_bottomRightRadius, 0);

        mRippleColor = mTypedArray.getColor(R.styleable.RadiusSwitch_rv_rippleColor, Color.parseColor("#C8CCCCCC"));
        mRippleEnable = mTypedArray.getBoolean(R.styleable.RadiusSwitch_rv_rippleEnable, false);
        mSelected = mTypedArray.getBoolean(R.styleable.RadiusSwitch_rv_selected, false);
        mEnterFadeDuration = mTypedArray.getInteger(R.styleable.RadiusSwitch_rv_enterFadeDuration, 0);
        mExitFadeDuration = mTypedArray.getInteger(R.styleable.RadiusSwitch_rv_exitFadeDuration, 0);
        mTypedArray.recycle();
    }

    /**
     * 是否默认开启水波纹
     *
     * @return
     */
    private boolean getDefaultRippleEnable() {
        boolean enable = !(mView instanceof CompoundButton) && !(mView instanceof EditText);
        return enable;
    }

    /**
     * 设置常态背景色
     *
     * @param color
     * @return
     */
    public T setBackgroundColor(int color) {
        this.mBackgroundColor = color;
        return (T) this;
    }

    /**
     * 设置按下状态背景色
     *
     * @param color
     * @return
     */
    public T setBackgroundPressedColor(int color) {
        this.mBackgroundPressedColor = color;
        return (T) this;
    }

    /**
     * 设置不可操作状态下背景色
     *
     * @param color
     * @return
     */
    public T setBackgroundDisabledColor(int color) {
        this.mBackgroundDisabledColor = color;
        return (T) this;
    }

    /**
     * 设置selected状态下背景色
     *
     * @param color
     * @return
     */
    public T setBackgroundSelectedColor(int color) {
        this.mBackgroundSelectedColor = color;
        return (T) this;
    }

    /**
     * 设置checked状态背景色
     *
     * @param color
     * @return
     */
    public T setBackgroundCheckedColor(int color) {
        this.mBackgroundCheckedColor = color;
        return (T) this;
    }

    /**
     * 设置边框线常态颜色
     *
     * @param strokeColor
     * @return
     */
    public T setStrokeColor(int strokeColor) {
        this.mStrokeColor = strokeColor;
        return (T) this;
    }

    /**
     * 设置边框线按下状态颜色
     *
     * @param strokePressedColor
     * @return
     */
    public T setStrokePressedColor(int strokePressedColor) {
        this.mStrokePressedColor = strokePressedColor;
        return (T) this;
    }

    /**
     * 设置边框线不可点击状态下颜色
     *
     * @param strokeDisabledColor
     * @return
     */
    public T setStrokeDisabledColor(int strokeDisabledColor) {
        this.mStrokeDisabledColor = strokeDisabledColor;
        return (T) this;
    }

    /**
     * 设置边框线selected状态颜色
     *
     * @param strokeSelectedColor
     * @return
     */
    public T setStrokeSelectedColor(int strokeSelectedColor) {
        this.mStrokeSelectedColor = strokeSelectedColor;
        return (T) this;
    }

    /**
     * 设置边框checked状态颜色
     *
     * @param strokeCheckedColor
     * @return
     */
    public T setStrokeCheckedColor(int strokeCheckedColor) {
        this.mStrokeCheckedColor = strokeCheckedColor;
        return (T) this;
    }

    /**
     * 设置边框线宽度(粗细)
     *
     * @param strokeWidth
     * @return
     */
    public T setStrokeWidth(int strokeWidth) {
        this.mStrokeWidth = strokeWidth;
        return (T) this;
    }

    /**
     * 设置虚线宽度
     *
     * @param strokeDashWidth
     * @return
     */
    public T setStrokeDashWidth(float strokeDashWidth) {
        this.mStrokeDashWidth = strokeDashWidth;
        return (T) this;
    }

    /**
     * 设置虚线间隔
     *
     * @param strokeDashGap
     * @return
     */
    public T setStrokeDashGap(float strokeDashGap) {
        this.mStrokeDashGap = strokeDashGap;
        return (T) this;
    }

    /**
     * 设置半高度圆角
     *
     * @param enable
     * @return
     */
    public T setRadiusHalfHeightEnable(boolean enable) {
        this.mRadiusHalfHeightEnable = enable;
        return (T) this;
    }

    /**
     * 设置宽高相等
     *
     * @param enable
     * @return
     */
    public T setWidthHeightEqualEnable(boolean enable) {
        this.mWidthHeightEqualEnable = enable;
        return (T) this;
    }

    /**
     * 设置整体圆角弧度
     *
     * @param radius
     * @return
     */
    public T setRadius(float radius) {
        this.mRadius = radius;
        return (T) this;
    }

    /**
     * 设置左上圆角
     *
     * @param radius
     * @return
     */
    public T setTopLeftRadius(float radius) {
        this.mTopLeftRadius = radius;
        return (T) this;
    }

    /**
     * 设置右上圆角
     *
     * @param radius
     * @return
     */
    public T setTopRightRadius(float radius) {
        this.mTopRightRadius = radius;
        return (T) this;
    }

    /**
     * 设置左下圆角
     *
     * @param radius
     * @return
     */
    public T setBottomLeftRadius(float radius) {
        this.mBottomLeftRadius = radius;
        return (T) this;
    }

    /**
     * 设置右下圆角
     *
     * @param radius
     * @return
     */
    public T setBottomRightRadius(float radius) {
        this.mBottomRightRadius = radius;
        return (T) this;
    }

    /**
     * 设置水波纹颜色 5.0以上支持
     *
     * @param color
     * @return
     */
    public T setRippleColor(int color) {
        this.mRippleColor = color;
        return (T) this;
    }

    /**
     * 设置是否支持水波纹效果--5.0及以上
     *
     * @param enable
     * @return
     */
    public T setRippleEnable(boolean enable) {
        this.mRippleEnable = enable;
        return (T) this;
    }

    /**
     * 设置选中状态变换监听
     *
     * @param listener
     * @return
     */
    public T setOnSelectedChangeListener(OnSelectedChangeListener listener) {
        this.mOnSelectedChangeListener = listener;
        return (T) this;
    }

    /**
     * @param selected
     */
    public void setSelected(boolean selected) {
        if (mView != null) {
            if (mSelected != selected) {
                mSelected = selected;
                if (mOnSelectedChangeListener != null) {
                    mOnSelectedChangeListener.onSelectedChanged(mView, mSelected);
                }
            }
        }
        init();
    }

    /**
     * 设置状态切换延时
     *
     * @param duration
     * @return
     */
    public T setEnterFadeDuration(int duration) {
        if (duration >= 0) {
            mEnterFadeDuration = duration;
        }
        return (T) this;
    }

    /**
     * 设置状态切换延时
     *
     * @param duration
     * @return
     */
    public T setExitFadeDuration(int duration) {
        if (duration > 0) {
            mExitFadeDuration = duration;
        }
        return (T) this;
    }

    public float getRadius() {
        return mRadius;
    }

    public boolean getRadiusHalfHeightEnable() {
        return mRadiusHalfHeightEnable;
    }

    public boolean getWidthHeightEqualEnable() {
        return mWidthHeightEqualEnable;
    }

    /**
     * 设置 背景Drawable颜色线框色及圆角值
     *
     * @param gd
     * @param color
     * @param strokeColor
     */
    private void setDrawable(GradientDrawable gd, int color, int strokeColor) {
        //任意值大于0执行
        if (mTopLeftRadius > 0 || mTopRightRadius > 0 || mBottomRightRadius > 0 || mBottomLeftRadius > 0) {
            mRadiusArr[0] = mTopLeftRadius;
            mRadiusArr[1] = mTopLeftRadius;
            mRadiusArr[2] = mTopRightRadius;
            mRadiusArr[3] = mTopRightRadius;
            mRadiusArr[4] = mBottomRightRadius;
            mRadiusArr[5] = mBottomRightRadius;
            mRadiusArr[6] = mBottomLeftRadius;
            mRadiusArr[7] = mBottomLeftRadius;
            gd.setCornerRadii(mRadiusArr);
        } else {
            gd.setCornerRadius(mRadius);
        }
        gd.setStroke(mStrokeWidth, strokeColor, mStrokeDashWidth, mStrokeDashGap);
        gd.setColor(color);
    }

    /**
     * 设置shape属性
     * 设置完所有属性后调用设置背景
     */
    public void init() {
        //获取view当前drawable--用于判断是否通过默认属性设置背景
        Drawable mDrawable = mView.getBackground();
        //判断是否使用自定义颜色值
        boolean isSetBg = mBackgroundColor != Integer.MAX_VALUE
                || mBackgroundPressedColor != Integer.MAX_VALUE
                || mBackgroundDisabledColor != Integer.MAX_VALUE
                || mBackgroundSelectedColor != Integer.MAX_VALUE
                || mStrokeWidth > 0 || mRadius > 0
                || mTopLeftRadius > 0 || mTopLeftRadius > 0 || mBottomLeftRadius > 0 || mBottomRightRadius > 0;

        setDrawable(mBackgroundChecked, mBackgroundCheckedColor, mStrokeCheckedColor);
        setDrawable(mBackgroundSelected, mBackgroundPressedColor, mStrokeSelectedColor);
        setDrawable(mBackground, mBackgroundColor, mStrokeColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && mRippleEnable && mView.isEnabled() && !mView.isSelected()) {//5.0以上且设置水波属性并且可操作
            RippleDrawable rippleDrawable = new RippleDrawable(
                    new ColorStateList(
                            new int[][]{
                                    new int[]{mStatePressed},
                                    new int[]{}
                            },
                            new int[]{
                                    mRippleColor != Integer.MAX_VALUE ? mRippleColor : mBackgroundPressedColor,
                                    mRippleColor
                            }
                    )
                    , getContentDrawable(mDrawable, isSetBg)
                    , null);
            mView.setBackground(rippleDrawable);
        } else {
            if (!isSetBg) {
                return;
            }
            StateListDrawable mStateDrawable = new StateListDrawable();
            mStateDrawable.setEnterFadeDuration(mEnterFadeDuration);
            mStateDrawable.setExitFadeDuration(mExitFadeDuration);
            if (mBackgroundPressedColor != Integer.MAX_VALUE || mStrokePressedColor != Integer.MAX_VALUE) {
                setDrawable(mBackgroundPressed, mBackgroundPressedColor, mStrokePressedColor);
                mStateDrawable.addState(new int[]{mStatePressed}, mBackgroundPressed);
            }
            if (mBackgroundSelectedColor != Integer.MAX_VALUE || mStrokeSelectedColor != Integer.MAX_VALUE) {
                setDrawable(mBackgroundSelected, mBackgroundSelectedColor, mStrokeSelectedColor);
                mStateDrawable.addState(new int[]{mStateSelected}, mBackgroundSelected);
            }
            if (mBackgroundCheckedColor != Integer.MAX_VALUE || mStrokeCheckedColor != Integer.MAX_VALUE) {
                setDrawable(mBackgroundChecked, mBackgroundCheckedColor, mStrokeCheckedColor);
                mStateDrawable.addState(new int[]{mStateChecked}, mBackgroundChecked);
            }
            if (mBackgroundDisabledColor != Integer.MAX_VALUE || mStrokeDisabledColor != Integer.MAX_VALUE) {
                setDrawable(mBackgroundDisabled, mBackgroundDisabledColor, mStrokeDisabledColor);
                mStateDrawable.addState(new int[]{mStateDisabled}, mBackgroundDisabled);
            }
            mStateDrawable.addState(new int[]{}, mBackground);//默认状态--放置在最后否则其它状态不生效
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mView.setBackground(mStateDrawable);
            } else {
                mView.setBackgroundDrawable(mStateDrawable);
            }
        }
        return;
    }

    /**
     * 水波纹效果完成后最终展示的背景Drawable
     *
     * @param mDrawable
     * @param isSetBg
     * @return
     */
    private Drawable getContentDrawable(Drawable mDrawable, boolean isSetBg) {
        if (mView instanceof CompoundButton) {
            return !isSetBg ? mDrawable :
                    ((CompoundButton) mView).isChecked() ? mBackgroundChecked :
                            mView.isSelected() ? mBackgroundSelected :
                                    mBackground;
        }
        return !isSetBg ? mDrawable : mView.isSelected() ? mBackgroundSelected : mBackground;
    }

    protected int dp2px(float dipValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}

package com.meis.widget.radius;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.meis.widget.radius.delegate.RadiusViewDelegate;

/**
 * Created: AriesHoo on 2017-02-10 14:24
 * E-Mail: AriesHoo@126.com
 * Function: 用于需要圆角矩形框背景的FrameLayout的情况,减少直接使用FrameLayout时引入的shape资源文件
 * Description:
 */
public class RadiusFrameLayout extends FrameLayout {

    private RadiusViewDelegate delegate;

    public RadiusFrameLayout(Context context) {
        this(context, null);
    }

    public RadiusFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        delegate = new RadiusViewDelegate(this, context, attrs);
    }

    /**
     * 获取代理类用于Java代码控制shape属性
     *
     * @return
     */
    public RadiusViewDelegate getDelegate() {
        return delegate;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (delegate != null && delegate.getWidthHeightEqualEnable() && getWidth() > 0 && getHeight() > 0) {
            int max = Math.max(getWidth(), getHeight());
            int measureSpec = MeasureSpec.makeMeasureSpec(max, MeasureSpec.EXACTLY);
            super.onMeasure(measureSpec, measureSpec);
            return;
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (delegate != null) {
            if (delegate.getRadiusHalfHeightEnable()) {
                delegate.setRadius(getHeight() / 2);
            }
            delegate.init();
        }
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (delegate != null)
            delegate.setSelected(selected);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (delegate != null) {
            delegate.init();
        }
    }
}

package com.meis.widget.horizontal;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by wenshi on 2018/12/5.
 * Description tag 判定是否拦截与分发事件
 */
public class InterceptViewPager extends ViewPager {

    private static final String TAG_DISPATCH = "viewpager_dispatch";

    private boolean mInterceptEvent = true;

    public InterceptViewPager(Context context) {
        this(context, null);
    }

    public InterceptViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mInterceptEvent = !childInterceptEvent(this, (int) ev.getRawX(), (int) ev.getRawY());
                break;
        }
        return mInterceptEvent ? super.onInterceptTouchEvent(ev) : false;
    }

    // 遍历树
    private boolean childInterceptEvent(ViewGroup parentView, int touchX, int touchY) {
        boolean isConsume = false;
        for (int i = parentView.getChildCount() - 1; i >= 0; i--) {
            View childView = parentView.getChildAt(i);
            if (!childView.isShown()) {
                continue;
            }
            boolean isTouchView = isTouchView(touchX, touchY, childView);
            if (isTouchView && childView.getTag() != null && TAG_DISPATCH.equals(childView.getTag().toString())) {
                isConsume = true;
                break;
            }
            if (childView instanceof ViewGroup) {
                ViewGroup itemView = (ViewGroup) childView;
                if (!isTouchView) {
                    continue;
                } else {
                    isConsume |= childInterceptEvent(itemView, touchX, touchY);
                    if (isConsume) {
                        break;
                    }
                }
            }
        }
        return isConsume;
    }

    private boolean isTouchView(int touchX, int touchY, View view) {
        Rect rect = new Rect();
        view.getGlobalVisibleRect(rect);
        return rect.contains(touchX, touchY);
    }
}

package com.demo.widget.meis;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.demo.widget.R;
import com.meis.widget.xiaohongshu.CoordinatorLinearLayout;
import com.meis.widget.xiaohongshu.CoordinatorRecyclerView;
import com.meis.widget.xiaohongshu.MCropImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wenshi on 2019/3/1.
 * Description
 */
public class MeiCropImageActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    MCropImageView mMCropImageView;
    CoordinatorRecyclerView mRecyclerView;
    MyAdapter mMyAdapter;
    CoordinatorLinearLayout mCoordinatorLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mei_crop_activity);
        
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mMCropImageView = findViewById(R.id.crop_view);
        mRecyclerView = findViewById(R.id.recycler);
        mCoordinatorLayout = findViewById(R.id.coordinator_layout);

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));

        mRecyclerView.setAdapter(mMyAdapter = new MyAdapter());

        List<Integer> datas = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            datas.add(i % 2);
        }
        mMyAdapter.setNewData(datas);

        // 实现回调接口
        mRecyclerView.setOnCoordinatorListener(new CoordinatorRecyclerView.OnCoordinatorListener() {
            @Override
            public void onScroll(float y, float deltaY, int maxParentScrollRange) {
                mCoordinatorLayout.onScroll(y, deltaY, maxParentScrollRange);
            }

            @Override
            public void onFiling(int velocityY) {
                mCoordinatorLayout.onFiling(velocityY);
            }

            @Override
            public void handlerInvalidClick(int rawX, int rawY) {
                handlerRecyclerInvalidClick(mRecyclerView, rawX, rawY);
            }
        });

        mCoordinatorLayout.setOnScrollListener(new CoordinatorLinearLayout.OnScrollListener() {
            @Override
            public void onScroll(int scrollY) {
                mRecyclerView.setCurrentParenScrollY(scrollY);
            }

            @Override
            public void isExpand(boolean isExpand) {
                mRecyclerView.setExpand(isExpand);
            }

            @Override
            public void completeExpand() {
                mRecyclerView.resetRecyclerHeight();
            }
        });
    }

    /**
     * @param recyclerView
     * @param touchX
     * @param touchY
     */
    public void handlerRecyclerInvalidClick(RecyclerView recyclerView, int touchX, int touchY) {
        if (recyclerView != null && recyclerView.getChildCount() > 0) {
            for (int i = 0; i < recyclerView.getChildCount(); i++) {
                View childView = recyclerView.getChildAt(i);
                if (childView != null) {
                    if (childView != null && isTouchView(touchX, touchY, childView)) {
                        childView.performClick();
                        return;
                    }
                }
            }
        }
    }

    // 触摸点是否view区域内 parent.isPointInChildBounds(child, x, y)
    private boolean isTouchView(int touchX, int touchY, View view) {
        Rect rect = new Rect();
        view.getGlobalVisibleRect(rect);
        return rect.contains(touchX, touchY);
    }

    public class MyAdapter extends BaseQuickAdapter<Integer, BaseViewHolder> {

        public MyAdapter() {
            super(R.layout.item_coordinator_layout, new ArrayList<Integer>());
        }

        @Override
        protected void convert(final BaseViewHolder helper, final Integer item) {
            helper.setImageResource(R.id.iv, item == 0 ? R.mipmap.ic_gril2 : R.mipmap.ic_gril);
            helper.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 如果获取图片地址 请调用setImagePath方法
                    mMCropImageView.setImageRes(item == 0 ? R.mipmap.ic_gril2 : R.mipmap.ic_gril);
                    Log.e("CoordinatorRecyclerView", "==onClick==" + v.getId());
                    Toast.makeText(v.getContext(), "click position = " + helper.getAdapterPosition(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}

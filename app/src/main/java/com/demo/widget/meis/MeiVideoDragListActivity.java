package com.demo.widget.meis;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.demo.widget.R;
import com.demo.widget.bean.SmallVideoBean;
import com.demo.widget.event.ScrollToPositionEvent;
import com.meis.widget.utils.DensityUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wenshi on 2018/5/29.
 * Description
 */
public class MeiVideoDragListActivity extends AppCompatActivity {

    Toolbar mToolbar;
    RecyclerView mRecyclerView;
    BaseQuickAdapter mAdapter;
    List<SmallVideoBean> mData = new ArrayList<>();
    int mClickPosition = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mei_video_list_activity);
        mRecyclerView = findViewById(R.id.recycler);

        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mRecyclerView.setAdapter(mAdapter = new BaseQuickAdapter<SmallVideoBean, BaseViewHolder>(R.layout.mei_video_drag_item, mData = getData()) {

            @Override
            protected void convert(BaseViewHolder helper, SmallVideoBean item) {
                helper.addOnClickListener(R.id.iv_bg);
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone((ConstraintLayout) helper.itemView);
                constraintSet.setDimensionRatio(R.id.iv_bg, "H," + DensityUtil.getScreenSize(mContext).x + ":" + DensityUtil.getScreenSize(mContext).y);
                constraintSet.applyTo((ConstraintLayout) helper.itemView);

                helper.getView(R.id.iv_bg).setBackgroundResource(R.mipmap.ic_video_drag_bg);
                helper.setText(R.id.tv_name, item.title + helper.getAdapterPosition());
            }
        });
        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                int pos = parent.getChildAdapterPosition(view);
                outRect.top = (pos / 2 == 0) ? 0 : 2;
                if (pos % 2 == 0) {
                    outRect.right = 1;
                } else {
                    outRect.left = 1;
                }
            }
        });
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, final int position) {
                mClickPosition = position;
                Intent intent = new Intent(MeiVideoDragListActivity.this, MeiVideoDragActivity.class);
                Rect globalRect = new Rect();
                view.getGlobalVisibleRect(globalRect);
                // 设置转场信息  传值根据具体需求而定
                intent.putExtra("region", new int[]{globalRect.left, globalRect.top, globalRect.right, globalRect.bottom, view.getWidth(), view.getHeight()});
                intent.putExtra("video_url", mData.get(position % 3).video_url);
                intent.putExtra("position", position);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
        mAdapter.bindToRecyclerView(mRecyclerView);
    }

    private List<SmallVideoBean> getData() {
        List<SmallVideoBean> datas = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            SmallVideoBean bean = new SmallVideoBean();
            bean.title = getResources().getStringArray(R.array.small_video_title)[i % 3];
            bean.cover_url = getResources().getStringArray(R.array.small_video_cover_url)[i % 3];
            bean.video_url = getResources().getStringArray(R.array.small_video_video_url)[i % 3];
            datas.add(bean);
        }
        return datas;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(final ScrollToPositionEvent event) {
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                moveToPosition(event.position);
                View childView = mAdapter.getViewByPosition(event.position, R.id.iv_bg);
                if (childView != null && event.listener != null) {
                    Rect rect = new Rect();
                    childView.getGlobalVisibleRect(rect);
                    event.listener.onRegion(rect.left, rect.top, rect.right, rect.bottom, childView.getWidth(), childView.getHeight());
                }
            }
        }, event.delayEnable ? event.delayDuration : 0);
    }

    // 列表滚动到指定位置
    private void moveToPosition(int n) {
        if (mRecyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
            int firstItem = linearLayoutManager.findFirstVisibleItemPosition();
            int lastItem = linearLayoutManager.findLastVisibleItemPosition();
            // 然后区分情况
            if (n <= firstItem) {
                // 当要置顶的项在当前显示的第一个项的前面时
                int top = mRecyclerView.getChildAt(0).getTop();
                mRecyclerView.scrollBy(0, top);
            } else if (n <= lastItem) {
                // 当要置顶的项已经在屏幕上显示时
                mRecyclerView.scrollToPosition(n);
            } else {
                // 当要置顶的项在当前显示的最后一项的后面时
                mRecyclerView.scrollToPosition(n);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}

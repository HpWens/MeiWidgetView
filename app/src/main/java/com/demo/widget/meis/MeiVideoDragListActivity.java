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
import com.demo.widget.event.ScrollTopEvent;
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
                intent.putExtra("global_rect", new int[]{globalRect.left, globalRect.top, globalRect.right, globalRect.bottom, view.getHeight()});
                intent.putExtra("video_url", mData.get(position % 3).video_url);
                intent.putExtra("video_index", position);
                //判定点击的是不是最后一行的item
                intent.putExtra("is_last_row", mClickPosition >= mAdapter.getData().size() - 2);

                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }

    private void moveToPosition(LinearLayoutManager layoutManager, int selectedPosition) {
        int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
        int top = mRecyclerView.getChildAt(selectedPosition - firstVisiblePosition).getTop();
        mRecyclerView.scrollBy(0, top);
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
    public void onMessageEvent(ScrollTopEvent event) {
        if (event.isScroll) {
            moveToPosition((LinearLayoutManager) mRecyclerView.getLayoutManager(), mClickPosition);
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

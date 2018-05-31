package com.demo.widget.meis;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.demo.widget.R;
import com.demo.widget.bean.SmallVideoBean;
import com.meis.widget.utils.DensityUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wenshi on 2018/5/29.
 * Description
 */
public class MeiVideoDragListActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    BaseQuickAdapter mAdapter;
    List<SmallVideoBean> mData = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mei_video_list_activity);
        mRecyclerView = findViewById(R.id.recycler);


        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mRecyclerView.setAdapter(mAdapter = new BaseQuickAdapter<SmallVideoBean, BaseViewHolder>(R.layout.mei_video_drag_item, mData = getData()) {

            @Override
            protected void convert(BaseViewHolder helper, SmallVideoBean item) {
                helper.addOnClickListener(R.id.iv_bg);
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone((ConstraintLayout) helper.itemView);
                constraintSet.setDimensionRatio(R.id.iv_bg, "H," + DensityUtil.getScreenSize(mContext).x + ":" + DensityUtil.getScreenSize(mContext).y);
                constraintSet.applyTo((ConstraintLayout) helper.itemView);

                helper.getView(R.id.iv_bg).setBackgroundResource(helper.getAdapterPosition() % 3 == 0 ? R.mipmap.ic_small_video_0 : helper.getAdapterPosition() % 3 == 1
                        ? R.mipmap.ic_small_video_1 : R.mipmap.ic_small_video_2);
                helper.setText(R.id.tv_name, item.title);
            }
        });
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                Intent intent = new Intent(MeiVideoDragListActivity.this, MeiVideoDragActivity.class);
                Rect globalRect = new Rect();
                view.getGlobalVisibleRect(globalRect);
                intent.putExtra("global_rect", new int[]{globalRect.left, globalRect.top, globalRect.right, globalRect.bottom, view.getHeight()});
                intent.putExtra("video_url", mData.get(position % 3).video_url);
                intent.putExtra("video_index", position);

                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
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
}

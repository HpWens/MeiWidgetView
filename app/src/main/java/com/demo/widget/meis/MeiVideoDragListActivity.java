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
    List<String> mData = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mei_video_list_activity);
        mRecyclerView = findViewById(R.id.recycler);


        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mRecyclerView.setAdapter(mAdapter = new BaseQuickAdapter(R.layout.mei_video_drag_item, mData = getData()) {

            @Override
            protected void convert(BaseViewHolder helper, Object item) {
                helper.addOnClickListener(R.id.iv_bg);
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone((ConstraintLayout) helper.itemView);
                constraintSet.setDimensionRatio(R.id.iv_bg, "H," + DensityUtil.getScreenSize(mContext).x + ":" + DensityUtil.getScreenSize(mContext).y);
                constraintSet.applyTo((ConstraintLayout) helper.itemView);
            }
        });
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                Intent intent = new Intent(MeiVideoDragListActivity.this, MeiVideoDragActivity.class);
                Rect globalRect = new Rect();
                view.getGlobalVisibleRect(globalRect);
                intent.putExtra("global_rect", new int[]{globalRect.left, globalRect.top, globalRect.right, globalRect.bottom, view.getHeight()});
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }

    private List<String> getData() {
        List<String> datas = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            datas.add("");
        }
        return datas;
    }
}

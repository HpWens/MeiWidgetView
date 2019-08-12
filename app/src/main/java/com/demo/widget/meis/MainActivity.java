package com.demo.widget.meis;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.demo.widget.R;
import com.demo.widget.adapter.StackLayoutMangerAdapter;
import com.demo.widget.bean.IntentBean;
import com.meis.widget.manager.StackLayoutManager;

import java.util.ArrayList;
import java.util.List;

/**
 * desc:
 * author: wens
 * date: 2018/4/30.
 */
public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    // private MeiViewAdapter mMeiViewAdapter;
    private StackLayoutMangerAdapter mAdapter;
    private StackLayoutManager mStackLayoutManager;

    private Class[] targetArray = {
            MeiCornersGifActivity.class,
            MeiSpiderWebActivity.class,
            MeiRandomDragTagActivity.class,
            MeiCropImageActivity.class,
            MeiScrollParallaxActivity.class,
            MeiHeartActivity.class,
            MeiRadiusActivity.class,
            MeiPhotoDragActivity.class,
            MeiVideoDragListActivity.class,
            MeiMoBikeActivity.class,
            MeiRoseActivity.class,
            MeiFireflyActivity.class,
            MeiPraiseActivity.class,
            MeiEvaporateActivity.class,
            MeiScrollViewActivity.class,
            MeiTextPathActivity.class,
            MeiStackLayoutManagerActivity.class};
    private String[] nameArray = {
            "GIF圆角",
            "五彩蛛网",
            "小红书\n标签",
            "小红书\n图片裁剪",
            "滚动视差",
            "直播间\n送爱心",
            "shape控件集",
            "仿百度\n图片拖拽",
            "仿头条视频\n拖拽控件",
            "仿摩拜单车\n贴纸动画效果",
            "LOVE 玫瑰",
            "浮动粒子",
            "直播间点赞",
            "跳动的文本",
            "豆瓣弹性\n滑动卡片",
            "文字路径",
            "自定义\nLayoutManager"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meis_activity);
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mRecyclerView = findViewById(R.id.recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // mRecyclerView.setAdapter(mMeiViewAdapter = new MeiViewAdapter());
        // mMeiViewAdapter.setNewData(getData());

        mRecyclerView.setLayoutManager(mStackLayoutManager = new StackLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter = new StackLayoutMangerAdapter(nameArray, new StackLayoutMangerAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, final int position) {
                int focusPosition = mStackLayoutManager.findShouldSelectPosition();
                if (focusPosition == position) {
                    startActivity(new Intent(MainActivity.this, targetArray[position]));
                } else {
                    mStackLayoutManager.smoothScrollToPosition(position, new StackLayoutManager.OnStackListener() {
                        @Override
                        public void onFocusAnimEnd() {
                            startActivity(new Intent(MainActivity.this, targetArray[position]));
                        }
                    });
                }
            }
        }));
    }

    public List<IntentBean> getData() {
        List<IntentBean> beanList = new ArrayList<>();
        for (int i = 0; i < targetArray.length; i++) {
            IntentBean bean = new IntentBean();
            bean.targetClass = targetArray[i];
            bean.name = nameArray[i];
            beanList.add(bean);
        }
        return beanList;
    }
}

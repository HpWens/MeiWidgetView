package com.demo.widget.meis;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.demo.widget.R;
import com.demo.widget.adapter.MeiViewAdapter;
import com.demo.widget.bean.IntentBean;

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
    private MeiViewAdapter mMeiViewAdapter;

    private Class[] targetArray = {
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
            MeiScrollViewActivity.class};
    private String[] nameArray = {
            "滚动视差",
            "直播间送爱心",
            "shape控件集",
            "仿百度浏览器图片拖拽",
            "仿头条视频拖拽控件",
            "仿摩拜单车贴纸动画效果",
            "LOVE 玫瑰",
            "浮动粒子",
            "直播间点赞",
            "跳动的文本",
            "豆瓣弹性滑动卡片"};

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

        mRecyclerView.setAdapter(mMeiViewAdapter = new MeiViewAdapter());
        mMeiViewAdapter.setNewData(getData());
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

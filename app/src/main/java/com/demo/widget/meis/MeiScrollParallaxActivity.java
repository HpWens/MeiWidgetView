package com.demo.widget.meis;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.demo.widget.R;

import java.util.ArrayList;
import java.util.List;

/**
 * desc:
 * author: wens
 * date: 2018/4/30.
 */
public class MeiScrollParallaxActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private MeiScrollParallaxAdapter mAdapter;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scroll_parallax_activity);

        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mRecyclerView = findViewById(R.id.recycler);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter = new MeiScrollParallaxAdapter());

        List<String> mData = new ArrayList<>();
        for (int i = 0; i < 66; i++) {
            mData.add("");
        }
        mAdapter.setNewData(mData);
    }
}

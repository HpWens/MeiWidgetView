package com.demo.widget.meis;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.demo.widget.R;
import com.meis.widget.praise.BezierPraiseAnimator;

/**
 * Created by wenshi on 2018/7/11.
 * Description
 */
public class MeiPraiseActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageView mIvPraise;
    private BezierPraiseAnimator mPraiseAnimator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mei_praise_activity);

        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mPraiseAnimator = new BezierPraiseAnimator(this);
        mIvPraise = findViewById(R.id.iv_praise);
        mIvPraise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPraiseAnimator.startAnimation(mIvPraise);
            }
        });
    }
}

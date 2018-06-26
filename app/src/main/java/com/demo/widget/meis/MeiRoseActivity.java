package com.demo.widget.meis;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.demo.widget.R;
import com.meis.widget.rose.RoseGiftSurfaceView;

/**
 * Created by wenshi on 2018/6/26.
 * Description
 */
public class MeiRoseActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RoseGiftSurfaceView mRoseGiftSurfaceView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mei_rose_activity);

        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mRoseGiftSurfaceView = findViewById(R.id.rose);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        mRoseGiftSurfaceView.startAnimation();
    }
}

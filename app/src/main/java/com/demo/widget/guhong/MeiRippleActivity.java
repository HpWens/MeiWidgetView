package com.demo.widget.guhong;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.demo.widget.R;
import com.demo.widget.utils.Eyes;
import com.meis.widget.MeiRippleView;

/**
 * desc:
 * author: wens
 * date: 2018/4/29.
 */
public class MeiRippleActivity extends AppCompatActivity {

    private FloatingActionButton mFloat;
    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;
    private TextView mTextView;
    private ImageView mImageView;

    private int[] mColors = new int[]{Color.BLUE, Color.RED, Color.GREEN, Color.LTGRAY, Color.YELLOW, Color.DKGRAY, Color.CYAN};
    private int mCount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mei_ripple_activity);
        Eyes.translucentStatusBar(this, true);

        mFloat = findViewById(R.id.fab);
        mAppBarLayout = findViewById(R.id.app_bar);
        mToolbar = findViewById(R.id.toolbar);
        mTextView = findViewById(R.id.tv_content);
        mImageView = findViewById(R.id.iv_content);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void onCheck(View view) {
        MeiRippleView rippleView = new MeiRippleView(this);
        int location[] = new int[2];
        view.getLocationOnScreen(location);
        rippleView.startRipple(location[0] + view.getWidth() / 2, location[1] + view.getHeight() / 2,
                Math.min(view.getWidth() / 2, view.getHeight() / 2));

        updateColor(mColors[mCount % mColors.length]);
        mCount++;
    }

    private void updateColor(int color) {
        mAppBarLayout.setBackgroundColor(color);
        mTextView.setTextColor(color);
        //mToolbar.setTitleTextColor(color);
        mFloat.setBackgroundColor(color);
        mImageView.setBackgroundColor(color);
    }
}

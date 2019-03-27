package com.demo.widget.meis;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.demo.widget.R;
import com.meis.widget.spiderweb.SpiderWebView;

/**
 * Created by wenshi on 2019/3/27.
 * Description
 */
public class MeiSpiderWebActivity extends AppCompatActivity implements View.OnTouchListener, SeekBar.OnSeekBarChangeListener {

    private SpiderWebView mSpiderWebView;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar mToolbar;
    private SeekBar mSeekbar1;
    private SeekBar mSeekbar2;
    private SeekBar mSeekbar3;
    private SeekBar mSeekbar4;
    private TextView mTextView1;
    private TextView mTextView2;
    private TextView mTextView3;
    private TextView mTextView4;
    private TextView mTextView5;
    private TextView mTextView6;
    private TextView mTextView7;
    private TextView mTextView8;
    private SeekBar mSeekbar5;
    private SeekBar mSeekbar6;
    private SeekBar mSeekbar7;
    private SeekBar mSeekbar8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sipder_web);

        initView();
        initListener();
        mSeekbar1.setProgress(49);
        mSeekbar2.setProgress(4);
        mSeekbar3.setProgress(250);
        mSeekbar4.setProgress(150);
        mSeekbar5.setProgress(2);
        mSeekbar6.setProgress(1);
        mSeekbar7.setProgress(1);
        mSeekbar8.setProgress(50);
    }

    private void initView() {
        mSpiderWebView = (SpiderWebView) findViewById(R.id.cob_web_view);
        mTextView1 = (TextView) findViewById(R.id.textview1);
        mTextView2 = (TextView) findViewById(R.id.textview2);
        mTextView3 = (TextView) findViewById(R.id.textview3);
        mTextView4 = (TextView) findViewById(R.id.textview4);
        mTextView5 = (TextView) findViewById(R.id.textview5);
        mTextView6 = (TextView) findViewById(R.id.textview6);
        mTextView7 = (TextView) findViewById(R.id.textview7);
        mTextView8 = (TextView) findViewById(R.id.textview8);
        mSeekbar1 = (SeekBar) findViewById(R.id.seekbar1);
        mSeekbar2 = (SeekBar) findViewById(R.id.seekbar2);
        mSeekbar3 = (SeekBar) findViewById(R.id.seekbar3);
        mSeekbar4 = (SeekBar) findViewById(R.id.seekbar4);
        mSeekbar5 = (SeekBar) findViewById(R.id.seekbar5);
        mSeekbar6 = (SeekBar) findViewById(R.id.seekbar6);
        mSeekbar7 = (SeekBar) findViewById(R.id.seekbar7);
        mSeekbar8 = (SeekBar) findViewById(R.id.seekbar8);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerToggle.syncState();
    }

    private void initListener() {
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_item1:
                        mSpiderWebView.restart();
                        return true;
                }
                return false;
            }
        });
        mSeekbar1.setOnTouchListener(this);
        mSeekbar2.setOnTouchListener(this);
        mSeekbar3.setOnTouchListener(this);
        mSeekbar4.setOnTouchListener(this);
        mSeekbar5.setOnTouchListener(this);
        mSeekbar6.setOnTouchListener(this);
        mSeekbar7.setOnTouchListener(this);
        mSeekbar8.setOnTouchListener(this);
        mSeekbar1.setOnSeekBarChangeListener(this);
        mSeekbar2.setOnSeekBarChangeListener(this);
        mSeekbar3.setOnSeekBarChangeListener(this);
        mSeekbar4.setOnSeekBarChangeListener(this);
        mSeekbar5.setOnSeekBarChangeListener(this);
        mSeekbar6.setOnSeekBarChangeListener(this);
        mSeekbar7.setOnSeekBarChangeListener(this);
        mSeekbar8.setOnSeekBarChangeListener(this);
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                mDrawerToggle.onDrawerSlide(drawerView, slideOffset);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                mDrawerToggle.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                mDrawerToggle.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                mSpiderWebView.resetTouchPoint();
                mDrawerToggle.onDrawerStateChanged(newState);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);//加载menu文件到布局
        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v instanceof SeekBar)
            mDrawerLayout.requestDisallowInterceptTouchEvent(true);
        return false;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.seekbar1:
                mSpiderWebView.setPointNum(progress + 1);
                mTextView1.setText(String.format("小点数量： %d", mSpiderWebView.getPointNum()));
                break;
            case R.id.seekbar2:
                mSpiderWebView.setPointAcceleration(progress + 3);
                mTextView2.setText(String.format("加速度： %d", mSpiderWebView.getPointAcceleration()));
                break;
            case R.id.seekbar3:
                mSpiderWebView.setMaxDistance(progress + 20);
                mTextView3.setText(String.format("最大连线距离： %d", mSpiderWebView.getMaxDistance()));
                break;
            case R.id.seekbar4:
                mSpiderWebView.setLineAlpha(progress);
                mTextView4.setText(String.format("连线透明度： %d", mSpiderWebView.getLineAlpha()));
                break;
            case R.id.seekbar5:
                mSpiderWebView.setLineWidth(progress);
                mTextView5.setText(String.format("连线粗细： %d", mSpiderWebView.getLineWidth()));
                break;
            case R.id.seekbar6:
                mSpiderWebView.setPointRadius(progress);
                mTextView6.setText(String.format("小点半径： %d", mSpiderWebView.getPointRadius()));
                break;
            case R.id.seekbar7:
                mSpiderWebView.setTouchPointRadius(progress);
                mTextView7.setText(String.format("触摸点半径： %d", mSpiderWebView.getTouchPointRadius()));
                break;
            case R.id.seekbar8:
                mSpiderWebView.setGravitation_strength(progress);
                mTextView8.setText(String.format("引力强度： %d", mSpiderWebView.getGravitation_strength()));
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}

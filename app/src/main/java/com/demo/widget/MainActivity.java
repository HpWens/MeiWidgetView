package com.demo.widget;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.demo.widget.adapter.StackLayoutMangerAdapter;
import com.meis.widget.manager.StackLayoutManager;
import com.meis.widget.mobike.MoBikeView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private MoBikeView mMobikeView;

    private SensorManager mSensorManager;
    private Sensor mSensor;

    private int[] mImgs = {
            R.mipmap.ic_share_fb,
            R.mipmap.ic_share_kongjian,
            R.mipmap.ic_share_pyq,
            R.mipmap.ic_share_qq,
            R.mipmap.ic_share_tw,
            R.mipmap.ic_share_wechat,
            R.mipmap.ic_share_weibo
    };

    private RecyclerView mRecyclerView;
    private StackLayoutMangerAdapter mAdapter;
    private StackLayoutManager mStackLayoutManager;
    private String[] mTitles = {
            "MEI控件",
            "郭鸿控件",
            "关于我",
    };
    private Class[] mJumpActivities = {
            com.demo.widget.meis.MainActivity.class,
            com.demo.widget.guhong.MainActivity.class,
            AboutActivity.class,};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mMobikeView = findViewById(R.id.mo_bike);
        mRecyclerView = findViewById(R.id.recycler);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        addViews();

        mMobikeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMobikeView.onRandomChanged();
            }
        });

        mRecyclerView.setLayoutManager(mStackLayoutManager = new StackLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter = new StackLayoutMangerAdapter(mTitles, new StackLayoutMangerAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, final int position) {
                int focusPosition = mStackLayoutManager.findShouldSelectPosition();
                if (focusPosition == position) {
                    startActivity(new Intent(MainActivity.this, mJumpActivities[position]));
                } else {
                    mStackLayoutManager.smoothScrollToPosition(position, new StackLayoutManager.OnStackListener() {
                        @Override
                        public void onFocusAnimEnd() {
                            startActivity(new Intent(MainActivity.this, mJumpActivities[position]));
                        }
                    });
                }
            }
        }));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    private void addViews() {
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        for (int i = 0; i < mImgs.length; i++) {
            ImageView iv = new ImageView(this);
            iv.setImageResource(mImgs[i]);
            iv.setTag(R.id.wd_view_circle_tag, true);
            mMobikeView.addView(iv, lp);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1] * 2.0f;
            mMobikeView.onSensorChanged(-x, y);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void onMei(View view) {
        startActivity(new Intent(this, com.demo.widget.meis.MainActivity.class));
    }

    public void onGuHong(View view) {
        startActivity(new Intent(this, com.demo.widget.guhong.MainActivity.class));
    }

    public void onAbout(View view) {
        startActivity(new Intent(this, AboutActivity.class));
    }
}

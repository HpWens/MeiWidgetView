package com.demo.widget.meis;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.demo.widget.R;
import com.demo.widget.utils.Eyes;
import com.meis.widget.particle.FireflyView;

/**
 * Created by wenshi on 2018/7/5.
 * Description 浮动粒子界面
 */
public class MeiFireflyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mei_fire_fly_activity);
        Eyes.translucentStatusBar(this, true);
    }
}

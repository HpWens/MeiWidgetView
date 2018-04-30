package com.demo.widget.guhong;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.demo.widget.R;

/**
 * desc:
 * author: wens
 * date: 2018/4/30.
 */
public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gu_hong_main_activity);
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void onBounceBall(View view) {
        startActivity(new Intent(this, BounceBallActivity.class));
    }

    public void onRipple(View view) {
        startActivity(new Intent(this, MeiRippleActivity.class));
    }

    public void onLinePath(View view) {
        startActivity(new Intent(this, MeiLinePathActivity.class));
    }
}

package com.demo.widget.meis;

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
        setContentView(R.layout.meis_activity);
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void onParallax(View view) {
        startActivity(new Intent(this, MeiScrollParallaxActivity.class));
    }

    public void onLoveStar(View view) {
        startActivity(new Intent(this, MeiHeartActivity.class));
    }

    public void onRadiusView(View view) {
        startActivity(new Intent(this, MeiRadiusActivity.class));
    }

    public void onPhotoDrag(View view) {
        startActivity(new Intent(this, MeiPhotoDragActivity.class));
    }

    public void onVideoDrag(View view) {
        startActivity(new Intent(this, MeiVideoDragListActivity.class));
    }

    public void onMokibe(View view) {
        startActivity(new Intent(this, MeiMoBikeActivity.class));
    }

    public void onRose(View view) {
        startActivity(new Intent(this, MeiRoseActivity.class));
    }

    public void onFirefly(View view) {
        startActivity(new Intent(this, MeiFireflyActivity.class));
    }

    public void onPraise(View view) {
        startActivity(new Intent(this, MeiPraiseActivity.class));
    }
}

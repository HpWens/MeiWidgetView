package com.demo.widget;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.meis.widget.evaporate.EvaporateTextView;

/**
 * Created by wenshi on 2019/4/9.
 * Description
 */
public class AboutActivity extends AppCompatActivity {

    private EvaporateTextView mEvaporateTextView;
    private Toolbar mToolbar;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (index > (sentences.length - 1)) {
                index = 0;
            }

            mEvaporateTextView.animateText(sentences[index]);

            mHandler.sendEmptyMessageDelayed(1, 1000);

            index++;
        }
    };

    private int index = 0;
    private String[] sentences = {"我曾经有一个梦想", "梦想着有一天", "能够站在你的面前", "对我说", "我爱你"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);

        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mEvaporateTextView = findViewById(R.id.etv);
        // mEvaporateTextView.animateText(sentences[0]);
        mEvaporateTextView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mHandler.sendEmptyMessageDelayed(1, 0);
            }
        }, 500);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}

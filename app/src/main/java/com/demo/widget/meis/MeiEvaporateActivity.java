package com.demo.widget.meis;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.demo.widget.R;
import com.meis.widget.evaporate.EvaporateTextView;

/**
 * Created by wenshi on 2018/11/23.
 * Description
 */
public class MeiEvaporateActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EvaporateTextView mEvaporateTextView;
    private Button mBtn;
    private int index = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mei_evaporate_activity);

        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mEvaporateTextView = findViewById(R.id.etv);
        mBtn = findViewById(R.id.btn);
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (index >= (sentences.length - 1)) {
                    index = 0;
                }
                mEvaporateTextView.animateText(sentences[index++]);
            }
        });

        mEvaporateTextView.animateText("hello world");
    }

    private String[] sentences = {
            "A material",
            "metaphor is the unifying theory",
            "of a rationalized space and a system of motion",
            "material",
            "grounded",
            "in tactile reality",
            "inspired",
            "study of paper and ink",
            "understand",
            "new affordances",
            "The fundamentals of light, surface, and movement are key to conveying how objects move",
            "interact",
            "divides space",
            "fundamentals",
            "欢迎关注",
            "控件人生",
            "公众号"
    };
}

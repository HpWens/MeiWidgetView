package com.demo.widget.meis;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.demo.widget.R;
import com.meis.widget.xiaohongshu.tag.RandomDragTagLayout;
import com.meis.widget.xiaohongshu.tag.RandomDragTagView;
import com.meis.widget.xiaohongshu.tag.TagModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by wenshi on 2019/3/14.
 * Description
 */
public class MeiRandomDragTagActivity extends AppCompatActivity {

    Toolbar mToolbar;

    RandomDragTagLayout mRandomDragTagLayout;
    String mTagText = "仿小红书任意拖拽控件、欢迎关注公众号：控件人生";

    List<TagModel> mTagList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mei_drag_tag_activity);

        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mRandomDragTagLayout = findViewById(R.id.tag_layout);

        findViewById(R.id.tv_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRandomDragTagLayout.addTagView(
                        mTagText.substring(0, new Random().nextInt(mTagText.length())),
                        new Random().nextFloat(),
                        new Random().nextFloat(),
                        new Random().nextBoolean()
                );
            }
        });

        findViewById(R.id.tv_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTag();
            }
        });

        findViewById(R.id.tv_restore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restoreTag();
            }
        });

    }

    private void saveTag() {
        mTagList.clear();
        for (int i = 0; i < mRandomDragTagLayout.getChildCount(); i++) {
            View childView = mRandomDragTagLayout.getChildAt(i);
            if (childView instanceof RandomDragTagView) {
                RandomDragTagView tagView = (RandomDragTagView) childView;
                TagModel tagModel = new TagModel();
                tagModel.direction = tagView.isShowLeftView();
                tagModel.text = tagView.getTagText();
                tagModel.x = tagView.getPercentTransX();
                tagModel.y = tagView.getPercentTransY();
                mTagList.add(tagModel);
            }
        }
    }

    private void restoreTag() {
        if (!mTagList.isEmpty()) {
            mRandomDragTagLayout.removeAllViews();
            for (TagModel tagModel : mTagList) {
                mRandomDragTagLayout.addTagView(tagModel.text, tagModel.x, tagModel.y, tagModel.direction);
            }
        }
    }
}

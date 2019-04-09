package com.demo.widget.meis;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.demo.widget.R;
import com.meis.widget.corners.CornersGifView;

/**
 * Created by wenshi on 2019/4/9.
 * Description
 */
public class MeiCornersGifActivity extends AppCompatActivity {

    Toolbar mToolbar;

    CornersGifView mGifImageView;

    SeekBar mSeekBar01, mSeekBar02, mSeekBar03, mSeekBar04;
    TextView mTextView01, mTextView02, mTextView03, mTextView04;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.corners_gif_activity);

        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        mGifImageView = findViewById(R.id.iv_gif);
        mSeekBar01 = findViewById(R.id.seek_01);
        mSeekBar02 = findViewById(R.id.seek_02);
        mSeekBar03 = findViewById(R.id.seek_03);
        mSeekBar04 = findViewById(R.id.seek_04);

        mTextView01 = findViewById(R.id.tv_01);
        mTextView02 = findViewById(R.id.tv_02);
        mTextView03 = findViewById(R.id.tv_03);
        mTextView04 = findViewById(R.id.tv_04);

        Glide.with(this).load(R.mipmap.gif_01)
                .asGif()
                .override(720, 512)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(mGifImageView);

        mSeekBar01.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTextView01.setText("左上圆角：" + progress);
                mGifImageView.setLeftTopCorner(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        mSeekBar02.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTextView02.setText("左下圆角：" + progress);
                mGifImageView.setLeftBottomCorner(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        mSeekBar03.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTextView03.setText("右上圆角：" + progress);
                mGifImageView.setRightTopCorner(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSeekBar04.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTextView04.setText("右下圆角：" + progress);
                mGifImageView.setRightBottomCorner(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }
}

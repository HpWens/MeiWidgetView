package com.demo.widget.meis;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.demo.widget.R;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.meis.widget.photodrag.PhotoDragHelper;
import com.meis.widget.photodrag.PhotoDragRelativeLayout;

import me.relex.photodraweeview.PhotoDraweeView;

/**
 * Created by wenshi on 2018/5/18.
 * Description
 */
public class MeiPhotoDragActivity extends AppCompatActivity {

    Toolbar mToolbar;
    PhotoDragRelativeLayout mPdrLayout;
    PhotoDraweeView mPdvView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mei_photo_drag_activity);

        mPdrLayout = findViewById(R.id.pdr_content);
        mPdvView = findViewById(R.id.pdv_photo);
        mPdvView.setPhotoUri(Uri.parse("res://" + getPackageName() + "/" + R.mipmap.ic_mei_ripple));

        mPdrLayout.setDragListener(new PhotoDragHelper().setOnDragListener(new PhotoDragHelper.OnDragListener() {
            @Override
            public void onAlpha(float alpha) {
                mPdrLayout.setAlpha(alpha);
            }

            @Override
            public View getDragView() {
                return mPdvView;
            }

            @Override
            public void onAnimationEnd(boolean mSlop) {
                if (mSlop) {
                    finish();
                    overridePendingTransition(0, 0);
                }
            }
        }));

        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}

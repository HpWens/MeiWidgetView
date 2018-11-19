package com.demo.widget.meis;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.demo.widget.R;

import java.util.ArrayList;
import java.util.List;

import me.yokeyword.fragmentation.SupportActivity;

/**
 * Created by wenshi on 2018/5/23.
 * Description
 */
public class MeiVideoDragActivity extends SupportActivity {

    ViewPager mViewPager;
    int[] mGlobalRect = new int[5];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mei_video_drag_activity);
        // Eyes.translucentStatusBar(this, true);

        mViewPager = findViewById(R.id.view_pager);

        Intent intent = getIntent();
        if (intent != null) {
            mGlobalRect = intent.getIntArrayExtra("region");
        }

        List<Fragment> mFragments = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            MeiVideoDragFragment videoDragFragment = new MeiVideoDragFragment();
            Bundle bundle = new Bundle();
            bundle.putIntArray("region", mGlobalRect);
            bundle.putString("video_url", intent.getStringExtra("video_url"));
            bundle.putInt("index", i);
            bundle.putInt("position", intent.getIntExtra("position", 0));
            videoDragFragment.setArguments(bundle);
            mFragments.add(videoDragFragment);
        }

        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(new AdapterFragment(getSupportFragmentManager(), mFragments));
    }


    public class AdapterFragment extends FragmentPagerAdapter {
        private List<Fragment> mFragments;

        public AdapterFragment(FragmentManager fm, List<Fragment> mFragments) {
            super(fm);
            this.mFragments = mFragments;
        }

        @Override
        public Fragment getItem(int position) {//必须实现
            return mFragments.get(position);
        }

        @Override
        public int getCount() {//必须实现
            return mFragments.size();
        }

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}

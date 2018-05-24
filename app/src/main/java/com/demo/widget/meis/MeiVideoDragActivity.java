package com.demo.widget.meis;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.demo.widget.R;
import com.demo.widget.utils.Eyes;

import java.util.ArrayList;
import java.util.List;

import me.yokeyword.fragmentation.SupportActivity;

/**
 * Created by wenshi on 2018/5/23.
 * Description
 */
public class MeiVideoDragActivity extends SupportActivity {

    ViewPager mViewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mei_video_drag_activity);
        Eyes.translucentStatusBar(this, true);

        mViewPager = findViewById(R.id.view_pager);

        List<Fragment> mFragments = new ArrayList<>();

        mFragments.add(new MeiVideoDragFragment());
        mFragments.add(new MeiVideoDragFragment());
        mFragments.add(new MeiVideoDragFragment());
        mFragments.add(new MeiVideoDragFragment());

        mViewPager.setOffscreenPageLimit(0);
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
}

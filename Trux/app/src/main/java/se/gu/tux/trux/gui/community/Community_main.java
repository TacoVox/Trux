package se.gu.tux.trux.gui.community;

import android.support.v4.view.ViewPager;
import android.app.FragmentManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;


import tux.gu.se.trux.R;

public class Community_main extends FragmentActivity {

    ViewPager viewpager;
    FragmentPageAdapterCommunity ft;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewpager = new ViewPager(this);
        viewpager.setId(R.id.pager);
        setContentView(viewpager);
        ft = new FragmentPageAdapterCommunity(getSupportFragmentManager());
        viewpager.setAdapter(ft);

        viewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {


            }

            @Override
            public void onPageSelected(int arg0) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }

        });
    }
}

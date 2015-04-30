package se.gu.tux.trux.gui.simpleStats;

import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;

import se.gu.tux.trux.gui.simpleStats.FragmentPageAdapterSimpleUI;
import tux.gu.se.trux.R;

public class SimpleStats extends FragmentActivity {

    ViewPager viewpager;
    FragmentPageAdapterSimpleUI ft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewpager = new ViewPager(this);
        viewpager.setId(R.id.pager);
        setContentView(viewpager);
        ft = new FragmentPageAdapterSimpleUI(getSupportFragmentManager());
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

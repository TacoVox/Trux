package se.gu.tux.trux.gui.detailedStatistics;

import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import se.gu.tux.trux.gui.simpleStats.FragmentPageAdapterSimpleUI;
import tux.gu.se.trux.R;

public class StatsWindows extends ActionBarActivity {

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


    }

}
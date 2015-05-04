package se.gu.tux.trux.gui.community;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.Menu;
import android.view.MenuItem;

import se.gu.tux.trux.gui.BaseAppActivity;
import se.gu.tux.trux.gui.simpleStats.SimpleDistanceTraveledWindow;
import se.gu.tux.trux.gui.simpleStats.SimpleFuelWindow;
import se.gu.tux.trux.gui.simpleStats.SimpleSpeedWindow;
import tux.gu.se.trux.R;

public class FragmentPageAdapterCommunity extends FragmentPagerAdapter {

    public FragmentPageAdapterCommunity(FragmentManager fm) {
        super(fm);
    }

    public Fragment getItem(int arg0) {

        switch(arg0) {
            case 0:
                return new MapFragment();
            case 1:
                return new SimpleFuelWindow();
            case 2:
                return new SimpleDistanceTraveledWindow();
            default:
                break;
        }
        return null;

    }

    @Override
    public int getCount() {
        return 3;
    }


}

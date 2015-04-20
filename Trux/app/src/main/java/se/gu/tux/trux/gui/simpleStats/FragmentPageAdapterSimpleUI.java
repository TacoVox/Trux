package se.gu.tux.trux.gui.simpleStats;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by dennis on 2015-03-26.
 */
public class FragmentPageAdapterSimpleUI extends FragmentPagerAdapter {

    public FragmentPageAdapterSimpleUI(FragmentManager fm) {
        super(fm);
    }

    public Fragment getItem(int arg0) {

        switch(arg0) {
            case 0:
                return new SimpleSpeedWindow();
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

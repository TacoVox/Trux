package se.gu.tux.trux.gui.detailedStats;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

import se.gu.tux.trux.appplication.DetailedStatsBundle;

/**
 * Created by dennis on 2015-03-26.
 */
public class FragmentPageAdapterOverall extends FragmentPagerAdapter {
    private volatile OverallTextWindow otw = null;
    private volatile OverallGraphWindow ogw = null;

    public FragmentPageAdapterOverall(FragmentManager fm) {
        super(fm);
    }

    public OverallTextWindow getOTW() {
        if (otw == null) {
            otw = new OverallTextWindow();
        }
        return otw;
    }

    public OverallGraphWindow getOGW() {
        if (ogw == null) {
            ogw = new OverallGraphWindow();
        }
        return ogw;
    }

    public Fragment getItem(int arg0) {

        switch(arg0) {
            case 0:
                return getOTW();
            case 1:
                return getOGW();
            default:
                break;
        }
        return null;

    }

    @Override
    public int getCount() {
        return 2;
    }
}

package se.gu.tux.trux.gui.detailedStats;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by dennis on 2015-03-26.
 */
public class FragmentPageAdapterOverall extends FragmentPagerAdapter {

    public FragmentPageAdapterOverall(FragmentManager fm) {
        super(fm);
    }

    public Fragment getItem(int arg0) {

        switch(arg0) {
            case 0:
                return new OverallTextWindow();
            case 1:
                return new OverallGraphWindow();
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

package se.gu.tux.trux.gui.detailedStatistics;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;




/**
 * Created by Niklas on 20/04/15.
 */
public class FragmentPageAdapterDetailedUI extends FragmentPagerAdapter {

    public FragmentPageAdapterDetailedUI(FragmentManager fm) {
        super(fm);
    }

    public Fragment getItem(int arg0) {

        switch(arg0) {
            case 0:
                return new SpeedWindow();
            case 1:
                return new FuelWindow();
            case 2:
                return new DistTravWindow();
            default:
                break;

        }
        return null;

    }

    @Override
    public int getCount() {
        return 1;
    }

}

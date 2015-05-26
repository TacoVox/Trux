package se.gu.tux.trux.gui.main_home;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

import se.gu.tux.trux.gui.statistics.StatisticsMainFragment;

/**
 * Handles the fragments in HomeActivity.
 */
public class HomePagerAdapter extends FragmentStatePagerAdapter
{

    // the fragments to display
    private ArrayList<Fragment> fragmentArrayList;


    /**
     * Constructor.
     *
     * @param fm            The fragment manager.
     */
    public HomePagerAdapter(FragmentManager fm)
    {
        // call super
        super(fm);
    }


    /**
     * The fragments are initialized once for each item and then the viewPager will manage the
     * already created fragments.
     *
     * @param position  The position of the requested fragment
     * @return          The fragment at the selected position
     */
    @Override
    public Fragment getItem(int position)
    {
        switch (position) {
            case 0:
                // Welcome screen
                return new WelcomeMainFragment();
            case 1:
                // Map
                return new CommunityMainFragment();
            case 2:
                // Statistics
                return new StatisticsMainFragment();
        }
        // Should never happen
        return null;
    }


    @Override
    public int getCount()
    {
        return 3;
    }


} // end class

package se.gu.tux.trux.gui.main_home;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the display of the fragments in main activity.
 */
public class HomePagerAdapter extends FragmentStatePagerAdapter
{

    // the fragments to display
    private ArrayList<Fragment> fragmentArrayList;


    /**
     * Constructor. Takes the fragment manager to use and a list with
     * the fragments to display.
     *
     * @param fm            The fragment manager.
     * @param arrayList     The fragments to display.
     */
    public HomePagerAdapter(FragmentManager fm, List<Fragment> arrayList)
    {
        // call super
        super(fm);
        // get the fragments to display
        fragmentArrayList = (ArrayList<Fragment>) arrayList;
    }


    @Override
    public Fragment getItem(int position)
    {
        return fragmentArrayList.get(position);
    }


    @Override
    public int getCount()
    {
        return fragmentArrayList.size();
    }


} // end class

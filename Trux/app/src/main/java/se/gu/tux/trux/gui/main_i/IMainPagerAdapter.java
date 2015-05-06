package se.gu.tux.trux.gui.main_i;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ivryashkov on 2015-05-05.
 *
 * Handles the display of the fragments in main activity.
 */
public class IMainPagerAdapter extends FragmentStatePagerAdapter
{

    private static ArrayList<Fragment> fragmentArrayList;


    public IMainPagerAdapter(FragmentManager fm, List<Fragment> arrayList)
    {
        super(fm);
        fragmentArrayList = (ArrayList) arrayList;
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

package se.gu.tux.trux.gui.main_i;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

/**
 * Created by ivryashkov on 2015-05-05.
 */
public class IMainPagerAdapter extends FragmentStatePagerAdapter
{

    ArrayList<Fragment> fragmentArrayList;


    public IMainPagerAdapter(FragmentManager fm, ArrayList<Fragment> arrayList)
    {
        super(fm);
        fragmentArrayList = arrayList;
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

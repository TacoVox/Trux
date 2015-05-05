package se.gu.tux.trux.gui.main_i;

import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;

import java.util.ArrayList;

import se.gu.tux.trux.gui.BaseAppActivity;
import se.gu.tux.trux.gui.statistics_i.IStatisticsActivity;
import tux.gu.se.trux.R;

/**
 * Created by ivryashkov on 2015-05-05.
 *
 * Handles the main activity and screens.
 */
@SuppressWarnings("deprecation")
public class IMainActivity extends BaseAppActivity implements ActionBar.TabListener
{

    // constants
    private static final int LAYOUT_ID = R.layout.activity_main_i;
    private static final int STATS_BUTTON = R.id.fm_i_statistics_check_stats_button;


    IMainPagerAdapter pagerAdapter;
    ViewPager viewPager;

    ArrayList<Fragment> fragmentArrayList;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // set layout for this activity
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT_ID);

        // set current view showing
        setCurrentViewId(LAYOUT_ID);

        // get the pager
        viewPager = (ViewPager) findViewById(R.id.activity_main_i_container);

        // initialise array
        fragmentArrayList = new ArrayList<>();

        // create fragments
        Fragment welcomeFragment = new IWelcomeFragment();
        Fragment communityFragment = new ICommunityFragment();
        Fragment statsFragment = new IStatisticsFragment();

        // add fragments to array
        fragmentArrayList.add(welcomeFragment);
        fragmentArrayList.add(communityFragment);
        fragmentArrayList.add(statsFragment);

        // set adapter and view pager
        pagerAdapter = new IMainPagerAdapter(getSupportFragmentManager(), fragmentArrayList);

        final ActionBar actionBar = getSupportActionBar();

        // Specify that we will be displaying tabs in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        viewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the
                        // corresponding tab.
                        actionBar.setSelectedNavigationItem(position);
                    }
                });

        viewPager.setAdapter(pagerAdapter);

        actionBar.addTab(actionBar.newTab().setCustomView(R.layout.tab_home).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setCustomView(R.layout.tab_community).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setCustomView(R.layout.tab_statistics).setTabListener(this));

    }



    public void onFragmentViewClick(int id)
    {
        if (id == STATS_BUTTON)
        {
            Intent intent = new Intent(this, IStatisticsActivity.class);
            startActivity(intent);
        }
        else
        {
            showToast("Something is wrong. Called from IMainActivity.class.");
        }
    }


    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction)
    {
        // show the given tab
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}


} // end class

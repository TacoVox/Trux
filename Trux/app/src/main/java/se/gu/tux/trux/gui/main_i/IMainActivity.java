package se.gu.tux.trux.gui.main_i;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;

import java.util.ArrayList;
import java.util.List;

import se.gu.tux.trux.gui.BaseAppActivity;
import se.gu.tux.trux.gui.statistics_i.IStatisticsActivity;
import tux.gu.se.trux.R;

/**
 * Created by ivryashkov on 2015-05-05.
 *
 * Handles the main activity and screens.
 */
@SuppressWarnings("deprecation")
public class IMainActivity extends BaseAppActivity implements ActionBar.TabListener, ViewPager.OnPageChangeListener
{

    // constants
    private static final int LAYOUT_ID = R.layout.activity_main_i;
    private static final int STATS_BUTTON = R.id.fm_i_statistics_check_stats_button;


    IMainPagerAdapter pagerAdapter;
    ViewPager viewPager;

    List<Fragment> fragmentArrayList;
    static ActionBar actionBar;


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

        // get action bar
        actionBar = getSupportActionBar();

        // specify that we will be displaying tabs in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.WHITE));

        // set page listener
        viewPager.setOnPageChangeListener(this);
        // set adapter
        viewPager.setAdapter(pagerAdapter);

        // adding tabs here for now
        // TODO: create tabs in separate method
        actionBar.addTab(actionBar.newTab().setCustomView(R.layout.tab_home).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setCustomView(R.layout.tab_community).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setCustomView(R.layout.tab_statistics).setTabListener(this));

    }


    /**
     * Handles calls from child fragments components.
     *
     * For now maybe this is better since we don't have many components in the main
     * activity (UI view). In total perhaps we will have 4-5 fragments * 2-4 components
     * worst-case = 20 -- which is not so bad, considering not all calls
     * will be important maybe we get around 10-12 expected calls --> then it is better
     * to handle calls here (as a wrap up)
     *
     * TODO: discuss this ^
     *
     * TODO: handle calls
     *
     * @param id    The view id.
     */
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


    /******************************************************************************
     *
     * Override methods below required by implemented interfaces TabListener and
     * OnPageChangeListener. Do not insert outside methods in between.
     *
     */
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


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}


    @Override
    public void onPageSelected(int position)
    {
        // when swiping between pages, select the
        // corresponding tab.
        actionBar.setSelectedNavigationItem(position);
    }


    @Override
    public void onPageScrollStateChanged(int state) {}

    /********************************************************************************
     *
     * End override methods.
     *
     */



} // end class

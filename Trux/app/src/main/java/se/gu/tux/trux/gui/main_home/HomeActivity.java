package se.gu.tux.trux.gui.main_home;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.widget.Button;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.datastructure.Notification;
import se.gu.tux.trux.datastructure.Speed;
import se.gu.tux.trux.gui.base.BaseAppActivity;
import se.gu.tux.trux.gui.community.CommunityProfileActivity;
import se.gu.tux.trux.gui.community.FriendsWindow;
import se.gu.tux.trux.gui.messaging.MessageActivity;
import se.gu.tux.trux.gui.statistics.StatisticsMainFragment;
import se.gu.tux.trux.gui.welcome.WelcomeMainFragment;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import tux.gu.se.trux.R;

/**
 * Created by ivryashkov on 2015-05-05.
 *
 * Handles the main activity and screens.
 */
@SuppressWarnings("deprecation")
public class HomeActivity extends BaseAppActivity implements ActionBar.TabListener, ViewPager.OnPageChangeListener
{

    // constants
    private static final int LAYOUT_ID = R.layout.activity_home;
    //private static final int STATS_BUTTON = R.id.fm_i_statistics_check_stats_button;
    private static final int FRIENDS_BUTTON_WELCOME = R.id.fragment_welcome_friend_button;
    private static final int FRIENDS_BUTTON = R.id.fragment_main_friend_button;
    private static final int PROFILE_BUTTON = R.id.fragment_main_profile_button;
    private static final int MESSAGE_BUTTON = R.id.fragment_welcome_message_button;


    private ViewPager viewPager;

    private List<Fragment> fragmentArrayList;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        Fragment welcomeFragment = new WelcomeMainFragment();
        Fragment communityFragment = new CommunityMainFragment();
        Fragment statsFragment = new StatisticsMainFragment();

        // add fragments to array
        fragmentArrayList.add(welcomeFragment);
        fragmentArrayList.add(communityFragment);
        fragmentArrayList.add(statsFragment);

        // set adapter and view pager
        HomePagerAdapter pagerAdapter = new HomePagerAdapter(getSupportFragmentManager(), fragmentArrayList);

        // get action bar
        actionBar = getSupportActionBar();
        // initialise action bar
        initActionBar(actionBar);

        // set page listener
        viewPager.setOnPageChangeListener(this);
        // set adapter
        viewPager.setAdapter(pagerAdapter);

    }



    /**
     * Handles calls from child fragments components.
     *
     * TODO: handle calls
     *
     * @param id    The view id.
     */
    public void onFragmentViewClick(int id)
    {
        if (id == FRIENDS_BUTTON || id == FRIENDS_BUTTON_WELCOME)
        {
            Intent intent = new Intent(this, FriendsWindow.class);
            startActivity(intent);
        }
        else if (id == PROFILE_BUTTON)
        {
            if(!isDriving()) {
                Intent intent = new Intent(this, CommunityProfileActivity.class);
                startActivity(intent);
            }
        }
        else if (id == MESSAGE_BUTTON)
        {
            Intent intent = new Intent(this, MessageActivity.class);
            startActivity(intent);
        }
        else
        {
            showToast("Something is wrong. Called from IMainActivity.class.");
        }
    }

    private boolean isDriving(){
        ;
        try{
            Speed speed = (Speed) DataHandler.getInstance().getData(new Speed(0));
            if(speed.getValue() != null && (double) speed.getValue() > 15){
                return true;
            }
        }
        catch (NotLoggedInException nLIE){
            nLIE.printStackTrace();
        }
        return false;
    }

    @Override
    public void onBackPressed()
    {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0)
        {
            System.out.println("Minimizing...");
            // Minimize
            moveTaskToBack(true);
        }
        else
        {

            if (!getSupportFragmentManager().popBackStackImmediate("MENU",
                    FragmentManager.POP_BACK_STACK_INCLUSIVE) &&
                !getSupportFragmentManager().popBackStackImmediate("PROFILE",
                    FragmentManager.POP_BACK_STACK_INCLUSIVE)) {

                System.out.println("Poping back stack...");
                getSupportFragmentManager().popBackStack();
            }
        }
    }


    /**
     * Helper method to initialise the action bar.
     *
     * @param actionBar     The action bar.
     */
    private void initActionBar(ActionBar actionBar)
    {
        // if null, return
        if (actionBar == null) { return; }

        // specify that we will be displaying tabs in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // add tabs
        actionBar.addTab(actionBar.newTab().setCustomView(R.layout.tab_home).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setCustomView(R.layout.tab_community).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setCustomView(R.layout.tab_statistics).setTabListener(this));
    }


    /*****************************************************************************************
     * Override methods below required by implemented interfaces TabListener and             *
     * OnPageChangeListener. Do not insert outside methods in between.                       *
     *****************************************************************************************/
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

    /***************************************************************************************
     * End override methods.                                                               *
     ***************************************************************************************/

} // end class

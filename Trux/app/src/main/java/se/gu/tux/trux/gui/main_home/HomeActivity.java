package se.gu.tux.trux.gui.main_home;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;


import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.datastructure.Notification;
import se.gu.tux.trux.gui.base.BaseAppActivity;
import se.gu.tux.trux.gui.community.CommunityProfileActivity;
import se.gu.tux.trux.gui.community.FriendsWindow;
import se.gu.tux.trux.gui.messaging.MessageActivity;
import tux.gu.se.trux.R;


/**
 * Handles the main activity and screens.
 */
@SuppressWarnings("deprecation")
public class HomeActivity extends BaseAppActivity implements ActionBar.TabListener, ViewPager.OnPageChangeListener
{
    // constants
    private static final int LAYOUT_ID = R.layout.activity_home;
    private static final int FRIENDS_BUTTON_WELCOME = R.id.fragment_welcome_friend_button;
    private static final int FRIENDS_BUTTON = R.id.fragment_main_friend_button;
    private static final int PROFILE_BUTTON = R.id.fragment_main_profile_button;
    private static final int MESSAGE_BUTTON = R.id.fragment_welcome_message_button;
    // Used for identifying the result when friend window returns a friend selected for following
    private static final int CLICKED_FRIEND = 1;

    // Holds the friend id if the user decided to follow a friends location
    public long selectedFriendID = (long) -1;

    private ViewPager viewPager;
    private ActionBar actionBar;
    private Handler handler = new Handler();
    private Activity thisActivity = this;
    private boolean newMessages = false, newFriends = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // set layout for this activity
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT_ID);

        // set current view showing
        setCurrentViewId(LAYOUT_ID);

        // get the pager
        viewPager = (ViewPager) findViewById(R.id.activity_main_i_container);

        // set adapter and view pager
        HomePagerAdapter pagerAdapter = new HomePagerAdapter(getSupportFragmentManager());

        // get action bar
        actionBar = getSupportActionBar();
        // initialise action bar
        initActionBar(actionBar);

        // set page listener
        viewPager.setOnPageChangeListener(this);
        // set adapter
        viewPager.setAdapter(pagerAdapter);

        // Start timer
        handler.postDelayed(new StatusRunnable(), 10000);
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
            //Want to recieve results from clicked friend
            startActivityForResult(intent, CLICKED_FRIEND);
        }
        else if (id == PROFILE_BUTTON)
        {
            Intent intent = new Intent(this, CommunityProfileActivity.class);
            startActivity(intent);
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
        return DataHandler.gI().getSafetyStatus() != DataHandler.SafetyStatus.IDLE;
    }

    //Getting results from the FriendsWindow
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == CLICKED_FRIEND) {
            if (resultCode == RESULT_OK) {
                //Gets the friendID of the friend which was clicked
                selectedFriendID = data.getLongExtra("FriendID",  -1);
                //Sets the new view to the map
                viewPager.setCurrentItem(1, true);
            }
            else selectedFriendID = -1;
        }

    }

    public void setSelectedFriend(Long friendID){
        this.selectedFriendID = friendID;
    }

    public long getSelectedFriend() {
        return selectedFriendID;
    }


    @Override
    public void onBackPressed() {

        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {

            // Minimize if no backstack
            moveTaskToBack(true);

        } else {

            // Clear the backstack from any MENU or PROFILE entries - if they were present, they
            // are removed. If not, just do a normal popBackStack()
            // This means when the user has gone to a profile through the map and goes back, the
            // "menu" also is hidden
            if (!getSupportFragmentManager().popBackStackImmediate("MENU",
                    FragmentManager.POP_BACK_STACK_INCLUSIVE) &&
                !getSupportFragmentManager().popBackStackImmediate("PROFILE",
                    FragmentManager.POP_BACK_STACK_INCLUSIVE)) {

                // None of the above (MENU / PROFILE) was present in backstack
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


    private boolean isSimple() {
        return DataHandler.gI().getSafetyStatus() != DataHandler.SafetyStatus.IDLE;
    }


    @Override
    protected void onSaveInstanceState (Bundle outState) {
        outState.putInt("currentTab", viewPager.getCurrentItem());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState (Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        viewPager.setCurrentItem(savedInstanceState.getInt("currentTab"));
    }


    class StatusRunnable implements Runnable {
        /**
         * Push notifications for new messages / friends
         */
        @Override
        public void run() {
            System.out.println("Statusrunnable running...");

            //Get the notificaiton service from the phone
            NotificationManager notiMan =  (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);

            // Get the notification status from datahandler and compare it with last known values
            Notification not = DataHandler.getInstance().getNotificationStatus();

            if (not != null) {
                // If new messages now, but not in last known value, means the message is new
                if (not.isNewMessages() && !newMessages) {

                    //Creates a new intent - when pressing the push notification, the user will be
                    // taken to the message activity
                    Intent intent = new Intent(thisActivity, MessageActivity.class);
                    //Create a PendingIntent that will get to the intent
                    PendingIntent pendingIntent = PendingIntent.getActivity(thisActivity, 0, intent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

                    // Create a notificaiton builder and use it to build a notification
                    NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(thisActivity);
                    notiBuilder.setSmallIcon(R.drawable.truxlogo);
                    notiBuilder.setContentTitle("Trux");
                    notiBuilder.setContentText("You have a new message!");
                    notiBuilder.setVibrate(new long[]{1000, 1000});
                    notiBuilder.setLights(Color.GREEN, 3000, 3000);
                    notiBuilder.setContentIntent(pendingIntent);

                    //Pushes the notification
                    notiMan.notify(0, notiBuilder.build());

                } else if (!not.isNewMessages()) {
                    // There are no longer undread messages.
                    // Take any notification with id 0 away
                    notiMan.cancel(0);
                }

                // The same for friend requests
                if (not.isNewFriends() && !newFriends) {

                    //Creates a new intent
                    Intent intent = new Intent(thisActivity, FriendsWindow.class);
                    // Create a PendingIntent that will get to the intent
                    PendingIntent pendingIntent =
                            PendingIntent.getActivity(thisActivity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    //Create a notificaiton builder
                    NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(thisActivity);
                    notiBuilder.setSmallIcon(R.drawable.truxlogo);
                    notiBuilder.setContentTitle("Trux");
                    notiBuilder.setContentText("You have a new friend request!");
                    notiBuilder.setVibrate(new long[]{1000, 1000});
                    notiBuilder.setLights(Color.GREEN, 3000, 3000);
                    notiBuilder.setContentIntent(pendingIntent);
                    //Pushes the notification
                    notiMan.notify(1, notiBuilder.build());

                } else if (!not.isNewFriends()) {
                    notiMan.cancel(1);
                }

                // Update status of flags
                newMessages = not.isNewMessages();
                newFriends = not.isNewFriends();

            }

            // Repeat
            handler.postDelayed(this, 10000);
        }
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

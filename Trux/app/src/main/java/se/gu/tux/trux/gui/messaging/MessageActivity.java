package se.gu.tux.trux.gui.messaging;

import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;

import se.gu.tux.trux.datastructure.Friend;
import se.gu.tux.trux.gui.base.BaseAppActivity;
import tux.gu.se.trux.R;


/**
 * Created by ivryashkov on 2015-05-13.
 *
 * Handles the message interaction.
 */
public class MessageActivity extends BaseAppActivity
{

    // constants
    private static final int LAYOUT_ID = R.layout.activity_message;
    private static final int HOME_FRAGMENT = R.layout.fragment_message_list_holder;

    // the object holding the friend and message objects
    private CustomObject customObject;

    // to keep track of the current fragment showing
    private int currentFragmentId;



    /**
     * Called when this activity is first created.
     * @param savedInstanceState    Bundle.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // set layout for this activity
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT_ID);

        // set current view showing
        setCurrentViewId(LAYOUT_ID);

        // get the action to perform
        String action = getIntent().getAction();

        // if the action is to open chat, then we called this from another activity
        // open chat fragment immediately, set the friend id and username for reference
        if (action != null && action.equals("OPEN_CHAT"))
        {
            // get the needed data
            long id = getIntent().getLongExtra("FRIEND_ID", 0);
            String username = getIntent().getStringExtra("FRIEND_USERNAME");

            // create a new friend object for reference
            Friend friend = new Friend(id);
            friend.setUsername(username);

            // set friend in custom object, will be used later by the chat fragment
            customObject = new CustomObject(friend, null);

            // set current fragment showing to home fragment
            // NOTE: this is not really the case because we actually call chat fragment to open
            // but we use this reference so that when the user navigates back with the back button
            // on the phone we redirect to previous screen from where this call was made instead
            // of opening a new conversation list window
            // CHECK: check onBackPressed() method in this class for further reference
            currentFragmentId = HOME_FRAGMENT;

            // create and begin transaction
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.message_frame_container, new ChatFragment());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
        else
        {
            // else, we navigated here naturally from the welcome screen message function
            // show the conversation list for this user
            currentFragmentId = HOME_FRAGMENT;

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.message_frame_container, new ConversationListFragment());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }

    }



    /**
     * Called when this window is resumed.
     */
    @Override
    protected void onResume()
    {
        super.onResume();
        setCurrentViewId(LAYOUT_ID);
    }



    /**
     * Returns the custom object holding reference to the friend and message.
     *
     * @return      CustomObject
     */
    public CustomObject getCustomObject()   { return  customObject; }



    /**
     * Starts a new chat conversation when a conversation is selected from the conversation list.
     * Takes a CustomObject holding reference to the friend and message and the id of the fragment
     * from where this call was made as parameters.
     *
     * @param object    The CustomObject holding references.
     * @param id        The fragment id.
     */
    public void onItemClick(CustomObject object, int id)
    {
        this.customObject = object;
        currentFragmentId = id;

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.message_frame_container, new ChatFragment());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }



    /**
     * Called when the back button on the phone is pressed. If the current showing fragment
     * is the home one simply finish this activity. This will redirect naturally to the screen
     * from where we called this activity. Else, we come back from a chat fragment --> show the
     * conversation list again.
     */
    @Override
    public void onBackPressed()
    {
        if (currentFragmentId == HOME_FRAGMENT)
        {
            this.finish();
        }
        else
        {
            currentFragmentId = HOME_FRAGMENT;
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.message_frame_container, new ConversationListFragment());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }


} // end class

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

    private static final int LAYOUT_ID = R.layout.activity_message;

    private Friend friend;

    private int homeFragment = R.layout.fragment_message_list_holder;

    private int currentFragmentId;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // set layout for this activity
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT_ID);

        // set current view showing
        setCurrentViewId(LAYOUT_ID);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.message_frame_container, new FriendListFragment());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }



    @Override
    protected void onResume()
    {
        super.onResume();
        setCurrentViewId(LAYOUT_ID);
    }



    public Friend getFriend()
    {
        return friend;
    }



    public void onItemClick(Friend friend, int id)
    {
        this.friend = friend;
        currentFragmentId = id;

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.message_frame_container, new ChatFragment());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }



    @Override
    public void onBackPressed()
    {
        if (currentFragmentId == homeFragment)
        {
            this.finish();
        }
        else
        {
            currentFragmentId = homeFragment;
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.message_frame_container, new FriendListFragment());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }


} // end class

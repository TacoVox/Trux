package se.gu.tux.trux.gui.community;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.datastructure.Notification;
import se.gu.tux.trux.gui.base.TimerUpdateFragment;
import se.gu.tux.trux.gui.main_home.HomeActivity;
import tux.gu.se.trux.R;


/**
 * The community fragment is the second page in the viewpager in the main activity.
 * This fragment handles the buttons in the bottom and holds a MapFrag that takes care of the
 * Google map.
 */
public class CommunityMainFragment extends TimerUpdateFragment implements View.OnClickListener
{
    private ImageButton friendsButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // inflate view
        View view = inflater.inflate(R.layout.fragment_main_i_community, container, false);

        // get components
        FrameLayout mapContainer = (FrameLayout) view.findViewById(R.id.mapContainer);
        friendsButton = (ImageButton) view.findViewById(R.id.fragment_main_friend_button);
        ImageButton profileButton = (ImageButton) view.findViewById(R.id.fragment_main_profile_button);

        MapFrag mapFrag;
        if (savedInstanceState != null) {
            // Do not recreate the MapFrag if there is a saved instance bundle
            mapFrag = (MapFrag) getActivity().getSupportFragmentManager().findFragmentById(R.id.map);
        } else {
            mapFrag = new MapFrag();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.mapContainer, mapFrag);
            fragmentTransaction.commit();
        }

        // set listener to components
        friendsButton.setOnClickListener(this);
        profileButton.setOnClickListener(this);

        // return the view
        return view;

    }


    @Override
    public void onClick(View view)
    {
        ((HomeActivity) getActivity() ).onFragmentViewClick(view.getId());
    }

    /**
     * This method puts up the correct image on the message button depending on if the
     * user has seen all messages or not.
     */
    private void updateIcons() {
        Notification not = DataHandler.getInstance().getNotificationStatus();

        // Update friend icon
        if (not != null && not.isNewFriends()) {
            friendsButton.setImageResource(R.drawable.friendsnotificationicon);
        } else {
            friendsButton.setImageResource(R.drawable.friendsicon);
        }
    }

    @Override
    public void setStatus(DataHandler.SafetyStatus safetyStatus, Notification notificationStatus) {
        Activity a = getActivity();
        if (a != null) {
            a.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateIcons();
                }
            });
        }
    }

} // end class

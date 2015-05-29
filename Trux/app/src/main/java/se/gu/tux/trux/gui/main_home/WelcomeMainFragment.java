package se.gu.tux.trux.gui.main_home;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.util.Timer;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.datastructure.Notification;
import se.gu.tux.trux.gui.base.TimerUpdateFragment;
import tux.gu.se.trux.R;

public class WelcomeMainFragment extends TimerUpdateFragment implements View.OnClickListener
{
    ImageButton messageButton, friendButton;

    private Timer timer;
    private boolean hasPushed = true;

    /**
     * This method creates the two buttons messageButton and friendButton and sets OnClicklisteners to them.
     * It creates a new Timer and TimerTask and schedule them to every 10th second.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_main_welcome, container, false);

        messageButton = (ImageButton) view.findViewById(R.id.fragment_welcome_message_button);
        friendButton = (ImageButton) view.findViewById(R.id.fragment_welcome_friend_button);

        messageButton.setOnClickListener(this);
        friendButton.setOnClickListener(this);
        return view;
    }


    /**
     * Listens for a click on the view.
     * @param view
     */
    @Override
    public void onClick(View view)
    {
        ((HomeActivity) getActivity()).onFragmentViewClick(view.getId());
    }

    /**
     * This method puts up the correct image on the message button depending on if the
     * user has seen every messages or not. The same for new friends.
     */
    private void updateIcons(){
        Notification not = DataHandler.getInstance().getNotificationStatus();

        // Update message icon
        if (not != null && not.isNewMessages()) {
            messageButton.setImageResource(R.drawable.messagenotificationicon);
        } else {
            messageButton.setImageResource(R.drawable.messageicon);
        }

        // Update friend icon
        if (not != null && not.isNewFriends()) {
            friendButton.setImageResource(R.drawable.friendsnotificationicon);
        } else {
            friendButton.setImageResource(R.drawable.friendsicon);
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

package se.gu.tux.trux.gui.welcome;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import java.util.Timer;
import java.util.TimerTask;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.datastructure.Notification;
import se.gu.tux.trux.gui.main_home.HomeActivity;
import tux.gu.se.trux.R;

public class WelcomeMainFragment extends Fragment implements View.OnClickListener
{
    ImageButton messageButton, friendButton;

    private Timer timer;
    private UpdateIcons iconUpdater;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            View view = inflater.inflate(R.layout.fragment_main_welcome, container, false);

            messageButton = (ImageButton) view.findViewById(R.id.fragment_welcome_message_button);
            friendButton = (ImageButton) view.findViewById(R.id.fragment_welcome_friend_button);

            messageButton.setOnClickListener(this);
            friendButton.setOnClickListener(this);

            timer = new Timer();
            iconUpdater = new UpdateIcons();
            timer.schedule(iconUpdater, 0, 10000);

            return view;
        }

        public void onStop(){
            super.onStop();
            if(timer != null) {
                timer.cancel();
                timer = null;
            }
        }

        public void onResume(){
            super.onResume();
            if(timer == null) {
                timer = new Timer();
                iconUpdater = new UpdateIcons();
                timer.schedule(iconUpdater, 0, 10000);
            }
        }


        @Override
        public void onClick(View view)
        {
            ((HomeActivity) getActivity()).onFragmentViewClick(view.getId());
        }

        public void unseenMessages(){
            Notification not = DataHandler.getInstance().getNotificationStatus();
            if (not != null && not.isNewMessages()) {
                messageButton.setImageResource(R.drawable.messagenotificationicon);
            } else {
                messageButton.setImageResource(R.drawable.messageicon);
            }

        }
        public void unseenFriendRequest(){
            Notification not = DataHandler.getInstance().getNotificationStatus();
            if (not != null && not.isNewFriends()) {
                friendButton.setImageResource(R.drawable.friendsnotificationicon);
            } else {
                friendButton.setImageResource(R.drawable.friendsicon);
            }

        }

        class UpdateIcons extends TimerTask {
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        unseenMessages();
                        unseenFriendRequest();
                    }
                });
            }
        }

} // end class

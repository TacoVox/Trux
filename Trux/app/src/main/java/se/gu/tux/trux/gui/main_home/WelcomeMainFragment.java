package se.gu.tux.trux.gui.main_home;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.util.Timer;
import java.util.TimerTask;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.datastructure.Notification;
import se.gu.tux.trux.gui.community.FriendsWindow;
import se.gu.tux.trux.gui.messaging.MessageActivity;
import tux.gu.se.trux.R;

public class WelcomeMainFragment extends Fragment implements View.OnClickListener
{
    ImageButton messageButton, friendButton;

    private Timer timer;
    private UpdateIcons iconUpdater;
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

            timer = new Timer();
            iconUpdater = new UpdateIcons();
            timer.schedule(iconUpdater, 0, 10000);

            return view;
        }

    /**
     * Cancels the timer and put it as null
     * when the fragment closes.
     */

        public void onStop(){
            super.onStop();
            if(timer != null) {
                timer.cancel();
                timer = null;
            }
        }

    /**
     * Creates a new Timer and TimerTask
     * when the fragment resumes.
     */

        public void onResume(){
            super.onResume();
            if(timer == null) {
                timer = new Timer();
                iconUpdater = new UpdateIcons();
                timer.schedule(iconUpdater, 0, 10000);
            }
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
     * user has seen every messages or not.
     */

        public void unseenMessages(){
            //Get the notificaiton service from the phone
            NotificationManager notiMan =  (NotificationManager)
                    getActivity().getSystemService(Context.NOTIFICATION_SERVICE);;
            Notification not = DataHandler.getInstance().getNotificationStatus();
            if (not != null && not.isNewMessages()) {
                messageButton.setImageResource(R.drawable.messagenotificationicon);
                if(!hasPushed) {
                    //Creates a new intent
                    Intent intent = new Intent(getActivity(), MessageActivity.class);
                    //Create a PendingIntent that will get to the intent
                    PendingIntent pendingIntent =
                            PendingIntent.getActivity(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    //Create a notificaiton builder
                    NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(getActivity());
                    notiBuilder.setSmallIcon(R.drawable.truxlogo);
                    notiBuilder.setContentTitle("Trux");
                    notiBuilder.setContentText("You have a new message!");
                    notiBuilder.setVibrate(new long[]{1000, 1000});
                    notiBuilder.setLights(Color.GREEN, 3000, 3000);
                    notiBuilder.setContentIntent(pendingIntent);
                    //Pushes the notification
                    //notiMan.notify(0, notiBuilder.build());
                    hasPushed = true;

                }
            } else {
                messageButton.setImageResource(R.drawable.messageicon);
                hasPushed = false;
                //Takes away the notificaiton when pressed in the notificaiton bar
                notiMan.cancel(0);

            }

        }

    /**
     * This method puts up the correct image on the friend request button depending on if the
     * user has a new friend request or not.
     */

        public void unseenFriendRequest(){
            Notification not = DataHandler.getInstance().getNotificationStatus();
            //Get the notificaiton service from the phone
            NotificationManager notiMan = (NotificationManager)
                    getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            if (not != null && not.isNewFriends()) {
                friendButton.setImageResource(R.drawable.friendsnotificationicon);
                if(!hasPushed) {
                    //Creates a new intent
                    Intent intent = new Intent(getActivity(), FriendsWindow.class);
                    //Create a PendingIntent that will get to the intent
                    PendingIntent pendingIntent =
                            PendingIntent.getActivity(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    //Create a notificaiton builder
                    NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(getActivity());
                    notiBuilder.setSmallIcon(R.drawable.truxlogo);
                    notiBuilder.setContentTitle("Trux");
                    notiBuilder.setContentText("You have a new friend request!");
                    //notiBuilder.setVibrate(new long[]{1000, 1000});
                    notiBuilder.setLights(Color.GREEN, 3000, 3000);
                    notiBuilder.setContentIntent(pendingIntent);
                    //Pushes the notification
                    //notiMan.notify(1, notiBuilder.build());
                    hasPushed = true;
                }
            } else {
                friendButton.setImageResource(R.drawable.friendsicon);
                hasPushed = false;
                notiMan.cancel(1);
            }

        }

    /**
     * This TimerTask updates the icons.
     */

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

package se.gu.tux.trux.technical_services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import android.graphics.Color;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.datastructure.Notification;
import se.gu.tux.trux.gui.community.FriendsWindow;
import se.gu.tux.trux.gui.messaging.MessageActivity;
import tux.gu.se.trux.R;

/*
 * Created by ivryashkov on 2015-05-28.
 *
 * Handles the notifications pushed.
 */
public class NotificationService extends IntentService
{


    private Handler handler = new Handler();

    private boolean newMessages = false;
    private boolean newFriends = false;



    /**
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */
    public NotificationService()
    {
        super("NotificationService");
    }



    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns, IntentService
     * stops the service, as appropriate.
     */
    @Override
    protected void onHandleIntent(Intent intent)
    {
        if (intent.getAction().equals("START_NOTIFICATION"))
        {
            // Start timer
            handler.postDelayed(new StatusRunnable(), 10000);
        }

    } // end onHandleIntent()



    private class StatusRunnable implements Runnable
    {
        @Override
        public void run()
        {
            System.out.println("-----------------------------------------------------");
            System.out.println("Status Runnable running...");
            System.out.println("-----------------------------------------------------");

            //Get the notificaiton service from the phone
            NotificationManager notiMan = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Get the notification status from datahandler and compare it with last known values
            Notification notification = DataHandler.getInstance().getNotificationStatus();

            if (notification != null)
            {
                // If new messages now, but not in last known value, means the message is new
                if (notification.isNewMessages() && !newMessages)
                {
                    //Creates a new intent - when pressing the push notification, the user will be
                    // taken to the message activity
                    Intent intnt = new Intent(getApplicationContext(), MessageActivity.class);

                    //Create a PendingIntent that will get to the intent
                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intnt,
                            PendingIntent.FLAG_UPDATE_CURRENT);

                    // Create a notificaiton builder and use it to build a notification
                    NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(getApplicationContext());
                    notiBuilder.setSmallIcon(R.drawable.truxlogo);
                    notiBuilder.setContentTitle("Trux");
                    notiBuilder.setContentText("You have a new message!");
                    notiBuilder.setVibrate(new long[]{1000, 1000});
                    notiBuilder.setLights(Color.GREEN, 3000, 3000);
                    notiBuilder.setContentIntent(pendingIntent);

                    //Pushes the notification
                    notiMan.notify(0, notiBuilder.build());
                }
                else if (!notification.isNewMessages())
                {
                    // There are no longer undread messages.
                    // Take any notification with id 0 away
                    notiMan.cancel(0);
                }

                // The same for friend requests
                if (notification.isNewFriends() && !newFriends)
                {
                    //Creates a new intent
                    Intent intnt = new Intent(getApplicationContext(), FriendsWindow.class);

                    // Create a PendingIntent that will get to the intent
                    PendingIntent pendingIntent =
                            PendingIntent.getActivity(getApplicationContext(), 0, intnt, PendingIntent.FLAG_UPDATE_CURRENT);

                    //Create a notificaiton builder
                    NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(getApplicationContext());
                    notiBuilder.setSmallIcon(R.drawable.truxlogo);
                    notiBuilder.setContentTitle("Trux");
                    notiBuilder.setContentText("You have a new friend request!");
                    notiBuilder.setVibrate(new long[]{1000, 1000});
                    notiBuilder.setLights(Color.GREEN, 3000, 3000);
                    notiBuilder.setContentIntent(pendingIntent);

                    //Pushes the notification
                    notiMan.notify(1, notiBuilder.build());
                }
                else if (!notification.isNewFriends())
                {
                    notiMan.cancel(1);
                }

                // Update status of flags
                newMessages = notification.isNewMessages();
                newFriends = notification.isNewFriends();

            }

            // Repeat
            handler.postDelayed(this, 10000);
        }

    } // end inner class


} // end class

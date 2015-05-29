package se.gu.tux.trux.technical_services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import java.util.Timer;
import java.util.TimerTask;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.datastructure.Notification;
import se.gu.tux.trux.gui.community.FriendsWindow;
import se.gu.tux.trux.gui.messaging.MessageActivity;
import tux.gu.se.trux.R;

/**
 * Created by ivryashkov on 2015-05-28.
 *
 * Handles the notifications pushed.
 */
public class NotificationService extends Service
{

    // the timer we use to execute thread
    private Timer timer;

    // boolean flags
    private volatile boolean timerRunning;
    private volatile boolean newMessages = false;
    private volatile boolean newFriends = false;

    // time intervals for the timer
    private static final long RETRY_TIME = 10000;
    private static final long START_TIME = 1000;

    // binder given to clients
    private final IBinder binder = new CustomBinder();



    @Override
    public void onCreate()
    {
        super.onCreate();

        timer = new Timer();
        timer.scheduleAtFixedRate(new ServiceTask(), START_TIME, RETRY_TIME);
        timerRunning = true;
    }


    /**
     * Called when the service is started.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (!timerRunning)
        {
            timer = new Timer();
            timer.scheduleAtFixedRate(new ServiceTask(), START_TIME, RETRY_TIME);
            timerRunning = true;
        }

        return super.onStartCommand(intent, flags, startId);
    }



    @Override
    public void onDestroy()
    {
        super.onDestroy();

        // stop timer
        if (timer != null)  { timer.cancel(); }

        // set boolean flag
        timerRunning = false;
    }



    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }



    /**
     * Class used for the client binder.
     */
    public class CustomBinder extends Binder
    {
        public NotificationService getService()
        {
            // return this instance of NotificationService so clients can call public methods
            return NotificationService.this;
        }
    }



    /**
     * Private timer task class. Checks for notifications and pushes them.
     */
    private class ServiceTask extends TimerTask
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

        } // end run()

    } // end inner class


} // end class

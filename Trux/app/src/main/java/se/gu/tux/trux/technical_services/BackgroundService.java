package se.gu.tux.trux.technical_services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.datastructure.User;

/**
 * Created by jerker on 2015-05-28.
 */
public class BackgroundService extends Service {
    // Keep track of instances so we are sure they aren't killed
    private static DataPoller d;
    private static AGADataParser a;
    private static ServerConnector s;
    private static RealTimeDataHandler rtdh;
    private static DataHandler dH;
    private static LocationHandler ls;

    public BackgroundService() {

    }

    @Override
    public void onCreate() {
        if (ls == null) {
            System.out.println("Creating background service...");
            dH = DataHandler.getInstance();
            ls = new LocationHandler(getApplicationContext());
            rtdh = new RealTimeDataHandler(ls);
            d = DataPoller.getInstance();
            d.start(rtdh);
            a = AGADataParser.getInstance();
            s = ServerConnector.getInstance();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        System.out.println("Starting background service...");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (intent == null) {
            System.out.println("Trying to restore user from SharedPreferences...");
            // Started after being destroyed - try to recover user settings
            if (prefs.getString("username", null) != null) {
                User u = new User();
                u.setUsername(prefs.getString("username", null));
                u.setPasswordHash(prefs.getString("password", null));
                u.setSessionId(prefs.getLong("sessionid", -1));
                u.setUserId(prefs.getLong("userid", -1));
                System.out.println("Userid: " + u.getUserId());
                dH.setUser(u);
            } else {
                System.out.println("Failed.");
            }

        } else {
            System.out.println("Storing user to SharedPreferences...");

            // Started from an activity - set the user settings now
            User u = DataHandler.gI().getUser();
            System.out.println("Username: " + u.getUsername());
            SharedPreferences.Editor edit = prefs.edit();
            edit.putLong("sessionid", u.getSessionId());
            edit.putLong("userid", u.getUserId());
            edit.putString("username", u.getUsername());
            edit.putString("passwordhash", u.getPasswordHash());
            System.out.println("Storing: " + edit.commit());
        }
        // Keep running
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

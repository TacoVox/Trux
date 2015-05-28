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
 * Maybe somewhat hackish way to try to keep all the background stuff alive to protect it from
 * garbage collection, otherwise things like the AGA listener seemed to dissapear anytime after
 * you close the app. Ideally we should probably have turned a few things in the technical services
 * package into services, but were solving this in the last days before hand-in now just to get it
 * working fairly stable.
 * On really high memory usage the service may be killed temporarily by android. It is then restarted
 * on a convenient time, usually almost immediately. Therefore we let datahandler store and restore
 * the info about the curren logged in user from SharedPreferences because that is the only state
 * data that is critical for the service and app to continue working.
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
            // Started by android after being killed - try to recover stored user data
            dH.loadFromPrefs(prefs);
        } else {
            // Started from an activity - means we can store the datahandlers logged in user right
            // now
            dH.storeToPrefs(prefs);
        }

        // Keep running
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

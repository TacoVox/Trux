package se.gu.tux.trux.gui.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.Timer;
import java.util.TimerTask;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.datastructure.Notification;

/**
 * Created by jerker on 2015-05-27.
 */
public abstract class TimerUpdateFragment extends Fragment {
    private Timer timer;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        timer = new Timer();
        timer.schedule(new StatusTimerTask(), 0, 10000);
    }

    public abstract void setStatus(DataHandler.SafetyStatus safetyStatus,
                                   Notification notificationStatus);

    class StatusTimerTask extends TimerTask {
        @Override
        public void run() {
            setStatus(DataHandler.gI().getSafetyStatus(), DataHandler.gI().getNotificationStatus());
        }
    }
}

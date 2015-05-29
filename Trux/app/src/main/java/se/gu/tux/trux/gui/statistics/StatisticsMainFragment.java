package se.gu.tux.trux.gui.statistics;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.datastructure.Notification;
import se.gu.tux.trux.gui.base.TimerUpdateFragment;
import tux.gu.se.trux.R;

/**
 * Handles the main fragment for the statistics. Shows the appropriate
 * views depending on the driving mode -- in motion or not.
 */
public class StatisticsMainFragment extends TimerUpdateFragment
{

    private DataHandler.SafetyStatus lastKnownStatus;
    private boolean stopped = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // get the view
        View view = inflater.inflate(R.layout.fragment_statistics_main, container, false);
        chooseFragment(DataHandler.gI().getSafetyStatus());

        // return the view
        return view;
    }



    @Override
    public void onStop()
    {
        super.onStop();
        stopped = true;
    }



    @Override
    public void onResume()
    {
        super.onResume();
        stopped = false;
        // Tell datahandler to fetch detailed stats in the background if not already cached
        DataHandler.getInstance().cacheDetailedStats();

        // Choose which fragment to show
        chooseFragment(DataHandler.gI().getSafetyStatus());
    }

    private void chooseFragment(DataHandler.SafetyStatus status) {
        // Check if status has changed
        if (!stopped && status != null && status != lastKnownStatus) {
            // check if driving or not to display right fragments
            if (status == DataHandler.SafetyStatus.MOVING || status == DataHandler.SafetyStatus.FAST_MOVING) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.statistics_main_container, new StatisticsSimpleFragment());
                fragmentTransaction.commit();
            } else {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.statistics_main_container, new StatisticsDetailedFragment());
                fragmentTransaction.commit();
            }
            lastKnownStatus = status;
        }
    }


    @Override
    public void setStatus(DataHandler.SafetyStatus safetyStatus, Notification notificationStatus) {
        chooseFragment(safetyStatus);
    }
} // end class

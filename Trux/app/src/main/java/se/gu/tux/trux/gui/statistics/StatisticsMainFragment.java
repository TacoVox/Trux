package se.gu.tux.trux.gui.statistics;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import se.gu.tux.trux.application.DataHandler;

import tux.gu.se.trux.R;

/**
 * Handles the main fragment for the statistics. Shows the appropriate
 * views depending on the driving mode -- in motion or not.
 */
public class StatisticsMainFragment extends Fragment
{

    private DataHandler.SafetyStatus status;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // get the view
        View view = inflater.inflate(R.layout.fragment_statistics_main, container, false);

        DataHandler.getInstance().cacheDetailedStats();

        status = DataHandler.getInstance().getSafetyStatus();

        // check if driving or not and add fragments
        if (status == DataHandler.SafetyStatus.MOVING || status == DataHandler.SafetyStatus.FAST_MOVING)
        {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.statistics_main_container, new StatisticsSimpleFragment());
            fragmentTransaction.commitAllowingStateLoss();
        }
        else
        {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.statistics_main_container, new StatisticsDetailedFragment());
            fragmentTransaction.commitAllowingStateLoss();
        }

        // return the view
        return view;
    }



    @Override
    public void onStop()
    {
        super.onStop();
        status = null;
    }



    @Override
    public void onResume()
    {
        super.onResume();

        status = DataHandler.getInstance().getSafetyStatus();

        // when we resume from previous state
        // check if driving or not to display right fragments
        if (status == DataHandler.SafetyStatus.MOVING || status == DataHandler.SafetyStatus.FAST_MOVING)
        {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.statistics_main_container, new StatisticsSimpleFragment());
            fragmentTransaction.commit();
        }
        else
        {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.statistics_main_container, new StatisticsDetailedFragment());
            fragmentTransaction.commit();
        }
    }


} // end class

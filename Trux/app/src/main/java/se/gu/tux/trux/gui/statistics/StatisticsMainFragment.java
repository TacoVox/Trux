package se.gu.tux.trux.gui.statistics;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import se.gu.tux.trux.appplication.DataHandler;
import se.gu.tux.trux.datastructure.Speed;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import tux.gu.se.trux.R;

/**
 * Created by ivryashkov on 2015-05-07.
 *
 * Handles the main fragment for the statistics. Shows the appropriate
 * views depending on the driving mode -- in motion or not.
 */
public class StatisticsMainFragment extends Fragment
{

    // to check if in driving mode or not
    Speed speed;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_statistics_main, container, false);

        // start fetching data for the statistics
        DataHandler.getInstance().cacheDetailedStats();

        // check if in driving mode by getting a speed object
        try
        {
            speed = (Speed) DataHandler.getInstance().getData(new Speed(0));
        }
        catch (NotLoggedInException e) { e.printStackTrace(); }


        if (speed.getValue() != null && ((Double) speed.getValue()) > 5)
        {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.statistics_main_container, new StatisticsSimpleFragment());
            fragmentTransaction.commit();
        }
        else
        {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.statistics_main_container, new StatisticsDetailedFragment());
            fragmentTransaction.commit();
        }

        return view;
    }


    @Override
    public void onResume()
    {
        super.onResume();

        // check if in driving mode by getting a speed object
        try
        {
            speed = (Speed) DataHandler.getInstance().getData(new Speed(0));
        }
        catch (NotLoggedInException e) { e.printStackTrace(); }


        if (speed.getValue() != null && ((Double) speed.getValue()) > 5)
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

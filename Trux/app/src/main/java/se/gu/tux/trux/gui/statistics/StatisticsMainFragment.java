package se.gu.tux.trux.gui.statistics;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import se.gu.tux.trux.application.DataHandler;
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
    private Speed speed;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // get the view
        View view = inflater.inflate(R.layout.fragment_statistics_main, container, false);

        // check if driving or not and add fragments
        if (isInDrivingMode())
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

        // return the view
        return view;
    }


    @Override
    public void onStop()
    {
        super.onStop();
        // set speed to null
        speed = null;
    }


    @Override
    public void onResume()
    {
        super.onResume();

        // when we resume from previous state
        // check if driving or not to display right fragments
        if (isInDrivingMode())
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


    /**
     * Helper method. Checks if in driving mode.
     *
     * @return  true if driving, false otherwise
     */
    private boolean isInDrivingMode()
    {
        // get a speed object
        try
        {
            speed = (Speed) DataHandler.getInstance().getData(new Speed(0));
        }
        catch (NotLoggedInException e) { e.printStackTrace(); }

        // return true if value is not null and bigger than 1 --> vehicle moving
        // false otherwise
        return speed.getValue() != null && ((Double) speed.getValue()) > 1;
    }


} // end class

package se.gu.tux.trux.gui.statistics_i;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

import java.io.Serializable;

import se.gu.tux.trux.appplication.DataHandler;
import se.gu.tux.trux.gui.BaseAppActivity;
import se.gu.tux.trux.gui.detailedStats.DistTravWindow;
import se.gu.tux.trux.gui.detailedStats.FuelWindow;
import se.gu.tux.trux.gui.detailedStats.OverallStats;
import se.gu.tux.trux.gui.detailedStats.SpeedWindow;
import tux.gu.se.trux.R;

/**
 * Created by ivryashkov on 2015-05-04.
 *
 * Handles the statistics for the application.
 */
public class IStatisticsActivity extends BaseAppActivity implements Serializable
{

    // for any fragments that want to interact with this activity
    // when any action needs to be handle, do it here to avoid
    // fragments from holding too much application logic
    //
    // NOTE: can be possibly extended with
    // a super class to be more generic
    interface StatisticsFragmentInterface
    {
        void onFragmentViewClick(int viewId);
    }

    // constants
    private static final int LAYOUT_ID = R.layout.activity_statistics_i;

    //private static final int SPEED_BTN = R.id.activity_statistics_i_speed_button;
    //private static final int FUEL_BTN = R.id.activity_statistics_i_fuel_button;
    //private static final int DISTANC_BTN = R.id.activity_statistics_i_distance_button;

    private static final int SPEED_BUTTON = R.id.speed_button;
    private static final int FUEL_BUTTON = R.id.fuel_button;
    private static final int DISTANCE_TRAVELED = R.id.distance_traveled;
    private static final int OVERALL_BTN = R.id.overall_button;


    // fragments
    SpeedWindow speedFragment;
    FuelWindow fuelFragment;
    DistTravWindow distFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // set layout for this view
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT_ID);
        // set current view showing
        setCurrentViewId(LAYOUT_ID);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.activity_statistics_i_container, new IMainFragment());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        // set current view showing
        setCurrentViewId(LAYOUT_ID);

        // Tell DataHandler to start fetching detailed stats (unless they are cached)
        DataHandler.getInstance().cacheDetailedStats();
    }


    @Override
    public void onBackPressed()
    {
        // if nothing in back stack when back button clicked, finish this activity
        // else go back to this activity main screen, not home screen, maybe user
        // wants to check some other statistics
        // NOTE back stack count 1 means on the menu, so finish this activity from there as well
        if (getFragmentManager().getBackStackEntryCount() == 0 ||
                getFragmentManager().getBackStackEntryCount() == 1)
        {
            this.finish();
        }
        else
        {
            getFragmentManager().popBackStack();
        }
    }


    /**
     * Performs action based on the view id.
     *
     * @param view      The view id.
     */
    public void onFragmentViewClick(int view)
    {
        FragmentTransaction transaction = this.getFragmentManager().beginTransaction();

        if (view == SPEED_BUTTON)
        {
            if (speedFragment == null)
            {
                speedFragment = new SpeedWindow();
            }
            transaction.replace(R.id.activity_statistics_i_container, speedFragment);
            // add to back stack for access
            transaction.addToBackStack(null);
        }
        else if (view == FUEL_BUTTON)
        {
            if (fuelFragment == null)
            {
                fuelFragment = new FuelWindow();
            }
            transaction.replace(R.id.activity_statistics_i_container, fuelFragment);
            // add to back stack for access
            transaction.addToBackStack(null);
        }
        else if (view == DISTANCE_TRAVELED)
        {
            if (distFragment == null)
            {
                distFragment = new DistTravWindow();
            }
            transaction.replace(R.id.activity_statistics_i_container, distFragment);
            // add to back stack for access
            transaction.addToBackStack(null);
        }
        else if (view == OVERALL_BTN)
        {
            Intent intent = new Intent(this, OverallStats.class);
            startActivity(intent);
        }

        // set transition
        transaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        // commit transaction
        transaction.commit();
    }



} // end class

package se.gu.tux.trux.gui.statistics_i;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import java.io.Serializable;

import se.gu.tux.trux.appplication.DataHandler;
import se.gu.tux.trux.datastructure.Distance;
import se.gu.tux.trux.datastructure.Fuel;
import se.gu.tux.trux.datastructure.Speed;
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

    private static final int SPEED_BTN = R.id.activity_statistics_i_speed_button;
    private static final int FUEL_BTN = R.id.activity_statistics_i_fuel_button;
    private static final int DISTANC_BTN = R.id.activity_statistics_i_distance_button;

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
    }


    @Override
    public void onBackPressed()
    {
        // if nothing in back stack when back button clicked, finish this activity
        // else go back to this activity main screen, not home screen, maybe user
        // wants to check some other statistics
        if (getFragmentManager().getBackStackEntryCount() == 0)
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

        if (view == SPEED_BTN)
        {
            showToast("Speed button in Stats.class clicked");

            if (speedFragment == null)
            {
                speedFragment = new SpeedWindow();
            }

            // Make sure values are set once they are loaded
            AsyncTask myTask = new AsyncTask<Void, Void, Boolean>()
            {
                Speed s = new Speed(0);

                @Override
                protected Boolean doInBackground(Void... voids)
                {
                    while (!(DataHandler.getInstance().detailedStatsReady(s)
                            && speedFragment.hasLoaded()))
                    {
                        try { Thread.sleep(100); } catch (InterruptedException e) {}
                    }
                    return null;
                }

                @Override
                public void onPreExecute() {
                    super.onPreExecute();
                }

                @Override
                protected void onPostExecute(Boolean b)
                {
                    super.onPostExecute(b);
                    speedFragment.setValues(DataHandler.getInstance().getDetailedStats(s));
                    speedFragment.hideLoading();
                }
            }.execute();

            transaction.replace(R.id.activity_statistics_i_container, speedFragment);

        }
        else if (view == FUEL_BTN)
        {
            showToast("Fuel button in Stats.class clicked");

            if (fuelFragment == null)
            {
                fuelFragment = new FuelWindow();
            }

            AsyncTask myTask = new AsyncTask<Void, Void, Boolean>()
            {
                Fuel f = new Fuel(0);

                @Override
                protected Boolean doInBackground(Void... voids)
                {
                    while (!(DataHandler.getInstance().detailedStatsReady(f)
                            && fuelFragment.hasLoaded()))
                    {
                        try { Thread.sleep(100); } catch (InterruptedException e) {}
                    }
                    return null;
                }

                @Override
                public void onPreExecute() {
                    super.onPreExecute();
                }

                @Override
                protected void onPostExecute(Boolean b) {
                    super.onPostExecute(b);
                    fuelFragment.setValues(DataHandler.getInstance().getDetailedStats(f));
                    fuelFragment.hideLoading();
                }
            }.execute();

            transaction.replace(R.id.activity_statistics_i_container, fuelFragment);

        }
        else if (view == DISTANC_BTN)
        {
            showToast("Distance button in Stats.class clicked");

            if (distFragment == null)
            {
                distFragment = new DistTravWindow();
            }

            AsyncTask myTask = new AsyncTask<Void, Void, Boolean>()
            {
                Distance d = new Distance(0);

                @Override
                protected Boolean doInBackground(Void... voids)
                {
                    while (!(DataHandler.getInstance().detailedStatsReady(d)
                            && distFragment.hasLoaded()))
                    {
                        try { Thread.sleep(100); } catch (InterruptedException e) {}
                    }
                    return null;
                }

                @Override
                public void onPreExecute() {
                    super.onPreExecute();
                }

                @Override
                protected void onPostExecute(Boolean b) {
                    super.onPostExecute(b);
                    distFragment.setValues(DataHandler.getInstance().getDetailedStats(d));
                    distFragment.hideLoading();
                }
            }.execute();

            transaction.replace(R.id.activity_statistics_i_container, distFragment);

        }
        /*else if (view == OVERALL_BTN)
        {
            showToast("Overall button in Stats.class clicked");

            Intent intent = new Intent(this, OverallStats.class);
            startActivity(intent);
        }*/

        // add to back stack for access
        transaction.addToBackStack(null);
        // set transition
        transaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        // commit transaction
        transaction.commit();
    }



} // end class

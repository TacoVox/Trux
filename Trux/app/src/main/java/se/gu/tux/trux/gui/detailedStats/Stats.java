package se.gu.tux.trux.gui.detailedStats;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.Serializable;

import se.gu.tux.trux.appplication.DataHandler;
import se.gu.tux.trux.datastructure.Distance;
import se.gu.tux.trux.datastructure.Fuel;
import se.gu.tux.trux.datastructure.Speed;
import se.gu.tux.trux.gui.BaseAppActivity;
import tux.gu.se.trux.R;


/**
 * Handles the statistics screen and display of fragments.
 */
public class Stats extends BaseAppActivity implements Serializable, View.OnClickListener
{
    //volatile Fragment fragment;
    private volatile DetailedStatsFragment speedFragment, fuelFragment, distFragment;
    private Button speedBtn, fuelBtn, distanceBtn, overallBtn;

    // layout id
    private static final int LAYOUT_ID = R.layout.activity_stats;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // set layout for this view
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT_ID);

        // set current view
        setCurrentViewId(LAYOUT_ID);

        // get components for this view
        speedBtn = (Button) findViewById(R.id.speed_button);
        fuelBtn = (Button) findViewById(R.id.fuel_button);
        distanceBtn = (Button) findViewById(R.id.distance_traveled);
        overallBtn = (Button) findViewById(R.id.overall_button);

        // set listener for buttons
        speedBtn.setOnClickListener(this);
        fuelBtn.setOnClickListener(this);
        distanceBtn.setOnClickListener(this);
        overallBtn.setOnClickListener(this);

        // Tell data handler to start downloading all stats
        //DataHandler.getInstance().cacheDetailedStats();;
    }


    @Override
    public void onStop()
    {
        super.onStop();
    }


    @Override
    public void onPause()
    {
        super.onPause();
    }


    @Override
    protected void onResume()
    {
        super.onResume();

        // set current view
        setCurrentViewId(LAYOUT_ID);

        // Tell data handler to start downloading all stats
        DataHandler.getInstance().cacheDetailedStats();
    }



    @Override
    public void onClick(View view)
    {
        FragmentTransaction transaction = this.getFragmentManager().beginTransaction();

        if (view.getId() == R.id.speed_button)
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

            transaction.replace(R.id.stats_view_container, speedFragment);

        }
        else if (view.getId() == R.id.fuel_button)
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

            transaction.replace(R.id.stats_view_container, fuelFragment);

        }
        else if (view.getId() == R.id.distance_traveled)
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

            transaction.replace(R.id.stats_view_container, distFragment);

        }
        else if (view.getId() == R.id.overall_button)
        {
            showToast("Overall button in Stats.class clicked");

            Intent intent = new Intent(Stats.this, OverallStats.class);
            startActivity(intent);
        }

        // add to back stack for access
        transaction.addToBackStack(null);
        // set transition
        transaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        // commit transaction
        transaction.commit();

    } // end onClick()



    @Override
    public void onBackPressed()
    {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            this.finish();
        } else {
            getFragmentManager().popBackStack();
        }
    }


/*
    public void goToOverall(View view) {
        Intent intent = new Intent(this, OverallStats.class);
        startActivity(intent);
    }

    public void logout(MenuItem item){
        Intent intent = new Intent(Stats.this, MainActivity.class);
        startActivity(intent);
    }
*/


} // end class



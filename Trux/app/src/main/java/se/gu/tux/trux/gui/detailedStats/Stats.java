package se.gu.tux.trux.gui.detailedStats;


import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;
import android.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;

import se.gu.tux.trux.appplication.DataHandler;
import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.Distance;
import se.gu.tux.trux.datastructure.Fuel;
import se.gu.tux.trux.datastructure.MetricData;
import se.gu.tux.trux.datastructure.Speed;
import se.gu.tux.trux.gui.BaseAppActivity;
import se.gu.tux.trux.gui.ItemMenu;
import se.gu.tux.trux.gui.MainActivity;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import tux.gu.se.trux.R;

/**
 * TODO TODO TODO Möjligtvis snygga till koden genom att:
 * Skapa en datatyp som innehåller today - total + linegraphseries
 * Ev låt DataHandler fylla HELA den med data?
 * SE LÄNGRE KOMMENTAR I ONCLICK
 *
 */


public class Stats extends BaseAppActivity implements Serializable
{
    //volatile Fragment fragment;
    private volatile DetailedStatsFragment speedFragment, fuelFragment, distFragment;
    private Button speedBtn, fuelBtn, distanceBtn, overallBtn;
    private FragmentTransaction transaction;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        speedBtn = (Button) findViewById(R.id.speed_button);
        fuelBtn = (Button) findViewById(R.id.fuel_button);
        distanceBtn = (Button) findViewById(R.id.distance_traveled);
        overallBtn = (Button) findViewById(R.id.overall_button);
        Button.OnClickListener btnOnClick = new ButtonListener();

        speedBtn.setOnClickListener(btnOnClick);
        fuelBtn.setOnClickListener(btnOnClick);
        distanceBtn.setOnClickListener(btnOnClick);
        overallBtn.setOnClickListener(btnOnClick);

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

        // Tell data handler to start downloading all stats
        DataHandler.getInstance().cacheDetailedStats();;
    }



    class ButtonListener implements Button.OnClickListener {

        @Override
        public void onClick(final View v) {



            // TODO: Tell datahandler to start downloading stats
            // then check if loaded (for the seleceted metric type)
            // the data handler should be able to return a wrapper object for all detailed stats
            // with just a metric data as key argument (give it a fuel object to receive detailed
            // stats object for fuel etc) - The data handler should then store these in a hash map
            // or something - cache them and also let the wrapper object contain a timestamp -
            // that way if they are older than say 15 minutes, update them : ))


            if (v == speedBtn) {
                if (speedFragment == null) {
                    speedFragment = new SpeedWindow();
                }

                // Make sure values are set once they are loaded
                AsyncTask myTask = new AsyncTask<Void, Void, Boolean>() {
                    Speed s = new Speed(0);

                    @Override
                    protected Boolean doInBackground(Void... voids) {
                        while (!(DataHandler.getInstance().detailedStatsReady(s)
                                && speedFragment.hasLoaded())) {
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
                        speedFragment.setValues(DataHandler.getInstance().getDetailedStats(s));
                        speedFragment.hideLoading();
                    }
                }.execute();

                transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.StatsView, speedFragment);
                transaction.addToBackStack(null);
                transaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
                transaction.commit();

            } else if (v == fuelBtn) {
                if (fuelFragment == null) {
                    fuelFragment = new FuelWindow();
                }

                AsyncTask myTask = new AsyncTask<Void, Void, Boolean>() {
                    Fuel f = new Fuel(0);

                    @Override
                    protected Boolean doInBackground(Void... voids) {
                        while (!(DataHandler.getInstance().detailedStatsReady(f)
                                && fuelFragment.hasLoaded())) {
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

                transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.StatsView, fuelFragment);
                transaction.addToBackStack(null);
                transaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
                transaction.commit();

            } else if (v == distanceBtn) {
                if (distFragment == null) {
                    distFragment = new DistTravWindow();
                }

                AsyncTask myTask = new AsyncTask<Void, Void, Boolean>() {
                    Distance d = new Distance(0);

                    @Override
                    protected Boolean doInBackground(Void... voids) {
                        while (!(DataHandler.getInstance().detailedStatsReady(d)
                                && distFragment.hasLoaded())) {
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

                transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.StatsView, distFragment);
                transaction.addToBackStack(null);
                transaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
                transaction.commit();

            } else if (v == overallBtn){
                Intent intent = new Intent(Stats.this, OverallStats.class);
                startActivity(intent);
            }
        }
    };


    @Override
    public void onBackPressed() {
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



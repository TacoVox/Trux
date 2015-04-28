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


public class Stats extends ActionBarActivity implements Serializable {
    Fragment fragment;
    DetailedStatsFragment newFragment;
    Button speedBtn, fuelBtn, distanceBtn, overallBtn;
    FragmentTransaction transaction;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        // Tell data handler to start downloading all stats
        DataHandler.getInstance().cacheDetailedStats();;
    }

    @Override
    protected void onResume() {
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

            newFragment = null;

            if (v == speedBtn) {
                newFragment = new SpeedWindow();

                // Make sure values are set once they are loaded
                AsyncTask myTask = new AsyncTask<Void, Void, Boolean>() {
                    Speed s = new Speed(0);

                    @Override
                    protected Boolean doInBackground(Void... voids) {
                        while (!DataHandler.getInstance().detailedStatsReady(s)) {
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
                        newFragment.setValues(DataHandler.getInstance().getDetailedStats(s));
                        newFragment.hideLoading();
                    }
                }.execute();



            } else if (v == fuelBtn) {

                newFragment = new FuelWindow();

                AsyncTask myTask = new AsyncTask<Void, Void, Boolean>() {
                    Fuel f = new Fuel(0);

                    @Override
                    protected Boolean doInBackground(Void... voids) {
                        while (!DataHandler.getInstance().detailedStatsReady(f)) {
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
                        newFragment.setValues(DataHandler.getInstance().getDetailedStats(f));
                        newFragment.hideLoading();
                    }
                }.execute();
                
            } else if (v == distanceBtn) {
                
                newFragment = new DistTravWindow();

                AsyncTask myTask = new AsyncTask<Void, Void, Boolean>() {
                    Distance d = new Distance(0);

                    @Override
                    protected Boolean doInBackground(Void... voids) {
                        while (!DataHandler.getInstance().detailedStatsReady(d)) {
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
                        newFragment.setValues(DataHandler.getInstance().getDetailedStats(d));
                        newFragment.hideLoading();
                    }
                }.execute();
            } else if (v == overallBtn){
                Intent intent = new Intent(Stats.this, OverallStats.class);
                startActivity(intent);
            }

            transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.StatsView, newFragment);
            transaction.addToBackStack(null);
            transaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
            transaction.commit();
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_stats, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void goToOverall(View view) {
        Intent intent = new Intent(this, OverallStats.class);
        startActivity(intent);
    }

    public void logout(MenuItem item){
        Intent intent = new Intent(Stats.this, MainActivity.class);
        startActivity(intent);
    }

    public void contact(MenuItem item){
        fragment = new Contact();
        transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.StatsView2, fragment);
        transaction.addToBackStack(null);
        transaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        transaction.commit();
    }
}



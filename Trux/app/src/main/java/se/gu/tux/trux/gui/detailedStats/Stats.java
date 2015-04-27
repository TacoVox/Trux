package se.gu.tux.trux.gui.detailedStats;


import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.Serializable;

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
 * Möjligtvis snygga till koden genom att:
 * Skapa en datatyp som innehåller today - total + linegraphseries
 * Ev låt DataHandler fylla HELA den med data?
 * Skapa en metod i stats som returnerar dessa mot en key (t ex speed)
 * OM de är satta, annars null
 * Låt en inre klass av asynctask fråga regelbundet
 * Skicka hela datatypen till DetailedStatsFragment.setValues när den är laddad == inte null
 *
 *
 */


public class Stats extends ActionBarActivity implements Serializable {

    DetailedStatsFragment newFragment;
    Button speedBtn, fuelBtn, distanceBtn;
    FragmentTransaction transaction;

    private volatile Speed speedToday;
    private volatile Speed speedWeek;
    private volatile Speed speedMonth;
    private volatile Speed speedTotal;
    private volatile LineGraphSeries speedValues;
    private volatile Boolean speedLoaded = false;

    private volatile Fuel fuelToday;
    private volatile Fuel fuelWeek;
    private volatile Fuel fuelMonth;
    private volatile Fuel fuelTotal;
    private volatile LineGraphSeries fuelValues;
    private volatile boolean fuelLoaded = false;

    private volatile Distance distanceToday;
    private volatile Distance distanceWeek;
    private volatile Distance distanceMonth;
    private volatile Distance distanceTotal;
    private volatile LineGraphSeries distanceValues;
    private volatile boolean distanceLoaded = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);


        speedBtn = (Button) findViewById(R.id.speed_button);
        fuelBtn = (Button) findViewById(R.id.fuel_button);
        distanceBtn = (Button) findViewById(R.id.distance_traveled);

        Button.OnClickListener btnOnClick = new ButtonListener();

        speedBtn.setOnClickListener(btnOnClick);
        fuelBtn.setOnClickListener(btnOnClick);
        distanceBtn.setOnClickListener(btnOnClick);

        getValues();
    }

     class ButtonListener implements Button.OnClickListener {

        @Override
        public void onClick(final View v) {
            newFragment = null;
            if (v == speedBtn) {
                newFragment = new SpeedWindow();
                // Make sure values are set once they are loaded
                new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] objects) {
                        while (!speedLoaded) {
                            try { Thread.sleep(100); } catch (InterruptedException e) {}
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                newFragment.setValues(speedToday, speedWeek, speedMonth, speedTotal,
                                        speedValues);
                            }
                        });
                        return null;
                    }
                }.execute();
            } else if (v == fuelBtn) {
                newFragment = new FuelWindow();

                new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] objects) {
                        while (!fuelLoaded) {
                            try { Thread.sleep(100); } catch (InterruptedException e) {}
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                newFragment.setValues(fuelToday, fuelWeek, fuelMonth, fuelTotal,
                                        fuelValues);
                            }
                        });
                        return null;
                    }
                }.execute();
            } else if (v == distanceBtn) {
                newFragment = new DistTravWindow();

                new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] objects) {
                        while (!distanceLoaded) {
                            try { Thread.sleep(100); } catch (InterruptedException e) {}
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                newFragment.setValues(distanceToday, distanceWeek, distanceMonth, distanceTotal,
                                        distanceValues);
                            }
                        });
                        return null;
                    }
                }.execute();
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
    public void getValues() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    speedToday = (Speed) DataHandler.getInstance().getData(new Speed(MetricData.DAY));
                    speedWeek = (Speed) DataHandler.getInstance().getData(new Speed(MetricData.WEEK));
                    speedMonth = (Speed) DataHandler.getInstance().getData(new Speed(MetricData.THIRTYDAYS));
                    speedTotal = (Speed) DataHandler.getInstance().getData(new Speed(MetricData.FOREVER));

                    // Create speed object and mark it with current time.
                    // Then request an array of speed average values for last 30 days.
                    Speed mySpeed = new Speed(0);
                    mySpeed.setTimeStamp(System.currentTimeMillis());
                    Data[] avgSpeedPerDay = DataHandler.getInstance().getPerDay(mySpeed, 30);
                    DataPoint[] speedPoints = getDataPoints(avgSpeedPerDay);
                    speedValues = new LineGraphSeries(speedPoints);

                    speedLoaded = true;
                    System.out.println("SPEED LOADED");


                    fuelToday = (Fuel) DataHandler.getInstance().getData(new Fuel(MetricData.DAY));
                    fuelWeek = (Fuel) DataHandler.getInstance().getData(new Fuel(MetricData.WEEK));
                    fuelMonth = (Fuel) DataHandler.getInstance().getData(new Fuel(MetricData.THIRTYDAYS));
                    fuelTotal = (Fuel) DataHandler.getInstance().getData(new Fuel(MetricData.FOREVER));

                    Fuel myFuel = new Fuel(0);
                    myFuel.setTimeStamp(System.currentTimeMillis());
                    Data[] avgFuelPerDay = DataHandler.getInstance().getPerDay(myFuel, 30);
                    DataPoint[] fuelPoints = getDataPoints(avgFuelPerDay);
                    fuelValues = new LineGraphSeries(fuelPoints);

                    fuelLoaded = true;
                    System.out.println("FUEL LOADED");


                    distanceToday = (Distance) DataHandler.getInstance().getData(new Distance(MetricData.DAY));
                    distanceWeek = (Distance) DataHandler.getInstance().getData(new Distance(MetricData.WEEK));
                    distanceMonth = (Distance) DataHandler.getInstance().getData(new Distance(MetricData.THIRTYDAYS));
                    distanceTotal = (Distance) DataHandler.getInstance().getData(new Distance(MetricData.FOREVER));

                    Distance myDistance = new Distance(0);
                    myDistance.setTimeStamp(System.currentTimeMillis());
                    Data[] avgDistancePerDay = DataHandler.getInstance().getPerDay(myDistance, 30);
                    DataPoint[] distancePoints = getDataPoints(avgDistancePerDay);
                    distanceValues = new LineGraphSeries(distancePoints);

                    distanceLoaded = true;
                    System.out.println("DISTANCE LOADED");
                }
                catch (NotLoggedInException nLIE){
                    System.out.println("NotLoggedInException: " + nLIE.getMessage());
                    Intent intent = new Intent(Stats.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        }).start();
    }

    public DataPoint[] getDataPoints(Data[] data) {

        DataPoint[] dataPoints = new DataPoint[30];
        for (int i = 0; i < 30; i++) {
            if (data[i].getValue() == null) {
                System.out.println("Assuming 0 at null value at pos: " + i );
                dataPoints[i] = new DataPoint(i + 1, 0);
            } else {
                if(!speedLoaded || !fuelLoaded)
                    dataPoints[i] = new DataPoint(i + 1, (Double)(data[i]).getValue());
                else
                    dataPoints[i] = new DataPoint(i + 1, (Long)(data[i]).getValue());
            }
        }
        return dataPoints;
    }
}



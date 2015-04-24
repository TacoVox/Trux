package se.gu.tux.trux.gui.detailedStats;


import android.app.FragmentTransaction;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.jjoe64.graphview.series.DataPoint;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import java.lang.reflect.Array;
import java.util.Calendar;
import java.util.Date;

import se.gu.tux.trux.appplication.DataHandler;
import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.Distance;
import se.gu.tux.trux.datastructure.Fuel;
import se.gu.tux.trux.datastructure.MetricData;
import se.gu.tux.trux.datastructure.Speed;
import se.gu.tux.trux.gui.MainActivity;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import tux.gu.se.trux.R;


public class Stats extends ActionBarActivity {

    Fragment newFragment;
    Button speedBtn, fuelBtn, distanceBtn, overallBtn;
    FragmentTransaction transaction;

    Speed speedToday;
    Speed speedWeek;
    Speed speedMonth;
    Speed speedTotal;

    Fuel fuelToday;
    Fuel fuelWeek;
    Fuel fuelMonth;
    Fuel fuelTotal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);


        speedBtn = (Button) findViewById(R.id.speed_button);
        fuelBtn = (Button) findViewById(R.id.fuel_button);
        distanceBtn = (Button) findViewById(R.id.distance_traveled);


        speedBtn.setOnClickListener(btnOnClick);
        fuelBtn.setOnClickListener(btnOnClick);
        distanceBtn.setOnClickListener(btnOnClick);

    }
    Button.OnClickListener btnOnClick = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == speedBtn) {
                newFragment = new SpeedWindow();
            }
            if (v == fuelBtn) {
                newFragment = new FuelWindow();
            }
            if (v == distanceBtn) {
                newFragment = new DistTravWindow();
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
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                try {
                    speedToday = (Speed) DataHandler.getInstance().getData(new Speed(MetricData.DAY));
                    speedWeek = (Speed) DataHandler.getInstance().getData(new Speed(MetricData.WEEK));
                    speedMonth = (Speed) DataHandler.getInstance().getData(new Speed(MetricData.THIRTYDAYS));
                    speedTotal = (Speed) DataHandler.getInstance().getData(new Speed(MetricData.FOREVER));

                    final Fuel fuelToday = (Fuel) DataHandler.getInstance().getData(new Fuel(MetricData.DAY));
                    final Fuel fuelWeek = (Fuel) DataHandler.getInstance().getData(new Fuel(MetricData.WEEK));
                    final Fuel fuelMonth = (Fuel) DataHandler.getInstance().getData(new Fuel(MetricData.THIRTYDAYS));
                    final Fuel fuelTotal = (Fuel) DataHandler.getInstance().getData(new Fuel(MetricData.FOREVER));

                    // Create speed object and mark it with current time.
                    // Then request an array of speed average values for last 30 days.
                    Speed mySpeed = new Speed(0);
                    mySpeed.setTimeStamp(System.currentTimeMillis());
                    Data[] avgSpeedPerDay = DataHandler.getInstance().getPerDay(mySpeed, 30);
                    DataPoint[] speedPoints = new DataPoint[30];
                    for (int i = 0; i < 30; i++) {
                        if (avgSpeedPerDay[i].getValue() == null) {
                            System.out.println("Assuming 0 at null value at pos: " + i);
                            speedPoints[i] = new DataPoint(i + 1, 0);
                        } else {
                            speedPoints[i] = new DataPoint(i + 1, (Double) (avgSpeedPerDay[i]).getValue());
                        }
                    }
                }
                catch (NotLoggedInException nLIE){
                    System.out.println("NotLoggedInException: " + nLIE.getMessage());
                    Intent intent = new Intent(Stats.this, MainActivity.class);
                    startActivity(intent);
                }

            return null;
            }
        }.execute();
    }
    public Speed getSpeedToday(){
      return speedToday;
    }
    public Speed getSpeedMonth(){
        return speedMonth;
    }
    public Speed getSpeedWeek(){
        return speedWeek;
    }
    public Speed getSpeedTotal(){
        return speedTotal;
    }

    public Fuel getFuelToday(){
        return fuelToday;
    }
    public Fuel getFuelWeek(){
        return fuelWeek;
    }
    public Fuel getFuelMonth(){
        return fuelMonth;
    }
    public Fuel getFuelTotal(){
        return fuelTotal;
    }
/*
    public Distance getDistanceToday(){
        return distanceToday;
    }
    public Distance getDistanceWeek(){
        return distanceWeek;
    }
    public Distance getDistanceMonth(){
        return distanceMonth;
    }
    public Distance getDistanceTotal(){
        return distanceTotal;
    }
    */
}



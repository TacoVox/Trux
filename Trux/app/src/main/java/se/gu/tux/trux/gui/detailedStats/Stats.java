package se.gu.tux.trux.gui.detailedStats;


import android.app.FragmentTransaction;
import android.app.Fragment;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import java.lang.reflect.Array;
import java.util.Calendar;
import java.util.Date;

import se.gu.tux.trux.datastructure.MetricData;
import tux.gu.se.trux.R;


public class Stats extends ActionBarActivity {

    Fragment newFragment;
    Button speedBtn, fuelBtn, distanceBtn, overallBtn;
    FragmentTransaction transaction;

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

}



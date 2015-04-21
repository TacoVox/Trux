package se.gu.tux.trux.gui.detailedStats;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import se.gu.tux.trux.gui.detailedStatistics.DistTravWindow;
import se.gu.tux.trux.gui.detailedStatistics.StatsScreen;
import se.gu.tux.trux.gui.detailedStats.DistanceTraveledWindow;
import se.gu.tux.trux.gui.detailedStats.FuelWindow;
import se.gu.tux.trux.gui.detailedStats.OverallStats;
import se.gu.tux.trux.gui.detailedStats.SpeedWindow;
import tux.gu.se.trux.R;


public class Stats extends ActionBarActivity {
    Fragment fragment;
    View myFragmentView;
    Button speedBtn, fuelBtn, distanceBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);


        speedBtn = (Button) findViewById(R.id.speed_button);
        fuelBtn = (Button) findViewById(R.id.fuel_button);
        distanceBtn = (Button) findViewById(R.id.distance_traveled_button);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        Stats myStatsScreen = new Stats();
        //  ft.add(R.id.Stats, myStatsScreen);
        ft.commit();

        speedBtn.setOnClickListener(btnOnClickListener);
        fuelBtn.setOnClickListener(btnOnClickListener);
        distanceBtn.setOnClickListener(btnOnClickListener);


    }

    Button.OnClickListener btnOnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            Fragment newFragment;

            if (v == speedBtn) {
                newFragment = new se.gu.tux.trux.gui.detailedStatistics.SpeedWindow();
            } else if (v == fuelBtn) {
                newFragment = new se.gu.tux.trux.gui.detailedStatistics.FuelWindow();
            } else if (v == distanceBtn) {
                newFragment = new DistTravWindow();
            }
            else newFragment = new StatsScreen();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.Stats, newFragment);
            ft.addToBackStack(null);
            ft.commit();

        }
    };




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

/*
    public void goToSpeed(final View view)
    {
        final Intent intent = new Intent(this, SpeedWindow.class);

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                view.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        startActivity(intent);
                    }
                });
            }

        }).start();

    }

    public void goToFuel(View view) {
        Intent intent = new Intent(this, FuelWindow.class);
        startActivity(intent);
    }

    public void goToDistanceTraveled(View view){
        Intent intent = new Intent(this, DistanceTraveledWindow.class);
        startActivity(intent);
    }

    public void goToOverallStats(View view){
        Intent intent = new Intent(this, OverallStats.class);
        startActivity(intent);
    }*/
}

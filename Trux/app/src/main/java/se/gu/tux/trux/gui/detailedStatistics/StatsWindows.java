package se.gu.tux.trux.gui.detailedStatistics;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;

import java.io.FileDescriptor;
import java.io.PrintWriter;

import se.gu.tux.trux.gui.simpleStats.FragmentPageAdapterSimpleUI;
import tux.gu.se.trux.R;

public class StatsWindows extends ActionBarActivity {
    Fragment fragment;
    ViewPager viewpager;
    FragmentPageAdapterDetailedUI ft;

    Button speedBtn, fuelBtn, distanceBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewpager = new ViewPager(this);
        viewpager.setId(R.id.pager);
        setContentView(viewpager);
        ft = new FragmentPageAdapterDetailedUI(getSupportFragmentManager());
        viewpager.setAdapter(ft);

/*
        setContantView(R.layout.fragment_stats_screen);
        speedBtn = (Button) findViewById(R.id.speed_button);
        fuelBtn = (Button) findViewById(R.id.fuel_button);
        distanceBtn = (Button) findViewById(R.id.distance_traveled_button);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        StatsScreen myStatsScreen = new StatsScreen();
        ft.add(R.id.myStatsScreen, myStatsScreen);
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
                newFragment = new SpeedWindow();
            } else if (v == fuelBtn) {
                newFragment = new FuelWindow();
            } else if (v == distanceBtn) {
                newFragment = new DistTravWindow();
            }

            FragmentTransaction ft =  getFragmentManager().beginTransaction();
          //  ft.replace(R.id.myStatsScreen, newFragment);
            ft.addToBackStack(null);
            ft.commit();

        }
    };*/
    }
}
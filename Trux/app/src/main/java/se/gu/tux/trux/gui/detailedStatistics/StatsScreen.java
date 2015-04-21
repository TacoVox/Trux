package se.gu.tux.trux.gui.detailedStatistics;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import tux.gu.se.trux.R;


public class StatsScreen extends Fragment {
    View myFragmentView;

    Button speedBtn, fuelBtn, distanceBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myFragmentView = inflater.inflate(R.layout.fragment_stats_screen, container, false);
        return myFragmentView;
    }
/*
        setContantView(R.layout.fragment_stats_screen);
        speedBtn = (Button)findViewById(R.id.speed_button);
        fuelBtn = (Button)findViewById(R.id.fuel_button);
        distanceBtn = (Button)findViewById(R.id.distance_traveled_button);

        speedBtn.setOnClickListener(btnOnClickListener);
        fuelBtn.setOnClickListener(btnOnClickListener);
        distanceBtn.setOnClickListener(btnOnClickListener);

        myFragmentView = inflater.inflate(R.layout.fragment_stats_screen, container, false);
        return myFragmentView;
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



        }
    };*/


}

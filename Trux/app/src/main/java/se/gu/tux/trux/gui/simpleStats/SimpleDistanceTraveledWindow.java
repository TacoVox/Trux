package se.gu.tux.trux.gui.simpleStats;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import se.gu.tux.trux.appplication.DataHandler;
import se.gu.tux.trux.datastructure.Distance;
import se.gu.tux.trux.datastructure.Fuel;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import tux.gu.se.trux.R;

public class SimpleDistanceTraveledWindow extends Fragment {

    private View myFragmentView;

    private TextView currentDistance;
    private Timer t;

    TimerTask timer;

    class myTask extends TimerTask {

        public void run() {
            try {
                final Distance dist = (Distance) DataHandler.getInstance().getData(new Distance(0));

                if (dist.getValue() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Long distance = (Long) dist.getValue() / 1000;
                            currentDistance.setText(distance.toString());
                        }
                    });
                }
            }
            catch (NotLoggedInException nLIE){
                System.out.println("NotLoggedInException: " + nLIE.getMessage());
            }

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        myFragmentView = inflater.inflate(R.layout.fragment_simple_distance_traveled_window, container, false);

        currentDistance = (TextView) myFragmentView.findViewById(R.id.currentDistance);

        t = new Timer();
        timer = new myTask();
        t.schedule(timer , 0 , 1000);

        return myFragmentView;
    }

    public void onStop(){
        super.onStop();

        t.cancel();
    }

}

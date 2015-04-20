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
import se.gu.tux.trux.datastructure.Speed;
import tux.gu.se.trux.R;

public class SimpleSpeedWindow extends Fragment
{

    private View myFragmentView;

    private volatile boolean running = true;

    TextView currentSpeed;

    Timer t;

    TimerTask timer = new TimerTask() {

        public void run() {

                final Speed speed = (Speed) DataHandler.getInstance().getData(new Speed(0));

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Speed speed = (Speed) dataHandler.signalIn(AutomotiveSignalId.FMS_WHEEL_BASED_SPEED, false);

                        currentSpeed.setText(String.format("%.1f km/h", speed.getValue()));
                    }
                });

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        myFragmentView = inflater.inflate(R.layout.fragment_simple_speed_window, container, false);

        currentSpeed = (TextView) myFragmentView.findViewById(R.id.currentSpeed);

        t = new Timer();
        t.schedule(timer , 1000 , 1000);

        return myFragmentView;
    }

}

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

    private TextView currentSpeed;
    private Timer t;

    TimerTask timer;

    class myTask extends TimerTask {

        public void run() {

                final Speed speed = (Speed) DataHandler.getInstance().getData(new Speed(0));

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        currentSpeed.setText(String.format("%.lf km/h", speed.getValue()));
                    }
                });

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        myFragmentView = inflater.inflate(R.layout.fragment_simple_speed_window, container, false);

        currentSpeed = (TextView) myFragmentView.findViewById(R.id.currentSpeed);

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

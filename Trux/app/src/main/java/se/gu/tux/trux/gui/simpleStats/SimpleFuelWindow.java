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
import se.gu.tux.trux.datastructure.Fuel;
import se.gu.tux.trux.datastructure.Speed;
import tux.gu.se.trux.R;

public class SimpleFuelWindow extends Fragment {

    private View myFragmentView;

    private TextView currentFuel;
    private Timer t;

    TimerTask timer;

    class myTask extends TimerTask {

        public void run() {

            final Fuel fuel = (Fuel) DataHandler.getInstance().getData(new Fuel(0));

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    currentFuel.setText(String.format("%f litres/h", fuel.getValue()));
                }
            });

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        myFragmentView = inflater.inflate(R.layout.fragment_simple_fuel_window, container, false);

        currentFuel = (TextView) myFragmentView.findViewById(R.id.currentFuel);

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

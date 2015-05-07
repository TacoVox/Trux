package se.gu.tux.trux.gui.simpleStats;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.datastructure.Fuel;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import tux.gu.se.trux.R;

public class SimpleFuelWindow extends Fragment {

    private View myFragmentView;

    private TextView currentFuel;
    private Timer t;

    TimerTask timer;

    class myTask extends TimerTask {

        public void run() {
            try {
                final Fuel fuel = (Fuel) DataHandler.getInstance().getData(new Fuel(0));

                if (fuel.getValue() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            currentFuel.setText(new Long(Math.round((Double) fuel.getValue())).toString());
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

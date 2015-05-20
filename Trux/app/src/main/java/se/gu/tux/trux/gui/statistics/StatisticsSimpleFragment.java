package se.gu.tux.trux.gui.statistics;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.datastructure.Distance;
import se.gu.tux.trux.datastructure.Fuel;
import se.gu.tux.trux.datastructure.Speed;
import se.gu.tux.trux.gui.main_home.HomeActivity;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import tux.gu.se.trux.R;

/**
 * Created by ivryashkov on 2015-05-05.
 */
public class StatisticsSimpleFragment extends Fragment
{
    private TextView currentSpeed;
    private TextView currentFuel;
    private TextView currentDistance;
    private Timer t;

    private TimerTask timer;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_statistics_simple, container, false);

        /*
        currentSpeed = (TextView) view.findViewById(R.id.currentSpeed);
        currentFuel = (TextView) view.findViewById(R.id.currentFuel);
        currentDistance = (TextView) view.findViewById(R.id.currentDistance);

        t = new Timer();
        timer = new myTask();
        t.schedule(timer, 0, 1000);
        */

        return view;
    }



    @Override
    public void onStop()
    {
        super.onStop();
        //t.cancel();
    }


    /**
     * Private class to post current collectable data to screen.
     */

    /*
    class myTask extends TimerTask {
        public void run() {
            try {
                final Speed s = (Speed) DataHandler.getInstance().getData(new Speed(0));
                final Fuel f = (Fuel) DataHandler.getInstance().getData(new Fuel(0));
                final Distance d = (Distance) DataHandler.getInstance().getData(new Distance(0));
                if (s.getValue() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            currentSpeed.setText(new Long(Math.round((Double) s.getValue())).toString());
                        }
                    });
                }
                if (f.getValue() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            currentFuel.setText(new Long(Math.round((Double) f.getValue())).toString());
                        }
                    });
                }
                if (d.getValue() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Long distance = (Long) d.getValue() / 1000;
                            currentDistance.setText(distance.toString());
                        }
                    });
                }
            } catch (NotLoggedInException nLIE){
                System.out.println("NotLoggedInException: " + nLIE.getMessage());
            }
        }
    }
    */

} // end class

package se.gu.tux.trux.gui.main_home;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import se.gu.tux.trux.appplication.DataHandler;
import se.gu.tux.trux.datastructure.Distance;
import se.gu.tux.trux.datastructure.Fuel;
import se.gu.tux.trux.datastructure.Speed;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import tux.gu.se.trux.R;

/**
 * Created by ivryashkov on 2015-05-05.
 */
public class IStatisticsFragment extends Fragment implements View.OnClickListener
{
    private TextView currentSpeed;
    private TextView currentFuel;
    private TextView currentDistance;
    private Timer t;

    TimerTask timer;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_main_i_statistics, container, false);

        // get the components
        Button statsButton = (Button) view.findViewById(R.id.fm_i_statistics_detailed_button);

        statsButton.setOnClickListener(this);

        currentSpeed = (TextView) view.findViewById(R.id.currentSpeed);
        currentSpeed = (TextView) view.findViewById(R.id.currentFuel);
        currentSpeed = (TextView) view.findViewById(R.id.currentDistance);

        t = new Timer();
        timer = new myTask();
        t.schedule(timer , 0 , 1000);

        return view;
    }


    @Override
    public void onClick(View view)
    {
        ( (HomeActivity) getActivity() ).onFragmentViewClick(view.getId());
    }


    public void onStop(){
        super.onStop();
        t.cancel();
    }


} // end class

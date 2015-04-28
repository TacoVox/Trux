package se.gu.tux.trux.gui.detailedStats;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjoe64.graphview.series.LineGraphSeries;

import se.gu.tux.trux.datastructure.MetricData;
import se.gu.tux.trux.datastructure.Speed;
import tux.gu.se.trux.R;

public class OverallTextWindow extends Fragment {

    View myFragmentView;

    TextView speedTextViewTotal, fuelTextViewTotal, distanceTextViewTotal;
    Stats values;
/*    final MetricData speedTotal = values.getTotalSpeed();
    final MetricData fuelTotal = values.getTotalFuel();
    final MetricData distanceTotal = values.getTotalDistance();

    public void setOverallValues(){
        if(speedTotal != null && fuelTotal != null && distanceTotal != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    speedTextViewTotal.setText(new Long(Math.round((Double) speedTotal.getValue())).toString());
                    fuelTextViewTotal.setText(new Long(Math.round((Double) fuelTotal.getValue())).toString());
                    distanceTextViewTotal.setText(new Long(Math.round((Double) distanceTotal.getValue())).toString());

                }
            });
        }
        }



*/
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        myFragmentView = inflater.inflate(R.layout.fragment_overall_text_window, container, false);

        speedTextViewTotal = (TextView) myFragmentView.findViewById(R.id.avg_speed_value);
        fuelTextViewTotal = (TextView) myFragmentView.findViewById(R.id.avg_fuel_value);
        distanceTextViewTotal = (TextView) myFragmentView.findViewById(R.id.avg_today_distance_traveled_value);

        return myFragmentView;
    }

}
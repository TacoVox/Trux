package se.gu.tux.trux.gui.detailedStats;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjoe64.graphview.series.LineGraphSeries;

import se.gu.tux.trux.appplication.DetailedStatsBundle;
import se.gu.tux.trux.datastructure.MetricData;
import se.gu.tux.trux.datastructure.Speed;
import tux.gu.se.trux.R;

public class OverallTextWindow extends Fragment {

    View myFragmentView;

    TextView speedTextViewTotal, fuelTextViewTotal, distanceTextViewTotal;
    Stats values;

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
        distanceTextViewTotal = (TextView) myFragmentView.findViewById(R.id.total_distance_traveled_value);
        myFragmentView.findViewById(R.id.loadingPanel).bringToFront();
        return myFragmentView;
    }

    public boolean hasLoaded() {
        if (myFragmentView != null) return true;
        return false;
    }

    public void setValues(DetailedStatsBundle speedBundle, DetailedStatsBundle fuelBundle,
                          DetailedStatsBundle distBundle) {
        if (speedBundle != null && speedTextViewTotal != null) {
            speedTextViewTotal.setText(
                    new Long(Math.round((Double) speedBundle.getTotal().getValue())).toString());
        }
        if (fuelBundle != null && fuelTextViewTotal != null) {
            fuelTextViewTotal.setText(
                    new Long(Math.round((Double) fuelBundle.getTotal().getValue())).toString());
        }
        if (distBundle != null && distanceTextViewTotal != null) {
            Long distTotal = (Long) distBundle.getTotal().getValue() / 1000;
            System.out.println(distBundle.getTotal().getValue().toString());
            distanceTextViewTotal.setText(distTotal.toString());
        }
        if (myFragmentView != null) {
            myFragmentView.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        }
    }
}
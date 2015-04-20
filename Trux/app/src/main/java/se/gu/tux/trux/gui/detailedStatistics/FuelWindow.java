package se.gu.tux.trux.gui.detailedStatistics;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import se.gu.tux.trux.appplication.DataHandler;
import se.gu.tux.trux.datastructure.MetricData;
import se.gu.tux.trux.datastructure.Fuel;
import tux.gu.se.trux.R;


public class FuelWindow extends Fragment {

    View myFragmentView;
    TextView fuelTextViewToday, fuelTextViewWeek, fuelTextViewMonth, fuelTextViewTotal;

    public void run() {

        final Fuel fuelToday = (Fuel) DataHandler.getInstance().getData(new Fuel(MetricData.DAY));
        final Fuel fuelWeek = (Fuel) DataHandler.getInstance().getData(new Fuel(MetricData.WEEK));
        final Fuel fuelMonth = (Fuel) DataHandler.getInstance().getData(new Fuel(MetricData.THIRTYDAYS));
        // final Fuel fuelTotal = (Fuel) DataHandler.getInstance().getData(new Fuel(MetricData.LIFETIME));
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Fuel fuel = (fuel) dataHandler.signalIn(AutomotiveSignalId.FMS_WHEEL_BASED_fuel, false);

                fuelTextViewToday.setText(String.format("%.1f l", fuelToday.getValue()));
                fuelTextViewWeek.setText(String.format("%.1f l", fuelWeek.getValue()));
                fuelTextViewMonth.setText(String.format("%.1f l", fuelMonth.getValue()));
                //   fuelTextViewTotal.setText(String.format("%.1f km/h", fuelTotal.getValue()));
            }
        });

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myFragmentView = inflater.inflate(R.layout.fragment_fuel_window, container, false);
        popFuelGraph(myFragmentView);
        fuelTextViewToday = (TextView) myFragmentView.findViewById(R.id.avg_today_fuel_value);
        fuelTextViewWeek = (TextView) myFragmentView.findViewById(R.id.avg_lastweek_fuel_value);
        fuelTextViewMonth = (TextView) myFragmentView.findViewById(R.id.avg_lastmonth_fuel_value);
        fuelTextViewTotal = (TextView) myFragmentView.findViewById(R.id.avg_total_fuel_value);
        return myFragmentView;


    }

    private void popFuelGraph(View view) {

        LineGraphSeries fuelValues = new LineGraphSeries(new DataPoint[]

                {
                        new DataPoint(0, 1),
                        new DataPoint(1, 5),
                        new DataPoint(2, 3),
                        new DataPoint(3, 2),
                        new DataPoint(4, 6)
                });
        GraphView fuelGraph = new GraphView(getActivity());
        fuelGraph.setTitle("Fuel Consumption");
        fuelGraph.setTitleTextSize(40);
        fuelGraph.getGridLabelRenderer().setVerticalAxisTitle("Avg Consumption");
        fuelGraph.getGridLabelRenderer().setHorizontalAxisTitle("Date");
        fuelGraph.addSeries(fuelValues);

        try {
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.FuelGraph);
            layout.addView(fuelGraph);
        } catch (NullPointerException e) {
            // something to handle the NPE.
        }
    }

}

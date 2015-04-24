package se.gu.tux.trux.gui.detailedStats;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Timer;
import java.util.TimerTask;

import se.gu.tux.trux.appplication.DataHandler;
import se.gu.tux.trux.datastructure.MetricData;
import se.gu.tux.trux.datastructure.Fuel;
import se.gu.tux.trux.datastructure.Speed;
import se.gu.tux.trux.gui.MainActivity;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import tux.gu.se.trux.R;


public class FuelWindow extends Fragment {

    TimerTask timer;
    private Timer t;

    View myFragmentView;
    TextView fuelTextViewToday, fuelTextViewWeek, fuelTextViewMonth, fuelTextViewTotal;

    class MyTask extends TimerTask {
        public void run() {
            try {
                final Fuel fuelToday = (Fuel) DataHandler.getInstance().getData(new Fuel(MetricData.DAY));
                final Fuel fuelWeek = (Fuel) DataHandler.getInstance().getData(new Fuel(MetricData.WEEK));
                final Fuel fuelMonth = (Fuel) DataHandler.getInstance().getData(new Fuel(MetricData.THIRTYDAYS));
                final Fuel fuelTotal = (Fuel) DataHandler.getInstance().getData(new Fuel(MetricData.FOREVER));

                if (fuelToday.getValue() != null && fuelWeek.getValue() != null
                        && fuelMonth.getValue() != null && fuelTotal != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fuelTextViewToday.setText(new Long(Math.round((Double) fuelToday.getValue())).toString());
                            fuelTextViewWeek.setText(new Long(Math.round((Double) fuelWeek.getValue())).toString());
                            fuelTextViewMonth.setText(new Long(Math.round((Double) fuelMonth.getValue())).toString());
                            fuelTextViewTotal.setText(new Long(Math.round((Double) fuelTotal.getValue())).toString());
                        }
                    });

                }
            }
            catch(NotLoggedInException nLIE){
                System.out.println("NotLoggedInException: " + nLIE.getMessage());
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            }
        }
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

        t = new Timer();
        timer = new MyTask();
        t.schedule(timer , 0 , 1000000);

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

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
import se.gu.tux.trux.datastructure.DetailedStatsBundle;
import se.gu.tux.trux.datastructure.MetricData;
import se.gu.tux.trux.datastructure.Fuel;
import se.gu.tux.trux.datastructure.Speed;
import se.gu.tux.trux.gui.MainActivity;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import tux.gu.se.trux.R;


public class FuelWindow extends DetailedStatsFragment {

    View myFragmentView;
    TextView fuelTextViewToday, fuelTextViewWeek, fuelTextViewMonth, fuelTextViewTotal;
    GraphView fuelGraph;

    @Override
    public void setValues(DetailedStatsBundle stats) {
        if (stats != null) {
            fuelTextViewToday.setText(new Long(Math.round((Double) stats.getToday().getValue())).toString());
            fuelTextViewWeek.setText(new Long(Math.round((Double) stats.getWeek().getValue())).toString());
            fuelTextViewMonth.setText(new Long(Math.round((Double) stats.getMonth().getValue())).toString());
            fuelTextViewTotal.setText(new Long(Math.round((Double) stats.getTotal().getValue())).toString());
            LineGraphSeries fuelValues = new LineGraphSeries(stats.getGraphPoints());
            fuelGraph.addSeries(fuelValues);
        }
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

        fuelGraph = new GraphView(getActivity());
        fuelGraph.setTitle("Fuel Consumption");
        fuelGraph.setTitleTextSize(40);
        fuelGraph.getGridLabelRenderer().setVerticalAxisTitle("Avg Consumption");
        fuelGraph.getGridLabelRenderer().setHorizontalAxisTitle("Date");

        try {
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.FuelGraph);
            layout.addView(fuelGraph);
        } catch (NullPointerException e) {
            // something to handle the NPE.
        }
    }


    public void hideLoading() {
        getView().findViewById(R.id.loadingPanel).setVisibility(View.GONE);
    }
}

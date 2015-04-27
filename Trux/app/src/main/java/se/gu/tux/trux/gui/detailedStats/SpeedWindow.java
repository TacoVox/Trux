package se.gu.tux.trux.gui.detailedStats;

import android.content.Context;
import android.content.Intent;
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
import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.MetricData;
import se.gu.tux.trux.datastructure.Speed;
import se.gu.tux.trux.gui.ChooseStatScreen;
import se.gu.tux.trux.gui.MainActivity;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import tux.gu.se.trux.R;

public class SpeedWindow extends DetailedStatsFragment {

    View myFragmentView;
    TextView speedTextViewToday, speedTextViewWeek, speedTextViewMonth, speedTextViewTotal;
    GraphView speedGraph;

    public void setValues(final MetricData speedToday, final MetricData speedWeek, final MetricData speedMonth,
                          final MetricData speedTotal, final LineGraphSeries speedValues) {
        if (speedToday.getValue() != null && speedWeek.getValue() != null
                && speedMonth.getValue() != null && speedTotal.getValue() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    speedTextViewToday.setText(new Long(Math.round((Double) speedToday.getValue())).toString());
                    speedTextViewWeek.setText(new Long(Math.round((Double) speedWeek.getValue())).toString());
                    speedTextViewMonth.setText(new Long(Math.round((Double) speedMonth.getValue())).toString());
                    speedTextViewTotal.setText(new Long(Math.round((Double) speedTotal.getValue())).toString());

                    speedGraph.addSeries(speedValues);
                }
            });

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        
        myFragmentView = inflater.inflate(R.layout.fragment_speed_window, container, false);

        speedTextViewToday = (TextView) myFragmentView.findViewById(R.id.avg_today_speed_value);
        speedTextViewWeek = (TextView) myFragmentView.findViewById(R.id.avg_lastweek_speed_value);
        speedTextViewMonth = (TextView) myFragmentView.findViewById(R.id.avg_lastmonth_speed_value);
        speedTextViewTotal = (TextView) myFragmentView.findViewById(R.id.avg_total_speed_value);

        popSpeedGraph(myFragmentView);

        return myFragmentView;
    }

    private void popSpeedGraph(View view) {

        speedGraph = new GraphView(getActivity());
        speedGraph.setTitle("Speed");
        speedGraph.setTitleTextSize(40);
        speedGraph.getGridLabelRenderer().setVerticalAxisTitle("Avg Speed");
        speedGraph.getGridLabelRenderer().setHorizontalAxisTitle("Date");

        try {
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.SpeedGraph);
            layout.addView(speedGraph);
        } catch (NullPointerException e) {
            // something to handle the NPE.
        }
    }


}

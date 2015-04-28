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
import se.gu.tux.trux.datastructure.Distance;
import se.gu.tux.trux.datastructure.MetricData;
import se.gu.tux.trux.datastructure.Speed;
import se.gu.tux.trux.gui.MainActivity;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import tux.gu.se.trux.R;


public class DistTravWindow extends DetailedStatsFragment {

    View myFragmentView;
    TextView distanceTextViewToday, distanceTextViewWeek, distanceTextViewMonth, distanceTextViewTotal;
    GraphView distanceGraph;


    @Override
    public void setValues(final DetailedStatsBundle stats) {
        if (stats != null) {
            Long distToday = (Long) stats.getToday().getValue() / 1000;
            Long distWeek = (Long) stats.getWeek().getValue() / 1000;
            Long distMonth = (Long) stats.getMonth().getValue() / 1000;
            Long distTotal = (Long) stats.getTotal().getValue() / 1000;

            distanceTextViewToday.setText(distToday.toString());
            distanceTextViewWeek.setText(distWeek.toString());
            distanceTextViewMonth.setText(distMonth.toString());
            distanceTextViewTotal.setText(distTotal.toString());

            LineGraphSeries distanceValues = new LineGraphSeries(stats.getGraphPoints());
            distanceGraph.addSeries(distanceValues);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myFragmentView = inflater.inflate(R.layout.fragment_dist_trav_window, container, false);

        distanceTextViewToday = (TextView) myFragmentView.findViewById(R.id.avg_today_distance_traveled_value);
        distanceTextViewWeek = (TextView) myFragmentView.findViewById(R.id.avg_lastweek_distance_traveled_value);
        distanceTextViewMonth = (TextView) myFragmentView.findViewById(R.id.avg_lastmonth_distance_traveled_value);
        distanceTextViewTotal = (TextView) myFragmentView.findViewById(R.id.avg_total_distance_traveled_value);

        popDistanceGraph(myFragmentView);

        return myFragmentView;


    }

    private void popDistanceGraph(View view) {

        distanceGraph = new GraphView(getActivity());
        distanceGraph.setTitle("Distance Traveled");
        distanceGraph.setTitleTextSize(40);
        distanceGraph.getGridLabelRenderer().setVerticalAxisTitle("Avg Distance");
        distanceGraph.getGridLabelRenderer().setHorizontalAxisTitle("Date");

        try {
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.DistanceGraph);
            layout.addView(distanceGraph);
        } catch (NullPointerException e) {
            // something to handle the NPE.
        }
    }


    public void hideLoading() {
        getView().findViewById(R.id.loadingPanel).setVisibility(View.GONE);
    }
}

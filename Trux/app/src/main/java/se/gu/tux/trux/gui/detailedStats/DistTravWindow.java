package se.gu.tux.trux.gui.detailedStats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.LineGraphSeries;

import se.gu.tux.trux.appplication.DetailedStatsBundle;
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
        distanceGraph.getViewport().setXAxisBoundsManual(true);
        distanceGraph.getViewport().setMaxX(30);

        try {
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.DistanceGraph);
            layout.addView(distanceGraph);
        } catch (NullPointerException e) {
            // something to handle the NPE.
        }
    }


    public void hideLoading() {
        myFragmentView.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
    }

    @Override
    public boolean hasLoaded() {
        if (distanceTextViewToday != null) return true;
        return false;
    }
}

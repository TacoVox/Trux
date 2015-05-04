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

public class SpeedWindow extends DetailedStatsFragment {

    View myFragmentView;
    TextView speedTextViewToday, speedTextViewWeek, speedTextViewMonth, speedTextViewTotal;
    GraphView speedGraph;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myFragmentView = inflater.inflate(R.layout.fragment_speed_window, container, false);

        speedTextViewToday = (TextView) myFragmentView.findViewById(R.id.avg_today_speed_value);
        speedTextViewWeek = (TextView) myFragmentView.findViewById(R.id.avg_lastweek_speed_value);
        speedTextViewMonth = (TextView) myFragmentView.findViewById(R.id.avg_lastmonth_speed_value);
        speedTextViewTotal = (TextView) myFragmentView.findViewById(R.id.avg_total_speed_value);

        popSpeedGraph(myFragmentView);
        myFragmentView.findViewById(R.id.loadingPanel).bringToFront();
        return myFragmentView;
    }

    private void popSpeedGraph(View view) {

        speedGraph = new GraphView(getActivity());
        speedGraph.getViewport().setXAxisBoundsManual(true);
        speedGraph.getGridLabelRenderer().setNumHorizontalLabels(7);
        speedGraph.getGridLabelRenderer().setNumHorizontalLabels(4);
        speedGraph.getGridLabelRenderer().setPadding(50);

        try {
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.SpeedGraph);
            layout.addView(speedGraph);
        } catch (NullPointerException e) {
            // something to handle the NPE.
        }
    }

    @Override
    public void setValues(DetailedStatsBundle stats) {
        if (stats != null) {
            speedTextViewToday.setText(new Long(Math.round((Double) stats.getToday().getValue())).toString() + " km/h");
            speedTextViewWeek.setText(new Long(Math.round((Double) stats.getWeek().getValue())).toString() + " km/h");
            speedTextViewMonth.setText(new Long(Math.round((Double) stats.getMonth().getValue())).toString() + " km/h");
            speedTextViewTotal.setText(new Long(Math.round((Double) stats.getTotal().getValue())).toString() + " km/h");
            LineGraphSeries speedValues = new LineGraphSeries(stats.getGraphPoints());
            speedGraph.addSeries(speedValues);
            speedGraph.getViewport().setMaxX(30);
            speedGraph.getViewport().setMaxY(150);
        }
    }

    public void hideLoading() {
        myFragmentView.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
    }

    @Override
    public boolean hasLoaded() {
        if (speedTextViewToday != null) return true;
        return false;
    }
}

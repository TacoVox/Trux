package se.gu.tux.trux.gui.detailedStats;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import se.gu.tux.trux.appplication.DetailedStatsBundle;
import tux.gu.se.trux.R;

public class OverallGraphWindow extends Fragment {
    private GraphView speedGraph, fuelGraph, distGraph;
    private View myFragmentView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myFragmentView = inflater.inflate(R.layout.fragment_overall_graph_window, container, false);
        popFuelGraph(myFragmentView);
        popDTGraph(myFragmentView);
        popSpeedGraph(myFragmentView);
        myFragmentView.findViewById(R.id.loadingPanel).bringToFront();
        return myFragmentView;
    }

    public boolean hasLoaded() {
        if (myFragmentView != null) return true;
        return false;
    }


    public void setValues(DetailedStatsBundle speedBundle, DetailedStatsBundle fuelBundle,
                          DetailedStatsBundle distBundle) {
        if (speedBundle != null && speedGraph!= null) {
            LineGraphSeries speedValues = new LineGraphSeries(speedBundle.getGraphPoints());
            speedGraph.addSeries(speedValues);
        }
        if (fuelBundle != null && fuelGraph != null) {
            LineGraphSeries fuelValues = new LineGraphSeries(fuelBundle.getGraphPoints());
            fuelGraph.addSeries(fuelValues);
        }
        if (distBundle != null && distGraph != null) {
            LineGraphSeries distValues = new LineGraphSeries(distBundle.getGraphPoints());
            distGraph.addSeries(distValues);
        }
        if (myFragmentView != null) {
            myFragmentView.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        }
    }


    private void popFuelGraph(View view) {
        fuelGraph = new GraphView(getActivity());
        fuelGraph.setTitle("Fuel Consumption");
        fuelGraph.setTitleTextSize(40);
        fuelGraph.getGridLabelRenderer().setVerticalAxisTitle("Avg Consumption");
        fuelGraph.getGridLabelRenderer().setHorizontalAxisTitle("Date");

        try {
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.fuelGraphOverall);
            layout.addView(fuelGraph);
        } catch (NullPointerException e) {
            // something to handle the NPE.
        }
    }

    private void popSpeedGraph(View view) {
        speedGraph = new GraphView(getActivity());
        speedGraph.setTitle("Speed");
        speedGraph.setTitleTextSize(40);
        speedGraph.getGridLabelRenderer().setVerticalAxisTitle("Avg Speed");
        speedGraph.getGridLabelRenderer().setHorizontalAxisTitle("Date");

        try {
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.speedGraphOverall);
            layout.addView(speedGraph);
        } catch (NullPointerException e) {
            // something to handle the NPE.
        }
    }

    private void popDTGraph(View view) {
        distGraph = new GraphView(getActivity());

        distGraph.setTitle("Distance Traveled");
        distGraph.setTitleTextSize(40);
        distGraph.getGridLabelRenderer().setVerticalAxisTitle("Avg Distance");
        distGraph.getGridLabelRenderer().setHorizontalAxisTitle("Date");

        try {
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.dTGraphOverall);
            layout.addView(distGraph);
        } catch (NullPointerException e) {
            // something to handle the NPE.
        }
    }
}
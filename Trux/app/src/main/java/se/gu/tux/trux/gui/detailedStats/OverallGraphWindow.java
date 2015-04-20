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

import tux.gu.se.trux.R;

public class OverallGraphWindow extends Fragment {

    View myFragmentView;

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
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.fuelGraphOverall);
            layout.addView(fuelGraph);
        } catch (NullPointerException e) {
            // something to handle the NPE.
        }
    }

    private void popSpeedGraph(View view) {

        LineGraphSeries fuelValues = new LineGraphSeries(new DataPoint[]

                {
                        new DataPoint(0, 1),
                        new DataPoint(1, 5),
                        new DataPoint(2, 3),
                        new DataPoint(3, 2),
                        new DataPoint(4, 6)
                });
        GraphView speedGraph = new GraphView(getActivity());
        speedGraph.setTitle("Speed");
        speedGraph.setTitleTextSize(40);
        speedGraph.getGridLabelRenderer().setVerticalAxisTitle("Avg Speed");
        speedGraph.getGridLabelRenderer().setHorizontalAxisTitle("Date");
        speedGraph.addSeries(fuelValues);

        try {
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.speedGraphOverall);
            layout.addView(speedGraph);
        } catch (NullPointerException e) {
            // something to handle the NPE.
        }
    }

    private void popDTGraph(View view) {

        LineGraphSeries fuelValues = new LineGraphSeries(new DataPoint[]

                {
                        new DataPoint(0, 1),
                        new DataPoint(1, 5),
                        new DataPoint(2, 3),
                        new DataPoint(3, 2),
                        new DataPoint(4, 6)
                });
        GraphView dTGraph = new GraphView(getActivity());

        dTGraph.setTitle("Distance Traveled");
        dTGraph.setTitleTextSize(40);
        dTGraph.getGridLabelRenderer().setVerticalAxisTitle("Avg Distance");
        dTGraph.getGridLabelRenderer().setHorizontalAxisTitle("Date");
        dTGraph.addSeries(fuelValues);

        try {
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.dTGraphOverall);
            layout.addView(dTGraph);
        } catch (NullPointerException e) {
            // something to handle the NPE.
        }
    }
}
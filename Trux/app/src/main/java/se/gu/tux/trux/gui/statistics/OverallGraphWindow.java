package se.gu.tux.trux.gui.statistics;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import se.gu.tux.trux.appplication.DataHandler;
import se.gu.tux.trux.appplication.DetailedStatsBundle;
import se.gu.tux.trux.datastructure.Distance;
import se.gu.tux.trux.datastructure.Fuel;
import se.gu.tux.trux.datastructure.Speed;
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
        popSpeedGraph(myFragmentView);
        popFuelGraph(myFragmentView);
        popDTGraph(myFragmentView);

        myFragmentView.findViewById(R.id.loadingPanel).bringToFront();
        return myFragmentView;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myFragmentView = getView();

        // Make sure values are set once they are loaded
        AsyncTask myTask = new AsyncTask<Void, Void, Boolean>()
        {
            Speed s = new Speed(0);
            Fuel f = new Fuel(0);
            Distance d = new Distance(0);

            @Override
            protected Boolean doInBackground(Void... voids)
            {
                while (!(DataHandler.getInstance().detailedStatsReady(s)
                        && DataHandler.getInstance().detailedStatsReady(f)
                        && DataHandler.getInstance().detailedStatsReady(d))) {
                    try {
                        Thread.sleep(100);
                        // Stop waiting if fragment was cancelled
                        if (!isVisible()) {
                            cancel(true);
                        }
                    } catch (InterruptedException e) {
                        System.out.println("Wait interrupted.");
                    }
                }
                return null;
            }

            @Override
            public void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Boolean b)
            {
                super.onPostExecute(b);
                setValues(DataHandler.getInstance().getDetailedStats(s),
                        DataHandler.getInstance().getDetailedStats(f),
                        DataHandler.getInstance().getDetailedStats(d));
            }
        }.execute();
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

        fuelGraph.getViewport().setXAxisBoundsManual(true);
        fuelGraph.getViewport().setMaxX(30);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 100, 0);
        fuelGraph.setLayoutParams(lp);

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

        speedGraph.getViewport().setXAxisBoundsManual(true);
        speedGraph.getViewport().setMaxX(30);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 100, 0);
        speedGraph.setLayoutParams(lp);

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

        distGraph.getViewport().setXAxisBoundsManual(true);
        distGraph.getViewport().setMaxX(30);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 100, 0);
        distGraph.setLayoutParams(lp);

        try {
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.dTGraphOverall);
            layout.addView(distGraph);
        } catch (NullPointerException e) {
            // something to handle the NPE.
        }
    }
}
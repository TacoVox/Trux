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
import com.jjoe64.graphview.series.LineGraphSeries;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.application.DetailedStatsBundle;
import se.gu.tux.trux.datastructure.Distance;
import se.gu.tux.trux.datastructure.Fuel;
import se.gu.tux.trux.datastructure.Speed;
import tux.gu.se.trux.R;

/**
 * This Fragment is responsible for displaying the monthly graphs for all
 * metrics (speed,fuel and distance traveled).
 */

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

        // Initialize the view.

        myFragmentView = inflater.inflate(R.layout.fragment_overall_graph_window, container, false);

        // Initialize the graph views.

        popSpeedGraph(myFragmentView);
        popFuelGraph(myFragmentView);
        popDTGraph(myFragmentView);

        // Return the view.

        return myFragmentView;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myFragmentView = getView();
        //refresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    /**
     * Here we have a new thread that checks if the data is fetched and ready to be
     * displayed. The data starts fetching when the StatisticsMainFragment have been
     * initialized (StatisticsMainFragment is initialized directly after a successful login).
     *
     * Until the data is ready a loadingscreen is shown, whenever the data is ready
     * the loadingscreen is removed and the setValues method is called.
     */

    public void refresh() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Speed s = new Speed(0);
                final Fuel f = new Fuel(0);
                final Distance d = new Distance(0);
                boolean cancelled = false;
                while (!cancelled && !(DataHandler.getInstance().detailedStatsReady(s)
                        && DataHandler.getInstance().detailedStatsReady(f)
                        && DataHandler.getInstance().detailedStatsReady(d))) {
                    try {
                        Thread.sleep(1000);
                        // Stop waiting if fragment was cancelled
                        if (!isVisible()) {
                           cancelled = true;
                        }
                    } catch (InterruptedException e) {
                        System.out.println("Wait interrupted.");
                    }
                }

                Activity a = getActivity();
                if (!cancelled && a != null) {
                    a.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setValues(DataHandler.getInstance().getDetailedStats(s),
                                    DataHandler.getInstance().getDetailedStats(f),
                                    DataHandler.getInstance().getDetailedStats(d));
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * This method retrieves the data from bundles that are retrieved and cached in
     * he DataHandler class, and changes the points in the graphs accordingly.
     *
     * @param speedBundle The speed detailed stats
     * @param fuelBundle The fuel detailed stats
     * @param distBundle The distance detailed stats
     *
     */

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

        // Hide the loadingscreen.

        if (myFragmentView != null) {
            myFragmentView.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        }
    }

    /**
     * In this method we give the fuel graph some layout parameters and add it
     * to a container.
     *
     * @param view
     */

    private void popFuelGraph(View view) {

        fuelGraph = new GraphView(getActivity());

        fuelGraph.getViewport().setXAxisBoundsManual(true);
        fuelGraph.getViewport().setYAxisBoundsManual(true);
        fuelGraph.getGridLabelRenderer().setNumHorizontalLabels(7);
        fuelGraph.getGridLabelRenderer().setNumHorizontalLabels(4);
        fuelGraph.getGridLabelRenderer().setPadding(50);
        fuelGraph.getViewport().setMaxX(30);
        fuelGraph.getViewport().setMaxY(80);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        fuelGraph.setLayoutParams(lp);

        try {
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.fuelGraphOverall);
            layout.addView(fuelGraph);
        } catch (NullPointerException e) {
            // something to handle the NPE.
        }
    }

    /**
     * In this method we give the speed graph some layout parameters and add it
     * to a container.
     *
     * @param view
     */

    private void popSpeedGraph(View view) {

        speedGraph = new GraphView(getActivity());

        speedGraph.getViewport().setXAxisBoundsManual(true);
        speedGraph.getViewport().setYAxisBoundsManual(true);
        speedGraph.getGridLabelRenderer().setNumHorizontalLabels(7);
        speedGraph.getGridLabelRenderer().setNumHorizontalLabels(4);
        speedGraph.getGridLabelRenderer().setNumVerticalLabels(4);
        speedGraph.getGridLabelRenderer().setPadding(50);
        speedGraph.getViewport().setMaxX(30);
        speedGraph.getViewport().setMaxY(150);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        speedGraph.setLayoutParams(lp);

        try {
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.speedGraphOverall);
            layout.addView(speedGraph);
        } catch (NullPointerException e) {
            // something to handle the NPE.
        }
    }

    /**
     * In this method we give the distance graph some layout parameters and add it
     * to a container.
     *
     * @param view
     */

    private void popDTGraph(View view) {

        distGraph = new GraphView(getActivity());

        distGraph.getViewport().setXAxisBoundsManual(true);
        distGraph.getViewport().setYAxisBoundsManual(true);
        distGraph.getGridLabelRenderer().setNumHorizontalLabels(7);
        distGraph.getGridLabelRenderer().setNumHorizontalLabels(4);
        distGraph.getGridLabelRenderer().setPadding(50);
        distGraph.getViewport().setMaxX(30);
        distGraph.getViewport().setMaxY(1000);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        distGraph.setLayoutParams(lp);

        try {
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.dTGraphOverall);
            layout.addView(distGraph);
        } catch (NullPointerException e) {
            // something to handle the NPE.
        }
    }
}
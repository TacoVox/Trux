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
        //refresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    public void refresh() {
        System.out.println("REFRESHING Overall....");

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

        /*
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
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                 */
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

        fuelGraph.getViewport().setXAxisBoundsManual(true);
        fuelGraph.getViewport().setYAxisBoundsManual(true);
        fuelGraph.getGridLabelRenderer().setNumHorizontalLabels(7);
        fuelGraph.getGridLabelRenderer().setNumHorizontalLabels(4);
        fuelGraph.getGridLabelRenderer().setPadding(50);
        fuelGraph.getViewport().setMaxX(30);
        fuelGraph.getViewport().setMaxY(800);

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

    private void popDTGraph(View view) {

        distGraph = new GraphView(getActivity());

        distGraph.getViewport().setXAxisBoundsManual(true);
        distGraph.getViewport().setYAxisBoundsManual(true);
        distGraph.getGridLabelRenderer().setNumHorizontalLabels(7);
        distGraph.getGridLabelRenderer().setNumHorizontalLabels(4);
        distGraph.getGridLabelRenderer().setPadding(50);
        distGraph.getViewport().setMaxX(30);
        distGraph.getViewport().setMaxY(100);

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
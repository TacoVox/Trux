package se.gu.tux.trux.gui.statistics;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.LineGraphSeries;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.application.DetailedStatsBundle;
import se.gu.tux.trux.datastructure.Distance;
import se.gu.tux.trux.datastructure.Speed;
import tux.gu.se.trux.R;

/**
 * This Fragment is responsible for displaying the distance the logged in user have traveled.
 * The data is displayed in different timeframes, daily, weekly, monthly and total.
 */

public class DistTravWindow extends Fragment {

    View myFragmentView;
    TextView distanceTextViewToday, distanceTextViewWeek, distanceTextViewMonth, distanceTextViewTotal;
    GraphView distanceGraph;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Initialize the view.

        myFragmentView = inflater.inflate(R.layout.fragment_dist_trav_window, container, false);

        // Initialize the text views.

        distanceTextViewToday = (TextView) myFragmentView.findViewById(R.id.avg_today_distance_traveled_value);
        distanceTextViewWeek = (TextView) myFragmentView.findViewById(R.id.avg_lastweek_distance_traveled_value);
        distanceTextViewMonth = (TextView) myFragmentView.findViewById(R.id.avg_lastmonth_distance_traveled_value);
        distanceTextViewTotal = (TextView) myFragmentView.findViewById(R.id.avg_total_distance_traveled_value);

        // Initialize the graph view.

        popDistanceGraph(myFragmentView);

        return myFragmentView;


    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myFragmentView = getView();
    }

    @Override
    public void onResume() {
        super.onResume();

        /**
         * Here we have a new thread that checks if the data is fetched and ready to be
         * displayed. The data starts fetching when the StatisticsMainFragment have been
         * initialized (StatisticsMainFragment is initialized directly after a successful login).
         *
         * Until the data is ready a loadingscreen is shown, whenever the data is ready
         * the loadingscreen is removed and the setValues method is called.
         */

        new Thread(new Runnable() {
            @Override
            public void run() {
                final Distance d = new Distance(0);
                boolean cancelled = false;

                while (!cancelled && !DataHandler.getInstance().detailedStatsReady(d)) {
                    try {
                        Thread.sleep(200);
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
                            setValues(DataHandler.getInstance().getDetailedStats(d));
                            hideLoading();
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * This method retrieves the data from a bundle that is sent
     * from the DataHandler class, and changes the textViews accordingly.
     *
     * @param stats
     */

    public void setValues(DetailedStatsBundle stats) {
        if (stats != null) {

            /**
             * Divide the stats fetched from the bundle by 1000 to get the values in km
             * instead of meters (The data fetched from AGA is fecthed in meters).
             */

            Long distToday = (Long) stats.getToday().getValue() / 1000;
            Long distWeek = (Long) stats.getWeek().getValue() / 1000;
            Long distMonth = (Long) stats.getMonth().getValue() / 1000;
            Long distTotal = (Long) stats.getTotal().getValue() / 1000;

            // Set the textViews to show the data.

            distanceTextViewToday.setText(distToday.toString() + " km");
            distanceTextViewWeek.setText(distWeek.toString() + " km");
            distanceTextViewMonth.setText(distMonth.toString() + " km");
            distanceTextViewTotal.setText(distTotal.toString() + " km");

            // Initialize the graph with values collected from the bundle.

            LineGraphSeries distanceValues = new LineGraphSeries(stats.getGraphPoints());
            distanceGraph.addSeries(distanceValues);
        }
    }

    /**
     * In this method we give the graph some layout parameters and add it
     * to a container.
     *
     * @param view
     */

    private void popDistanceGraph(View view) {

        distanceGraph = new GraphView(getActivity());

        distanceGraph.getViewport().setXAxisBoundsManual(true);
        distanceGraph.getViewport().setYAxisBoundsManual(true);
        distanceGraph.getGridLabelRenderer().setNumHorizontalLabels(7);
        distanceGraph.getGridLabelRenderer().setNumHorizontalLabels(4);
        distanceGraph.getGridLabelRenderer().setPadding(50);
        distanceGraph.getViewport().setMaxX(30);
        distanceGraph.getViewport().setMaxY(1000);

        try {
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.DistanceGraph);
            layout.addView(distanceGraph);
        } catch (NullPointerException e) {
            // something to handle the NPE.
        }
    }

    // A helper method to hide the loading screen

    public void hideLoading() {
        myFragmentView.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
    }
}

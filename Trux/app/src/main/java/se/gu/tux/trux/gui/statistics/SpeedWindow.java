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
import se.gu.tux.trux.datastructure.Fuel;
import se.gu.tux.trux.datastructure.Speed;
import tux.gu.se.trux.R;

/**
 * This Fragment is responsible for displaying the average speed of the current user.
 * The data is displayed in different timeframes, daily, weekly, monthly and total.
 */

public class SpeedWindow extends Fragment {

    View myFragmentView;
    TextView speedTextViewToday, speedTextViewWeek, speedTextViewMonth, speedTextViewTotal;
    GraphView speedGraph;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Initialize the view.

        myFragmentView = inflater.inflate(R.layout.fragment_speed_window, container, false);

        // Initialize the text views.

        speedTextViewToday = (TextView) myFragmentView.findViewById(R.id.avg_today_speed_value);
        speedTextViewWeek = (TextView) myFragmentView.findViewById(R.id.avg_lastweek_speed_value);
        speedTextViewMonth = (TextView) myFragmentView.findViewById(R.id.avg_lastmonth_speed_value);
        speedTextViewTotal = (TextView) myFragmentView.findViewById(R.id.avg_total_speed_value);

        // Initialize the graph view.

        popSpeedGraph(myFragmentView);

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
                final Speed s = new Speed(0);
                boolean cancelled = false;

                while (!cancelled && !DataHandler.getInstance().detailedStatsReady(s)) {
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
                            setValues(DataHandler.getInstance().getDetailedStats(s));
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

            // Set the textViews to show the data.

            speedTextViewToday.setText(new Long(Math.round((Double) stats.getToday().getValue())).toString() + " km/h");
            speedTextViewWeek.setText(new Long(Math.round((Double) stats.getWeek().getValue())).toString() + " km/h");
            speedTextViewMonth.setText(new Long(Math.round((Double) stats.getMonth().getValue())).toString() + " km/h");
            speedTextViewTotal.setText(new Long(Math.round((Double) stats.getTotal().getValue())).toString() + " km/h");

            // Initialize the graph with values collected from the bundle.

            LineGraphSeries speedValues = new LineGraphSeries(stats.getGraphPoints());
            speedGraph.addSeries(speedValues);
        }
    }

    /**
     * In this method we give the graph some layout parameters and add it
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

        try {
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.SpeedGraph);
            layout.addView(speedGraph);
        } catch (NullPointerException e) {
            // something to handle the NPE.
        }
    }

    // A helper method to hide the loading screen

    public void hideLoading() {
        myFragmentView.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
    }
}

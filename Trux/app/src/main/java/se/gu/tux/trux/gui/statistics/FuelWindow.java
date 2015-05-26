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
 * This Fragment is responsible for displaying the fuel consumption of the current user.
 * The data is displayed in different timeframes, daily, weekly, monthly and total.
 */

public class FuelWindow extends Fragment {

    View myFragmentView;

    TextView fuelTextViewToday, fuelTextViewWeek, fuelTextViewMonth, fuelTextViewTotal;
    GraphView fuelGraph;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Initialize the view.

        myFragmentView = inflater.inflate(R.layout.fragment_fuel_window, container, false);

        // Initialize the text views.

        fuelTextViewToday = (TextView) myFragmentView.findViewById(R.id.avg_today_fuel_value);
        fuelTextViewWeek = (TextView) myFragmentView.findViewById(R.id.avg_lastweek_fuel_value);
        fuelTextViewMonth = (TextView) myFragmentView.findViewById(R.id.avg_lastmonth_fuel_value);
        fuelTextViewTotal = (TextView) myFragmentView.findViewById(R.id.avg_total_fuel_value);

        // Initialize the graph view.

        popFuelGraph(myFragmentView);

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
                final Fuel f = new Fuel(0);
                boolean cancelled = false;
                while (!cancelled && !(DataHandler.getInstance().detailedStatsReady(f))) {
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
                            setValues(DataHandler.getInstance().getDetailedStats(f));
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

            fuelTextViewToday.setText(new Long(Math.round((Double) stats.getToday().getValue())).toString() + " L/h");
            fuelTextViewWeek.setText(new Long(Math.round((Double) stats.getWeek().getValue())).toString() + " L/h");
            fuelTextViewMonth.setText(new Long(Math.round((Double) stats.getMonth().getValue())).toString() + " L/h");
            fuelTextViewTotal.setText(new Long(Math.round((Double) stats.getTotal().getValue())).toString() + " L/h");

            // Initialize the graph with values collected from the bundle.

            LineGraphSeries fuelValues = new LineGraphSeries(stats.getGraphPoints());
            fuelGraph.addSeries(fuelValues);

        }
    }

    /**
     * In this method we give the graph some layout parameters and add it
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

        try {
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.FuelGraph);
            layout.addView(fuelGraph);
        } catch (NullPointerException e) {
            // something to handle the NPE.
        }
    }

    // A helper method to hide the loading screen

    public void hideLoading() {
        myFragmentView.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
    }
}

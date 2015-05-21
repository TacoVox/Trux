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


public class FuelWindow extends Fragment {

    View myFragmentView;

    TextView fuelTextViewToday, fuelTextViewWeek, fuelTextViewMonth, fuelTextViewTotal;
    GraphView fuelGraph;

    public void setValues(DetailedStatsBundle stats) {
        if (stats != null) {
            fuelTextViewToday.setText(new Long(Math.round((Double) stats.getToday().getValue())).toString() + " L/h");
            fuelTextViewWeek.setText(new Long(Math.round((Double) stats.getWeek().getValue())).toString() + " L/h");
            fuelTextViewMonth.setText(new Long(Math.round((Double) stats.getMonth().getValue())).toString() + " L/h");
            fuelTextViewTotal.setText(new Long(Math.round((Double) stats.getTotal().getValue())).toString() + " L/h");
            LineGraphSeries fuelValues = new LineGraphSeries(stats.getGraphPoints());
            fuelGraph.addSeries(fuelValues);
            fuelGraph.invalidate();

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myFragmentView = inflater.inflate(R.layout.fragment_fuel_window, container, false);

        popFuelGraph(myFragmentView);

        fuelTextViewToday = (TextView) myFragmentView.findViewById(R.id.avg_today_fuel_value);
        fuelTextViewWeek = (TextView) myFragmentView.findViewById(R.id.avg_lastweek_fuel_value);
        fuelTextViewMonth = (TextView) myFragmentView.findViewById(R.id.avg_lastmonth_fuel_value);
        fuelTextViewTotal = (TextView) myFragmentView.findViewById(R.id.avg_total_fuel_value);

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


    private void popFuelGraph(View view) {

        fuelGraph = new GraphView(getActivity());

        fuelGraph.getViewport().setXAxisBoundsManual(true);
        fuelGraph.getViewport().setYAxisBoundsManual(true);
        fuelGraph.getGridLabelRenderer().setNumHorizontalLabels(7);
        fuelGraph.getGridLabelRenderer().setNumHorizontalLabels(4);
        fuelGraph.getGridLabelRenderer().setPadding(50);
        fuelGraph.getViewport().setMaxX(30);
        fuelGraph.getViewport().setMaxY(800);

        try {
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.FuelGraph);
            layout.addView(fuelGraph);
        } catch (NullPointerException e) {
            // something to handle the NPE.
        }
    }


    public void hideLoading() {
        myFragmentView.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
    }
}

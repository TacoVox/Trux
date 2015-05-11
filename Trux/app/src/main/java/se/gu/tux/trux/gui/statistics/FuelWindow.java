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
import se.gu.tux.trux.datastructure.Fuel;
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
            fuelGraph.getViewport().setMaxX(30);
            fuelGraph.getViewport().setMaxY(800);

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

        AsyncTask myTask = new AsyncTask<Void, Void, Boolean>()
        {
            Fuel f = new Fuel(0);

            @Override
            protected Boolean doInBackground(Void... voids)
            {
                while (!(DataHandler.getInstance().detailedStatsReady(f)))
                {
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
            protected void onPostExecute(Boolean b) {
                super.onPostExecute(b);
                setValues(DataHandler.getInstance().getDetailedStats(f));
                hideLoading();
            }
        }.execute();
    }


    private void popFuelGraph(View view) {

        fuelGraph = new GraphView(getActivity());
        fuelGraph.setTitleTextSize(40);
        fuelGraph.getViewport().setXAxisBoundsManual(true);
        fuelGraph.getGridLabelRenderer().setNumHorizontalLabels(7);
        fuelGraph.getGridLabelRenderer().setNumHorizontalLabels(4);
        fuelGraph.getGridLabelRenderer().setPadding(50);

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

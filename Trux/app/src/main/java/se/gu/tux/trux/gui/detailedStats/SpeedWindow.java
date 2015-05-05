package se.gu.tux.trux.gui.detailedStats;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.LineGraphSeries;

import se.gu.tux.trux.appplication.DataHandler;
import se.gu.tux.trux.appplication.DetailedStatsBundle;
import se.gu.tux.trux.datastructure.Speed;
import tux.gu.se.trux.R;

public class SpeedWindow extends Fragment {

    View myFragmentView;
    TextView speedTextViewToday, speedTextViewWeek, speedTextViewMonth, speedTextViewTotal;
    GraphView speedGraph;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myFragmentView = inflater.inflate(R.layout.fragment_speed_window, container, false);

        speedTextViewToday = (TextView) myFragmentView.findViewById(R.id.avg_today_speed_value);
        speedTextViewWeek = (TextView) myFragmentView.findViewById(R.id.avg_lastweek_speed_value);
        speedTextViewMonth = (TextView) myFragmentView.findViewById(R.id.avg_lastmonth_speed_value);
        speedTextViewTotal = (TextView) myFragmentView.findViewById(R.id.avg_total_speed_value);

        popSpeedGraph(myFragmentView);
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

            @Override
            protected Boolean doInBackground(Void... voids)
            {
                while (!(DataHandler.getInstance().detailedStatsReady(s)))
                {
                    try {
                        Thread.sleep(100);
                        // Stop waiting if fragment was cancelled
                        if (!isAdded()) {
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
                setValues(DataHandler.getInstance().getDetailedStats(s));
                hideLoading();
            }
        }.execute();
    }

    private void popSpeedGraph(View view) {

        speedGraph = new GraphView(getActivity());
        speedGraph.getViewport().setXAxisBoundsManual(true);
        speedGraph.getGridLabelRenderer().setNumHorizontalLabels(7);
        speedGraph.getGridLabelRenderer().setNumHorizontalLabels(4);
        speedGraph.getGridLabelRenderer().setPadding(50);

        try {
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.SpeedGraph);
            layout.addView(speedGraph);
        } catch (NullPointerException e) {
            // something to handle the NPE.
        }
    }

    public void setValues(DetailedStatsBundle stats) {
        if (stats != null) {
            speedTextViewToday.setText(new Long(Math.round((Double) stats.getToday().getValue())).toString() + " km/h");
            speedTextViewWeek.setText(new Long(Math.round((Double) stats.getWeek().getValue())).toString() + " km/h");
            speedTextViewMonth.setText(new Long(Math.round((Double) stats.getMonth().getValue())).toString() + " km/h");
            speedTextViewTotal.setText(new Long(Math.round((Double) stats.getTotal().getValue())).toString() + " km/h");
            LineGraphSeries speedValues = new LineGraphSeries(stats.getGraphPoints());
            speedGraph.addSeries(speedValues);
            speedGraph.getViewport().setMaxX(30);
            speedGraph.getViewport().setMaxY(150);
        }
    }

    public void hideLoading() {
        myFragmentView.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
    }
}

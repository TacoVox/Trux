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
import tux.gu.se.trux.R;


public class DistTravWindow extends Fragment {

    View myFragmentView;
    TextView distanceTextViewToday, distanceTextViewWeek, distanceTextViewMonth, distanceTextViewTotal;
    GraphView distanceGraph;


    public void setValues(DetailedStatsBundle stats) {
        if (stats != null) {
            Long distToday = (Long) stats.getToday().getValue() / 1000;
            Long distWeek = (Long) stats.getWeek().getValue() / 1000;
            Long distMonth = (Long) stats.getMonth().getValue() / 1000;
            Long distTotal = (Long) stats.getTotal().getValue() / 1000;

            distanceTextViewToday.setText(distToday.toString() + " km");
            distanceTextViewWeek.setText(distWeek.toString() + " km");
            distanceTextViewMonth.setText(distMonth.toString() + " km");
            distanceTextViewTotal.setText(distTotal.toString() + " km");

            LineGraphSeries distanceValues = new LineGraphSeries(stats.getGraphPoints());
            distanceGraph.addSeries(distanceValues);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myFragmentView = inflater.inflate(R.layout.fragment_dist_trav_window, container, false);

        distanceTextViewToday = (TextView) myFragmentView.findViewById(R.id.avg_today_distance_traveled_value);
        distanceTextViewWeek = (TextView) myFragmentView.findViewById(R.id.avg_lastweek_distance_traveled_value);
        distanceTextViewMonth = (TextView) myFragmentView.findViewById(R.id.avg_lastmonth_distance_traveled_value);
        distanceTextViewTotal = (TextView) myFragmentView.findViewById(R.id.avg_total_distance_traveled_value);

        popDistanceGraph(myFragmentView);
        myFragmentView.findViewById(R.id.loadingPanel).bringToFront();
        return myFragmentView;


    }

    private void popDistanceGraph(View view) {

        distanceGraph = new GraphView(getActivity());
        distanceGraph.setTitle("Distance Traveled");
        distanceGraph.setTitleTextSize(40);
        distanceGraph.getViewport().setXAxisBoundsManual(true);
        distanceGraph.getViewport().setMaxX(30);

        try {
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.DistanceGraph);
            layout.addView(distanceGraph);
        } catch (NullPointerException e) {
            // something to handle the NPE.
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myFragmentView = getView();

        AsyncTask myTask = new AsyncTask<Void, Void, Boolean>() {
            Distance d = new Distance(0);

            @Override
            protected Boolean doInBackground(Void... voids)
            {
                while (!(DataHandler.getInstance().detailedStatsReady(d)))
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
                setValues(DataHandler.getInstance().getDetailedStats(d));
                hideLoading();
            }
        }.execute();
    }

    public void hideLoading() {
        myFragmentView.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
    }
}

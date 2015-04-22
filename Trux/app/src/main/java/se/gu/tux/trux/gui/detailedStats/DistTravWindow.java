package se.gu.tux.trux.gui.detailedStats;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import se.gu.tux.trux.appplication.DataHandler;
import se.gu.tux.trux.datastructure.Distance;
import se.gu.tux.trux.datastructure.MetricData;
import tux.gu.se.trux.R;


public class DistTravWindow extends Fragment {

    View myFragmentView;
    TextView distanceTextViewToday, distanceTextViewWeek, distanceTextViewMonth, distanceTextViewTotal;

    public void run() {

        final Distance distanceToday = (Distance) DataHandler.getInstance().getData(new Distance(MetricData.DAY));
        final Distance distanceWeek = (Distance) DataHandler.getInstance().getData(new Distance(MetricData.WEEK));
        final Distance distanceMonth = (Distance) DataHandler.getInstance().getData(new Distance(MetricData.THIRTYDAYS));
        // final Distance distanceTotal = (Distance) DataHandler.getInstance().getData(new Distance(MetricData.LIFETIME));
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Distance distance = (Distance) dataHandler.signalIn(AutomotiveSignalId.FMS_WHEEL_BASED_DISTANCE, false);

                distanceTextViewToday.setText(String.format("%.1f km", distanceToday.getValue()));
                distanceTextViewWeek.setText(String.format("%.1f km", distanceWeek.getValue()));
                distanceTextViewMonth.setText(String.format("%.1f km", distanceMonth.getValue()));
                //   distanceTextViewTotal.setText(String.format("%.1f km", distanceTotal.getValue()));
            }
        });

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myFragmentView = inflater.inflate(R.layout.fragment_dist_trav_window, container, false);
        popDistanceGraph(myFragmentView);
        distanceTextViewToday = (TextView) myFragmentView.findViewById(R.id.avg_today_distance_traveled_value);
        distanceTextViewWeek = (TextView) myFragmentView.findViewById(R.id.avg_lastweek_distance_traveled_value);
        distanceTextViewMonth = (TextView) myFragmentView.findViewById(R.id.avg_lastmonth_distance_traveled_value);
        distanceTextViewTotal = (TextView) myFragmentView.findViewById(R.id.avg_total_distance_traveled_value);
        return myFragmentView;


    }

    private void popDistanceGraph(View view) {

        LineGraphSeries distanceValues = new LineGraphSeries(new DataPoint[]

                {
                        new DataPoint(0, 1),
                        new DataPoint(1, 5),
                        new DataPoint(2, 3),
                        new DataPoint(3, 2),
                        new DataPoint(4, 6)
                });
        GraphView distanceGraph = new GraphView(getActivity());
        distanceGraph.setTitle("Distance Traveled");
        distanceGraph.setTitleTextSize(40);
        distanceGraph.getGridLabelRenderer().setVerticalAxisTitle("Avg Distance");
        distanceGraph.getGridLabelRenderer().setHorizontalAxisTitle("Date");
        distanceGraph.addSeries(distanceValues);

        try {
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.DistanceGraph);
            layout.addView(distanceGraph);
        } catch (NullPointerException e) {
            // something to handle the NPE.
        }
    }

}

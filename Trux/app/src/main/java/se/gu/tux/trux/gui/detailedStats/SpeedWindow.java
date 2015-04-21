package se.gu.tux.trux.gui.detailedStats;

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
import se.gu.tux.trux.datastructure.MetricData;
import se.gu.tux.trux.datastructure.Speed;
import tux.gu.se.trux.R;

public class SpeedWindow extends Fragment {

    View myFragmentView;
    TextView speedTextViewToday, speedTextViewWeek, speedTextViewMonth, speedTextViewTotal;

    public void run() {

        final Speed speedToday = (Speed) DataHandler.getInstance().getData(new Speed(MetricData.DAY));
        final Speed speedWeek = (Speed) DataHandler.getInstance().getData(new Speed(MetricData.WEEK));
        final Speed speedMonth = (Speed) DataHandler.getInstance().getData(new Speed(MetricData.THIRTYDAYS));
       // final Speed speedTotal = (Speed) DataHandler.getInstance().getData(new Speed(MetricData.LIFETIME));
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Speed speed = (Speed) dataHandler.signalIn(AutomotiveSignalId.FMS_WHEEL_BASED_SPEED, false);

                speedTextViewToday.setText(String.format("%.1f km/h", speedToday.getValue()));
                speedTextViewWeek.setText(String.format("%.1f km/h", speedWeek.getValue()));
                speedTextViewMonth.setText(String.format("%.1f km/h", speedMonth.getValue()));
             //   speedTextViewTotal.setText(String.format("%.1f km/h", speedTotal.getValue()));
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

        myFragmentView = inflater.inflate(R.layout.fragment_speed_window, container, false);
        popSpeedGraph(myFragmentView);
        speedTextViewToday = (TextView) myFragmentView.findViewById(R.id.avg_today_speed_value);
        speedTextViewWeek = (TextView) myFragmentView.findViewById(R.id.avg_lastweek_speed_value);
        speedTextViewMonth = (TextView) myFragmentView.findViewById(R.id.avg_lastmonth_speed_value);
        speedTextViewTotal = (TextView) myFragmentView.findViewById(R.id.avg_total_speed_value);
        return myFragmentView;


    }

    private void popSpeedGraph(View view) {

        LineGraphSeries speedValues = new LineGraphSeries(new DataPoint[]

                {
                        new DataPoint(0, 1),
                        new DataPoint(1, 5),
                        new DataPoint(2, 3),
                        new DataPoint(3, 2),
                        new DataPoint(4, 6)
                });
        GraphView fuelGraph = new GraphView(getActivity());
        fuelGraph.setTitle("Speed");
        fuelGraph.setTitleTextSize(40);
        fuelGraph.getGridLabelRenderer().setVerticalAxisTitle("Avg Speed");
        fuelGraph.getGridLabelRenderer().setHorizontalAxisTitle("Date");
        fuelGraph.addSeries(speedValues);

        try {
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.SpeedGraph);
            layout.addView(fuelGraph);
        } catch (NullPointerException e) {
            // something to handle the NPE.
        }
    }


}

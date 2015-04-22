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

import java.util.Timer;
import java.util.TimerTask;

import se.gu.tux.trux.appplication.DataHandler;
import se.gu.tux.trux.datastructure.MetricData;
import se.gu.tux.trux.datastructure.Speed;
import tux.gu.se.trux.R;

public class SpeedWindow extends Fragment {

    TimerTask timer;
    private Timer t;

    View myFragmentView;
    TextView speedTextViewToday, speedTextViewWeek, speedTextViewMonth, speedTextViewTotal;

    class MyTask extends TimerTask {
        public void run() {
            
            final Speed speedToday = (Speed) DataHandler.getInstance().getData(new Speed(MetricData.DAY));
            final Speed speedWeek = (Speed) DataHandler.getInstance().getData(new Speed(MetricData.WEEK));
            final Speed speedMonth = (Speed) DataHandler.getInstance().getData(new Speed(MetricData.THIRTYDAYS));
            final Speed speedTotal = (Speed) DataHandler.getInstance().getData(new Speed(MetricData.FOREVER));

            if (speedToday.getValue() != null && speedWeek.getValue() != null && speedMonth.getValue() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        speedTextViewToday.setText(new Long(Math.round((Double) speedToday.getValue())).toString());
                        speedTextViewWeek.setText(new Long(Math.round((Double) speedWeek.getValue())).toString());
                        speedTextViewMonth.setText(new Long(Math.round((Double) speedMonth.getValue())).toString());
                        speedTextViewTotal.setText(new Long(Math.round((Double) speedTotal.getValue())).toString());
                    }
                });

            }
        }
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

        t = new Timer();
        timer = new MyTask();
        t.schedule(timer , 0 , 1000000);

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

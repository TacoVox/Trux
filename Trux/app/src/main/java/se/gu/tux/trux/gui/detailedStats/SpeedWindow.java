package se.gu.tux.trux.gui.detailedStats;

import android.content.Intent;
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
import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.MetricData;
import se.gu.tux.trux.datastructure.Speed;
import se.gu.tux.trux.gui.ChooseStatScreen;
import se.gu.tux.trux.gui.MainActivity;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import tux.gu.se.trux.R;

public class SpeedWindow extends Fragment {

    TimerTask timer;
    private Timer t;

    View myFragmentView;
    TextView speedTextViewToday, speedTextViewWeek, speedTextViewMonth, speedTextViewTotal;
    GraphView speedGraph;
    LineGraphSeries speedValues;

    class MyTask extends TimerTask {
        public void run() {
            try {
                final Speed speedToday = (Speed) DataHandler.getInstance().getData(new Speed(MetricData.DAY));
                final Speed speedWeek = (Speed) DataHandler.getInstance().getData(new Speed(MetricData.WEEK));
                final Speed speedMonth = (Speed) DataHandler.getInstance().getData(new Speed(MetricData.THIRTYDAYS));
                final Speed speedTotal = (Speed) DataHandler.getInstance().getData(new Speed(MetricData.FOREVER));


                // Create speed object and mark it with current time.
                // Then request an array of speed average values for last 30 days.
                Speed mySpeed = new Speed(0);
                mySpeed.setTimeStamp(System.currentTimeMillis());
                Data[] avgSpeedPerDay = DataHandler.getInstance().getPerDay(mySpeed, 30);
                DataPoint[] speedPoints = new DataPoint[30];
                for (int i = 0; i < 30; i++) {
                    if (avgSpeedPerDay[i].getValue() == null) {
                        System.out.println("Assuming 0 at null value at pos: " + i );
                        speedPoints[i] = new DataPoint(i + 1, 0);
                    } else {
                        speedPoints[i] = new DataPoint(i + 1, (Double)(avgSpeedPerDay[i]).getValue());
                    }
                }
                speedValues = new LineGraphSeries(speedPoints);

                if (speedToday.getValue() != null && speedWeek.getValue() != null
                        && speedMonth.getValue() != null && speedTotal.getValue() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(speedToday.getValue() ==   new Double(0.0))
                            System.out.println(speedToday.getValue()+ "Hejsankaksdfkasdkfaksdfkasdfk");
                            speedTextViewToday.setText(new Long(Math.round((Double) speedToday.getValue())).toString());
                            speedTextViewWeek.setText(new Long(Math.round((Double) speedWeek.getValue())).toString());
                            speedTextViewMonth.setText(new Long(Math.round((Double) speedMonth.getValue())).toString());
                            speedTextViewTotal.setText(new Long(Math.round((Double) speedTotal.getValue())).toString());

                            // Update the graph
                            speedGraph.addSeries(speedValues);
                        }
                    });

                }

            }
            catch(NotLoggedInException nLIE){
                System.out.println("NotLoggedInException: " + nLIE.getMessage());

                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myFragmentView = inflater.inflate(R.layout.fragment_speed_window, container, false);
        speedTextViewToday = (TextView) myFragmentView.findViewById(R.id.avg_today_speed_value);
        speedTextViewWeek = (TextView) myFragmentView.findViewById(R.id.avg_lastweek_speed_value);
        speedTextViewMonth = (TextView) myFragmentView.findViewById(R.id.avg_lastmonth_speed_value);
        speedTextViewTotal = (TextView) myFragmentView.findViewById(R.id.avg_total_speed_value);

        popSpeedGraph(myFragmentView);

        t = new Timer();
        timer = new MyTask();
        t.schedule(timer , 0 , 1000000);

        return myFragmentView;


    }

    private void popSpeedGraph(View view) {

        speedGraph = new GraphView(getActivity());
        speedGraph.setTitle("Speed");
        speedGraph.setTitleTextSize(40);
        speedGraph.getGridLabelRenderer().setVerticalAxisTitle("Avg Speed");
        speedGraph.getGridLabelRenderer().setHorizontalAxisTitle("Date");

        try {
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.SpeedGraph);
            layout.addView(speedGraph);
        } catch (NullPointerException e) {
            // something to handle the NPE.
        }
    }


}

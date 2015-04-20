package se.gu.tux.trux.gui.detailedStats;


import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.swedspot.automotiveapi.AutomotiveSignalId;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import se.gu.tux.trux.datastructure.MetricData;
import se.gu.tux.trux.datastructure.Speed;
import se.gu.tux.trux.appplication.DataHandler;
import tux.gu.se.trux.R;



public class SpeedWindow extends ActionBarActivity
{

    private DataHandler dataHandler;

    // controls the thread
    private volatile boolean running = true;

    

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed);

        final TextView speedTextViewToday = (TextView) findViewById(R.id.avg_today_speed_value);
        final TextView speedTextViewWeek = (TextView) findViewById(R.id.avg_lastweek_speed_value);
        final TextView speedTextViewMonth = (TextView) findViewById(R.id.avg_lastmonth_speed_value);
        final TextView speedTextViewTotal = (TextView) findViewById(R.id.avg_total_speed_value);
        //dataHandler = DataHandler.getInstance();

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (running)
                {
                    //Speed speed = (Speed) dataHandler.signalIn(AutomotiveSignalId.FMS_WHEEL_BASED_SPEED, false);
                    final Speed speedToday = (Speed) DataHandler.getInstance().getData(new Speed(MetricData.DAY));
                    //Speed speed = (Speed) dataHandler.signalIn(AutomotiveSignalId.FMS_WHEEL_BASED_SPEED, false);
                    final Speed speedWeek = (Speed) DataHandler.getInstance().getData(new Speed(MetricData.WEEK));
                    //Speed speed = (Speed) dataHandler.signalIn(AutomotiveSignalId.FMS_WHEEL_BASED_SPEED, false);
                    final Speed speedMonth = (Speed) DataHandler.getInstance().getData(new Speed(MetricData.THIRTYDAYS));
                    final Speed speedTotal = (Speed) DataHandler.getInstance().getData(new Speed(Long.MAX_VALUE));
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {

                            //setting the TextView Strings to the correct value
                            speedTextViewToday.setText(String.format("%.1f km/h", speedToday.getValue()));
                            speedTextViewWeek.setText(String.format("%.1f km/h", speedWeek.getValue()));
                            speedTextViewMonth.setText(String.format("%.1f km/h", speedMonth.getValue()));
                            speedTextViewTotal.setText(String.format("%.1f km/h", speedTotal.getValue()));
                        }
                    });

                    // pause for 1 second
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e)
                    {
                        // TODO: assume app is shutting down, do any cleanup
                        e.printStackTrace();
                    }

                } // end while

            } // end run()

        }).start();

        GraphView graph = (GraphView) findViewById(R.id.speedGraph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(30, 12)
        });
        graph.setTitle("Speed");
        graph.setTitleTextSize(40);
        graph.getGridLabelRenderer().setVerticalAxisTitle("Avg Speed");
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Date");
        graph.addSeries(series);

    } // end onCreate()


    @Override
    protected void onStop()
    {
        super.onStop();

        // activity is not active, stop the thread from execution
        // reduce memory usage
        running = false;
    }


    @Override
    protected void onStart()
    {
        super.onStart();

        // activity is active, resume thread
        running = true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_speed, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);

    }


} // end class

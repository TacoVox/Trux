package se.gu.tux.trux.gui.detailedStats;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import tux.gu.se.trux.R;

public class DistanceTraveledWindow extends ActionBarActivity {

    /*
    final TextView avgTodDTV = (TextView) findViewById(R.id.avg_today_distance_traveled_value);
    final TextView avgLwDTV = (TextView) findViewById(R.id.avg_lastweek_distance_traveled_value);
    final TextView avgLmDTV = (TextView) findViewById(R.id.avg_lastmonth_distance_traveled_value);
    final TextView avgTotDTV = (TextView) findViewById(R.id.avg_total_distance_traveled_value);
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distance_traveled);

        GraphView graph = (GraphView) findViewById(R.id.distanceGraph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        graph.setTitle("Distance Traveled");
        graph.setTitleTextSize(40);
        graph.getGridLabelRenderer().setVerticalAxisTitle("Avg Distance");
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Date");
        graph.addSeries(series);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_distance_traveled, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

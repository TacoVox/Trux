package se.gu.tux.trux.gui;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import tux.gu.se.trux.R;

public class DistanceTraveledWindow extends ActionBarActivity {

    final TextView avgTodDTV = (TextView) findViewById(R.id.avg_today_distance_traveled_value);
    final TextView avgLwDTV = (TextView) findViewById(R.id.avg_lastweek_distance_traveled_value);
    final TextView avgLmDTV = (TextView) findViewById(R.id.avg_lastmonth_distance_traveled_value);
    final TextView avgTotDTV = (TextView) findViewById(R.id.avg_total_distance_traveled_value);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distance_traveled);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_distance__traveled, menu);
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

package se.gu.tux.trux.gui;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import tux.gu.se.trux.R;

public class FuelWindow extends ActionBarActivity {

    /*
    final TextView avgTodFV = (TextView) findViewById(R.id.avg_today_fuel_value);
    final TextView avgLwFV = (TextView) findViewById(R.id.avg_lastweek_fuel_value);
    final TextView avgLmFV = (TextView) findViewById(R.id.avg_lastmonth_fuel_value);
    final TextView avgTotFV = (TextView) findViewById(R.id.avg_total_fuel_value);
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fuel, menu);
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

package se.gu.tux.trux.gui;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import se.gu.tux.trux.datastructure.Fuel;
import se.gu.tux.trux.technical_services.DataHandler;
import tux.gu.se.trux.R;

public class Speed extends ActionBarActivity {

    DataHandler data_conn;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed);

        final TextView ds = (TextView) findViewById(R.id.display_todays_speed);

        new AsyncTask()        {
            @Override
            protected Object doInBackground(Object[] objects)
            {
                Fuel fuel = new Fuel(0);
                data_conn = DataHandler.getInstance();
                fuel = (Fuel) data_conn.signalIn(fuel);

                ds.setText(String.format("%.1f km/h", fuel.getValue()));

                return null;
            }

        }.execute();






    } // end onCreate()


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_speed, menu);
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

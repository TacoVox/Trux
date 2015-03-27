package se.gu.tux.trux.gui;


import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.swedspot.automotiveapi.AutomotiveSignalId;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import se.gu.tux.trux.datastructure.Speed;
import se.gu.tux.trux.technical_services.DataHandler;
import tux.gu.se.trux.R;



public class SpeedWindow extends ActionBarActivity
{

    DataHandler dataHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed);

        final TextView ds = (TextView) findViewById(R.id.display_todays_speed);

        dataHandler = DataHandler.getInstance();

        ds.post(new Runnable()
        {
            @Override
            public void run()
            {
                Speed speed = new Speed(0);
                speed = (Speed) dataHandler.signalIn(AutomotiveSignalId.FMS_WHEEL_BASED_SPEED, speed);
                Float value = speed.getValue();

                System.out.println("------------------------------------------");
                System.out.println("value in speed object is: " + value);
                System.out.println("------------------------------------------");

                ds.setText(String.format("%.1f km/h", value));
            }
        });

    } // end onCreate()


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

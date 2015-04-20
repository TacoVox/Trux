package se.gu.tux.trux.gui.detailedStats;


import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.swedspot.automotiveapi.AutomotiveSignalId;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

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

        final TextView speedTextView = (TextView) findViewById(R.id.display_todays_speed);

        dataHandler = DataHandler.getInstance();

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (running)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Speed speed = (Speed) dataHandler.signalIn(AutomotiveSignalId.FMS_WHEEL_BASED_SPEED, false);

                            speedTextView.setText(String.format("%.1f km/h", speed.getValue()));
                        }
                    });

                    // pause for 1 second
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }

                } // end while

            } // end run()

        }).start();

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

package se.gu.tux.trux.gui;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import se.gu.tux.trux.appplication.DataHandler;
import se.gu.tux.trux.datastructure.Speed;
import se.gu.tux.trux.gui.detailedStats.Stats;
import se.gu.tux.trux.gui.simpleStats.SimpleStats;
import tux.gu.se.trux.R;


public class DriverHomeScreen extends BaseAppActivity
{


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home_screen);
    }


    @Override
    public void onStop()
    {
        super.onStop();
    }


    @Override
    public void onResume()
    {
        super.onResume();
    }


    @Override
    public void onPause()
    {
        super.onPause();
    }



    public void goToStats(View view)
    {
        Intent rich = new Intent(DriverHomeScreen.this, Stats.class);
        Intent simple = new Intent(DriverHomeScreen.this, SimpleStats.class);

        try
        {
            final Speed speed = (Speed) DataHandler.getInstance().getData(new Speed(0));

            if (speed.getValue() != null && (Double) speed.getValue() > 0)
            {
                startActivity(simple);
            }
            else
            {
                startActivity(rich);
            }

        }
        catch (Exception e) { e.printStackTrace(); }

    } // end goToStats()


    //These lines are commented-out for now when we are doing the super.class to hold the menu items

/*

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            this.finish();
        } else {
            getFragmentManager().popBackStack();
        }
    }

*/


} // end class

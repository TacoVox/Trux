package se.gu.tux.trux.gui;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import se.gu.tux.trux.appplication.DataHandler;
import se.gu.tux.trux.datastructure.Speed;
import se.gu.tux.trux.gui.detailedStats.Stats;
import se.gu.tux.trux.gui.simpleStats.SimpleStats;
import se.gu.tux.trux.gui.statistics_i.IStatisticsActivity;
import tux.gu.se.trux.R;


public class DriverHomeScreen extends BaseAppActivity
{

    // layout id
    private static final int LAYOUT_ID = R.layout.activity_driver_home_screen;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // set layout for this view
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT_ID);

        // set current view
        setCurrentViewId(LAYOUT_ID);
    }


    @Override
    public void onStop()
    {
        super.onStop();
    }


    @Override
    public void onResume()
    {
        setCurrentViewId(LAYOUT_ID);
        super.onResume();
    }


    @Override
    public void onPause()
    {
        super.onPause();
    }



    public void goToStats(View view)
    {
        // for testing only
        Intent rich = new Intent(DriverHomeScreen.this, IStatisticsActivity.class);

        //Intent rich = new Intent(DriverHomeScreen.this, Stats.class);
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



    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            // From the driver home screen, just hide the app if back is pressed if nothing else
            // is showing
            moveTaskToBack(true);
        } else {
            getFragmentManager().popBackStack();
        }
    }



} // end class

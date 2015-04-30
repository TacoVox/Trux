package se.gu.tux.trux.gui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import se.gu.tux.trux.gui.detailedStats.Stats;
import se.gu.tux.trux.gui.simpleStats.SimpleStats;
import tux.gu.se.trux.R;



public class ChooseStatScreen extends BaseAppActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_stat_screen);
    }


    public void goToStats(View view)
    {
        Intent intent = new Intent(this, Stats.class);
        startActivity(intent);
    }


    public void goToSimpleStats(View view)
    {
        Intent intent = new Intent(this, SimpleStats.class);
        startActivity(intent);
    }


} // end class

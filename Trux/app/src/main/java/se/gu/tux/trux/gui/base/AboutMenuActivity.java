package se.gu.tux.trux.gui.base;

import android.os.Bundle;

import tux.gu.se.trux.R;

/**
 * Created by ivryashkov on 2015-04-30.
 *
 * Simple activity to display information about the developers and the application.
 */
public class AboutMenuActivity extends BaseAppActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_menu_activity);
    }


} // end class

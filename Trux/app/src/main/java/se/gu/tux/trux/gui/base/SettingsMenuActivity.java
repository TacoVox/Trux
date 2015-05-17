package se.gu.tux.trux.gui.base;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;

import tux.gu.se.trux.R;

public class SettingsMenuActivity extends BaseAppActivity
{
    private Switch safetySwitch = (Switch) findViewById(R.id.safetySwitch);
    private Spinner mapTypes = (Spinner) findViewById(R.id.mapSpinner);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_menu_activity);
    }

}

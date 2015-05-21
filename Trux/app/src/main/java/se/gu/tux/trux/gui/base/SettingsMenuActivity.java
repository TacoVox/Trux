package se.gu.tux.trux.gui.base;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;

import se.gu.tux.trux.application.SettingsHandler;
import tux.gu.se.trux.R;

public class SettingsMenuActivity extends BaseAppActivity
{
    private Switch safetySwitch;
    private Spinner mapTypes;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_menu_activity);

        safetySwitch = (Switch) findViewById(R.id.safetySwitch);
        mapTypes = (Spinner) findViewById(R.id.mapSpinner);

        initMapType();
    }

    private void initMapType () {
        mapTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (mapTypes.getSelectedItem().toString().equals("Standard"))
                    SettingsHandler.gI(getApplicationContext()).setNormalMap(true);
                else
                    SettingsHandler.gI(getApplicationContext()).setNormalMap(false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });

        if(SettingsHandler.gI(getApplicationContext()).isNormalMap())
            mapTypes.setSelection(0);
        else
            mapTypes.setSelection(1);
    }
}

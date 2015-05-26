package se.gu.tux.trux.gui.base;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import se.gu.tux.trux.application.SettingsHandler;
import tux.gu.se.trux.R;

/**
 * Handles the settings page.
 */
public class SettingsMenuActivity extends BaseAppActivity
{

    // the spinner holding the map types the user can choose
    private Spinner mapTypes;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_menu_activity);

        mapTypes = (Spinner) findViewById(R.id.mapSpinner);

        initMapType();
    }


    /**
     * Changes the map view based on the user choise.
     */
    private void initMapType ()
    {
        // set listener to the spinner
        mapTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
            {
                // set the map type
                if (mapTypes.getSelectedItem().toString().equals("Standard"))
                {
                    SettingsHandler.gI(getApplicationContext()).setNormalMap(true);
                }
                else
                {
                    SettingsHandler.gI(getApplicationContext()).setNormalMap(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        if(SettingsHandler.gI(getApplicationContext()).isNormalMap())
        {
            mapTypes.setSelection(0);
        }
        else
        {
            mapTypes.setSelection(1);
        }

    } // end initMapType()


} // end class

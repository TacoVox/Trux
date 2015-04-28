package se.gu.tux.trux.gui;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import se.gu.tux.trux.appplication.DataHandler;
import se.gu.tux.trux.appplication.LoginService;
import se.gu.tux.trux.datastructure.Distance;
import se.gu.tux.trux.datastructure.Speed;
import se.gu.tux.trux.gui.detailedStats.Contact;
import se.gu.tux.trux.gui.detailedStats.Stats;
import se.gu.tux.trux.gui.simpleStats.SimpleStats;
import tux.gu.se.trux.R;


public class DriverHomeScreen extends ActionBarActivity {


    Fragment fragment;
    FragmentTransaction transaction;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home_screen);

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_driver_home_screen, menu);
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

    public void goToStats(View view){

        Intent intent = new Intent(DriverHomeScreen.this, ChooseStatScreen.class);
        startActivity(intent);

        /**
        Intent rich = new Intent(DriverHomeScreen.this, Stats.class);
        Intent simple = new Intent(DriverHomeScreen.this, SimpleStats.class);

        if (speed.getValue() == null) {
            startActivity(rich);
        }
        else {
            startActivity(simple);
        }
        **/
    }

    public void logout(MenuItem item){

        LoginService.getInstance().logout();
        Intent intent = new Intent(DriverHomeScreen.this, MainActivity.class);
        startActivity(intent);
    }
    public void contact(MenuItem item){
        fragment = new Contact();
        transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.driverhome, fragment);
        transaction.addToBackStack(null);
        transaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        transaction.commit();
    }


    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            this.finish();
        } else {
            getFragmentManager().popBackStack();
        }
    }

}

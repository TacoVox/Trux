package se.gu.tux.trux.gui;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.app.Fragment;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.concurrent.ExecutionException;

import se.gu.tux.trux.appplication.DataHandler;
import se.gu.tux.trux.appplication.LoginService;
import se.gu.tux.trux.datastructure.Distance;
import se.gu.tux.trux.datastructure.MetricData;
import se.gu.tux.trux.datastructure.Speed;
import se.gu.tux.trux.gui.detailedStats.Contact;
import se.gu.tux.trux.gui.detailedStats.Stats;
import se.gu.tux.trux.gui.simpleStats.SimpleStats;
import tux.gu.se.trux.R;


public class DriverHomeScreen extends ItemMenu {


    Fragment fragment;
    FragmentTransaction transaction;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home_screen);
        driverHomeScreenActive = true;
        System.out.println("DriverHomeScreen = True---------------------------------------");
    }
    @Override
    public void onStop(){
        super.onStop();
        driverHomeScreenActive = false;
        System.out.println("DriverHomeScreen = false---------------------------------------");
    }
    @Override
    public void onResume(){
        super.onResume();
        driverHomeScreenActive = true;
        System.out.println("DriverHomeScreen = true---------------------------------------");
    }
    @Override
    public void onPause(){
        super.onPause();
        driverHomeScreenActive = false;
        System.out.println("DriverHomeScreen = false---------------------------------------");
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

    public void goToStats(View view) {
        Intent rich = new Intent(DriverHomeScreen.this, Stats.class);
        Intent simple = new Intent(DriverHomeScreen.this, SimpleStats.class);
        ;
        try {
            final Speed speed = (Speed) DataHandler.getInstance().getData(new Speed(0));
            if (speed.getValue() != null && (Double) speed.getValue() > 0) {
                startActivity(simple);
            } else
                startActivity(rich);
        } catch (Exception e) {

        }
    }
    public void goLogout(MenuItem item){
           logout(item);

    }

    public void goSettings(MenuItem item){
        goToSettings(item);
    }

    public void goAbout(MenuItem item){
        goToAbout(item);
    }

    public void goContact(MenuItem item){
        goToContact(item);
    }


    //These lines are commented-out for now when we are doing the super.class to hold the menu items

/*
    public void logout(MenuItem item){

        AsyncTask<Void, Void, Boolean> check = new Logout().execute();

        boolean loggedOut = false;

        try
        {
            loggedOut = check.get();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (loggedOut)
        {
            Intent intent = new Intent(DriverHomeScreen.this, MainActivity.class);
            startActivity(intent);
        }
        else
        {
            Intent intent = new Intent(DriverHomeScreen.this, MainActivity.class);
            startActivity(intent);
        }
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


    private class Logout extends AsyncTask<Void, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(Void... voids)
        {
            boolean success = LoginService.getInstance().logout();
            return success;
        }
    }
*/
}

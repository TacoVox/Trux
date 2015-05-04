package se.gu.tux.trux.gui;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.app.Fragment;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.concurrent.ExecutionException;

import se.gu.tux.trux.appplication.LoginService;
import se.gu.tux.trux.gui.detailedStats.About;
import se.gu.tux.trux.gui.detailedStats.Contact;
import se.gu.tux.trux.gui.detailedStats.Settings;
import tux.gu.se.trux.R;

public class ItemMenu extends ActionBarActivity
{
    Fragment newFragment;
    FragmentTransaction transaction;

   public Boolean mainActivityActive = false;
   public Boolean driverHomeScreenActive = false;
   public Boolean statsActive = false;
   public Boolean simpleStatsActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_menu);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.action_settings)
        {
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    public void goToSettings(MenuItem item)
    {
         newFragment = new Settings();
        transaction = getFragmentManager().beginTransaction();
        if(driverHomeScreenActive) {
            transaction.replace(R.id.driverhome, newFragment);
        }
        else if (mainActivityActive){
            transaction.replace(R.id.mainActivity, newFragment);
        }
        else if(statsActive){
            transaction.replace(R.id.StatsView, newFragment);
        }
        else if(simpleStatsActive){

        }
        transaction.addToBackStack(null);
        transaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        transaction.commit();
    }


    public void goToAbout(MenuItem item)
    {

        newFragment = new About();
        transaction = getFragmentManager().beginTransaction();
        if(driverHomeScreenActive) {
            transaction.replace(R.id.driverhome, newFragment);
        }
        else if (mainActivityActive){
            transaction.replace(R.id.mainActivity, newFragment);
        }
        else if(statsActive){
            transaction.replace(R.id.StatsView, newFragment);
        }
        else if(simpleStatsActive){

        }
        transaction.addToBackStack(null);
        transaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        transaction.commit();
}
    public void goToContact(MenuItem item)
    {
        newFragment = new Contact();
        transaction = getFragmentManager().beginTransaction();
        if(driverHomeScreenActive) {
            transaction.replace(R.id.driverhome, newFragment);
        }
        else if (mainActivityActive){
            transaction.replace(R.id.mainActivity, newFragment);
        }
        else if(statsActive){
            transaction.replace(R.id.StatsView, newFragment);
        }
        else if(simpleStatsActive){

        }
        transaction.addToBackStack(null);
        transaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        transaction.commit();
    }
    public void logout(MenuItem item)
    {
        System.out.println("Logout is clicked");
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
            Intent intent = new Intent(ItemMenu.this, MainActivity.class);
            startActivity(intent);
        }
        else
        {
            Intent intent = new Intent(ItemMenu.this, MainActivity.class);
            startActivity(intent);
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
    @Override
    public void onBackPressed()
    {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            this.finish();
        } else {
            getFragmentManager().popBackStack();
        }
    }
}

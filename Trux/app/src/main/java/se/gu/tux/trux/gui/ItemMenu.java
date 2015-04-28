package se.gu.tux.trux.gui;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import se.gu.tux.trux.appplication.LoginService;
import se.gu.tux.trux.gui.detailedStats.About;
import se.gu.tux.trux.gui.detailedStats.Contact;
import tux.gu.se.trux.R;

public class ItemMenu extends ActionBarActivity {
    Fragment newFragment;
    FragmentTransaction transaction;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_menu);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_item_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }


        return super.onOptionsItemSelected(item);
    }
/*
    public void goToSettings(MenuItem item){
        newFragment = new Settings();
        transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.StatsView2, newFragment);
        transaction.addToBackStack(null);
        transaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        transaction.commit();
    }
*/
    public void goToAbout(MenuItem item) {
        newFragment = new About();
        transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.StatsView2, newFragment);
        transaction.addToBackStack(null);
        transaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        transaction.commit();
}
    public void goToContact(MenuItem item) {
        newFragment = new Contact();
        transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.StatsView2, newFragment);
        transaction.addToBackStack(null);
        transaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        transaction.commit();
    }
    public void logOut(MenuItem item) {
        LoginService.getInstance().logout();
        Intent intent = new Intent(ItemMenu.this, MainActivity.class);
        startActivity(intent);
    }
}

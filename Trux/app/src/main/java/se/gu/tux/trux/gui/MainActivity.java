package se.gu.tux.trux.gui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.swedspot.automotiveapi.AutomotiveManager;

import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.Fuel;
import se.gu.tux.trux.technical_services.RealTimeDataParser;
import se.gu.tux.trux.technical_services.ServerConnector;
import tux.gu.se.trux.R;


public class MainActivity extends ActionBarActivity
{

    RealTimeDataParser rtdp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rtdp = RealTimeDataParser.getInstance();
        System.out.println("Connecting to server...");
        System.out.println("------------------------------------------------");
        System.out.println("------------------------------------------------");
        ServerConnector.gI().connect("10.0.2.2");
        ServerConnector.gI().send(new Fuel(0));
        //This is a comment YOU FUCKIN APP
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
    public void goToHome(View view){
        Intent intent = new Intent(this, DriverHomeScreen.class);
        startActivity(intent);
    }
}

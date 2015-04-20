package se.gu.tux.trux.gui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.swedspot.automotiveapi.AutomotiveManager;

import se.gu.tux.trux.appplication.LoginService;
import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.Fuel;
import se.gu.tux.trux.technical_services.DataPoller;
import se.gu.tux.trux.technical_services.IServerConnector;
import se.gu.tux.trux.technical_services.RealTimeDataParser;
import se.gu.tux.trux.technical_services.ServerConnector;
import tux.gu.se.trux.R;


public class MainActivity extends ActionBarActivity
{

    LoginService ls;

    RealTimeDataParser rtdp;

    TextView userField = (TextView) findViewById(R.id.username);
    TextView passField = (TextView) findViewById(R.id.password);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rtdp = RealTimeDataParser.getInstance();

        ls = new LoginService();

        System.out.println("Main activity calling ServerConnector connect...");

        ServerConnector.gI().connect("www.derkahler.de");

        //IServerConnector.getInstance().connectTo("10.0.2.2");

        DataPoller.gI().start();
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


    public void goToHome(View view)
    {
        /*
        String username = (String) userField.getText();
        String password = (String) passField.getText();

        if (username.isEmpty() || password.isEmpty())
        {
            return;
        }

        boolean isAllowed = ls.isAllowed(username, password);

        if (isAllowed)
        {
            Intent intent = new Intent(this, DriverHomeScreen.class);
            startActivity(intent);
        }
        */

        Intent intent = new Intent(this, DriverHomeScreen.class);
        startActivity(intent);

    }

} // end class

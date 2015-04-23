package se.gu.tux.trux.gui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.swedspot.automotiveapi.AutomotiveManager;

import java.util.concurrent.ExecutionException;

import se.gu.tux.trux.appplication.LoginService;
import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.Fuel;
import se.gu.tux.trux.gui.detailedStats.DistTravWindow;
import se.gu.tux.trux.gui.detailedStats.FuelWindow;
import se.gu.tux.trux.gui.detailedStats.SpeedWindow;
import se.gu.tux.trux.technical_services.AGADataParser;
import se.gu.tux.trux.technical_services.DataPoller;
import se.gu.tux.trux.technical_services.IServerConnector;
import se.gu.tux.trux.technical_services.RealTimeDataParser;
import se.gu.tux.trux.technical_services.ServerConnector;
import tux.gu.se.trux.R;


public class MainActivity extends ActionBarActivity
{

    Fragment newFragment;
    FragmentTransaction transaction;

    TextView userField;
    TextView passField;

    Button btnRegister;

    LoginService ls;

    //RealTimeDataParser rtdp;


    AGADataParser rtdp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userField = (TextView) findViewById(R.id.username);
        passField = (TextView) findViewById(R.id.password);

        btnRegister = (Button) findViewById(R.id.register);

        btnRegister.setOnClickListener(btnOnClick);

        //rtdp = RealTimeDataParser.getInstance();

        rtdp = AGADataParser.getInstance();

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

        final String username = userField.getText().toString();
        final String password = passField.getText().toString();

        if (username.isEmpty() || password.isEmpty())
        {
            return;
        }

        AsyncTask<String, Void, Boolean> check = new LoginCheck().execute(username, password);

        boolean isAllowed = false;

        try
        {
            isAllowed = check.get();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
        }

        if (isAllowed)
        {
            Intent intent = new Intent(this, DriverHomeScreen.class);
            startActivity(intent);
        }


        //Intent intent = new Intent(this, DriverHomeScreen.class);
        //startActivity(intent);

    } // end goToHome()

    Button.OnClickListener btnOnClick = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == btnRegister) {
                newFragment = new RegisterWindow();
            }

            transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.mainActivity, newFragment);
            transaction.addToBackStack(null);
            transaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
            transaction.commit();
        }
    };

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            this.finish();
        } else {
            getFragmentManager().popBackStack();
        }
    }

    private class LoginCheck extends AsyncTask<String, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(String... strings)
        {
            boolean isAllowed = ls.isAllowed(strings[0], strings[1]);
            return isAllowed;
        }
    } // end inner class


} // end class

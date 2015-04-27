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
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.concurrent.ExecutionException;

import se.gu.tux.trux.appplication.DataHandler;
import se.gu.tux.trux.appplication.LoginService;
import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.ProtocolMessage;
import se.gu.tux.trux.datastructure.User;
import se.gu.tux.trux.technical_services.AGADataParser;
import se.gu.tux.trux.technical_services.DataPoller;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import se.gu.tux.trux.technical_services.ServerConnector;
import tux.gu.se.trux.R;


public class MainActivity extends ActionBarActivity
{
    Fragment newFragment;
    FragmentTransaction transaction;

    TextView userField;
    TextView passField;
    Button btnRegister;
    CheckBox checkBox;

    LoginService ls;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Add login form
        userField = (TextView) findViewById(R.id.username);
        passField = (TextView) findViewById(R.id.password);

        btnRegister = (Button) findViewById(R.id.register);
        btnRegister.setOnClickListener(btnOnClick);

        checkBox = (CheckBox) findViewById(R.id.autoLogin);

        // Create login service
        ls = new LoginService(this.getBaseContext());

        ServerConnector.gI().connect("www.derkahler.de");
        //IServerConnector.getInstance().connectTo("10.0.2.2");

        // Just make sure a AGA data parser is created
        AGADataParser.getInstance();

        // Start the DataPoller that will send AGA metrics to the server with regular interavals
        DataPoller.gI().start();

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
        String username = "";
        String password = "";
        Long sessionId;
        Long userId;

        if (keepLoggedIn())
        {
            username = DataHandler.getInstance().getUser().getUsername();
            password = DataHandler.getInstance().getUser().getPasswordHash();
            sessionId = DataHandler.getInstance().getUser().getSessionId();
            userId = DataHandler.getInstance().getUser().getUserId();
        }
        else
        {
            username = userField.getText().toString();
            password = passField.getText().toString();
            sessionId = User.LOGIN_REQUEST;
            userId = -1L;
        }

        if (username.isEmpty() || password.isEmpty())
        {
            return;
        }

        AsyncTask<String, Void, Boolean> check =
                new LoginCheck().execute(username, password, Long.toString(sessionId), Long.toString(userId));

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


    private boolean keepLoggedIn()
    {
        boolean keep = false;

        if (checkBox.isChecked())
        {
            // get user info from file
            String[] info = ls.readFromFile();

            // new protocol message, set type to auto login request and password hash
            final ProtocolMessage request = new ProtocolMessage(ProtocolMessage.Type.AUTO_LOGIN_REQUEST, info[1]);

            // set session ID and user ID
            request.setSessionId(Long.parseLong(info[2]));
            request.setUserId(Long.parseLong(info[3]));

            ProtocolMessage msg = null;

            AsyncTask<ProtocolMessage, Void, ProtocolMessage> check = new AutoLoginCheck().execute(request);

            try
            {
                msg = check.get();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            catch (ExecutionException e)
            {
                e.printStackTrace();
            }


            if (msg.getType() == ProtocolMessage.Type.LOGIN_SUCCESS)
            {
                User user = new User();

                // set info for user
                user.setUsername(info[0]);
                user.setPasswordHash(info[1]);
                user.setSessionId(msg.getSessionId());
                user.setUserId(msg.getUserId());

                // set user in DataHandler for future use
                DataHandler.getInstance().setUser(user);

                keep = true;
            }
            else
            {
                System.out.println("-------- ERROR: login fail ----------");
            }

        }

        return keep;
    }




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


    /**
     * Private class. Checks if the user is allowed to login.
     */
    private class LoginCheck extends AsyncTask<String, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(String... strings)
        {
            boolean isAllowed = ls.login(strings[0], strings[1], Long.parseLong(strings[2]), Long.parseLong(strings[3]));
            return isAllowed;
        }
    } // end inner class


    /**
     * Private class. Checks if the user is allowed to login when auto-login requested.
     */
    private class AutoLoginCheck extends AsyncTask<ProtocolMessage, Void, ProtocolMessage>
    {
        ProtocolMessage msg = null;

        @Override
        protected ProtocolMessage doInBackground(ProtocolMessage... protocolMessages)
        {
            try
            {
                msg = (ProtocolMessage) ServerConnector.gI().answerQuery(protocolMessages[0]);
            }
            catch (NotLoggedInException e)
            {
                e.printStackTrace();
            }
            
            return msg;
        }
    }


    /*
    public void onStop() {
        // TODO
        // clean-up on stop
        System.out.println("ONSTOP....!");
    }
    */



} // end class

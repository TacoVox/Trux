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
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import se.gu.tux.trux.appplication.DataHandler;
import se.gu.tux.trux.appplication.LoginService;
import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.ProtocolMessage;
import se.gu.tux.trux.datastructure.User;
import se.gu.tux.trux.gui.detailedStats.Contact;
import se.gu.tux.trux.technical_services.AGADataParser;
import se.gu.tux.trux.technical_services.DataPoller;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import se.gu.tux.trux.technical_services.ServerConnector;
import tux.gu.se.trux.R;


public class MainActivity extends ItemMenu
{
    Fragment newFragment;
    FragmentTransaction transaction;

    TextView userField;
    TextView passField;
    Button btnRegister;
    CheckBox checkBox;

    private String[] userInfo;

    // file name
    private static final String FILE_NAME = "trux_user_config";

    private File file;

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
        LoginService.createInstance(this.getBaseContext(), FILE_NAME);

        ServerConnector.gI().connect("www.derkahler.de");
        //IServerConnector.getInstance().connectTo("10.0.2.2");

        // Just make sure a AGA data parser is created
        AGADataParser.getInstance();

        // Start the DataPoller that will send AGA metrics to the server with regular interavals
        DataPoller.gI().start();

        file = new File(getFilesDir(), FILE_NAME);

        // create a file to store data
        if (file == null || !file.exists())
        {
            try
            {
                file.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            System.out.println("------- file path: " + file.getAbsolutePath() + " ----------");
        }
        else
        {
            userInfo = LoginService.getInstance().readFromFile();
        }

        if (userInfo != null && userInfo[4].equals("true"))
        {
            autoLogin();
        }
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
        String username = userField.getText().toString();
        String password = passField.getText().toString();
        Long sessionId = User.LOGIN_REQUEST;
        Long userId = -1L;
        Short keepFlag = 0;

        if (checkBox.isChecked())
        {
            keepFlag = 1;
        }


        if (username.isEmpty() || password.isEmpty())
        {
            // TODO
            // something wrong with credentials, display info to user
            // refresh app, ask for login again
            return;
        }

        AsyncTask<String, Void, Boolean> check =
                new LoginCheck().execute(username, password, Long.toString(sessionId), Long.toString(userId), Short.toString(keepFlag));

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

    } // end goToHome()



    private void autoLogin()
    {
        // new protocol message, set type to auto login request and password hash
        ProtocolMessage request = new ProtocolMessage(ProtocolMessage.Type.AUTO_LOGIN_REQUEST, userInfo[1]);

        // set session ID and user ID
        request.setSessionId(Long.parseLong(userInfo[2]));
        request.setUserId(Long.parseLong(userInfo[3]));

        ProtocolMessage msg = null;

        User user = new User();

        user.setUsername(userInfo[0]);
        user.setPasswordHash(userInfo[1]);
        user.setSessionId(Long.parseLong(userInfo[2]));
        user.setUserId(Long.parseLong(userInfo[3]));

        DataHandler.getInstance().setUser(user);

        System.out.println("------ sending request for auto-login ------------");
        AsyncTask<ProtocolMessage, Void, ProtocolMessage> check = new AutoLoginCheck().execute(request);
        System.out.println("------ request for auto-login received ------------");

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
            Intent intent = new Intent(this, DriverHomeScreen.class);
            startActivity(intent);
        }
        else
        {
            System.out.println("-------- ERROR: login fail ----------");
        }
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

    public void goContact(MenuItem item){
        newFragment = new Contact();
        transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.mainActivity, newFragment);
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


    /**
     * Private class. Checks if the user is allowed to login.
     */
    private class LoginCheck extends AsyncTask<String, Void, Boolean>
    {
        @Override
        protected void onPreExecute()
        {
            Toast.makeText(getApplicationContext(), "Logging in. Please wait...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Boolean doInBackground(String... strings)
        {
            boolean isAllowed =
                    LoginService.getInstance().login(strings[0], strings[1], Long.parseLong(strings[2]), Long.parseLong(strings[3]), Short.parseShort(strings[4]));

            return isAllowed;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean)
        {
            Toast.makeText(getApplicationContext(), "You are now logged in.", Toast.LENGTH_SHORT).show();
        }
        
    } // end inner class


    /**
     * Private class. Checks if the user is allowed to login when auto-login requested.
     */
    private class AutoLoginCheck extends AsyncTask<ProtocolMessage, Void, ProtocolMessage>
    {
        ProtocolMessage msg = null;

        @Override
        protected void onPreExecute()
        {
            Toast.makeText(getApplicationContext(), "Auto-logging in. Please wait...", Toast.LENGTH_SHORT).show();
        }

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

        @Override
        protected void onPostExecute(ProtocolMessage protocolMessage)
        {
            Toast.makeText(getApplicationContext(), "You are now logged in.", Toast.LENGTH_SHORT).show();
        }

    } // end inner class


    /*
    public void onStop() {
        // TODO
        // clean-up on stop
        System.out.println("ONSTOP....!");
    }
    */



} // end class

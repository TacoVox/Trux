package se.gu.tux.trux.gui.main_home;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.application.LoginService;
import se.gu.tux.trux.datastructure.ProtocolMessage;
import se.gu.tux.trux.datastructure.User;
import se.gu.tux.trux.gui.base.BaseAppActivity;
import se.gu.tux.trux.gui.base.RegisterActivity;
import se.gu.tux.trux.technical_services.AGADataParser;
import se.gu.tux.trux.technical_services.DataPoller;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import se.gu.tux.trux.technical_services.ServerConnector;
import tux.gu.se.trux.R;


public class MainActivity extends BaseAppActivity
{
    Fragment newFragment;
    FragmentTransaction transaction;

    TextView userField;
    TextView passField;
    CheckBox checkBox;

    private String[] userInfo;

    // file name
    private static final String FILE_NAME = "trux_user_config";
    // layout id
    private static final int LAYOUT_ID = R.layout.activity_main;

    private File file;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // set layout for this view
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT_ID);

        // set current view
        setCurrentViewId(LAYOUT_ID);

        // Add login form
        userField = (TextView) findViewById(R.id.username);
        passField = (TextView) findViewById(R.id.password);

        checkBox = (CheckBox) findViewById(R.id.autoLogin);

        ServerConnector.gI().connect("83.248.219.57");

        // Create login service
        LoginService.createInstance(this.getBaseContext(), FILE_NAME);

        // Just make sure a AGA data parser is created
        AGADataParser.getInstance();

        // Start the DataPoller that will send AGA metrics to the server with regular interavals
        DataPoller.gI().start();

        file = new File(getFilesDir(), FILE_NAME);

        // create a file to store data
        if (!file.exists())
        {
            try
            {
                System.out.println("File does not exist, clearing user info");
                file.createNewFile();
                // If user logged out now, make sure no user details are cached.
                userInfo = null;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            System.out.println("------- file path: " + file.getAbsolutePath() + " ----------");
        }
        else
        {
            System.out.println("File exists, loading user info");
            userInfo = LoginService.getInstance().readFromFile();
            // userInfo will be set to null here if the file existed but didn't contain all
            // user details = the user had logged out
        }

        if (userInfo != null && userInfo[4].equals("true"))
        {
            autoLogin();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onResume()
    {
        setCurrentViewId(LAYOUT_ID);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    /*    new Thread(new Runnable() {
            @Override
            public void run() {
                ServerConnector.gI().disconnect();
            }
        }).start();*/
    }

    public void goToRegister(View view)
    {
        // Start new activity
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
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
            showDialogBox("Login failed", "Invalid username or password");
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
            showToast("You are now logged in.");

            Intent intent = new Intent(this, HomeActivity.class);

            // Make sure there is no history for the back button
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else
        {
            showToast("Login failed. Please try again.");
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
        catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }

        // check message is not null
        assert msg != null;
        // if login successful
        if (msg.getType() == ProtocolMessage.Type.LOGIN_SUCCESS)
        {
            showToast("You are now logged in.");

            Intent intent = new Intent(this, HomeActivity.class);

            // Make sure there is no history for the back button
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else
        {
            showToast("Problem logging in.\nMessage: " + msg.getMessage() + ".\nPlease try again.");
        }

    } // end autoLogin()



    @Override
    public void onBackPressed()
    {
        if (getFragmentManager().getBackStackEntryCount() == 0)
        {
            this.finish();
        }
        else
        {
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

            return LoginService.getInstance().login(strings[0], strings[1],
                    Long.parseLong(strings[2]), Long.parseLong(strings[3]), Short.parseShort(strings[4]));
        }
        
    } // end inner class



    /**
     * Private class. Checks if the user is allowed to login when auto-login requested.
     */
    private class AutoLoginCheck extends AsyncTask<ProtocolMessage, Void, ProtocolMessage>
    {
        private ProtocolMessage pMessage = null;

        @Override
        protected void onPreExecute()
        {
            showToast("Auto-logging in. Please wait...");
        }

        @Override
        protected ProtocolMessage doInBackground(ProtocolMessage... protocolMessages)
        {
            try
            {
                pMessage = (ProtocolMessage) ServerConnector.gI().answerQuery(protocolMessages[0]);
                if (pMessage.getType() == ProtocolMessage.Type.LOGIN_SUCCESS) {
                    System.out.println("Current user: "  + DataHandler.getInstance().getUser().getUserId());
                    // Also update the user info by making a request for a User object
                    DataHandler.getInstance().setUser((User)DataHandler.getInstance().getData(
                            DataHandler.getInstance().getUser()));
                    System.out.println("Current user: "  + DataHandler.getInstance().getUser().getUserId());
                    System.out.println("Current friends: "  + DataHandler.getInstance().getUser().getFriends());
                }
            }
            catch (NotLoggedInException e)
            {
                e.printStackTrace();
            }

            return pMessage;
        }

    } // end inner class


} // end class

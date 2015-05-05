package se.gu.tux.trux.gui;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

import se.gu.tux.trux.appplication.DataHandler;
import se.gu.tux.trux.appplication.LoginService;
import se.gu.tux.trux.datastructure.ProtocolMessage;
import se.gu.tux.trux.datastructure.User;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import se.gu.tux.trux.technical_services.ServerConnector;
import tux.gu.se.trux.R;


/**
 * Handles the registering for a new user.
 */
public class RegisterActivity extends BaseAppActivity implements View.OnClickListener
{
    private static final int LAYOUT_ID = R.layout.activity_register;

    // private fields for user info when registering
    private EditText username;
    private EditText firstName;
    private EditText lastName;
    private EditText email;
    private EditText password;
    private EditText confirmPassword;
    private CheckBox termsAndCond;

    // private fields to store data provided by user
    private String sUsername;
    private String sFirstName;
    private String sLastName;
    private String sEmail;
    private String sPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(LAYOUT_ID);
        // set current view
        setCurrentViewId(LAYOUT_ID);

        // find components
        username = (EditText) findViewById(R.id.userNameInput);
        firstName = (EditText) findViewById(R.id.firstNameInput);
        lastName = (EditText) findViewById(R.id.lastNameInput);
        email = (EditText) findViewById(R.id.emailInput);
        password = (EditText) findViewById(R.id.passwordInput);
        confirmPassword = (EditText) findViewById(R.id.verPasswordInput);
        termsAndCond = (CheckBox) findViewById(R.id.agreeButton);
        Button registerButton = (Button) findViewById(R.id.registerButton);

        // set listener to the button
        registerButton.setOnClickListener(this);
    }


    /**
     * Registers the user.
     */
    private void registerUser()
    {
        // check if the provided information is valid
        boolean check = checkCredentials();

        // if valid, send a request to register to the server
        if (check)
        {
            // create a User object to send to the server
            User user = new User();
            // fill in the required data
            user.setUsername(sUsername);
            user.setFirstName(sFirstName);
            user.setLastName(sLastName);
            user.setEmail(sEmail);
            // create hash for password
            String hashPassword = LoginService.getInstance().createHash(sPassword);
            // set hashed password
            user.setPasswordHash(hashPassword);
            // set session Id to REGISTER_REQUEST for the server
            user.setSessionId(User.REGISTER_REQUEST);

            // set user in data handler
            DataHandler.getInstance().setUser(user);

            // start async task to register
            AsyncTask<User, Void, ProtocolMessage> task = new RegisterCheck().execute(user);

            // the protocol message for response
            ProtocolMessage message = null;

            // get the response
            try
            {
                message = task.get();
            }
            catch (InterruptedException | ExecutionException e)  { e.printStackTrace(); }

            // if successful registration, go back to main screen
            assert message != null;
            if (message.getType() == ProtocolMessage.Type.SUCCESS)
            {
                AlertDialog.Builder confirmDialog = new AlertDialog.Builder(getApplicationContext());

                confirmDialog.setMessage("You have now been registered. To confirm registration, " +
                "please go to the e-mail you provided and click on the link. To enjoy our services, " +
                "login with your username and password. Have a nice day!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int i)
                        {
                            // dismiss the dialog box
                            dialog.dismiss();
                            // This activity is now finished
                            finish();
                        }
                    }).create();

                confirmDialog.show();
            }
            else
            {
                // Problem discovered by the server, show it to the user
                showToast(message.getMessage());
            }
        }
        else
        {
            // the credentials were not valid or there was some other
            // error, display message to user
            AlertDialog.Builder errorDialog = new AlertDialog.Builder(getApplicationContext());

            errorDialog.setMessage("There was a problem while registering. Please try " +
            "again later. If the problem persists, please contact the development team. Have a nice day!")
                .setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int i)
                    {
                        // dismiss the dialog box
                        dialog.dismiss();
                    }
                }).create();

            errorDialog.show();
        }

    } // end registerUser()



    /**
     * Checks if the user input is correct when registering.
     *
     * @return      true if credentials are correct, false otherwise
     */
    private boolean checkCredentials()
    {
        boolean isVerified = true;

        // get the username
        sUsername = username.getText().toString();
        // check if it has required number of characters
        if (sUsername.length() < 3)
        {
            username.setBackgroundColor(Color.RED);
            showToast("Username must be at least 3 characters long.");
            sUsername = "";
            isVerified = false;
        }

        // get the first name
        sFirstName = firstName.getText().toString();
        // perform check on first name
        if (sFirstName.isEmpty())
        {
            firstName.setBackgroundColor(Color.RED);
            showToast("First name can not be empty.");
            sFirstName = "";
            isVerified = false;
        }

        // get the last name
        sLastName = lastName.getText().toString();
        // perform check on last name
        if (sLastName.isEmpty())
        {
            lastName.setBackgroundColor(Color.RED);
            showToast("Last name can not be empty.");
            sLastName = "";
            isVerified = false;
        }

        // get the e-mail
        sEmail = email.getText().toString();
        // regex to use for checking if the e-mail has a right format
        String regex1 = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@" +
                "(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?";

        // back-up regex to check e-mail
        //String regex2 = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$";

        // perform check on e-mail
        if (!sEmail.matches(regex1))
        {
            email.setBackgroundColor(Color.RED);
            showToast("Please check e-mail address.");
            sEmail = "";
            isVerified = false;
        }

        // get the password
        sPassword = password.getText().toString();
        // perform check on password
        if (sPassword.isEmpty() || sPassword.length() < 6)
        {
            password.setBackgroundColor(Color.RED);
            showToast("Password must be at least 6 characters long.");
            sPassword = "";
            isVerified = false;
        }

        // get the confirmed password
        String sConfirmPass = confirmPassword.getText().toString();
        // check if the two passwords match
        if (!sConfirmPass.equals(sPassword))
        {
            confirmPassword.setBackgroundColor(Color.RED);
            showToast("Please make sure passwords match.");
            isVerified = false;
        }

        // check if the user agrees to the terms and conditions
        if (!termsAndCond.isChecked())
        {
            showToast( "Please agree to the terms and conditions to register.");
            isVerified = false;
        }

        return isVerified;

    } // end check()



    /**
     * Performs action when button is clicked. Overriden method.
     *
     * @param view      The view.
     */
    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.registerButton)
        {
            System.out.println("-------- calling onClick in RegisterWindow --------");
            registerUser();
        }
    }



    /**
     * Private class. Checks if the user register is successful.
     */
    private class RegisterCheck extends AsyncTask<User, Void, ProtocolMessage>
    {
        ProtocolMessage response = null;

        @Override
        protected void onPreExecute()
        {
            showToast("Sending request to register. Please wait...");
        }

        @Override
        protected ProtocolMessage doInBackground(User... users)
        {
            try
            {
                response = (ProtocolMessage) ServerConnector.getInstance().answerQuery(users[0]);
            }
            catch (NotLoggedInException e)
            {
                e.printStackTrace();
            }

            return response;
        }

    } // end inner class


} // end class
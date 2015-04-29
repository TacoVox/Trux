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
public class RegisterWindow extends Fragment implements View.OnClickListener
{

    // private fields for user info when registering
    private EditText username;
    private EditText firstName;
    private EditText lastName;
    private EditText email;
    private EditText password;
    private EditText confirmPassword;
    private CheckBox termsAndCond;
    private Button registerButton;

    // the view for this fragment
    private View view;

    // private fields to store data provided by user
    private String sUsername;
    private String sFirstName;
    private String sLastName;
    private String sEmail;
    private String sPassword;
    private String sConfirmPass;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // inflate the view, calling this here so we can use the view
        // to find the components for it
        view = inflater.inflate(R.layout.fragment_register_window, container, false);

        // find components
        username = (EditText) view.findViewById(R.id.userNameInput);
        firstName = (EditText) view.findViewById(R.id.firstNameInput);
        lastName = (EditText) view.findViewById(R.id.lastNameInput);
        email = (EditText) view.findViewById(R.id.emailInput);
        password = (EditText) view.findViewById(R.id.passwordInput);
        confirmPassword = (EditText) view.findViewById(R.id.verPasswordInput);
        termsAndCond = (CheckBox) view.findViewById(R.id.agreeButton);
        registerButton = (Button) view.findViewById(R.id.registerButton);

        // set listener to the button
        registerButton.setOnClickListener(this);

        // return the view
        return view;
    }


    /**
     * Registers the user.
     */
    private void registerUser()
    {
        // check if the provided information is valid
        boolean check = checkCredentials();

        // if valid, send a request to register to the server
        // else display an appropriate message
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
            catch (InterruptedException e)  { e.printStackTrace(); }
            catch (ExecutionException e)    { e.printStackTrace(); }

            // if successful registration, go back to main screen
            if (message.getType() == ProtocolMessage.Type.SUCCESS)
            {
                AlertDialog.Builder confirmDialog = new AlertDialog.Builder(view.getContext());

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
                                // get the transaction manager
                                FragmentManager fm = getActivity().getFragmentManager();
                                // go back to main screen
                                fm.popBackStack();
                            }
                        }).create();

                confirmDialog.show();
            }
            else
            {
                Toast.makeText(getActivity(), message.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }

        }
        else
        {
            AlertDialog.Builder errorDialog = new AlertDialog.Builder(view.getContext());

            errorDialog.setMessage("There was a problem while registering. Please try " +
            "again later. If the problem persists, please contact the development team. Have a nice day!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int i)
                        {
                            // dismiss the dialog box
                            dialog.dismiss();
                            // get the transaction manager
                            FragmentManager fm = getActivity().getFragmentManager();
                            // go back to main screen
                            fm.popBackStack();
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
            Toast.makeText(getActivity(), "Username must be at least 3 characters long.", Toast.LENGTH_SHORT).show();
            sUsername = "";
            isVerified = false;
        }

        // get the first name
        sFirstName = firstName.getText().toString();
        // perform check on first name
        if (sFirstName.isEmpty())
        {
            firstName.setBackgroundColor(Color.RED);
            Toast.makeText(getActivity(), "First name can not be empty.", Toast.LENGTH_SHORT).show();
            sFirstName = "";
            isVerified = false;
        }

        // get the last name
        sLastName = lastName.getText().toString();
        // perform check on last name
        if (sLastName.isEmpty())
        {
            lastName.setBackgroundColor(Color.RED);
            Toast.makeText(getActivity(), "Last name can not be empty.", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getActivity(), "Please check e-mail address.", Toast.LENGTH_SHORT).show();
            sEmail = "";
            isVerified = false;
        }

        // get the password
        sPassword = password.getText().toString();
        // perform check on password
        if (sPassword.isEmpty() || sPassword.length() < 6)
        {
            password.setBackgroundColor(Color.RED);
            Toast.makeText(getActivity(), "Password must be at least 6 characters long.", Toast.LENGTH_SHORT).show();
            sPassword = "";
            isVerified = false;
        }

        // get the confirmed password
        sConfirmPass = confirmPassword.getText().toString();
        // check if the two passwords match
        if (!sConfirmPass.equals(sPassword))
        {
            confirmPassword.setBackgroundColor(Color.RED);
            Toast.makeText(getActivity(), "Please make sure passwords match.", Toast.LENGTH_SHORT).show();
            sConfirmPass = "";
            isVerified = false;
        }

        // check if the user agrees to the terms and conditions
        if (!termsAndCond.isChecked())
        {
            Toast.makeText(getActivity(), "Please agree to the terms and conditions to register.", Toast.LENGTH_SHORT).show();
            isVerified = false;
        }

        return isVerified;

    } // end check()



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
            Toast.makeText(getActivity(), "Sending request to register. Please wait...", Toast.LENGTH_SHORT).show();
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
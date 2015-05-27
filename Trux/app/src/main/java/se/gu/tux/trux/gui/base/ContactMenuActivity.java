package se.gu.tux.trux.gui.base;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.datastructure.User;
import tux.gu.se.trux.R;

/**
 * Created by Aman ghezai on 2015-05-14.
 *
 * Handles the contact page.
 */
public class ContactMenuActivity extends BaseAppActivity implements View.OnClickListener
{

    private EditText contactName= null;
    private EditText feedbackInput = null;
    private Spinner spinner;
    private Button sendEmailButton;
    private User user;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_menu_activity);

        // get the components
        contactName = (EditText) findViewById(R.id.contactName);
        feedbackInput = (EditText) findViewById(R.id.feedbackInput);
        spinner = (Spinner) findViewById(R.id.spinnerFeedbackType);
        sendEmailButton = (Button) findViewById(R.id.send_email_button);

        // set listener to button
        sendEmailButton.setOnClickListener(this);

        // if the user is online, get the required information and display so
        // the user does not have to input it manually
        if(isOnline())
        {
            user = DataHandler.getInstance().getUser();
            contactName.setText(user.getFirstName() +  " " + user.getLastName());
        }

    } // end onCreate()



    @Override
    public void onClick(View view)
    {
        int id = view.getId();

        if (id == sendEmailButton.getId())
        {
            sendEmail();
        }
    }



    /**
     * Sends a contact e-mail.
     */
    private void sendEmail()
    {
        // the e-mail address
        String rec = "se.gu.trux@gmail.com";
        // the subject the user chose
        String subject = spinner.getSelectedItem().toString();
        // the user's name
        String userName = contactName.getText().toString();

        // start a new intent
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", rec, null));

        // put the subject
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);

        // check if the user is online and put the extra info we need
        if(isOnline())
        {
            emailIntent.putExtra(Intent.EXTRA_TEXT, userName + "\n\n" + feedbackInput.getText().toString() +
                    "\n\nUserID: " + user.getUserId());
        }
        else
        {
            emailIntent.putExtra(Intent.EXTRA_TEXT, userName + "\n\n" + feedbackInput.getText().toString());
        }

        // start an activity with the intent
        try
        {
            // the user can choose the email client
            startActivity(Intent.createChooser(emailIntent, "Send eMail..."));
        }
        catch (ActivityNotFoundException ex)
        {
            showToast("No e-mail client found");
        }

        // after sending the email, clear the fields
        contactName.setText("");
        feedbackInput.setText("");

    } // end sendEmail()



    /**
     * Helper method. Checks if the user is logged in.
     *
     * @return  true if logged in, false otherwise
     */
    private boolean isOnline()  { return DataHandler.getInstance().isLoggedIn(); }


} // end class

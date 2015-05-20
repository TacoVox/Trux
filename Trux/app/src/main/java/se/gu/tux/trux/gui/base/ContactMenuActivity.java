package se.gu.tux.trux.gui.base;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.datastructure.User;
import tux.gu.se.trux.R;

/**
 * Created by Aman ghezai on 2015-05-14.
 */
public class ContactMenuActivity extends BaseAppActivity {
    private EditText contactName= null;
    private EditText contactEmailInput= null;
    private EditText feedbackInput = null;
    private Spinner spinner;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_menu_activity);

        contactName = (EditText) findViewById(R.id.contactName);
        feedbackInput = (EditText) findViewById(R.id.feedbackInput);
        spinner = (Spinner) findViewById(R.id.spinnerFeedbackType);
        if(isOnline()){
            user = DataHandler.getInstance().getUser();
            contactName.setText(user.getFirstName() +  " " + user.getLastName());
        }

    }
    protected void sendEmail() {


            String rec = "se.gu.trux@gmail.com";
            String subject = spinner.getSelectedItem().toString();
            String userName = contactName.getText().toString();

            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", rec, null));

            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            if(isOnline()) {
                emailIntent.putExtra(Intent.EXTRA_TEXT, userName + "\n\n" + feedbackInput.getText().toString() +
                        "\n\nUserID: " + user.getUserId());
            }
            else
                emailIntent.putExtra(Intent.EXTRA_TEXT, userName + "\n\n" + feedbackInput.getText().toString());
        try {

            // the user can choose the email client
            startActivity(Intent.createChooser(emailIntent, "Send eMail..."));



            } catch (android.content.ActivityNotFoundException ex) {

            Toast.makeText(ContactMenuActivity.this, "No email client found.",

                    Toast.LENGTH_LONG).show();


            }

        }

    public void sendFeedback(View view)
    {
            System.out.println("-------- calling onClick in contactWindow --------");
            sendEmail();
            // after sending the email, clear the fields
            contactName.setText("");
            feedbackInput.setText("");

    }
    private boolean isOnline(){
        return DataHandler.getInstance().isLoggedIn();
    }
}

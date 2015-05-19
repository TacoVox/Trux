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

import tux.gu.se.trux.R;

/**
 * Created by Aman ghezai on 2015-05-14.
 */
public class ContactMenuActivity extends BaseAppActivity implements View.OnClickListener{
    private EditText contactSubjectInput= null;
    private EditText contactEmailInput= null;
    private EditText feedbackInput = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_menu_activity);

        contactSubjectInput = (EditText) findViewById(R.id.contactSubjectInput);
        contactEmailInput = (EditText) findViewById(R.id.contactEmailInput);
        feedbackInput = (EditText) findViewById(R.id.feedbackInput);
        Button emailButton = (Button) findViewById(R.id.sendEmail);
            // set listener to the button
        emailButton.setOnClickListener(this);

    }
    protected void sendEmail() {


            String[] user = {contactEmailInput.getText().toString()};

            Intent email = new Intent(Intent.ACTION_SEND, Uri.parse("mailto:"));

             // prompts email clients only

              email.setType("message/rfc822");

              email.putExtra(Intent.EXTRA_EMAIL, user);

              email.putExtra(Intent.EXTRA_SUBJECT, contactSubjectInput.getText().toString());

              email.putExtra(Intent.EXTRA_TEXT, feedbackInput.getText().toString());

        try {

            // the user can choose the email client
            startActivity(Intent.createChooser(email, "Choose your preferred email client..."));



            } catch (android.content.ActivityNotFoundException ex) {

            Toast.makeText(ContactMenuActivity.this, "No email client found.",

                    Toast.LENGTH_LONG).show();


            }

        }
    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.sendMessageButton)
        {
            System.out.println("-------- calling onClick in contactWindow --------");
            sendEmail();
            // after sending the email, clear the fields
            contactSubjectInput.setText("");
            contactEmailInput.setText("");
            feedbackInput.setText("");


        }
    }
}

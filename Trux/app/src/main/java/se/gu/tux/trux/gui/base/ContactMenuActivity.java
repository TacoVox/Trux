package se.gu.tux.trux.gui.base;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import tux.gu.se.trux.R;

/**
 * Created by Aman ghezai on 2015-05-14.
 */
public class ContactMenuActivity extends BaseAppActivity {
    private static final int LAYOUT_ID = R.layout.contact_menu_activity;


    private EditText name;
    private EditText email;
    private String nameField;
    private String emailField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(LAYOUT_ID);
        // set current view
        setCurrentViewId(LAYOUT_ID);

        EditText nameField = (EditText) findViewById(R.id.EditTextName);
        String name = nameField.getText().toString();

        EditText emailField = (EditText) findViewById(R.id.EditTextEmail);
        String email = emailField.getText().toString();

        EditText feedbackField = (EditText) findViewById(R.id.EditTextFeedbackBody);
        String feedback = feedbackField.getText().toString();
        Spinner feedbackSpinner = (Spinner) findViewById(R.id.SpinnerFeedbackType);
        String feedbackType = feedbackSpinner.getSelectedItem().toString();

    }

    /**
     * Get the feedback.
     */
    private void getfeedback() {
        checkCredentialvalues();
        //TODO: create a getfeedback to handle the feedback and send it to email... -->

    }


//TODO: checkCredentialvalues not working accordingly !

    private boolean checkCredentialvalues() {
        boolean isCorrect = true;

        // get the name
        nameField = name.getText().toString();
        // perform check on name
        if (nameField.isEmpty() || name.length() < 3) {
            name.setBackgroundColor(Color.RED);
            showToast("Name should have at least 4 characters ");
            nameField = "";
            isCorrect = false;
        }
        // get the e-mail address
        emailField = email.getText().toString();
        // regex for checking if the e-mail is in the correct format
        String regex1 = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@" +
                "(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?";

        // perform check on e-mail
        if (!emailField.matches(regex1)) {
            email.setBackgroundColor(Color.RED);
            showToast("Email address is incorrect.");
            emailField = "";
            isCorrect = false;

        }
        return isCorrect;
    }

}
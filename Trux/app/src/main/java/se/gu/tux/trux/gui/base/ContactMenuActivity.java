package se.gu.tux.trux.gui.base;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import tux.gu.se.trux.R;

/**
 * Created by aman ghezai on 2015-05-14.
 */
public class ContactMenuActivity extends BaseAppActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_menu_activity);
    }
    public void sendFeedback(View button) {
        // Do click handling here
    }
    final EditText nameField = (EditText) findViewById(R.id.EditTextName);
    String name = nameField.getText().toString();

    final EditText emailField = (EditText) findViewById(R.id.EditTextEmail);
    String email = emailField.getText().toString();

    final EditText feedbackField = (EditText) findViewById(R.id.EditTextFeedbackBody);
    String feedback = feedbackField.getText().toString();
    final Spinner feedbackSpinner = (Spinner) findViewById(R.id.SpinnerFeedbackType);
    String feedbackType = feedbackSpinner.getSelectedItem().toString();
} // end class

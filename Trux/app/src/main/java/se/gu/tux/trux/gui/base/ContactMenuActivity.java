package se.gu.tux.trux.gui.base;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import tux.gu.se.trux.R;

/**
 * Created by Aman ghezai on 2015-05-14.
 */
public class ContactMenuActivity extends BaseAppActivity {
    private EditText contactNameInput= null;
    private EditText contactEmailInput= null;
    private EditText feedbackInput = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contactNameInput = (EditText) findViewById(R.id.contactNameInput);
        contactEmailInput = (EditText) findViewById(R.id.contactEmailInput);
        feedbackInput = (EditText) findViewById(R.id.feedbackInput);

    }


}
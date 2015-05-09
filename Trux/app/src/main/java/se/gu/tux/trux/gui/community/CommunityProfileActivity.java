package se.gu.tux.trux.gui.community;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.datastructure.User;
import se.gu.tux.trux.gui.base.BaseAppActivity;
import tux.gu.se.trux.R;

/**
 * Created by ivryashkov on 2015-05-08.
 *
 * Handles the profile page for the user.
 */
@SuppressWarnings("deprecation")
public class CommunityProfileActivity extends BaseAppActivity implements View.OnClickListener
{

    // the action to perform in the activity when the user
    // wants to upload pictures -- 1 is for image, 2 is for videos (! i think)
    private static final int PICK_IMAGE = 1;

    // keeps track whether the user edited profile or not
    private boolean isEdited;
    // image data
    private byte[] imageData;
    // bitmap data
    private Bitmap bitmap;

    // user info
    private String sUsername;
    private String sFirstName;
    private String sLastName;
    private String sEmail;

    // the image container
    private ImageView imageView;

    // buttons
    private Button uploadPic;
    private Button editUsername;
    private Button editFirstName;
    private Button editLastName;
    private Button editEmail;
    private Button saveChanges;
    private Button cancel;

    // edit texts
    private EditText eUsername;
    private EditText eFirstName;
    private EditText eLastName;
    private EditText eEmail;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // set the layout for this view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_profile);

        // set current view showing
        setCurrentViewId(R.layout.activity_community_profile);

        // set edit
        isEdited = false;

        // get the components
        imageView = (ImageView) findViewById(R.id.profile_picture_container);

        uploadPic = (Button) findViewById(R.id.profile_upload_picture_button);
        editUsername = (Button) findViewById(R.id.profile_username_edit_button);
        editFirstName = (Button) findViewById(R.id.profile_firstname_edit_button);
        editLastName = (Button) findViewById(R.id.profile_lastname_edit_button);
        editEmail = (Button) findViewById(R.id.profile_email_edit_button);
        saveChanges = (Button) findViewById(R.id.profile_save_changes_button);
        cancel = (Button) findViewById(R.id.profile_cancel_button);

        eUsername = (EditText) findViewById(R.id.profile_username);
        eFirstName = (EditText) findViewById(R.id.profile_first_name);
        eLastName = (EditText) findViewById(R.id.profile_last_name);
        eEmail = (EditText) findViewById(R.id.profile_email);

        // set enabled option to edit texts
        eUsername.setEnabled(false);
        eFirstName.setEnabled(false);
        eLastName.setEnabled(false);
        eEmail.setEnabled(false);

        // set listener to buttons
        uploadPic.setOnClickListener(this);
        editUsername.setOnClickListener(this);
        editFirstName.setOnClickListener(this);
        editLastName.setOnClickListener(this);
        editEmail.setOnClickListener(this);
        saveChanges.setOnClickListener(this);
        cancel.setOnClickListener(this);

    } // end onCreate()



    @Override
    protected void onStop()
    {
        // call super
        super.onStop();
        // disable edit texts
        eUsername.setEnabled(false);
        eFirstName.setEnabled(false);
        eLastName.setEnabled(false);
        eEmail.setEnabled(false);
        // set edit flag
        isEdited = false;
    }



    @Override
    public void onClick(View view)
    {
        int id = view.getId();

        if (id == uploadPic.getId())
        {
            selectPicture(PICK_IMAGE);
        }
        else if (id == editUsername.getId())
        {
            eUsername.setEnabled(true);
            eUsername.requestFocus();
            isEdited = true;
        }
        else if (id == editFirstName.getId())
        {
            eFirstName.setEnabled(true);
            eFirstName.requestFocus();
            isEdited = true;
        }
        else if (id == editLastName.getId())
        {
            eLastName.setEnabled(true);
            eLastName.requestFocus();
            isEdited = true;
        }
        else if (id == editEmail.getId())
        {
            eEmail.setEnabled(true);
            eEmail.requestFocus();
            isEdited = true;
        }
        else if (id == saveChanges.getId())
        {
            if (isEdited)
            {
                if (checkValues())  { saveChanges(); }
                else                { showToast("Please check values and try again");}
            }
            else { showToast("No changes made"); }
        }
        else if (id == cancel.getId())
        {
            cancel();
        }

    } // end onClick()



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // call super
        super.onActivityResult(requestCode, resultCode, data);

        // check if data is not null
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            Uri uri = data.getData();

            try
            {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                imageView.setImageBitmap(bitmap);

                System.out.println("--------- uploading picture ------------");
                uploadPicture();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

    } // end onActivityResult()



    /**
     * Helper method. Cancels this activity and returns to previous page.
     */
    private void cancel()
    {
        finish();
    }


    /**
     * Helper method. Checks if the new values provided are valid.
     *
     * @return      true if valid, false otherwise
     */
    private boolean checkValues()
    {
        boolean isValid = true;

        // check username
        sUsername = eUsername.getText().toString();
        if (sUsername.isEmpty() || sUsername.length() < 3)
        {
            showToast("Username must be at least 3 characters long.");
            eUsername.setBackground(new ColorDrawable(Color.RED));
            eUsername.requestFocus();
            isValid = false;
        }

        // check first name
        sFirstName = eFirstName.getText().toString();
        if (sFirstName.isEmpty())
        {
            showToast("First name can not be empty.");
            eFirstName.setBackground(new ColorDrawable(Color.RED));
            eFirstName.requestFocus();
            isValid = false;
        }

        // check last name
        sLastName = eLastName.getText().toString();
        if (sLastName.isEmpty())
        {
            showToast("Last name can not be empty.");
            eLastName.setBackground(new ColorDrawable(Color.RED));
            eLastName.requestFocus();
            isValid = false;
        }

        // check e-mail
        sEmail = eEmail.getText().toString();
        // regex to use for checking if the e-mail has a right format
        String regex1 = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@" +
                "(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?";
        if (!sEmail.matches(regex1))
        {
            showToast("E-mail can not be empty.");
            eEmail.setBackground(new ColorDrawable(Color.RED));
            eEmail.requestFocus();
            isValid = false;
        }

        // return the result
        return isValid;

    } // end checkValues()



    /**
     * Helper method. Saves the changes to the user profile.
     */
    private void saveChanges()
    {
        // user object to send to server
        User user = new User();

        // get and set the unchanged values like sessionID and userID
        // maybe that's how we will change values ?
        user.setUserId(DataHandler.getInstance().getUser().getUserId());
        user.setSessionId(DataHandler.getInstance().getUser().getSessionId());
        user.setPasswordHash(DataHandler.getInstance().getUser().getPasswordHash());

        // set fields
        user.setUsername(sUsername);
        user.setFirstName(sFirstName);
        user.setLastName(sLastName);
        user.setEmail(sEmail);
    }



    /**
     * Helper method. Starts a new activity where the user can
     * choose the picture to upload. Takes an int with the action
     * to perform.
     *
     * @param code      The action to perform.
     */
    private void selectPicture(int code)
    {
        // start a new intent
        Intent intent = new Intent();

        // set type and action of the intent
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        // start an activity for result, we want some data returned
        // in this case it will be the picture data
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), code);
    }



    /**
     * Helper method. Uploads an image to the server.
     */
    private boolean uploadPicture()
    {
        AsyncTask<Void, Void, Boolean> task = new UploadImage().execute();

        boolean result = false;

        try
        {
             result = task.get();
        }
        catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }

        if (result)
        {
            System.out.println("--------- decoding byte array ------------");
            Bitmap bm = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            System.out.println("--------- setting image ------------");
            imageView.setImageBitmap(bm);
            return true;
        }
        else
        {
            return false;
        }

    } // end uploadPicture()



    /**
     * Private class to upload an image from phone gallery.
     */
    private class UploadImage extends AsyncTask<Void, Void, Boolean>
    {
        @Override
        protected void onPreExecute()
        {
            showToast("Uploading picture...");
        }

        @Override
        protected Boolean doInBackground(Void... voids)
        {
            // open byte output stream
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            // compress image
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            // get image byte data
            imageData = bos.toByteArray();
            System.out.println("--------- byte array image: " + Arrays.toString(imageData) + " ------------");

            // return false if no data
            if (imageData == null)
            {
                return false;
            }

            // true otherwise
            return true;
        }

    } // end inner class


} // end class

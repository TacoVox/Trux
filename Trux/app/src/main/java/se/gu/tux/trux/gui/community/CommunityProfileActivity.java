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
import android.widget.RelativeLayout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.datastructure.Picture;
import se.gu.tux.trux.datastructure.ProtocolMessage;
import se.gu.tux.trux.datastructure.User;
import se.gu.tux.trux.gui.base.BaseAppActivity;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import se.gu.tux.trux.technical_services.ServerConnector;
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
    private static final int SELECT_IMAGE = 1;

    // keeps track whether the user edited profile or not
    private boolean isPicEdited;
    private boolean isEdited;
    // bitmap data
    private Bitmap bitmap;

    // user info
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

    // layouts
    private RelativeLayout loadingPanel;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // set the layout for this view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_profile);

        // set current view showing
        setCurrentViewId(R.layout.activity_community_profile);

        // get the components
        imageView = (ImageView) findViewById(R.id.profile_picture_container);

        uploadPic = (Button) findViewById(R.id.profile_upload_picture_button);
        editUsername = (Button) findViewById(R.id.profile_username_edit_button);
        editFirstName = (Button) findViewById(R.id.profile_firstname_edit_button);
        editLastName = (Button) findViewById(R.id.profile_lastname_edit_button);
        editEmail = (Button) findViewById(R.id.profile_email_edit_button);
        saveChanges = (Button) findViewById(R.id.profile_save_changes_button);
        cancel = (Button) findViewById(R.id.profile_cancel_button);
        loadingPanel = (RelativeLayout) findViewById(R.id.loadingPanel);

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

        // set the profile pic and details
        setProfile();

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
    }



    @Override
    public void onClick(View view)
    {
        int id = view.getId();

        if (id == uploadPic.getId())
        {
            isPicEdited = true;
            selectPicture(SELECT_IMAGE);
        }
        else if (id == editUsername.getId())
        {
            showDialogBox("Change username",
                    "To change your username, please contact the system administrators.\nHave a nice day!");
        }
        else if (id == editFirstName.getId())
        {
            isEdited = true;
            eFirstName.setEnabled(true);
            eFirstName.requestFocus();
        }
        else if (id == editLastName.getId())
        {
            isEdited = true;
            eLastName.setEnabled(true);
            eLastName.requestFocus();
        }
        else if (id == editEmail.getId())
        {
            isEdited = true;
            eEmail.setEnabled(true);
            eEmail.requestFocus();
        }
        else if (id == saveChanges.getId())
        {
            if (isEdited || isPicEdited)
            {
                if (checkValues())
                {
                    saveChanges();
                    cancel();
                }
                else { showToast("Please check values and try again");}
            }
            else
            {
                showToast("No changes made. Returning to home screen.");
                cancel();
            }
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
        if (requestCode == SELECT_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            // get the uri address for the picture
            Uri uri = data.getData();

            try
            {
                // get the bitmap data
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                // set profile picture in profile page
                imageView.setImageBitmap(bitmap);
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
        showToast("Cancelling profile activity...");
        finish();
    }



    /**
     * Sets the profile picture and details.
     */
    private void setProfile()
    {
        loadingPanel.setVisibility(View.VISIBLE);

        new FetchImageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        eUsername.setText(DataHandler.getInstance().getUser().getUsername());
        eFirstName.setText(DataHandler.getInstance().getUser().getFirstName());
        eLastName.setText(DataHandler.getInstance().getUser().getLastName());
        eEmail.setText(DataHandler.getInstance().getUser().getEmail());

    } // end setProfile()



    /**
     * Helper method. Checks if the new values provided are valid.
     *
     * @return      true if valid, false otherwise
     */
    private boolean checkValues()
    {
        boolean isValid = true;

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
        if (isPicEdited)
        {
            boolean picIsUploaded = uploadPicture(bitmap);

            if (picIsUploaded) { showToast("Picture uploaded"); }
        }

        if (isEdited)
        {
            // user object to send to server
            User user = new User();

            // get and set the unchanged values like sessionID and userID
            user.setUserId(DataHandler.getInstance().getUser().getUserId());
            user.setSessionId(DataHandler.getInstance().getUser().getSessionId());
            user.setPasswordHash(DataHandler.getInstance().getUser().getPasswordHash());

            // set fields
            user.setFirstName(sFirstName);
            user.setLastName(sLastName);
            user.setEmail(sEmail);
            user.setRequestProfileChange(true);

            // set the user back to DataHandler
            DataHandler.getInstance().setUser(user);

            boolean saveChanges = false;

            AsyncTask<User, Void, Boolean> saveTask = new SaveChangesTask().execute(user);

            try
            {
                saveChanges = saveTask.get();
            }
            catch (InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
            }

            if (saveChanges)
            {
                DataHandler.getInstance().getUser().setRequestProfileChange(false);
                showToast("Profile changes saved");
            }
        }

    } // end saveChanges()



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
    private boolean uploadPicture(Bitmap bitmap)
    {
        System.out.println("--------- calling uploadPicture() ------------");
        // open byte output stream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // compress image
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);

        System.out.println("--------- decoding byte array ------------");
        // get image byte data
        byte[] imageData = bos.toByteArray();

        // create picture object
        Picture uploadPicture = new Picture(0);

        if (imageData != null)
        {
            uploadPicture.setImg(imageData);
        }

        AsyncTask<Picture, Void, Boolean> uploadTask = new UploadPictureTask().execute(uploadPicture);

        boolean result = false;

        try
        {
             result = uploadTask.get();
        }
        catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }

        return result;

    } // end uploadPicture()



    /**
     * Private class to upload an image from phone gallery.
     */
    private class UploadPictureTask extends AsyncTask<Picture, Void, Boolean>
    {
        @Override
        protected void onPreExecute()
        {
            showToast("Uploading picture...");
        }

        @Override
        protected Boolean doInBackground(Picture... pictures)
        {
            ProtocolMessage response = null;

            try
            {
                response = (ProtocolMessage) ServerConnector.getInstance().answerQuery(pictures[0]);
            }
            catch (NotLoggedInException e)
            {
                e.printStackTrace();
            }

            return response != null && response.getType() == ProtocolMessage.Type.SUCCESS;
        }

    } // end inner class



    /**
     * Private class to save changes to profile.
     */
    private class SaveChangesTask extends AsyncTask<User, Void, Boolean>
    {
        @Override
        protected void onPreExecute()
        {
            showToast("Saving changes to profile...");
        }

        @Override
        protected Boolean doInBackground(User... users)
        {
            ProtocolMessage response = null;

            try
            {
                response = (ProtocolMessage) ServerConnector.getInstance().answerQuery(users[0]);
            }
            catch (NotLoggedInException e)
            {
                e.printStackTrace();
            }

            return response != null && response.getType() == ProtocolMessage.Type.SUCCESS;
        }

    } // end inner class



    /**
     * Private class to fetch the profile picture.
     */
    private class FetchImageTask extends AsyncTask<Void, Void, Bitmap>
    {
        @Override
        protected void onPreExecute()
        {
            showToast("Fetching image for profile picture...");
        }

        @Override
        protected Bitmap doInBackground(Void... voids)
        {
            Bitmap image = null;

            try
            {
                image = DataHandler.getInstance().getPicture(DataHandler.getInstance().getUser().getProfilePicId());
            }
            catch (NotLoggedInException e)
            {
                e.printStackTrace();
            }

            if (image != null)
            {
                return image;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }

            loadingPanel.setVisibility(View.GONE);
        }
    } // end inner class


} // end class

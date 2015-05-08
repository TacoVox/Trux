package se.gu.tux.trux.gui.community;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutionException;

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

    byte[] imageData;
    Bitmap bitmap;
    Button uploadPic;
    ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // set the layout for this view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_profile);

        // set current view showing
        setCurrentViewId(R.layout.activity_community_profile);

        // get the components
        uploadPic = (Button) findViewById(R.id.profile_upload_picture_button);
        imageView = (ImageView) findViewById(R.id.profile_picture_container);

        // set listener to buttons
        uploadPic.setOnClickListener(this);
    }



    @Override
    public void onClick(View view)
    {
        int id = view.getId();

        if (id == uploadPic.getId())
        {
            showToast("Upload Profile Picture button clicked");

            selectPicture(PICK_IMAGE);
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // call super
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case PICK_IMAGE:
                if (resultCode == RESULT_OK)
                {
                    // get the bitmap data
                    bitmap = getPath(data.getData());
                    // set this image as profile
                    imageView.setImageBitmap(bitmap);
                    // upload image to server
                    //uploadPicture();
                }
                break;

            default:
                showToast("Could not select image");
                break;
        }
    }



    /**
     * Helper method. Gets the bitmap data for the image.
     *
     * @param uri       The uri.
     * @return          The bitmap data for the image.
     */
    private Bitmap getPath(Uri uri)
    {
        String[] projection = { MediaStore.Images.Media.DATA };

        Cursor cursor = managedQuery(uri, projection, null, null, null);

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        String filePath = cursor.getString(column_index);

        cursor.close();
        // Convert file path into bitmap image

        // return the bitmap
        return BitmapFactory.decodeFile(filePath);
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
            return true;
        }
        else
        {
            return false;
        }

    } // end uploadPicture()



    /**
     * Private class to upload an image to server.
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

            // return true if we have some data
            if (imageData != null)
            {
                return true;
            }

            // false otherwise
            return false;
        }

    } // end inner class


} // end class

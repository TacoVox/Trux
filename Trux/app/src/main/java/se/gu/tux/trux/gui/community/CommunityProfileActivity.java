package se.gu.tux.trux.gui.community;

import android.content.Intent;
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
import java.io.IOException;
import java.util.Arrays;
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

    private byte[] imageData;
    private Bitmap bitmap;
    private Button uploadPic;
    private ImageView imageView;


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

package se.gu.tux.trux.gui.community;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;

import se.gu.tux.trux.datastructure.Friend;
import se.gu.tux.trux.datastructure.Picture;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import tux.gu.se.trux.R;


public class InfoFragment extends Fragment {

    TextView nameText, infoText;
    Button removeButton;
    ImageView profilePic;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);
        removeButton = (Button) view.findViewById(R.id.infoRemoveButton);
        nameText = (TextView) view.findViewById(R.id.nameTextView);
        infoText = (TextView) view.findViewById(R.id.infoTextView);
        profilePic = (ImageView) view.findViewById(R.id.infoPicture);

        viewFriendInfo();

        return view;
    }

    private void viewFriendInfo() {

        Bundle bundle = this.getArguments();

        final Friend[] newFriend = (Friend[]) bundle.getSerializable("friendArray");
        System.out.println("FriendArray is sent to the InfoFragment: " + newFriend.length);
        final Bitmap[] newPicture = (Bitmap[]) bundle.getSerializable("pictureArray");
        System.out.println("PictureArray is sent to the InfoFragment: " + newFriend.length);
        HashMap<String, Friend> friendMarker = (HashMap) bundle.getSerializable("hashmap");
        String markerID = bundle.getString("markerID");
        for(int i = 0; i < newFriend.length; i++){
            if(friendMarker.containsKey(markerID)){
                nameText.setText(newFriend[i].getFirstname() + " " + newFriend[i].getLastname());
                profilePic.setImageBitmap(Bitmap.createScaledBitmap(newPicture[i], 50, 50, false));
                infoText.setText("");
            }
        }
    }
}




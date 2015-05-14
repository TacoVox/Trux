package se.gu.tux.trux.gui.community;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import se.gu.tux.trux.datastructure.Friend;
import se.gu.tux.trux.datastructure.Picture;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import tux.gu.se.trux.R;


public class InfoFragment extends MapFrag {

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
        final Friend[] newFriend = getFriends();
        final Bitmap[] newPicture = getPictures();
        for(int i = 0; i < newFriend.length; i++){
            if(friendMarker.containsKey(markerID)){
                nameText.setText(newFriend[i].getFirstname() + " " + newFriend[i].getLastname());
                profilePic.setImageBitmap(Bitmap.createScaledBitmap(newPicture[i], 50,50, false));
                infoText.setText("");
            }
        }
    }
}




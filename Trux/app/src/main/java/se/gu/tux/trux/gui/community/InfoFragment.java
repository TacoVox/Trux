package se.gu.tux.trux.gui.community;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;

import se.gu.tux.trux.application.SocialHandler;
import se.gu.tux.trux.datastructure.Friend;
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

    ViewFriendInfo();

    return view;
}

private void ViewFriendInfo() {

    Bundle bundle = this.getArguments();
    if(bundle != null){
        HashMap<String, Friend> friendMarker = (HashMap) bundle.getSerializable("friendHashmap");

        String markerID = bundle.getString("markerID");

            if (friendMarker != null ) {
                Friend friend = friendMarker.get(markerID);
                if(friend.getProfilePic()!=null) {
                    Bitmap pic = Bitmap.createScaledBitmap(
                            SocialHandler.pictureToBitMap(friend.getProfilePic())
                            , 200, 200, false);

                    nameText.setText(friend.getFirstname() + " " + friend.getLastname());
                    profilePic.setImageBitmap(pic);


                    infoText.setText("");
                }
        }

    }
}
    public void onStop() {
        super.onStop();
        nameText.setText("");

    }
    public void onPause(){
        super.onPause();
        nameText.setText("");

    }
    public void onResume(){
        super.onResume();
        ViewFriendInfo();
    }
}




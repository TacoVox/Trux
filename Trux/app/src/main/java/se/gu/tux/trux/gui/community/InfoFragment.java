package se.gu.tux.trux.gui.community;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import se.gu.tux.trux.application.SocialHandler;
import se.gu.tux.trux.datastructure.Friend;
import se.gu.tux.trux.gui.messaging.MessageActivity;
import tux.gu.se.trux.R;


public class InfoFragment extends Fragment {

    TextView profileTitle;
    ImageButton removeButton, messageButton;
    ImageView profilePic;
    Friend friend;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);
        removeButton = (ImageButton) view.findViewById(R.id.fragment_info_remove_friend_button);
        messageButton = (ImageButton) view.findViewById(R.id.fragment_info_message_button);
        profileTitle = (TextView) view.findViewById(R.id.profile_title);
        profilePic = (ImageView) view.findViewById(R.id.infoPicture);

        if(this.getArguments() != null) {
            friend = (Friend) this.getArguments().getSerializable("friend");
        }

        showFriendInfo();

        return view;
    }

    private void showFriendInfo() {
        if (friend != null) {
            profileTitle.setText(friend.getFirstname() + " " + friend.getLastname()
                    + "(" + friend.getUsername() + ")");

            if (friend.getProfilePic() != null) {
                Bitmap pic = Bitmap.createScaledBitmap(
                        SocialHandler.pictureToBitMap(friend.getProfilePic())
                        , 500, 500, false);
                profilePic.setImageBitmap(pic);
            }

            messageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity().getApplicationContext(), MessageActivity.class);
                    intent.setAction("OPEN_CHAT");
                    intent.putExtra("FRIEND_ID", friend.getFriendId());
                    intent.putExtra("FRIEND_USERNAME", friend.getUsername());
                    startActivity(intent);
                }
            });
        }
    }

    public void onStop() {
        super.onStop();

    }
    public void onPause(){
        super.onPause();

    }
    public void onResume(){
        super.onResume();
    }

}




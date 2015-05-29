package se.gu.tux.trux.gui.community;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.datastructure.Friend;
import se.gu.tux.trux.gui.messaging.MessageActivity;
import tux.gu.se.trux.R;


/**
 * This fragment holds the little menu that pops up when you click a friend in the map,
 * from which you can currently go to that friends profile or go to the chat window for that friend.
 */
public class MapSocialMenu extends Fragment {

    LinearLayout menu, layout;
    ImageButton messageButton, infoButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_map_social_menu, container, false);

        layout = (LinearLayout) view.findViewById(R.id.backLayout);
        menu = (LinearLayout) view.findViewById(R.id.menu);
        messageButton = (ImageButton) view.findViewById(R.id.messageButton);
        infoButton = (ImageButton) view.findViewById(R.id.infoButton);

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //removeMenu();
                //menu.setVisibility(View.GONE);
            }
        });

        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //removeMenu();
                Bundle b = getArguments();
                Friend friend = (Friend) b.getSerializable("friend");
                if (friend != null) {
                    Intent intent = new Intent(getActivity().getApplicationContext(), MessageActivity.class);
                    intent.setAction("OPEN_CHAT");
                    intent.putExtra("FRIEND_ID", friend.getFriendId());
                    intent.putExtra("FRIEND_USERNAME", friend.getUsername());
                    startActivity(intent);
                }
            }
        });

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfoWindow();
            }
        });

        System.out.println("Level: " + DataHandler.gI().getSafetyStatus());
        if(isSimple()) {
            infoButton.setVisibility(View.GONE);
        }

        return view;
    }



    public void showInfoWindow() {

        FriendProfileFragment ifragment = new FriendProfileFragment();

        //Passing the arguments from MapFrag to InfoFragment
        ifragment.setArguments(this.getArguments());

        menu.setVisibility(View.GONE);
        //removeMenu();

        //Transaction to the InfoFragment
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        fragmentTransaction.addToBackStack("PROFILE");
        fragmentTransaction.replace(R.id.contentContainer, ifragment);
        fragmentTransaction.commit();
    }

    private boolean isSimple() {
        return DataHandler.gI().getSafetyStatus() != DataHandler.SafetyStatus.IDLE;
    }

}

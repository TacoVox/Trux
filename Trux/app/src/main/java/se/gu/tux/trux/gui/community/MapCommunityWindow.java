package se.gu.tux.trux.gui.community;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import tux.gu.se.trux.R;


public class MapCommunityWindow extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_map_community_window, container, false);

        LinearLayout layout = (LinearLayout) view.findViewById(R.id.backLayout);
        Button messageButton = (Button) view.findViewById(R.id.messageButton);
        Button infoButton = (Button) view.findViewById(R.id.infoButton);
        Button removeFriendButton = (Button) view.findViewById(R.id.removeFriend);

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeMenu();
            }
        });

        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeMenu();
            }
        });

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfoWindow();
            }
        });

        removeFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeMenu();
            }
        });

        return view;
    }

    public void removeMenu() {

        Fragment mcw = this;

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        fragmentTransaction.remove(mcw);
        fragmentTransaction.commit();
    }

    public void showInfoWindow() {

        InfoFragment ifragment = new InfoFragment();

        //Passing the arguments from MapFrag to InfoFragment
        ifragment.setArguments(this.getArguments());

        //Transaction to the InfoFragment
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        fragmentTransaction.replace(R.id.contentContainer, ifragment);
        fragmentTransaction.commit();
    }

}

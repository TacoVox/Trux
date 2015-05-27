package se.gu.tux.trux.gui.community;

import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import se.gu.tux.trux.gui.community.MapFrag;
import se.gu.tux.trux.gui.main_home.HomeActivity;
import se.gu.tux.trux.gui.statistics.StatisticsSimpleFragment;
import tux.gu.se.trux.R;

/**
 * Handles the community fragment in the main activity. Contains a MapFrag.
 */
public class CommunityMainFragment extends Fragment implements View.OnClickListener
{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // inflate view
        View view = inflater.inflate(R.layout.fragment_main_i_community, container, false);

        // get components
        FrameLayout mapContainer = (FrameLayout) view.findViewById(R.id.mapContainer);
        ImageButton friendsButton = (ImageButton) view.findViewById(R.id.fragment_main_friend_button);
        ImageButton profileButton = (ImageButton) view.findViewById(R.id.fragment_main_profile_button);

        MapFrag mapFrag;
        if (savedInstanceState != null) {
            // Do not recreate the MapFrag if there is a saved instance bundle
            mapFrag = (MapFrag) getActivity().getSupportFragmentManager().findFragmentById(R.id.map);
        } else {
            mapFrag = new MapFrag();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.mapContainer, mapFrag);
            fragmentTransaction.commit();
        }

        // set listener to components
        friendsButton.setOnClickListener(this);
        profileButton.setOnClickListener(this);

        // return the view
        return view;

    }


    @Override
    public void onClick(View view)
    {
        ((HomeActivity) getActivity() ).onFragmentViewClick(view.getId());
    }


} // end class

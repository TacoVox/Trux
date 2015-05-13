package se.gu.tux.trux.gui.community;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import tux.gu.se.trux.R;


public class MapCommunityWindow extends Fragment {

    private static final int LAYOUT = R.id.emptyLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map_community_window, container, false);

        LinearLayout layout = (LinearLayout) view.findViewById(R.id.menuLayout);

        return view;
    }

    public void onFragmentViewClick(int id) {
        if(id == LAYOUT){
        }
    }


}

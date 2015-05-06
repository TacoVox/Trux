package se.gu.tux.trux.gui.main_i;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import tux.gu.se.trux.R;

/**
 * Created by ivryashkov on 2015-05-05.
 *
 * Handles the community fragment in the main activity.
 */
public class ICommunityFragment extends Fragment implements View.OnClickListener
{



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // inflate view
        View view = inflater.inflate(R.layout.fragment_main_i_community, container, false);

        // get components
        ImageView imageView = (ImageView) view.findViewById(R.id.fragment_main_i_image_view);

        // set listener to components
        imageView.setOnClickListener(this);

        // return the view
        return view;

    }


    @Override
    public void onClick(View view)
    {
        ( (IMainActivity) getActivity() ).onFragmentViewClick(view.getId());
    }


} // end class

package se.gu.tux.trux.gui.welcome;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.gui.main_home.HomeActivity;
import tux.gu.se.trux.R;

/**
 * Created by ivryashkov on 2015-05-05.
 */
public class WelcomeMainFragment extends Fragment implements View.OnClickListener
{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_main_welcome, container, false);

        Button messageButton = (Button) view.findViewById(R.id.fragment_welcome_message_button);

        messageButton.setOnClickListener(this);

        return view;
    }


    @Override
    public void onClick(View view)
    {
        ((HomeActivity) getActivity() ).onFragmentViewClick(view.getId());
    }


} // end class

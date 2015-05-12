package se.gu.tux.trux.gui.welcome;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import se.gu.tux.trux.application.DataHandler;
import tux.gu.se.trux.R;

/**
 * Created by ivryashkov on 2015-05-05.
 */
public class WelcomeMainFragment extends Fragment
{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_main_welcome, container, false);

        // start fetching data for the statistics
        // do it here instead of in StatisticsMainFragment, this fragment will always be
        // loaded first and so reduce waiting time when in statistics view


        return view;
    }


} // end class

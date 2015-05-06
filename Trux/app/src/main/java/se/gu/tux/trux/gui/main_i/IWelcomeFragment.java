package se.gu.tux.trux.gui.main_i;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tux.gu.se.trux.R;

/**
 * Created by ivryashkov on 2015-05-05.
 */
public class IWelcomeFragment extends Fragment
{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_main_i_welcome, container, false);

        return view;
    }


} // end class

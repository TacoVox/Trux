package se.gu.tux.trux.gui.statistics_i;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tux.gu.se.trux.R;

/**
 * Created by ivryashkov on 2015-05-04.
 */
public class IFuelFragment extends Fragment
{

    private View view;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // inflate the view
        view = inflater.inflate(R.layout.statistics_i_fragment_speed, container, false);
        // return the view
        return view;
    }


} // end class

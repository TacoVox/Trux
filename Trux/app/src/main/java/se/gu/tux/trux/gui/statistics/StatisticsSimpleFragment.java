package se.gu.tux.trux.gui.statistics;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tux.gu.se.trux.R;

/**
 * Simple view to display when in driving mode.
 */
public class StatisticsSimpleFragment extends Fragment
{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_statistics_simple, container, false);
    }


} // end class

package se.gu.tux.trux.gui.simpleStats;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tux.gu.se.trux.R;

public class SimpleSpeedWindow extends Fragment
{

    private View myFragmentView;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        myFragmentView = inflater.inflate(R.layout.fragment_simple_speed_window, container, false);

        return myFragmentView;
    }

}

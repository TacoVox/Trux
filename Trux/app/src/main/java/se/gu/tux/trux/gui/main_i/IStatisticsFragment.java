package se.gu.tux.trux.gui.main_i;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import tux.gu.se.trux.R;

/**
 * Created by ivryashkov on 2015-05-05.
 */
public class IStatisticsFragment extends Fragment implements View.OnClickListener
{


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_main_i_statistics, container, false);

        // get the components
        Button statsButton = (Button) view.findViewById(R.id.fm_i_statistics_check_stats_button);

        statsButton.setOnClickListener(this);

        return view;
    }


    @Override
    public void onClick(View view)
    {
        ( (IMainActivity) getActivity() ).onFragmentViewClick(view.getId());
    }


} // end class

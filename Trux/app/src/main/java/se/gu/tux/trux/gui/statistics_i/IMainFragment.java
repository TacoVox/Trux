package se.gu.tux.trux.gui.statistics_i;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import tux.gu.se.trux.R;

/**
 * Created by ivryashkov on 2015-05-04.
 */
public class IMainFragment extends Fragment implements IStatisticsActivity.StatisticsFragmentInterface,
        View.OnClickListener
{

    private static final int SPEED_BTN = R.id.speed_button;
    private static final int FUEL_BTN = R.id.fuel_button;
    private static final int DISTANC_BTN = R.id.distance_traveled;
    private static final int OVERALL_BTN = R.id.overall_button;

    private static final int SPEED_BUTTON = R.id.activity_statistics_i_speed_button;
    private static final int FUEL_BUTTON = R.id.activity_statistics_i_fuel_button;
    private static final int DISTANC_BUTTON = R.id.activity_statistics_i_distance_button;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // inflate the view
        View view = inflater.inflate(R.layout.activity_stats, container, false);

        // get components for this view
        Button speedBtn = (Button) view.findViewById(SPEED_BTN);
        Button fuelBtn = (Button) view.findViewById(FUEL_BTN);
        Button distanceBtn = (Button) view.findViewById(DISTANC_BTN);
        Button overallBtn = (Button) view.findViewById(OVERALL_BTN);

        //Button speedBtn = (Button) view.findViewById(SPEED_BUTTON);
        //Button fuelBtn = (Button) view.findViewById(FUEL_BUTTON);
        //Button distanceBtn = (Button) view.findViewById(DISTANC_BUTTON);

        // set listener for buttons
        speedBtn.setOnClickListener(this);
        fuelBtn.setOnClickListener(this);
        distanceBtn.setOnClickListener(this);
        overallBtn.setOnClickListener(this);

        // return the view
        return view;
    }


    @Override
    public void onClick(View view)
    {
        // get view id
        int viewId = view.getId();
        // button clicked
        onFragmentViewClick(viewId);
    }


    @Override
    public void onFragmentViewClick(int viewId)
    {
        // get parent activity and notify which button is clicked
        // the rest will be handled in the activity
        ( (IStatisticsActivity) getActivity() ).onFragmentViewClick(viewId);
    }


} // end class

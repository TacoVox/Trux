package se.gu.tux.trux.gui.statistics;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import se.gu.tux.trux.appplication.DataHandler;
import tux.gu.se.trux.R;

/**
 * Created by ivryashkov on 2015-05-06.
 *
 * Handles the main statistics fragment. Holds a spinner
 */
public class StatisticsMainFragment extends Fragment implements AdapterView.OnItemSelectedListener
{
    String[] spinnerItemsTitles = {"Overall", "Speed", "Distance traveled", "Fuel"};

    List<Fragment> fragmentList;
    LayoutInflater layoutInflater;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        layoutInflater = inflater;
        View view = inflater.inflate(R.layout.fragment_main_statistics, container, false);

        Spinner spinner = (Spinner) view.findViewById(R.id.fragment_statistics_spinner);

        DataHandler.getInstance().cacheDetailedStats();

        fragmentList = new ArrayList<>();
        fragmentList.add(new OverallGraphWindow());
        fragmentList.add(new SpeedWindow());
        fragmentList.add(new DistTravWindow());
        fragmentList.add(new FuelWindow());

        //ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_item, list);
        ArrayAdapter<String> dataAdapter = new StatisticsMainSpinnerAdapter(view.getContext(), R.layout.spinner_item, spinnerItemsTitles);

        dataAdapter.setDropDownViewResource(R.layout.spinner_item);

        spinner.setAdapter(dataAdapter);

        spinner.setOnItemSelectedListener(this);

        return view;
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
    {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.statistics_frame_container, fragmentList.get(i));
        fragmentTransaction.commit();
    }


    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}


} // end class

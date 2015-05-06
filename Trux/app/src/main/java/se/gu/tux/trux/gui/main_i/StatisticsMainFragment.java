package se.gu.tux.trux.gui.main_i;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import se.gu.tux.trux.appplication.DataHandler;
import se.gu.tux.trux.gui.detailedStats.DistTravWindow;
import se.gu.tux.trux.gui.detailedStats.OverallGraphWindow;
import se.gu.tux.trux.gui.detailedStats.SpeedWindow;
import tux.gu.se.trux.R;

/**
 * Created by ivryashkov on 2015-05-06.
 *
 * Handles the main statistics fragment. Holds a spinner
 */
public class StatisticsMainFragment extends Fragment implements AdapterView.OnItemSelectedListener
{
    String[] spinnerItemsTitles = {"Overall", "Speed", "Distance traveled"};

    List<Fragment> fragmentList;
    LayoutInflater layoutInflater;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        layoutInflater = inflater;
        View view = inflater.inflate(R.layout.fragment_statistics_main_view, container, false);

        Spinner spinner = (Spinner) view.findViewById(R.id.fragment_statistics_spinner);

        DataHandler.getInstance().cacheDetailedStats();

        fragmentList = new ArrayList<>();
        fragmentList.add(new OverallGraphWindow());
        fragmentList.add(new SpeedWindow());
        fragmentList.add(new DistTravWindow());

        //ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_item, list);
        ArrayAdapter<String> dataAdapter = new SpinnerAdapter(view.getContext(), R.layout.spinner_item, spinnerItemsTitles);

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


    /**
     * Custom spinner adapter. Loads a custom view for each spinner item.
     */
    class SpinnerAdapter extends ArrayAdapter<String>
    {
        // holds the titles for each spinner item
        String[] titles;

        /**
         * Constructor. Takes the context to which to apply, the layout for
         * the spinner objects and the titles for each row. Simply makes a call
         * to super with same parameters.
         *
         * @param context       The context to apply.
         * @param resource      The spinner layout
         * @param objects       The titles for each row in spinner.
         */
        public SpinnerAdapter(Context context, int resource, String[] objects)
        {
            // make a call to super
            super(context, resource, objects);
            // save the titles for future reference
            titles = objects;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            // here instead of returning super with default android layout
            // we call a helper method to return a custom view
            return getSpinnerItemView(position, convertView, parent);
        }


        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent)
        {
            // here instead of returning super with default android layout
            // we call a helper method to return a custom view
            // NOTE: required a call here as well, this happens when clicked on the
            // spinner to display items
            return getSpinnerItemView(position, convertView, parent);
        }


        /**
         * Helper method to customise the view for each spinner item.
         *
         * @param position      The current item position.
         * @param convertView   Not used, maybe can be removed.
         * @param parent        The parent view group to which this component belongs.
         * @return              The custom View.
         */
        private View getSpinnerItemView(int position, View convertView, ViewGroup parent)
        {
            View row = layoutInflater.inflate(R.layout.spinner_item, parent, false);
            TextView rowText = (TextView) row.findViewById(R.id.spinner_item_text_view);

            rowText.setText(titles[position]);

            return row;
        }

    } // end nested class


} // end class

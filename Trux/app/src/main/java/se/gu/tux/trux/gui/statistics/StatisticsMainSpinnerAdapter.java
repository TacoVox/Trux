package se.gu.tux.trux.gui.statistics;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import tux.gu.se.trux.R;

/**
 * Created by ivryashkov on 2015-05-07.
 *
 * Custom spinner adapter. Loads a custom view for each spinner item.
 */
public class StatisticsMainSpinnerAdapter extends ArrayAdapter<String>
{

    // holds the titles for each spinner item
    private String[] titles;
    // reference to the context
    private Context context;


    /**
     * Constructor. Takes the context to which to apply, the layout for
     * the spinner objects and the titles for each row. Simply makes a call
     * to super with same parameters.
     *
     * @param context       The context to apply.
     * @param resource      The spinner layout
     * @param objects       The titles for each row in spinner.
     */
    public StatisticsMainSpinnerAdapter(Context context, int resource, String[] objects)
    {
        // make a call to super
        super(context, resource, objects);
        // save the titles for future reference
        titles = objects;
        // get the context for reference to inflate views later
        this.context = context;
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
        LayoutInflater inflater = LayoutInflater.from(context);
        View row = inflater.inflate(R.layout.spinner_item, parent, false);
        TextView rowText = (TextView) row.findViewById(R.id.spinner_item_text_view);

        rowText.setText(titles[position]);

        return row;
    }


} // end class

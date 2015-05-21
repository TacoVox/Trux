package se.gu.tux.trux.gui.messaging;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import tux.gu.se.trux.R;

/**
 * Created by ivryashkov on 2015-05-21.
 */
public class SimpleMessageSpinner extends ArrayAdapter<String>
{

    private String[] titles;

    private Context context;




    public SimpleMessageSpinner(Context context, int resource, String[] objects)
    {
        super(context, resource, objects);

        titles = objects;

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

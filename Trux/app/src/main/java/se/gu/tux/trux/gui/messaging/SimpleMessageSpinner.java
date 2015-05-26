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
 *
 * Handles the spinner to display predefined messages when in simple mode (driving).
 */
public class SimpleMessageSpinner extends ArrayAdapter<String>
{

    // the titles to display in the spinner
    private String[] titles;
    // the context to use
    private Context context;



    /**
     * Constructor. Takes the context to use, the layout resourse for the spinner items and
     * the titles to show as parameters.
     *
     * @param context       The context to use.
     * @param resource      The layout resourse.
     * @param titles        The titles to show.
     */
    public SimpleMessageSpinner(Context context, int resource, String[] titles)
    {
        super(context, resource, titles);

        this.titles = titles;

        this.context = context;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        // here instead of returning super with default android layout
        // we call a helper method to return a custom view
        return getSpinnerItemView(position, parent);
    }



    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        // here instead of returning super with default android layout
        // we call a helper method to return a custom view
        // NOTE: required a call here as well, this happens when clicked on the
        // spinner to display items
        return getSpinnerItemView(position, parent);
    }



    /**
     * Helper method to customise the view for each spinner item.
     *
     * @param position      The current item position.
     * @param parent        The parent view group to which this component belongs.
     * @return              The custom View.
     */
    private View getSpinnerItemView(int position, ViewGroup parent)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        View row = inflater.inflate(R.layout.spinner_message_item, parent, false);
        TextView rowText = (TextView) row.findViewById(R.id.spinner_item_text_view_message);

        rowText.setText(titles[position]);

        return row;
    }


} // end class

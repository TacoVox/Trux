package se.gu.tux.trux.gui.messaging;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import se.gu.tux.trux.datastructure.Friend;
import tux.gu.se.trux.R;

/**
 * Created by ivryashkov on 2015-05-13.
 *
 * Handles the display for the message list activity.
 */
public class MessageListAdapter extends BaseAdapter
{

    // the data to display
    private Friend[] friends;
    // to inflate each layout
    private static LayoutInflater layoutInflater;



    /**
     * Constructor. Takes the activity where to display items and
     * the data to display on those items.
     *
     * @param activity          The activity.
     * @param friends           The data to display.
     */
    public MessageListAdapter(Activity activity, Friend[] friends)
    {
        this.friends = friends;
        layoutInflater = (LayoutInflater) activity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount()
    {
        return friends.length;
    }


    @Override
    public Object getItem(int i)
    {
        return friends[i];
    }


    @Override
    public long getItemId(int i)
    {
        return i;
    }


    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        // the view to return
        View newView = view;
        ViewHolder viewHolder;

        if (view == null)
        {
            // get the view for this holder
            newView = layoutInflater.inflate(R.layout.message_list_item, viewGroup, false);
            // initialise the view holder
            viewHolder = new ViewHolder();

            // get the components for this view
            viewHolder.userPicture = (ImageView) newView.findViewById(R.id.message_username_picture);
            viewHolder.username = (TextView) newView.findViewById(R.id.message_username_text_view);
            viewHolder.content = (TextView) newView.findViewById(R.id.message_content_text_view);

            // set the holder for this view
            newView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) newView.getTag();
        }

        if (friends.length <= 0)
        {
            // set no data
            viewHolder.username.setText("No Data");
            viewHolder.content.setText("No Data");
        }
        else
        {
            // set the data to show
            viewHolder.username.setText(friends[i].getUsername());
        }

        // return the view
        return newView;
    }


    /**
     * Holds the data for each view. Recommended pattern to use.
     */
    public static class ViewHolder
    {
        public ImageView userPicture;
        public TextView username;
        public TextView content;
    }


} // end class

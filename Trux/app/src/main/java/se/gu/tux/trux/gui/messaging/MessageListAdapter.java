package se.gu.tux.trux.gui.messaging;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import se.gu.tux.trux.application.SocialHandler;
import se.gu.tux.trux.datastructure.Friend;
import se.gu.tux.trux.datastructure.Message;
import tux.gu.se.trux.R;

/**
 * Created by ivryashkov on 2015-05-13.
 *
 * Handles the display for the message list activity.
 */
public class MessageListAdapter extends BaseAdapter
{

    // the data to display
    private ArrayList<Friend> friends;
    private ArrayList<Message> messages;

    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH:mm");

    // to inflate each layout
    private static LayoutInflater layoutInflater;

    // the activity for reference
    private Activity activity;



    /**
     * Constructor. Takes the activity where to display items and
     * the data to display on those items.
     *
     * @param activity  The inflater.
     */
    public MessageListAdapter(Activity activity)
    {
        this.activity = activity;
        layoutInflater = (LayoutInflater) activity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    public void setAdapterData(ArrayList<Friend> friends, ArrayList<Message> messages)
    {
        this.friends = friends;
        this.messages = messages;

        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                notifyDataSetChanged();
            }
        });

    }


    @Override
    public int getCount()
    {
        if (friends != null)
        {
            return friends.size();
        }

        return 0;
    }


    @Override
    public Object getItem(int i)
    {
        return new CustomObject(friends.get(i), messages.get(i));
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
            viewHolder.timestamp = (TextView) newView.findViewById(R.id.timestamp);

            // set the holder for this view
            newView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) newView.getTag();
        }

        if (friends.size() <= 0)
        {
            // set no data
            viewHolder.username.setText("No Data");
            viewHolder.content.setText("No Data");
        }
        else
        {
            // set the data to show
            viewHolder.userPicture.setImageBitmap(SocialHandler.pictureToBitMap(friends.get(i).getProfilePic()));
            viewHolder.username.setText(friends.get(i).getFirstname() + " " + friends.get(i).getLastname());
            viewHolder.content.setText((String) messages.get(i).getValue());

            Date date = new Date(messages.get(i).getTimeStamp());

            viewHolder.timestamp.setText("Latest Messages: " + df.format(date));
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
        public TextView timestamp;
    }


} // end class

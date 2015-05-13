package se.gu.tux.trux.gui.messaging;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.datastructure.Friend;
import se.gu.tux.trux.gui.base.BaseAppActivity;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import tux.gu.se.trux.R;

/**
 * Created by ivryashkov on 2015-05-13.
 *
 * Handles the message interaction.
 */
public class MessageActivity extends BaseAppActivity
{

    private static final int LAYOUT_ID = R.layout.activity_message;

    private Friend[] friends;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // set layout for this activity
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT_ID);

        // set current view showing
        setCurrentViewId(LAYOUT_ID);

        // get the friend list
        AsyncTask<Void, Void, Friend[]> friendTask = new FetchFriendsTask().execute();

        try
        {
            friends = friendTask.get();
        }
        catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }

        // get the list view
        ListView listView = (ListView) findViewById(R.id.messages_list_view);
        // get the adapter
        MessageListAdapter messageListAdapter = new MessageListAdapter(this, friends);
        // set adapter
        listView.setAdapter(messageListAdapter);

    }


    @Override
    protected void onResume()
    {
        super.onResume();
        setCurrentViewId(LAYOUT_ID);
    }



    private class FetchFriendsTask extends AsyncTask<Void, Void, Friend[]>
    {

        @Override
        protected Friend[] doInBackground(Void... voids)
        {
            Friend[] array = null;
            try
            {
                array = DataHandler.getInstance().getFriends();
            }
            catch (NotLoggedInException e)
            {
                e.printStackTrace();
            }

            return array;
        }

    } // end inner class


} // end class

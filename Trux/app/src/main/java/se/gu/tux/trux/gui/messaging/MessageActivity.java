package se.gu.tux.trux.gui.messaging;

import android.support.v4.app.FragmentTransaction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

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

    private Friend listFriend;



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
        final ListView listView = (ListView) findViewById(R.id.messages_list_view);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showToast("Click detected");
                listFriend = (Friend) adapterView.getAdapter().getItem(i);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.message_frame_container, new ChatFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
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


    public Friend getListFriend()
    {
        return listFriend;
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

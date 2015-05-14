package se.gu.tux.trux.gui.messaging;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.concurrent.ExecutionException;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.datastructure.Friend;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import tux.gu.se.trux.R;

/**
 * Created by ivryashkov on 2015-05-14.
 */
public class FriendListFragment extends Fragment implements AdapterView.OnItemClickListener
{

    ListView listView;

    private Friend friend;

    private Friend[] friends;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_message_list_holder, container, false);

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
        listView = (ListView) view.findViewById(R.id.list);

        listView.setOnItemClickListener(this);
        // get the adapter
        MessageListAdapter messageListAdapter = new MessageListAdapter(inflater, friends);
        // set adapter
        listView.setAdapter(messageListAdapter);

        return view;
    }



    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
        friend = (Friend) adapterView.getAdapter().getItem(i);

        ((MessageActivity) getActivity()).onItemClick(friend, this.getId());
    }



    /**
     * Private class to fetch the friend list.
     */
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

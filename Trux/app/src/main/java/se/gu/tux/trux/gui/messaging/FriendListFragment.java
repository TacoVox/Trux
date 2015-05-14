package se.gu.tux.trux.gui.messaging;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.concurrent.ExecutionException;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.datastructure.ArrayResponse;
import se.gu.tux.trux.datastructure.Friend;
import se.gu.tux.trux.datastructure.Message;
import se.gu.tux.trux.datastructure.ProtocolMessage;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import se.gu.tux.trux.technical_services.ServerConnector;
import tux.gu.se.trux.R;

/**
 * Created by ivryashkov on 2015-05-14.
 */
public class FriendListFragment extends Fragment implements AdapterView.OnItemClickListener
{

    ListView listView;

    private Friend friend;

    private Friend[] friendsArray;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_message_list_holder, container, false);

        // get the friend list
        AsyncTask<Void, Void, Friend[]> friendTask = new FetchFriendsTask().execute();

        try
        {
            friendsArray = friendTask.get();
        }
        catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }

        // get the list view
        listView = (ListView) view.findViewById(R.id.list);
        // set listener
        listView.setOnItemClickListener(this);
        // get the adapter
        MessageListAdapter messageListAdapter = new MessageListAdapter(inflater, friendsArray);
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



    private void initMessageService()
    {
        ProtocolMessage message = new ProtocolMessage(ProtocolMessage.Type.GET_LATEST_CONVERSATIONS);

        AsyncTask<ProtocolMessage, Void, ArrayResponse> conv = new FetchConversationTask().execute(message);

        ArrayResponse response = null;

        try
        {
            response = conv.get();
        }
        catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }

        Message[] messages = null;

        if (response != null)
        {
            messages = (Message[]) response.getArray();
        }



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



    private class FetchConversationTask extends AsyncTask<ProtocolMessage, Void, ArrayResponse>
    {

        @Override
        protected ArrayResponse doInBackground(ProtocolMessage... protocolMessages)
        {
            ArrayResponse array = null;

            try
            {
                array = (ArrayResponse) ServerConnector.getInstance().answerQuery(protocolMessages[0]);
            }
            catch (NotLoggedInException e)
            {
                e.printStackTrace();
            }

            return array;
        }

    } // end inner class


} // end class

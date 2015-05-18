package se.gu.tux.trux.gui.messaging;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.application.FriendFetchListener;
import se.gu.tux.trux.application.SocialHandler;
import se.gu.tux.trux.datastructure.ArrayResponse;
import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.Friend;
import se.gu.tux.trux.datastructure.Message;
import se.gu.tux.trux.datastructure.ProtocolMessage;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import se.gu.tux.trux.technical_services.ServerConnector;
import tux.gu.se.trux.R;

/**
 * Created by ivryashkov on 2015-05-14.
 *
 * Handles the conversations list with friends.
 */
public class FriendListFragment extends Fragment implements AdapterView.OnItemClickListener, FriendFetchListener
{

    View view;
    ListView listView;
    LayoutInflater inflater;

    private Friend friend;

    private ArrayList<Friend> friendsList;

    SocialHandler sh;

    Message[] messages;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_message_list_holder, container, false);

        this.inflater = inflater;

        initMessageService();

        sh = new SocialHandler();
        sh.fetchFriends(this, SocialHandler.FriendsUpdateMode.NONE);

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

        if (response != null)
        {
            messages = (Message[]) response.getArray();
        }

    }



    @Override
    public void FriendsFetched(ArrayList<Friend> friends)
    {
        long userId = DataHandler.getInstance().getUser().getUserId();

        for (Message msg : messages)
        {

        }



        // get the list view
        listView = (ListView) view.findViewById(R.id.list);
        // set listener
        listView.setOnItemClickListener(this);
        // get the adapter
        MessageListAdapter messageListAdapter = new MessageListAdapter(inflater, friendsList);
        // set adapter
        listView.setAdapter(messageListAdapter);
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
            Data array = null;

            try
            {
                array = ServerConnector.getInstance().answerQuery(protocolMessages[0]);
            }
            catch (NotLoggedInException e)
            {
                e.printStackTrace();
            }

            if (array instanceof ArrayResponse)
            {
                return (ArrayResponse) array;
            }
            else
            {
                return null;
            }
        }

    } // end inner class


} // end class

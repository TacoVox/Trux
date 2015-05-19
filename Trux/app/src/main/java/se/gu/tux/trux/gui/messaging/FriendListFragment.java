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

    private ArrayList<Friend> friendsList;
    private ArrayList<Message> messagesList;

    SocialHandler sh;

    Message[] messages;

    MessageListAdapter messageListAdapter;

    AsyncTask<ProtocolMessage, Void, ArrayResponse> conversations;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_message_list_holder, container, false);

        friendsList = new ArrayList<>();
        messagesList = new ArrayList<>();

        initMessageService();

        sh = new SocialHandler();
        sh.fetchFriends(this, SocialHandler.FriendsUpdateMode.NONE);

        // get the list view
        ListView listView = (ListView) view.findViewById(R.id.list);
        // set listener
        listView.setOnItemClickListener(this);
        // get the adapter
        messageListAdapter = new MessageListAdapter(this.getActivity());
        // set adapter
        listView.setAdapter(messageListAdapter);

        return view;

    } // end onCreateView()



    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
        CustomObject obj = (CustomObject) adapterView.getAdapter().getItem(i);

        ((MessageActivity) getActivity()).onItemClick(obj, this.getId());
    }



    /**
     * Fetches the latest conversations.
     */
    private void initMessageService()
    {
        ProtocolMessage message = new ProtocolMessage(ProtocolMessage.Type.GET_LATEST_CONVERSATIONS);

        conversations = new FetchConversationTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);

    } // end initMessageService()



    @Override
    public void FriendsFetched(ArrayList<Friend> friends)
    {
        long userId = DataHandler.getInstance().getUser().getUserId();

        ArrayResponse response = null;

        try
        {
            response = conversations.get();
        }
        catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }

        if (response != null)
        {
            Object[] array = response.getArray();

            messages = new Message[array.length];

            for (int i = 0; i < array.length; i++)
            {
                messages[i] = (Message) array[i];
            }
        }

        assert messages != null;
        for (Message msg : messages)
        {
            if (msg.getSenderId() != userId)
            {
                for (Friend friend : friends)
                {
                    if (friend.getFriendId() == msg.getSenderId())
                    {
                        friendsList.add(friend);
                        messagesList.add(msg);
                    }
                }
            }
            else if (msg.getReceiverId() != userId)
            {
                for (Friend friend : friends)
                {
                    if (friend.getFriendId() == msg.getReceiverId())
                    {
                        friendsList.add(friend);
                        messagesList.add(msg);
                    }
                }
            }
        } // end for each loop

        // set the data fetched into the adapter
        messageListAdapter.setAdapterData(friendsList, messagesList);

    } // end friendsFetched()



    /**
     * Private class. Fetches the conversations.
     */
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

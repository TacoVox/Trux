package se.gu.tux.trux.gui.messaging;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.android.gms.games.multiplayer.realtime.RealTimeMultiplayer;

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

    private RelativeLayout loadingPanel;

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

        loadingPanel = (RelativeLayout) view.findViewById(R.id.loadingPanel);

        loadingPanel.setVisibility(View.VISIBLE);

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
        // protocol message to send to server
        ProtocolMessage message = new ProtocolMessage(ProtocolMessage.Type.GET_LATEST_CONVERSATIONS);
        // start fetching conversations in a background thread
        conversations = new FetchConversationTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);

    } // end initMessageService()



    /**
     * Called when the friends are fetched in the social handler.
     *
     * @param friends   The friends.
     */
    @Override
    public void onFriendsFetched(ArrayList<Friend> friends)
    {
        long userId = DataHandler.getInstance().getUser().getUserId();

        ArrayResponse response = null;

        // get the fetched conversations
        try
        {
            response = conversations.get();
        }
        catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }

        // get the messages for those conversations
        if (response != null)
        {
            Object[] array = response.getArray();

            messages = new Message[array.length];

            for (int i = 0; i < array.length; i++)
            {
                messages[i] = (Message) array[i];
            }
        }

        // initiate the friend and message data to send to adapter
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

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingPanel.setVisibility(View.GONE);
            }
        });

    } // end friendsFetched()


    @Override
    public void onFriendRequestsFetched(ArrayList<Friend> friends) {}


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

package se.gu.tux.trux.gui.messaging;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Arrays;
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
public class ConversationListFragment extends Fragment implements AdapterView.OnItemClickListener,
        FriendFetchListener, View.OnClickListener
{

    // the data to pass to the adapter
    private ArrayList<Friend> friendsList;
    private ArrayList<Message> messagesList;

    // the loading panel to show until we fetch all information
    private RelativeLayout loadingPanel;

    private Message[] messages;

    private ConversationListAdapter messageListAdapter;

    private AsyncTask<ProtocolMessage, Void, ArrayResponse> conversations;

    private Button messageButton;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_message_list_holder, container, false);

        friendsList = new ArrayList<>();
        messagesList = new ArrayList<>();

        loadingPanel = (RelativeLayout) view.findViewById(R.id.loadingPanel);

        loadingPanel.setVisibility(View.VISIBLE);

        messageButton = (Button) view.findViewById(R.id.new_message_button);
        messageButton.setOnClickListener(this);
        messageButton.setEnabled(false);

        initMessageService();

        SocialHandler sh = new SocialHandler();
        sh.fetchFriends(this, SocialHandler.FriendsUpdateMode.NONE);

        // get the list view
        ListView listView = (ListView) view.findViewById(R.id.list);
        // set listener
        listView.setOnItemClickListener(this);
        // get the adapter
        messageListAdapter = new ConversationListAdapter(this.getActivity());
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
        Arrays.sort(messages);

        Message[] backMessages = new Message[messages.length];
        for (int i = 0, j = backMessages.length-1; i < backMessages.length; i++, j--)
        {
            backMessages[i] = messages[j];
        }

        for (Message msg : backMessages)
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

        // hide the loading panel
        Activity a = getActivity();
        if (a!= null)
        {
            a.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingPanel.setVisibility(View.GONE);
                }
            });
        }

        messageButton.setEnabled(true);

    } // end friendsFetched()



    @Override
    public void onFriendRequestsFetched(ArrayList<Friend> friends) {}



    @Override
    public void onClick(View view)
    {
        int id = view.getId();

        if (id == messageButton.getId())    { sendNewMessage(); }
    }



    /**
     * Presents the user with an option to choose a friend to send a new message.
     * Redirects to the chat fragment.
     */
    private void sendNewMessage()
    {
        // we use a simple dialog box to show the friends for this user
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose a friend");

        // the friends names to use in the dialog box
        String[] friendsNames = new String[friendsList.size()];

        // get the names from the friends list already fetched
        for (int i = 0; i < friendsNames.length; i++)
        {
            String name = friendsList.get(i).getFirstname() + " " + friendsList.get(i).getLastname() +
                    " (" + friendsList.get(i).getUsername() + ")";
            friendsNames[i] = name;
        }

        // set the names to show
        builder.setItems(friendsNames, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                CustomObject obj = (CustomObject) messageListAdapter.getItem(i);

                ((MessageActivity) getActivity()).onItemClick(obj, getId());
            }
        });

        builder.show();

    } // end sendNewMessage()



    /**
     * Private class to perform async task. Fetches the conversations.
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

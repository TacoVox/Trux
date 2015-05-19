package se.gu.tux.trux.gui.messaging;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.concurrent.ExecutionException;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.datastructure.ArrayResponse;
import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.Message;
import se.gu.tux.trux.datastructure.Notification;
import se.gu.tux.trux.datastructure.ProtocolMessage;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import se.gu.tux.trux.technical_services.ServerConnector;
import tux.gu.se.trux.R;

/**
 * Created by ivryashkov on 2015-05-13.
 *
 * Handles the chat window.
 */
public class ChatFragment extends Fragment implements View.OnClickListener
{

    private EditText userInput;
    private Button sendButton;

    private MessageActivity act;
    private LinearLayout msgContainer;

    private CustomObject object;

    private Message[] messages;
    private Message[] newMessages;

    private volatile boolean isRunning;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // inflate the layout for this view
        View view = inflater.inflate(R.layout.fragment_chat_head, container, false);
        // send seen confirmation for this conversation
        setSeenConfirmation();

        isRunning = true;

        // get the components
        TextView tv = (TextView) view.findViewById(R.id.chat_head_username_text_view);
        userInput = (EditText) view.findViewById(R.id.chat_input_edit_text);
        sendButton = (Button) view.findViewById(R.id.chat_send_button);
        // set listener to button
        sendButton.setOnClickListener(this);

        // get the activity
        act = (MessageActivity) getActivity();
        // get the object containing reference to the friend and messages
        object = act.getCustomObject();

        // set username of the friend we are writing with
        tv.setText(object.getFriend().getUsername());

        // get the container we gonna use for displaying the messages in
        msgContainer = (LinearLayout) view.findViewById(R.id.chat_container);

        // fetch the latest messages
        fetchLatestMessages();
        // start a thread to check for new messages
        checkNewMessages();

        // return the view
        return view;
    }


    @Override
    public void onStop()
    {
        super.onStop();
        isRunning = false;
    }

    @Override
    public void onClick(View view)
    {
        int id = view.getId();

        if (id == sendButton.getId()) { sendMessage(); }
    }



    private void checkNewMessages()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (isRunning)
                {
                    Notification notification = DataHandler.getInstance().getNotificationStatus();

                    if (notification.isNewMessages())
                    {
                        ProtocolMessage request = new ProtocolMessage(ProtocolMessage.Type.GET_UNREAD_MESSAGES,
                                Long.toString(object.getFriend().getFriendId()));

                        ArrayResponse response = null;

                        try
                        {
                            response = (ArrayResponse) ServerConnector.getInstance().answerQuery(request);
                        }
                        catch (NotLoggedInException e) { e.printStackTrace(); }

                        assert response != null;
                        Object[] array = response.getArray();

                        if (array != null)
                        {
                            newMessages = new Message[array.length];

                            for (int i = 0; i < array.length; i++)
                            {
                                newMessages[i] = (Message) array[i];
                            }

                            if (!(newMessages[0].getValue()).equals(messages[0].getValue()))
                            {
                                // display the messages
                                for (int i = newMessages.length-1; i >= 0; i--)
                                {
                                    // the text view to hold the message
                                    final TextView textView = new TextView(act.getApplicationContext());

                                    textView.setText(newMessages[i].getValue() + "\n");
                                    textView.setTextColor(Color.BLACK);

                                    // add this text view to the message container
                                    act.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            msgContainer.addView(textView);
                                        }
                                    });

                                    // send a new protocol message with the required data
                                    try {
                                        ServerConnector.getInstance().answerQuery(new ProtocolMessage(ProtocolMessage.Type.MESSAGE_SEEN,
                                                Long.toString(object.getFriend().getFriendId())));
                                    } catch (NotLoggedInException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            else
                            {
                                messages = newMessages;
                            }
                        }
                    }

                    try
                    {
                        Thread.sleep(2000);
                    }
                    catch (InterruptedException e) { e.printStackTrace(); }

                } // end while loop

            } // end run()

        }).start();

    } // end checkNewMessages()



    /**
     * Sends a confirmation that the messages in this conversation are seen.
     */
    private void setSeenConfirmation()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    // send a new protocol message with the required data
                    ServerConnector.getInstance().answerQuery(new ProtocolMessage(ProtocolMessage.Type.MESSAGE_SEEN,
                            Long.toString(object.getFriend().getFriendId())));
                }
                catch (NotLoggedInException e) { e.printStackTrace(); }
            }
        }).start();

    } // end setSeenConfirmation()



    /**
     * Fetches the latest messages for this conversation.
     */
    private void fetchLatestMessages()
    {
        // the protocol message to send
        ProtocolMessage request = new ProtocolMessage(ProtocolMessage.Type.GET_LATEST_MESSAGES,
                Long.toString(object.getFriend().getFriendId()));

        // create new async task to fetch the messages
        AsyncTask<ProtocolMessage, Void, ArrayResponse> conversations = new FetchMessagesTask().execute(request);

        // the array response
        ArrayResponse response = null;

        // get the response
        try
        {
            response = conversations.get();
        }
        catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }

        // if the response is not null, get the message objects
        if (response != null)
        {
            Object[] array = response.getArray();

            messages = new Message[array.length];

            for (int i = 0; i < array.length; i++)
            {
                messages[i] = (Message) array[i];
            }
        }

        // display the messages
        for (int i = messages.length-1; i >= 0; i--)
        {
            // the text view to hold the message
            final TextView textView = new TextView(act.getApplicationContext());

            textView.setText(messages[i].getValue() + "\n");
            textView.setTextColor(Color.BLACK);

            // add this text view to the message container
            msgContainer.addView(textView);
        }

    } // end fetchLatestMessages()



    /**
     * Sends a message.
     */
    private void sendMessage()
    {
        // get the message
        String message = userInput.getText().toString();

        // create the text view to hold the message
        final TextView textView = new TextView(act.getApplicationContext());
        textView.setText(message);
        textView.setTextColor(Color.BLACK);

        // add to container and display
        msgContainer.addView(textView);

        // the message object to send to server
        final Message msg = new Message();
        // set required fields
        msg.setSenderId(DataHandler.getInstance().getUser().getUserId());
        msg.setReceiverId(object.getFriend().getFriendId());
        msg.setValue(message);

        // send to server
        new Thread(new Runnable() {
            @Override
            public void run() {
                try
                {
                    ServerConnector.getInstance().answerQuery(msg);
                }
                catch (NotLoggedInException e) { e.printStackTrace(); }
            }
        }).start();

        // after message is sent, clear the message input field
        userInput.setText("");

    } // end sendMessage()



    /**
     * Private class. Fetches the messages for this conversation.
     */
    private class FetchMessagesTask extends AsyncTask<ProtocolMessage, Void, ArrayResponse>
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

            return null;

        } // end doInBackgorund()

    } // end inner class


} // end class

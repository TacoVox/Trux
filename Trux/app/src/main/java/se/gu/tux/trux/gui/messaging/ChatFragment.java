package se.gu.tux.trux.gui.messaging;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
public class ChatFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener
{

    private EditText userInput;
    private Button sendButton;
    private Spinner spinnerInput;

    private MessageActivity act;
    private LinearLayout msgContainer;
    private ScrollView scrollView;

    private CustomObject object;

    private Message[] messages;
    private Message[] newMessages;

    private volatile boolean isRunning;

    private long userId;

    private DateFormat df;

    private final int PLACE_PICKER_REQUEST = 1;

    private String[] spinnerTitles = { "Can't chat right now", "Call you later", "Yes",
            "Okay", "No, thanks pal!", "Busy driving!", "Meet at..." };

    private String placeMessage;
    private int spinnerPosition;



    @SuppressLint("SimpleDateFormat")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // inflate the layout for this view
        View view = inflater.inflate(R.layout.fragment_chat_head, container, false);

        isRunning = true;

        userId = DataHandler.getInstance().getUser().getUserId();

        df = new SimpleDateFormat("MM-dd-HH:mm");

        // get the components
        TextView tv = (TextView) view.findViewById(R.id.chat_head_username_text_view);
        sendButton = (Button) view.findViewById(R.id.chat_send_button);
        userInput = (EditText) view.findViewById(R.id.chat_input_edit_text);
        spinnerInput = (Spinner) view.findViewById(R.id.chat_input_spinner);
        scrollView = (ScrollView) view.findViewById(R.id.chat_scrool_view);

        if(isSimple())
        {
            spinnerInput.setVisibility(View.VISIBLE);
            userInput.setVisibility(View.GONE);
        }
        else
        {
            spinnerInput.setVisibility(View.GONE);
            userInput.setVisibility(View.VISIBLE);
        }

        ArrayAdapter<String> arrayAdapter =
                new SimpleMessageSpinner(view.getContext(), R.layout.spinner_message_item, spinnerTitles);

        arrayAdapter.setDropDownViewResource(R.layout.spinner_message_item);

        spinnerInput.setAdapter(arrayAdapter);

        spinnerInput.setOnItemSelectedListener(this);


        // set listener to button
        sendButton.setOnClickListener(this);

        // get the activity
        act = (MessageActivity) getActivity();
        // get the object containing reference to the friend and messages
        object = act.getCustomObject();

        // send seen confirmation for this conversation
        setSeenConfirmation();

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

    } // end onCreateView()



    @Override
    public void onStop()
    {
        super.onStop();
        isRunning = false;
    }



    @Override
    public void onResume()
    {
        super.onResume();
        isRunning = true;
    }



    @Override
    public void onClick(View view)
    {
        int id = view.getId();

        if (id == sendButton.getId()) { sendMessage(false); }
    }



    /**
     * Checks for new messages.
     */
    private void checkNewMessages()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (isRunning)
                {
                    // get notification
                    Notification notification = DataHandler.getInstance().getNotificationStatus();

                    // if there are new messages in general
                    if (notification.isNewMessages())
                    {
                        // send a request to server to get unread messages for this conversation
                        ProtocolMessage request = new ProtocolMessage(ProtocolMessage.Type.GET_UNREAD_MESSAGES,
                                Long.toString(object.getFriend().getFriendId()));

                        // get the response
                        ArrayResponse response = null;

                        try
                        {
                            response = (ArrayResponse) ServerConnector.getInstance().answerQuery(request);
                        }
                        catch (NotLoggedInException e) { e.printStackTrace(); }

                        assert response != null;
                        Object[] array = response.getArray();

                        // get the message objects from the response
                        if (array != null && array.length > 0)
                        {
                            newMessages = new Message[array.length];

                            for (int i = 0; i < array.length; i++)
                            {
                                newMessages[i] = (Message) array[i];
                            }

                            // check if there are new unread messages
                            if (messages.length>0 && !(newMessages[0].getValue()).equals(messages[0].getValue()))
                            {
                                // display the messages
                                for (int i = newMessages.length - 1; i >= 0; i--)
                                {
                                    // the text view to hold the message
                                    final TextView textView = getFriendTextView();

                                    Date date = new Date(messages[i].getTimeStamp());

                                    textView.setText(messages[i].getValue() + "\n\n" + df.format(date));

                                    // add this text view to the message container
                                    act.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            msgContainer.addView(textView);
                                        }
                                    });

                                    msgContainer.postInvalidate();
                                    scrollView.scrollTo(0, msgContainer.getBottom());

                                    // send a new protocol message that we saw these messages
                                    try
                                    {
                                        ServerConnector.getInstance().answerQuery(new ProtocolMessage(ProtocolMessage.Type.MESSAGE_SEEN,
                                                Long.toString(object.getFriend().getFriendId())));
                                    }
                                    catch (NotLoggedInException e) { e.printStackTrace(); }
                                }

                                // set the pointer to the new messages for future reference
                                messages = newMessages;
                            }
                            else
                            {
                                // set the pointer to the new messages for future reference
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
        AsyncTask<ProtocolMessage, Void, ArrayResponse> conversations =
                new FetchMessagesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, request);

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
            if (messages[i].getSenderId() == userId)
            {
                // the text view to hold the message
                final TextView textView = getUserTextView();

                Date date = new Date(messages[i].getTimeStamp());

                textView.setText(messages[i].getValue() + "\n\n" + df.format(date));

                // add this text view to the message container
                msgContainer.addView(textView);
            }
            else
            {
                // the text view to hold the message
                final TextView textView = getFriendTextView();

                Date date = new Date(messages[i].getTimeStamp());

                textView.setText(messages[i].getValue() + "\n\n" + df.format(date));

                // add this text view to the message container
                msgContainer.addView(textView);
            }
        }

        msgContainer.postInvalidate();
        scrollView.scrollTo(0, msgContainer.getBottom());

    } // end fetchLatestMessages()



    /**
     * Sends a message.
     */
    private void sendMessage(boolean isPlace)
    {
        String message;

        // get the message
        if (isSimple())
        {
            if (isPlace)
            {
                message = placeMessage;
            }
            else
            {
                message = spinnerTitles[spinnerPosition];
            }
        }
        else
        {
            message = userInput.getText().toString();
        }

        final TextView textView = getUserTextView();
        Date date = new Date(System.currentTimeMillis());
        textView.setText(message + "\n\n" + df.format(date));

        // add to container and display
        msgContainer.addView(textView);

        // check if message is null, return
        if (message == null) { return; }

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

        msgContainer.postInvalidate();
        msgContainer.postInvalidate();
        scrollView.scrollTo(0, msgContainer.getBottom());

    } // end sendMessage()



    /**
     * Returns the text view holder for the user messages.
     *
     * @return      TextView
     */
    private TextView getUserTextView()
    {
        // create the text view to hold the message
        TextView textView = new TextView(act.getApplicationContext());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.END;
        params.bottomMargin = 5;

        textView.setLayoutParams(params);
        textView.setGravity(Gravity.CENTER);

        textView.setTextColor(Color.parseColor("#E0E0E0"));
        textView.setPadding(20, 20, 20, 20);
        textView.setBackgroundColor(Color.parseColor("#61728d"));

        return textView;
    }



    /**
     * Returns the text view holder for the friend messages.
     *
     * @return      TextView
     */
    private TextView getFriendTextView()
    {
        // create the text view to hold the message
        TextView textView = new TextView(act.getApplicationContext());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.START;
        params.bottomMargin = 5;

        textView.setLayoutParams(params);
        textView.setGravity(Gravity.CENTER);

        textView.setTextColor(Color.parseColor("#E0E0E0"));
        textView.setPadding(20, 20, 20, 20);
        textView.setBackgroundColor(Color.parseColor("#ff404e68"));

        return textView;
    }



    /**
     * Helper method. Checks the distraction level.
     *
     * @return  true if low level, false otherwise
     */
    private boolean isSimple()
    {
        return (DataHandler.gI().getSafetyStatus() != DataHandler.SafetyStatus.IDLE);
    }



    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
    {
        if (i == spinnerTitles.length-1)
        {
            try
            {
                PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
                Intent intent = intentBuilder.build(this.getActivity().getApplicationContext());
                // Start the intent by requesting a result,
                // identified by a request code.
                startActivityForResult(intent, PLACE_PICKER_REQUEST);
            }
            catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            spinnerPosition = i;
        }

    } // end onItemSelected()



    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}



    /**
     * Called after request to pick a place to meet.
     *
     * @param requestCode   The request code.
     * @param resultCode    The result of the call.
     * @param data          The data returned.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == Activity.RESULT_OK && data != null)
        {
            // The user has selected a place. Extract the name and address.
            Place place = PlacePicker.getPlace(data, this.getActivity().getApplicationContext());

            String name = place.getName().toString();
            String address = place.getAddress().toString();

            placeMessage = "MEET AT\n" + name + " / " + address;

            sendMessage(true);

            spinnerInput.setSelection(0);
        }
    }



    /**
     * Private class to perform async task. Fetches the messages for this conversation.
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

package se.gu.tux.trux.gui.community;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Visibility;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.application.FriendFetchListener;
import se.gu.tux.trux.application.SocialHandler;
import se.gu.tux.trux.datastructure.ArrayResponse;
import se.gu.tux.trux.datastructure.Friend;
import se.gu.tux.trux.datastructure.Picture;
import se.gu.tux.trux.datastructure.ProtocolMessage;
import se.gu.tux.trux.gui.base.BaseAppActivity;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import tux.gu.se.trux.R;

public class FriendsWindow extends BaseAppActivity implements View.OnClickListener, FriendFetchListener {
    // For knowing which of search / show friends that should be listened to when they finish,
    // have an enum or so LAST_FETCH_CALL = SEARCH / FRIENDLIST that is checked on callback

    private ListView friendsList;
    private FriendAdapter friendAdapter;
    private EditText searchField;
    private TextView noFriends;
    private Button searchButton;
    private enum FetchCall {SEARCH, FRIENDLIST};
    private FetchCall lastFetchCall = FetchCall.FRIENDLIST;
    private String lastNeedle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_window);

        friendsList = (ListView) findViewById(R.id.friendsList);

        friendAdapter = new FriendAdapter(this, new ArrayList<Friend>());
        friendsList.setAdapter(friendAdapter);
        friendsList.setEmptyView(findViewById(R.id.noFriends));
        searchField = (EditText) findViewById(R.id.searchField);
        searchButton = (Button) findViewById(R.id.searchButton);
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                if (searchField.getText().toString().equals("")) {
                    showFriends();
                } else {
                    showSearchResults(searchField.getText().toString());
                }
            }
        });
        searchButton.setOnClickListener(this);
        noFriends = (TextView) findViewById(R.id.noFriends);
        showFriends();
    }

    @Override
    public void onClick(View view) {
        if (view == findViewById(R.id.searchButton)) {
            // Show loading animation and proceed to load friends or search results
            findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
            if (searchField.getText().toString().equals("")) {
                showFriends();
            } else {
                showSearchResults(searchField.getText().toString());
            }
        }
    }


    private void showFriends() {
        lastFetchCall = FetchCall.FRIENDLIST;
        DataHandler.gI().getSocialHandler().fetchFriends(this, SocialHandler.FriendsUpdateMode.NONE);
    }


    /**
     * Shows friends that match needle followed by other people who also match needle.
     *
     * @param needle
     */
    private void showSearchResults(final String needle) {
        DataHandler.gI().getSocialHandler().fetchFriends(this, SocialHandler.FriendsUpdateMode.ALL);
        noFriends.setText("No people found.");
        lastNeedle = needle;
        lastFetchCall = FetchCall.SEARCH;
    }


    /**
     * This is run in a background thread created by SocialHandler, so we are using this background
     * thread to fetch more stuff if relevant (the search results). By looking what request was last
     * issued by the user (to show list or search), we know what to render here
     * @param friends
     */
    @Override
    public void FriendsFetched(final ArrayList<Friend> friends) {

        // Last user action was to show friend list
        if (lastFetchCall == FetchCall.FRIENDLIST) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Update data in friendAdapter and hide loading animation
                    friendAdapter.setFriends(friends);
                    findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                }
            });

        } else if (lastFetchCall == FetchCall.SEARCH) {

            // Last user action was to search for people.
            ArrayList<Friend> allResults = new ArrayList<Friend>();
            try {
                // Fetch friends and keep the ones which are matching the search needle
                allResults = matchFriendSearch(friends, lastNeedle);

                // Fetch other people
                ArrayResponse ar = (ArrayResponse)DataHandler.getInstance().getData(
                        new ProtocolMessage(ProtocolMessage.Type.PEOPLE_SEARCH, lastNeedle));

                // Join friends and people, if there were any results.
                if (ar.getArray() != null) {
                    allResults = appendFriendObjects(allResults, ar.getArray());
                }

            } catch (NotLoggedInException e) {
                System.out.println("Trying to fetch friends while not logged in!");
            }

            // Render the updated list
            final ArrayList<Friend> finalResults = allResults;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Verify that this is still the last user action
                    // (the user hasn't for example removed the text from the search field)
                    if (lastFetchCall == FetchCall.SEARCH) {
                        friendAdapter.setFriends(finalResults);
                        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                    }
                }
            });
        }
    }


    /**
     * Returns an array with all elements that contain the needle
     * (in username, firstname or lastname)
     */
    public ArrayList<Friend> matchFriendSearch(ArrayList<Friend> haystack, String needle) {
        if (haystack == null || haystack.size() == 0) {
            return new ArrayList<Friend>();
        }
        ArrayList<Friend> matches = new ArrayList<Friend>();
        for (Friend f : haystack) {
            if (f.getUsername().toLowerCase().indexOf(needle.toLowerCase()) != -1 ||
                    f.getFirstname().toLowerCase().indexOf(needle.toLowerCase()) != -1 ||
                    f.getLastname().toLowerCase().indexOf(needle.toLowerCase()) != -1) {
                // Friend matched needle
                matches.add(f);
            }
        }

        return matches;
    }


    /**
     * Appends the second array to the list - also does casting simultaneously
     * @param list
     * @param friendArray
     * @return
     */
    private ArrayList<Friend> appendFriendObjects(ArrayList<Friend> list, Object[] friendArray) {
        if (friendArray != null) {
            for (int i = 0; i < friendArray.length; i++) {
                list.add((Friend)friendArray[i]);
            }
        }
        return list;
    }








    class FriendAdapter extends BaseAdapter {

        Context context;

        // The reason for not wrapping these together is that sometimes we want to be able to
        // send just friend info without the overhead of sending the picture. Could be handled
        // differentlyt though for example with a request boolean.
        ArrayList<Friend> friends;

        private LayoutInflater inflater = null;

        public FriendAdapter(Context context,  ArrayList<Friend> friends) {
            this.context = context;
            this.friends = friends;
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            if (friends != null) {
                return friends.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return friends.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void setFriends( ArrayList<Friend> friends) {
            this.friends = friends;
            notifyDataSetChanged();
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null)
                view = inflater.inflate(R.layout.friend_row, null);
            TextView name = (TextView) view.findViewById(R.id.friendName);
            TextView username = (TextView) view.findViewById(R.id.friendUserName);
            ImageView image = (ImageView) view.findViewById(R.id.friendPicture);
            Button friendRequestButton = (Button) view.findViewById(R.id.friendRequestButton);
            Button sendMessageButton = (Button) view.findViewById(R.id.sendMessageButton);

            // Set the name
            name.setText(friends.get(position).getFirstname() + " " + friends.get(position).getLastname());
            username.setText("@" + friends.get(position).getUsername());

            // Set the proper button visibility
            if (friends.get(position).getFriendType() == Friend.FriendType.FRIEND) {
                friendRequestButton.setVisibility(View.GONE);
                sendMessageButton.setVisibility(View.VISIBLE);
            } else if (friends.get(position).getFriendType() == Friend.FriendType.PENDING) {
                friendRequestButton.setVisibility(View.GONE);
                sendMessageButton.setVisibility(View.GONE);
            } else {
                friendRequestButton.setVisibility(View.VISIBLE);
                sendMessageButton.setVisibility(View.GONE);
            }

            // Set the picture
            image.setImageBitmap(SocialHandler.pictureToBitMap(friends.get(position).getProfilePic()));

            return view;
        }

    } // end nested class


} // end class
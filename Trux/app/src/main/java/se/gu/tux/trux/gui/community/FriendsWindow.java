package se.gu.tux.trux.gui.community;

import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.application.FriendFetchListener;
import se.gu.tux.trux.application.FriendActionListener;
import se.gu.tux.trux.application.SocialHandler;
import se.gu.tux.trux.datastructure.ArrayResponse;
import se.gu.tux.trux.datastructure.Friend;
import se.gu.tux.trux.datastructure.ProtocolMessage;
import se.gu.tux.trux.gui.base.BaseAppActivity;
import se.gu.tux.trux.gui.messaging.MessageActivity;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import tux.gu.se.trux.R;


/**
 * The friend window activity. Shows a list of friends. You can also search for friends in it and
 * it shows any friend requests.
 * Friends are cached in socialhandler. We get the friends by asking socialHandler for them, and
 * then socialHandler calls callback methods in the interface FriendFetchListener when they are
 * ready (which is immediately if they are cached).
 * The same goes for friend actions, for which the list adapter (inner class) implements the
 * listener interface.
 */
public class FriendsWindow extends BaseAppActivity implements View.OnClickListener,
        FriendFetchListener {

    // Different row types in the adapter (not allowed to create enum in inner class)
    public enum RowType {REQ_LABEL, REQ, FRIEND_LABEL, FRIEND};
    // We remember if the last action was to search or show friends
    private enum FetchCall {SEARCH, FRIENDLIST};
    private FetchCall lastFetchCall = FetchCall.FRIENDLIST;
    private String lastNeedle;

    // Visual components
    private ListView friendsList;
    private FriendAdapter friendAdapter;
    private EditText searchField;
    private TextView noFriends;
    private Button searchButton;


    /**
     * Build the friend window.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_window);

        // Initiate visual components
        friendsList = (ListView) findViewById(R.id.friendsList);
        friendAdapter = new FriendAdapter(this, new ArrayList<Friend>());
        friendsList.setAdapter(friendAdapter);
        friendsList.setEmptyView(findViewById(R.id.noFriends));
        searchButton = (Button) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(this);
        noFriends = (TextView) findViewById(R.id.noFriends);
        searchField = (EditText) findViewById(R.id.searchField);

        // Add a listener to the text field so if text is added, we search for it, if no text
        // is left, we go back to showing the friend list
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void afterTextChanged(Editable editable) {
                showLoadingBar();
                // Check the text
                if (searchField.getText().toString().equals("")) {
                    showFriends();
                } else {
                    showSearchResults(searchField.getText().toString());
                }
            }
        });

        // Start fetching the friends
        showFriends();
    }


    /**
     * Shows the loading bar.
     */
    private void showLoadingBar() {
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        noFriends.setText(R.string.loading);
    }


    /**
     * Hides the loading bar.
     */
    private void hideLoadingBar() {
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);

        // The "empty message" in the list varies depending on the user action
        if (lastFetchCall == FetchCall.FRIENDLIST) {
            noFriends.setText("You have no friends :(");
        } else {
            noFriends.setText("No people found.");
        }
    }


    /**
     * onCLick listener implementation. The search button is connected to this.
     * @param view
     */
    @Override
    public void onClick(View view) {
        if (view == findViewById(R.id.searchButton)) {
            // Show loading animation and proceed to load friends or search results
            if (searchField.getText().toString().equals("")) {
                showFriends();
            } else {
                showSearchResults(searchField.getText().toString());
            }
        }
    }


    /**
     * Show friend list - call fetchFriends on the SocialHandler.
     */
    private void showFriends() {
        showLoadingBar();
        lastFetchCall = FetchCall.FRIENDLIST;
        DataHandler.gI().getSocialHandler().fetchFriends(this, SocialHandler.FriendsUpdateMode.NONE);

        // Also adjust the UI depending on the distraction level
        if (isSimple()) {
            // Simplified UI- hide the search bar
            searchField.setVisibility(View.GONE);
            searchButton.setVisibility(View.GONE);
            friendAdapter.setFriendRequests(new ArrayList<Friend>());
        } else {
            // Otherwise include the friend requests on top provided that the user is not too distracted
            DataHandler.gI().getSocialHandler().fetchFriendRequests(this);
            searchField.setVisibility(View.VISIBLE);
            searchButton.setVisibility(View.VISIBLE);
        }
    }


    /**
     * Search results:
     * Shows friends that match needle followed by other people who also match needle.
     * The actual filtering occurs when the callback method is called.
     * @param needle
     */
    private void showSearchResults(final String needle) {
        showLoadingBar();
        DataHandler.gI().getSocialHandler().fetchFriends(this, SocialHandler.FriendsUpdateMode.ALL);
        lastNeedle = needle;
        lastFetchCall = FetchCall.SEARCH;
    }


    /**
     * Called when we need to update what is in the list.
     */
    public void refresh() {
        if (lastFetchCall == FetchCall.FRIENDLIST) {
            System.out.println("Refreshing friend list...");
            showFriends();
        } else {
            showSearchResults(lastNeedle);
        }
    }

    /**
     * This is run in a background thread created by SocialHandler, so we are using this background
     * thread to fetch more stuff if relevant (the search results). By looking what request was last
     * issued by the user (to show list or search), we know what to render here
     * @param friends
     */
    @Override
    public void onFriendsFetched(final ArrayList<Friend> friends) {
        // Last user action was to show friend list
        if (lastFetchCall == FetchCall.FRIENDLIST) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Update data in friendAdapter and hide loading animation
                    friendAdapter.setFriends(friends);
                    hideLoadingBar();
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
                    // This also adds pictures to the serch result friend objects
                    allResults = appendSearchResults(allResults, ar.getArray());
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
                        hideLoadingBar();
                    }
                }
            });
        }
    }


    @Override
    public void onFriendRequestsFetched(final ArrayList<Friend> friendRequests) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Update data in friendAdapter
                friendAdapter.setFriendRequests(friendRequests);
            }
        });
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
     * Appends the second array to the list - and fetches images for these people
     * @param list
     * @param friendArray
     * @return
     */
    private ArrayList<Friend> appendSearchResults(ArrayList<Friend> list, Object[] friendArray)
            throws NotLoggedInException {
        if (friendArray != null) {
            for (int i = 0; i < friendArray.length; i++) {
                Friend f = (Friend)friendArray[i];
                f.setProfilePic(DataHandler.gI().getSocialHandler().getPicture(f.getProfilePicId()));
                list.add(f);
            }
        }
        return list;
    }


    /**
     * Adapter that shows friends in the list view. It shows search results if the user searches,
     * the results then consist of both the filtered friend list at the top and then search results
     * below these. In addition to this, friend requests are shown at the top of the list, if any.
     *
     * Because of two datalists (friend requests in it's own) and label items to distinguish
     * between them, there is quite some logic here to determine how many rows there are in total,
     * which type they are etc...
     *
     */
    class FriendAdapter extends BaseAdapter implements FriendActionListener {

        private Context context;
        private ArrayList<Friend> friendRequests;
        private ArrayList<Friend> friends;
        private LayoutInflater inflater = null;
        private final FriendAdapter thisAdapter = this;


        /**
         * Initializes the instance with the given friend list
         * @param context
         * @param friends
         */
        public FriendAdapter(Context context,  ArrayList<Friend> friends) {
            this.context = context;
            this.friends = friends;
            this.friendRequests = new ArrayList<Friend>();
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }


        /**
         * Callback method called when the friend request was successfully sent.
         * @param friendId  The friend we sent the request to.
         */
        @Override
        public void onFriendRequestSent(long friendId) {
            // Update the friend object to pending instead of not a friend
            for (Friend f : friends) {
                if (f.getFriendId() == friendId) {
                    f.setFriendType(Friend.FriendType.PENDING);
                }
            }

            // Notify SocialHandler that the friends have updated
            DataHandler.gI().getSocialHandler().setFriendsChanged(true);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showToast("The friend request was sent.");
                    // Also notify this adapter
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onFriendRemoveSent(long friendId) {}

        /**
         * Callback method called when the friend request reply action was successfully sent.
         * @param friendId  The friend that sent the request.
         */
        @Override
        public void onFriendRequestAnswered(long friendId, final boolean accepted) {
            // Update the friend object
            Iterator<Friend> it = friendRequests.iterator();
            while (it.hasNext()) {
                Friend f = it.next();
                if (f.getFriendId() == friendId) {
                    if (accepted) {
                        // Add to friend list
                        f.setFriendType(Friend.FriendType.FRIEND);
                        friends.add(f);
                    } else {
                        // Set as not a friend
                        f.setFriendType(Friend.FriendType.NONE);
                    }
                    // Remove from list of friend requests regardless
                    it.remove();
                }
            }

            // Notify SocialHandler that things have changed
            DataHandler.gI().getSocialHandler().setFriendRequestsChanged(true);
            DataHandler.gI().getSocialHandler().setFriendsChanged(true);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (accepted) {
                        showToast("The friend request was accepted.");
                        System.out.println("ACCEPTED FRIEND REQUEST.");
                    } else {
                        showToast("The friend request was declined.");
                    }
                    // Refresh the data now that we know it has changed
                    notifyDataSetChanged();
                    refresh();
                }
            });
        }


        /**
         * Calculates the amount of items in the list. There are separator labels above each of the
         * two lists as needed (where the lists are 1. friend request and 2. friends / search results)
         * @return  The amount of items in the list.
         */
        @Override
        public int getCount() {
            int count, friendCount = 0, friendRequestCount = 0;
            if (friendRequests != null && friendRequests.size() > 0) {
                // + 1 to reserve a row for a text label "Friend requests"
                friendRequestCount = friendRequests.size() + 1;
            }
            if (friends != null && friends.size() > 0) {
                friendCount = friends.size();
            }
            if (friendCount > 0 && friendRequestCount > 0) {
                // + 1 more to reserve space for next text label
                count = friendCount + friendRequestCount + 1;
            } else {
                count = friendCount + friendRequestCount;
            }
            return count;
        }


        /**
         * Helps calculate if there are separator labels, in that case we need to offset the index
         * when retrieving from the friend list
         * @return  The offset that should be used when retrieving from the friend list
         */
        private int getFriendOffset() {
            if (friendRequests != null && friendRequests.size() > 0) {
                if (friends != null && friends.size() > 0) {
                    // Friends and friends requests
                    return friendRequests.size() + 2;
                }
            }
            // Else no friend requests (or no friends) - return 0 offset
            return 0;
        }


        /**
         * Returns the type of the row.
         * @param pos   The row number
         * @return      The type of row
         */
        private RowType getRowType(int pos) {

            if (friendRequests != null && friendRequests.size() > 0) {
                if (friends != null && friends.size() > 0) {
                    // Friends and friends requests
                    if (pos == 0) { return RowType.REQ_LABEL; }
                    if (pos > 0 && pos <= friendRequests.size()) { return RowType.REQ; }
                    if (pos == friendRequests.size() + 1) { return RowType.FRIEND_LABEL; }
                    if (pos > friendRequests.size() + 1) { return RowType.FRIEND; }

                } else {
                    // Friend requests but not friends
                    if (pos == 0) { return RowType.REQ_LABEL; }
                    if (pos > 0 && pos <= friendRequests.size()) { return RowType.REQ; }
                }
            } else if (friends != null && friends.size() > 0) {
                // Friends but no friend requests
                return RowType.FRIEND;
            }
            // Else nothing - no need to return anything
            return null;
        }


        @Override
        public Object getItem(int position) {
            System.out.println("Someone is trying to get ITEM from friend adapter. implement this!");
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

        public void setFriendRequests( ArrayList<Friend> friendRequests) {
            this.friendRequests = friendRequests;
            notifyDataSetChanged();
        }


        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            if (getRowType(position) == RowType.REQ_LABEL) {
                view = buildRequestLabelRow(view);
            } else if (getRowType(position) == RowType.REQ) {
                view = buildFriendRequestRow(position, view);
            } else if (getRowType(position) == RowType.FRIEND_LABEL) {
                view = buildFriendLabelRow(view);
            } else if (getRowType(position) == RowType.FRIEND) {
                view = buildFriendRow(position, view);
            }

            return view;
        }

        private View buildFriendRow(int position, View view) {

            view = inflater.inflate(R.layout.friend_row, null);
            // Offset the position by getFriendOffset - because there may be a couple of label rows
            // and friend requests - so we can get the index to work on the actual friend list
            final int pos = position - getFriendOffset();

            TextView name = (TextView) view.findViewById(R.id.friendName);
            TextView username = (TextView) view.findViewById(R.id.friendUserName);
            TextView pending = (TextView) view.findViewById(R.id.pending);
            ImageView image = (ImageView) view.findViewById(R.id.friendPicture);
            final Button friendRequestButton = (Button) view.findViewById(R.id.friendRequestButton);
            final Button sendMessageButton = (Button) view.findViewById(R.id.sendMessageButton);
            final Button goToFriendOnMap = (Button) view.findViewById(R.id.locateButton);

            friendRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    friendRequestButton.setEnabled(false);
                    try {
                        DataHandler.gI().getSocialHandler().sendFriendRequest(thisAdapter,
                                friends.get(pos).getFriendId());
                    } catch (NotLoggedInException e) {
                        e.printStackTrace();
                    }
                }
            });
            sendMessageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), MessageActivity.class);
                    intent.setAction("OPEN_CHAT");
                    intent.putExtra("FRIEND_ID", friends.get(pos).getFriendId());
                    intent.putExtra("FRIEND_USERNAME", friends.get(pos).getUsername());
                    startActivity(intent);
                }
            });
            goToFriendOnMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Creats an Intent to send back results to HomeActivity
                    Intent data = new Intent();
                    //Puts the friendID in the result
                    data.putExtra("FriendID", friends.get(pos).getFriendId());
                    setResult(RESULT_OK, data);
                    //Shows toast for the user how to stop follow there friends.
                    showToast("Following " + friends.get(pos).getFirstname() + "." + "\nClick Map to stop Following.");
                    //Closes the activity
                    finish();
                }
            });

            final TextView newName = (TextView) view.findViewById(R.id.friendName);
            newName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    FriendProfileFragment friendInfo = new FriendProfileFragment();
                    Bundle friendBundle = new Bundle();
                    friendBundle.putSerializable("friend", friends.get(pos));
                    friendInfo.setArguments(friendBundle);

                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    fragmentTransaction.addToBackStack("FRIENDPROFILE");
                    fragmentTransaction.replace(R.id.friendsContainer, friendInfo);
                    fragmentTransaction.commit();
                }
            });

            // Set the name
            name.setText(friends.get(pos).getFirstname() + " " + friends.get(pos).getLastname());
            username.setText("@" + friends.get(pos).getUsername());

            // Set the proper button visibility
            if (friends.get(pos).getFriendType() == Friend.FriendType.FRIEND) {
                friendRequestButton.setVisibility(View.GONE);
                pending.setVisibility(View.GONE);

            } else if (friends.get(pos).getFriendType() == Friend.FriendType.PENDING) {
                friendRequestButton.setVisibility(View.GONE);
                sendMessageButton.setVisibility(View.GONE);
                goToFriendOnMap.setVisibility(View.GONE);
            } else {
                sendMessageButton.setVisibility(View.GONE);
                pending.setVisibility(View.GONE);
                goToFriendOnMap.setVisibility(View.GONE);
            }

            if(isSimple()) {
                friendRequestButton.setVisibility(View.GONE);
                pending.setVisibility(View.GONE);
            }
            if(friends.get(pos).getStatus() != Friend.Status.ONLINE){
                goToFriendOnMap.setVisibility(View.GONE);
            }



            // Set the picture
            image.setImageBitmap(SocialHandler.pictureToBitMap(friends.get(pos).getProfilePic()));
            return view;
        }


        private View buildFriendLabelRow (View view) {
            view = inflater.inflate(R.layout.friend_title, null);
            return view;
        }

        private View buildRequestLabelRow (View view) {
            view = inflater.inflate(R.layout.friend_request_title, null);
            return view;
        }

        private View buildFriendRequestRow(final int position, View view) {
            // Offset the position by one since there is a label at row 0
            // So the elements we access at the list are at a lower position
            final int pos = position - 1;

            view = inflater.inflate(R.layout.friend_request_row, null);
            TextView name = (TextView) view.findViewById(R.id.friendRequestName);
            TextView username = (TextView) view.findViewById(R.id.friendRequestUserName);
            ImageView image = (ImageView) view.findViewById(R.id.friendRequestPicture);
            final Button acceptButton = (Button) view.findViewById(R.id.acceptRequest);
            final Button declineButton = (Button) view.findViewById(R.id.declineRequest);
            acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    acceptButton.setEnabled(false);
                    declineButton.setEnabled(false);
                    try {
                        DataHandler.gI().getSocialHandler().answerFriendRequest(thisAdapter,
                                friendRequests.get(pos).getFriendId(), true);
                    } catch (NotLoggedInException e) {
                        e.printStackTrace();
                    }
                }
            });
            declineButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    acceptButton.setEnabled(false);
                    declineButton.setEnabled(false);
                    try {
                        DataHandler.gI().getSocialHandler().answerFriendRequest(thisAdapter,
                                friendRequests.get(pos).getFriendId(), false);
                    } catch (NotLoggedInException e) {
                        e.printStackTrace();
                    }
                }
            });

            // Set the name
            name.setText(friendRequests.get(pos).getFirstname() + " "
                    + friendRequests.get(pos).getLastname());
            username.setText("@" + friendRequests.get(pos).getUsername());

            // Set the picture
            image.setImageBitmap(SocialHandler.pictureToBitMap(friendRequests.get(pos).getProfilePic()));
            return view;
        }

    } // end inner class

    private boolean isSimple() {
        return DataHandler.gI().getSafetyStatus() != DataHandler.SafetyStatus.IDLE;
    }

} // end class
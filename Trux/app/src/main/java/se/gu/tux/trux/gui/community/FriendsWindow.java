package se.gu.tux.trux.gui.community;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
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
import se.gu.tux.trux.technical_services.NotLoggedInException;
import tux.gu.se.trux.R;

public class FriendsWindow extends BaseAppActivity implements View.OnClickListener,
        FriendFetchListener {

    public enum RowType {REQ_LABEL, REQ, FRIEND_LABEL, FRIEND};
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

        // Start fetching the friends
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
        DataHandler.gI().getSocialHandler().fetchFriendRequests(this);
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


    public void refresh() {
        if (lastFetchCall == FetchCall.FRIENDLIST) {
            showSearchResults(lastNeedle);
        } else {
            showFriends();
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



    class FriendAdapter extends BaseAdapter implements FriendActionListener {

        private Context context;
        private ArrayList<Friend> friendRequests;
        private ArrayList<Friend> friends;
        private LayoutInflater inflater = null;
        private final FriendAdapter thisAdapter = this;

        public FriendAdapter(Context context,  ArrayList<Friend> friends) {
            this.context = context;
            this.friends = friends;
            this.friendRequests = new ArrayList<Friend>();
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public void onFriendRequestSent(long friendId) {
            // Update the friend object to pending instead of not a friend
            for (Friend f : friends) {
                if (f.getFriendId() == friendId) {
                    f.setFriendType(Friend.FriendType.PENDING);
                }
            }

            DataHandler.gI().getSocialHandler().setFriendsChanged(true);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showToast("The friend request was sent.");
                    notifyDataSetChanged();
                    refresh();
                }
            });
        }

        @Override
        public void onFriendRemoveSent(long friendId) {

        }

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

            DataHandler.gI().getSocialHandler().setFriendRequestsChanged(true);
            DataHandler.gI().getSocialHandler().setFriendsChanged(true);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (accepted) {
                        showToast("The friend request was accepted.");
                    } else {
                        showToast("The friend request was declined.");
                    }
                    notifyDataSetChanged();
                    refresh();
                }
            });
        }

        @Override
        public int getCount() {
            int count, friendCount = 0, friendRequestCount = 0;
            if (friendRequests != null && friendRequests.size() > 0) {
                // + 1 to reserve a row for a text label "Friend requests"
                friendRequestCount = friendRequests.size();
            }
            if (friends != null && friends.size() > 0) {
                friendCount = friends.size();
            }
            if (friendCount > 0 && friendRequestCount > 0) {
                // + 2 more to reserve rows for text labels
                count = friendCount + friendRequestCount + 2;
            } else {
                count = friendCount + friendRequestCount;
            }
            return count;
        }

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

        private RowType getRowType(int pos) {
            System.out.println("Friends size: " + friends.size());
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

        public void onClickedFriend(int position){
            final int pos = position - getFriendOffset();
            Fragment fragment = new MapFrag();
            Bundle bundle = new Bundle();
            bundle.putSerializable("clickedFriend", friends.get(pos).getFriendId());
            fragment.setArguments(bundle);
          /*  FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            fragmentTransaction.replace(R.id.menuContainer, fragment);
            fragmentTransaction.commit();*/
        }


        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            System.out.println("Rendering position " + position + ", total count is " + getCount() + "...");
            System.out.println("Type is " + getRowType(position));
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
            System.out.println("Building friend row");
            view = inflater.inflate(R.layout.friend_row, null);
            // Offset the position by getFriendOffset - because there may be a couple of label rows
            // and friend requests - so we can get the index to work on the actual friend list
            final int pos = position - getFriendOffset();
            System.out.println("Building friend row with pos " + pos);
            TextView name = (TextView) view.findViewById(R.id.friendName);
            TextView username = (TextView) view.findViewById(R.id.friendUserName);
            TextView pending = (TextView) view.findViewById(R.id.pending);
            ImageView image = (ImageView) view.findViewById(R.id.friendPicture);
            final Button friendRequestButton = (Button) view.findViewById(R.id.friendRequestButton);
            final Button sendMessageButton = (Button) view.findViewById(R.id.sendMessageButton);
            final Button profileButton = (Button) view.findViewById(R.id.profileButton);
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

            // Set the name
            name.setText(friends.get(pos).getFirstname() + " " + friends.get(pos).getLastname());
            username.setText("@" + friends.get(pos).getUsername());

            // Set the proper button visibility
            if (friends.get(pos).getFriendType() == Friend.FriendType.FRIEND) {
                friendRequestButton.setVisibility(View.GONE);
                sendMessageButton.setVisibility(View.VISIBLE);
                pending.setVisibility(View.GONE);
                profileButton.setVisibility(View.VISIBLE);

            } else if (friends.get(pos).getFriendType() == Friend.FriendType.PENDING) {
                friendRequestButton.setVisibility(View.GONE);
                sendMessageButton.setVisibility(View.GONE);
                pending.setVisibility(View.VISIBLE);
                profileButton.setVisibility(View.GONE);
            } else {
                friendRequestButton.setVisibility(View.VISIBLE);
                sendMessageButton.setVisibility(View.GONE);
                pending.setVisibility(View.GONE);
                profileButton.setVisibility(View.GONE);
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
            System.out.println("Building friend request row");


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


} // end class
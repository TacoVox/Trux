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

import java.util.ArrayList;
import java.util.List;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.datastructure.ArrayResponse;
import se.gu.tux.trux.datastructure.Friend;
import se.gu.tux.trux.datastructure.Picture;
import se.gu.tux.trux.datastructure.ProtocolMessage;
import se.gu.tux.trux.gui.base.BaseAppActivity;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import tux.gu.se.trux.R;

public class FriendsWindow extends BaseAppActivity implements View.OnClickListener {

    private ListView friendsList;
    private FriendAdapter friendAdapter;
    private EditText searchField;
    private TextView noFriends;
    private Button searchButton;
    private AsyncTask friendAS, searchAS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_window);

        friendsList = (ListView) findViewById(R.id.friendsList);

        friendAdapter = new FriendAdapter(this, new Friend[0], new Bitmap[0]);
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
        noFriends.setText("You have no friends :(");
        if (friendAS != null) {
            friendAS.cancel(true);
        }
        if (searchAS != null) {
            searchAS.cancel(true);
        }
        friendAS = new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] objects) {
                // Load friend list
                Friend[] friends = null;
                Bitmap[] pictures = null;
                try {
                    System.out.println("Fetching friends...");
                    friends = DataHandler.getInstance().getFriends();
                    pictures = getPicturesFor(friends);
                    System.out.println("Done.");

                } catch (NotLoggedInException e) {
                    System.out.println("Trying to fetch friends while not logged in!");
                    cancel(true);
                }

                final Friend[] finalFriends = friends;
                final Bitmap[] finalPictures = pictures;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Showing friends in list...");
                        friendAdapter.setFriends(finalFriends, finalPictures);
                        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                    }
                });

                return null;
            }
        };
        friendAS.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Returns an array with all elements that contain the needle
     * (in username, firstname or lastname)
     */
    public Friend[] matchFriendSearch(Friend[] haystack, String needle) {
        if (haystack == null || haystack.length == 0) {
            return new Friend[0];
        }
        List<Friend> matches = new ArrayList<Friend>();
        for (Friend f : haystack) {
            if (f.getUsername().toLowerCase().indexOf(needle.toLowerCase()) != -1 ||
                    f.getFirstname().toLowerCase().indexOf(needle.toLowerCase()) != -1 ||
                    f.getLastname().toLowerCase().indexOf(needle.toLowerCase()) != -1) {
                // Friend matched needle
                matches.add(f);
            }
        }

        // Cannot cast Object[] directly to Friend[]
        Friend[] matchesArray = new Friend[matches.size()];
        for (int i = 0; i < matchesArray.length; i++) {
            matchesArray[i] = matches.get(i);
        }
        return matchesArray;
    }

    /**
     * Shows friends that match needle followed by other people who also match needle.
     *
     * @param needle
     */
    private void showSearchResults(final String needle) {
        noFriends.setText("No people found.");
        if (searchAS != null) {
            searchAS.cancel(true);
        }
        if (friendAS != null) {
            friendAS.cancel(true);
        }
        searchAS = new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] objects) {
                // Load friend list
                Friend[] friends = null;
                Object[] people = null;
                Bitmap[] pictures = null;

                try {
                    System.out.println("Fetching friends and people from search...");

                    // Fetch friends and see which are matching
                    friends = matchFriendSearch(DataHandler.getInstance().getFriends(), needle);

                    // Fetch other people
                    ArrayResponse ar = (ArrayResponse)DataHandler.getInstance().getData(
                            new ProtocolMessage(ProtocolMessage.Type.PEOPLE_SEARCH, needle));
                    if (ar.getArray() != null) {
                        people = ar.getArray();
                    }

                    // Join friends and people
                    friends = appendFriendObjects(friends, people);
                    pictures = getPicturesFor(friends);

                    System.out.println("Done.");

                } catch (NotLoggedInException e) {
                    System.out.println("Trying to fetch friends while not logged in!");
                    cancel(true);
                }

                final Friend[] finalFriends = friends;
                final Bitmap[] finalPictures = pictures;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Showing friends in list...");
                        friendAdapter.setFriends(finalFriends, finalPictures);
                        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                    }
                });

                return null;
            }
        };
        searchAS.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Appends the second array to the first - also does casting simultaneously
     * @param firstArray
     * @param secondArray
     * @return
     */
    private Friend[] appendFriendObjects(Friend[] firstArray, Object[] secondArray) {
        Friend[] sumArray = null;
        if (secondArray == null) {
            return firstArray;
        } else {
            sumArray = new Friend[firstArray.length + secondArray.length];
            int i = 0;
            for (i = 0; i < firstArray.length; i++) {
                sumArray[i] = firstArray[i];
            } // Note i is reused
            for (; i < firstArray.length + secondArray.length; i++) {
                sumArray[i] = (Friend)secondArray[i - firstArray.length];
            }
        }
        return sumArray;
    }

    private Bitmap[] getPicturesFor(Friend[] friends) throws NotLoggedInException {
        Bitmap[] pictures = new Bitmap[friends.length];
        for (int i = 0; i < pictures.length; i++) {
            System.out.println("Fetching image " + friends[i].getProfilePicId() + " for friend " +
                    friends[i].getFirstname());
            if (friends[i].getProfilePicId() != -1) {
                pictures[i] = DataHandler.getInstance().getPicture(friends[i].getProfilePicId());
            }
        }
        return pictures;
    }

    class FriendAdapter extends BaseAdapter {

        Context context;

        // The reason for not wrapping these together is that sometimes we want to be able to
        // send just friend info without the overhead of sending the picture. Could be handled
        // differentlyt though for example with a request boolean.
        Friend[] friends;
        Bitmap[] pictures;

        private LayoutInflater inflater = null;

        public FriendAdapter(Context context, Friend[] friends, Bitmap[] pictures) {
            this.context = context;
            this.friends = friends;
            this.pictures = pictures;
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            if (friends != null) {
                return friends.length;
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return friends[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void setFriends(Friend[] friends, Bitmap[] pictures) {
            this.friends = friends;
            this.pictures = pictures;
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
            name.setText(friends[position].getFirstname() + " " + friends[position].getLastname());
            username.setText("@" + friends[position].getUsername());

            // Set the proper button visibility
            if (friends[position].isFriend()) {
                friendRequestButton.setVisibility(View.GONE);
                sendMessageButton.setVisibility(View.VISIBLE);
            } else {
                friendRequestButton.setVisibility(View.VISIBLE);
                sendMessageButton.setVisibility(View.GONE);
            }

            // Set the picture
            image.setImageBitmap(pictures[position]);

            return view;
        }

    } // end nested class


} // end class
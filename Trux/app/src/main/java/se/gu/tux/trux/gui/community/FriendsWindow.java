package se.gu.tux.trux.gui.community;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.datastructure.Friend;
import se.gu.tux.trux.gui.base.BaseAppActivity;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import tux.gu.se.trux.R;

public class FriendsWindow extends BaseAppActivity {

    private ListView friendsList;
    private FriendAdapter friendAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_window);

        friendsList = (ListView) findViewById(R.id.friendsList);
        friendAdapter = new FriendAdapter(this, new Friend[0]);
        friendsList.setAdapter(friendAdapter);
        friendsList.setEmptyView(findViewById(R.id.noFriends));
        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] objects) {
                Friend[] friends = null;
                try {
                    friends = DataHandler.getInstance().getFriends();
                } catch (NotLoggedInException e) {
                    System.out.println("Trying to fetch friends while not logged in!");
                    cancel(true);
                }

                final Friend[] finalFriends = friends;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        friendAdapter.setFriends(finalFriends);
                        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                    }
                });
                return null;
            }
        }.execute();

    }
}

class FriendAdapter extends BaseAdapter {

    Context context;

    Friend[] friends;
    private static LayoutInflater inflater = null;

    public FriendAdapter(Context context, Friend[] friends) {
        // TODO Auto-generated constructor stub
        this.context = context;
        this.friends = friends;
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

    public void setFriends(Friend[] friends) {
        this.friends = friends;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null)
            view = inflater.inflate(R.layout.friend_row, null);
        TextView text = (TextView) view.findViewById(R.id.friendName);

        // TODO: Here just create a small asynctask that waits if the picture needs loading,
        // then just show it

        text.setText(friends[position].getFirstname() + " " + friends[position].getLastname());
        return view;
    }
}

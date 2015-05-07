package se.gu.tux.trux.gui.community;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import se.gu.tux.trux.appplication.DataHandler;
import se.gu.tux.trux.datastructure.Friend;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import tux.gu.se.trux.R;

public class FriendsWindow extends ActionBarActivity {

    private ListView friendsList;
    private FriendAdapter friendAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_window);

        friendsList = (ListView) findViewById(R.id.friendsList);
        friendAdapter = new FriendAdapter(this, new Friend[0]);
        friendsList.setAdapter(friendAdapter);

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
                    }
                });
                return null;
            }
        }.execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_friends_window, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        return friends.length;
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
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.friend_row, null);
        TextView text = (TextView) vi.findViewById(R.id.friendName);

        // TODO: Here just create a small asynctask that waits if the picture needs loading,
        // then just show it
        text.setText(friends[position].getUsername());
        return vi;
    }
}

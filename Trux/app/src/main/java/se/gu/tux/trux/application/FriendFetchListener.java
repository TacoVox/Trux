package se.gu.tux.trux.application;

import java.util.ArrayList;
import java.util.List;

import se.gu.tux.trux.datastructure.Friend;

/**
 * Created by jerker on 2015-05-15.
 */
public interface FriendFetchListener {

    public void FriendsFetched(ArrayList<Friend> friends);

}

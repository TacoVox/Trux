package se.gu.tux.trux.application;

import java.util.ArrayList;

import se.gu.tux.trux.datastructure.Friend;

/**
 * Created by jerker on 2015-05-15.
 */
public interface FriendRequestSentListener {

    public void onFriendRequestSent(long friendId);

}

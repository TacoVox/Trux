package se.gu.tux.trux.application;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.ArrayList;
import java.util.HashMap;

import se.gu.tux.trux.datastructure.ArrayResponse;
import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.Friend;
import se.gu.tux.trux.datastructure.Picture;
import se.gu.tux.trux.datastructure.ProtocolMessage;
import se.gu.tux.trux.datastructure.User;
import se.gu.tux.trux.technical_services.NotLoggedInException;

/**
 * Created by jerker on 2015-05-15.
 *
 * We want fetching of friends and pictures etc to be independent - so we have SocialCacher that
 * puts it all together. We have a Picture reference in Friend but that is transient, so we
 * intentionally do not send it automatically from the server each time we request a Friend.
 * Here each time we update the friend hashmap, we combine the Friend object with cached Picture.
 */
public class SocialHandler {
    private HashMap<Long, Friend> friendCache;
    private HashMap<Long, Friend> friendRequestCache;
    private HashMap<Long, Picture> pictureCache;
    public enum FriendsUpdateMode {NONE, ALL, ONLINE};
    private boolean friendsChanged, friendRequestsChanged;

    public SocialHandler() {
        friendCache = new HashMap<Long, Friend>();
        friendRequestCache = new HashMap<Long, Friend>();
        pictureCache = new HashMap<Long, Picture>();
        // Assume freind requests have changed so they are fetched the first time the friend
        // window is shown.
        friendRequestsChanged = true;
    }

    /**
     * Fetches in its own background thread, then calls back to the FriendFetchListener object.
     * NOTE: FriendsUpdateMode ONLINE only fetches AND ALSO returns the friends that are online.
     * @param listener
     * @param reqUpdateMode
     */
    public void fetchFriends(final FriendFetchListener listener, final FriendsUpdateMode reqUpdateMode) {
        new Thread(new Runnable() {
            ArrayList<Friend> friends = new ArrayList<Friend>();

            @Override
            public void run() {
                // Update the list of friends this user has
                try {
                    User currentUser = DataHandler.gI().getUser();
                    // If user was logged out recently, just return
                    if (currentUser == null) {
                        return;
                    }
                    Data user = DataHandler.gI().getData(currentUser);
                    if (user instanceof User) {
                        DataHandler.gI().setUser((User) user);
                    }
                } catch (NotLoggedInException e) {
                    listener.onFriendsFetched(new ArrayList<Friend>());
                }
                final long[] friendIds = DataHandler.gI().getUser().getFriends();

                FriendsUpdateMode updateMode = reqUpdateMode;
                if (updateMode == FriendsUpdateMode.NONE) {
                    // If no forced update, still update ALL if the list doesn't have the correct objects
                    if (!allFriendsInCache() || friendsChanged) {
                        System.out.println("Forcing update of all friends.");
                        updateMode = FriendsUpdateMode.ALL;
                    }
                }

                // If forced update ALL, fetch all friend objects
                if (updateMode == FriendsUpdateMode.ALL) {
                    System.out.println("Updating all friends.");
                    friendCache.clear();
                    for (int i = 0; i < friendIds.length; i++) {
                        System.out.println("Friend "  + i);
                        Data d = null;
                        try {
                            d = DataHandler.gI().getData(new Friend(friendIds[i]));
                        } catch (NotLoggedInException e) {
                            listener.onFriendsFetched(new ArrayList<Friend>());
                        }
                        if (d instanceof Friend) {
                            // Join this Friend object with its matching picture and cache it
                            Friend cachedFriend = cacheFriend((Friend) d, friendCache);
                            // Simultaneously build the list that will be returned to the listener
                            friends.add(cachedFriend);

                        } else if (d instanceof ProtocolMessage) {
                            System.out.println("Friend fetch problem: "
                                    + ((ProtocolMessage)d).getMessage());
                        }
                    }

                } else if (updateMode == FriendsUpdateMode.ONLINE) {
                    // If forced update ONLINE fetch online friends and merge with cache
                    System.out.println("Updating online friends.");
                    Data d = null;
                    try {
                        d = DataHandler.gI().getData(
                                new ProtocolMessage(ProtocolMessage.Type.GET_ONLINE_FRIENDS));
                    } catch (NotLoggedInException e) {
                        listener.onFriendsFetched(new ArrayList<Friend>());
                    }
                    if (d instanceof ArrayResponse && ((ArrayResponse) d).getArray() != null) {
                        for (Object currentFriendO : ((ArrayResponse) d).getArray()) {
                            // Cache all online friends
                            Friend cachedFriend = cacheFriend((Friend)currentFriendO, friendCache);

                            // Also put them into a list that will be returned specifically after
                            // requesting online friends
                            friends.add(cachedFriend);

                        }
                    }

                } else {
                    System.out.println("Returning cached friends.");
                    // The caceh was not updated, just return the previously cached friends
                    friends = new ArrayList<Friend>(friendCache.values());
                }

                friendsChanged = false;
                System.out.println("Returning fetched friends.");
                listener.onFriendsFetched(friends);
            }
        }).start();
    }



    /**
     * Fetches in its own background thread, then calls back to the FriendRequestFetchListener
     * object. Only fetches if friendRequestsChanged is set to true, so notifications should
     * change this boolean. It is set to true in the constructor of this class as well, to
     * make sure requests are always fetched the first time the friend window is loaded.
     */
    public void fetchFriendRequests(final FriendFetchListener listener) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                ArrayList<Friend> friendRequests = new ArrayList<Friend>();

                // If we know the friend requests have changed recently (also true on startup),
                // be sure to fetch/update them before returning a list from the cache hashmap
                if (friendRequestsChanged) {
                    try {
                        // Empty cache
                        friendRequestCache.clear();

                        // Get the data
                        Data d = null;
                        d = DataHandler.gI().getData(new ProtocolMessage(ProtocolMessage.Type.
                                CAN_YOU_PLEASE_GIVE_ME_AN_ARRAY_WITH_EVERYONE_WHO_SENT_THIS_USER_A_FRIEND_REQUEST_THANK_YOU_IN_ADVANCE_DEAR_BROTHER));

                        // Put friend request friends in friend request cache
                        if (d instanceof ArrayResponse && ((ArrayResponse) d).getArray() != null) {
                            Object[] responseArray = ((ArrayResponse) d).getArray();

                            for (Object friendO : ((ArrayResponse) d).getArray()) {
                                Friend friendRequest = (Friend) friendO;
                                // Put in cache
                                cacheFriend(friendRequest, friendRequestCache);

                                // Also notify the server that this is seen
                                DataHandler.gI().getData(new ProtocolMessage(
                                        ProtocolMessage.Type.FRIEND_REQUEST_SEEN,
                                        Long.toString(friendRequest.getFriendId())));
                            }

                        } else if (d instanceof ProtocolMessage) {
                            System.out.println("Problem with friend request fetch: "
                                    + ((ProtocolMessage)d).getMessage());
                        } else {
                            System.out.println("No friend requests.");
                        }
                    } catch (NotLoggedInException e) {
                        // Return empty list if any problems with session
                        listener.onFriendRequestsFetched(new ArrayList<Friend>());
                        return;
                    }
                }

                // Return the cached friend request list
                listener.onFriendRequestsFetched(new ArrayList<Friend>(friendRequestCache.values()));
            }
        }).start();
    }


    /**
     * Caches this friend after matching it with a profile pic - returns the matched friend
     * @param f
     * @param friendMap
     * @return
     */
    private Friend cacheFriend(Friend f, HashMap<Long, Friend> friendMap) {
        try {
            f.setProfilePic(getPicture(f.getProfilePicId()));
        } catch (NotLoggedInException e) {
            e.printStackTrace();
        }
        friendMap.put(f.getFriendId(), f);
        return f;
    }


    /**
     * Finds out if the friendCache matches the friends list - we check if every entry in the users
     * friend id array matches an entry in the friendCache, AND that the cache doesn't have a
     * different amount of entries than the friend list. This way we are sure to update even if
     * someone removed this user as a friend.
     * @return
     */
    private boolean allFriendsInCache() {
        long[] friendIds = DataHandler.gI().getUser().getFriends();
        boolean friendsMatchCache = false;

        if (friendIds == null) {
            // Nothing in friends list means cache matches it
            friendsMatchCache = true;

        } else if (friendCache.size() == friendIds.length){

            // If the size is the same, try to assume cache matches friend list, which is does
            // UNLESS someone is missing in cache
            friendsMatchCache = true;
            for (Long friendId : DataHandler.gI().getUser().getFriends()) {
                if (!friendCache.containsKey(friendId)) {
                    friendsMatchCache = false;
                }
            }
        }
        return friendsMatchCache;
    }

    /**
     * Get the requested picture as a Bitmap object.
     * @param pictureId
     * @return
     * @throws se.gu.tux.trux.technical_services.NotLoggedInException
     */
    public Picture getPicture(Long pictureId) throws NotLoggedInException {
        if (pictureCache == null) {
            pictureCache = new HashMap<Long, Picture>();
        }
        if (pictureId == -1) {
            System.out.println("User doesn't have a picture");
            return null;
        }

        // Empty cache if it is really big
        if (pictureCache.size() > 500) {
            pictureCache.clear();
        }

        // See if the image is not yet cached
        if (pictureCache.get(pictureId) == null) {
            // Try to fecth it
            System.out.println("Fetching picture...");
            pictureCache.put(pictureId, (Picture)DataHandler.gI().getData(new Picture(pictureId)));
        }

        return pictureCache.get(pictureId);
    }


    public static Bitmap pictureToBitMap(Picture p) {
        // Convert picture to a bitmap so it can be used in the app
        Bitmap bmp = null;
        if (p != null && p.getImg() != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            bmp = BitmapFactory.decodeByteArray(p.getImg(), 0,
                    p.getImg().length, options);
        }
        return bmp;
    }


    public void sendFriendRequest(final FriendActionListener listener, final long friendId)
            throws NotLoggedInException {
        if (!DataHandler.gI().isLoggedIn()) {
            throw new NotLoggedInException();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                ProtocolMessage friendRequest =
                        new ProtocolMessage(ProtocolMessage.Type.FRIEND_REQUEST,
                        Long.toString(friendId));
                try {
                    DataHandler.gI().getData(friendRequest);
                } catch (NotLoggedInException e) {
                    e.printStackTrace();
                }

                listener.onFriendRequestSent(friendId);
            }
        }).start();
    }


    public void sendFriendRemove(final FriendActionListener listener, final long friendId)
            throws NotLoggedInException {
        if (!DataHandler.gI().isLoggedIn()) {
            throw new NotLoggedInException();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                ProtocolMessage friendRemove =
                        new ProtocolMessage(ProtocolMessage.Type.FRIEND_REMOVE,
                                Long.toString(friendId));
                try {
                    DataHandler.gI().getData(friendRemove);
                } catch (NotLoggedInException e) {
                    e.printStackTrace();
                }

                listener.onFriendRemoveSent(friendId);
            }
        }).start();
    }


    public void answerFriendRequest(final FriendActionListener listener, final long friendId,
                                    final boolean accept) throws NotLoggedInException {
        if (!DataHandler.gI().isLoggedIn()) {
            throw new NotLoggedInException();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {

                ProtocolMessage friendRequestAction = null;
                if (accept) {
                    friendRequestAction = new ProtocolMessage(ProtocolMessage.Type.ACCEPT_FRIEND,
                            Long.toString(friendId));
                } else {
                    friendRequestAction = new ProtocolMessage(ProtocolMessage.Type.DECLINE_FRIEND,
                            Long.toString(friendId));
                }

                try {
                    DataHandler.gI().getData(friendRequestAction);
                } catch (NotLoggedInException e) {
                    e.printStackTrace();
                }

                listener.onFriendRequestAnswered(friendId, accept);
            }
        }).start();
    }


    public void setFriendsChanged(boolean friendsChanged) {
        this.friendsChanged = friendsChanged;
    }

    public void setFriendRequestsChanged(boolean friendRequestsChanged) {
        this.friendRequestsChanged = friendRequestsChanged;
    }

    public void clearCache() {
        friendCache.clear();
        pictureCache.clear();
        friendRequestCache.clear();
    }
}

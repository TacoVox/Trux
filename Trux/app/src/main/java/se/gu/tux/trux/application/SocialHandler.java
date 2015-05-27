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
 * This class contains friend-related helper, fetching and caching methods.
 * We want fetching of friends and pictures etc to be independent - so we have SocialCacher that
 * puts it all together. We have a Picture reference in Friend that is transient, so we
 * intentionally do not send it automatically from the server each time we request a Friend.
 * Here each time we update the friend hashmap, we combine the Friend object with cached Picture.
 *
 * Note that other people than the current user are referred to as friends even though they don't
 * necessarily have a friend relation, because of the Friend datatype.
 */
public class SocialHandler {
    // Update modes - used when updating the friend list
    public enum FriendsUpdateMode {NONE, ALL, ONLINE};

    // Cache hashmaps
    private HashMap<Long, Friend> friendCache;
    private HashMap<Long, Friend> friendRequestCache;
    private HashMap<Long, Picture> pictureCache;

    // Flags, representing if the cache is outdated
    private boolean friendsChanged, friendRequestsChanged;


    /**
     * Constructor. Initiates the hash maps and sets the friendRequestsChanged flag so we are
     * sure to fetch them the next time they are requested.
     */
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
     * @param listener      The listener will be called via the onFriendsFetched method of the
     *                      FriendFetchListener interface, from the background thread.
     * @param reqUpdateMode The update mode. NONE doesn't force an update unless we know that they
     *                      have changed. ALL forces an update of all friends. ONLINE is a special
     *                      mode that updates the friends that are online and only returns these
     *                      friends to the listener.
     */
    public void fetchFriends(final FriendFetchListener listener, final FriendsUpdateMode reqUpdateMode) {
        new Thread(new Runnable() {
            ArrayList<Friend> friends = new ArrayList<Friend>();

            @Override
            public void run() {

                try {
                    // Update the list of friends this user has
                    // First copy the logged in user
                    User currentUser = DataHandler.gI().getUser();

                    // If user was logged out recently, just return
                    if (currentUser == null) {
                        return;
                    }

                    // Update the user object, so we get the newest friend list
                    Data user = DataHandler.gI().getData(currentUser);
                    if (user instanceof User) {
                        DataHandler.gI().setUser((User) user);
                    }
                } catch (NotLoggedInException e) {
                    listener.onFriendsFetched(new ArrayList<Friend>());
                }

                // Copy the friend list, here an array with Ids.
                final long[] friendIds = DataHandler.gI().getUser().getFriends();

                // If no forced update, still update ALL if the list doesn't have the correct objects
                FriendsUpdateMode updateMode = reqUpdateMode;
                if (updateMode == FriendsUpdateMode.NONE) {
                    if (!allFriendsInCache() || friendsChanged) {
                        updateMode = FriendsUpdateMode.ALL;
                    }
                }

                // If updateMode is ALL, fetch all friend objects
                if (updateMode == FriendsUpdateMode.ALL) {
                    // Clear the cache.
                    friendCache.clear();

                    for (int i = 0; i < friendIds.length; i++) {

                        // Fetch friend data
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
                    Data d = null;
                    try {
                        d = DataHandler.gI().getData(
                                new ProtocolMessage(ProtocolMessage.Type.GET_ONLINE_FRIENDS));
                    } catch (NotLoggedInException e) {
                        listener.onFriendsFetched(new ArrayList<Friend>());
                    }

                    // Check that we got a reasonable response
                    if (d instanceof ArrayResponse && ((ArrayResponse) d).getArray() != null) {

                        // Loop through the online friends
                        for (Object currentFriendO : ((ArrayResponse) d).getArray()) {

                            // Cache all online friends
                            Friend cachedFriend = cacheFriend((Friend)currentFriendO, friendCache);

                            // Also put them into a list that will be returned specifically after
                            // requesting online friends
                            friends.add(cachedFriend);
                        }
                    }

                } else {
                    // Neither ALL or ONLINE
                    // The caceh was not updated, just return the previously cached friends
                    friends = new ArrayList<Friend>(friendCache.values());
                }

                friendsChanged = false;
                listener.onFriendsFetched(friends);
            }
        }).start();
    }


    /**
     * Fetches in its own background thread, then calls back to the FriendRequestFetchListener
     * object. Only fetches if friendRequestsChanged is set to true, which it is initially
     * and when we get a notification.
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

                        // Check response type
                        if (d instanceof ArrayResponse && ((ArrayResponse) d).getArray() != null) {

                            Object[] responseArray = ((ArrayResponse) d).getArray();

                            // Put all friend request friends in friend request cache
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
     * Caches this friend after matching it with a profile pic - returns the matched friend.
     * Be sure to run from a background thread.
     * @param f             The friend to cache.
     * @param friendMap     The map to cache it in.
     * @return              The friend, now matched with it's profile picture in its transient
     *                      profilePic field.
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
     * @return  Boolean representing wheter friend list and cache matches.
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
     * Get the requested picture as a Picture object.
     * @param pictureId     The desired pictures id.
     * @return              The desired picture.
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
            pictureCache.put(pictureId, (Picture)DataHandler.gI().getData(new Picture(pictureId)));
        }

        return pictureCache.get(pictureId);
    }


    /**
     * Helper method to convert Picture to Bitmap. Unfortunately we cannot have this in the Picture
     * datatype since they datatypes are known to the server, and Bitmap is an android class.
     * @param p     The picture to convert.
     * @return      A bitmap object.
     */
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


    /**
     * Sends a friend request to the specified person. This is done in a background thread, once
     * it is sent the listener is called.
     * @param listener      The FriendActionListener.
     * @param friendId      The friend.
     * @throws NotLoggedInException
     */
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


    /**
     * Sends a friend remove request for the specified friend. This is done in a background thread,
     * once it is sent the listener is called.
     * @param listener      The FriendActionListener.
     * @param friendId      The friend.
     * @throws NotLoggedInException
     */
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


    /**
     * Answers a friend request. This is done in a background thread, once
     * it is sent the listener is called.
     * @param listener      The FriendActionListener.
     * @param friendId      The friend.
     * @param accept        A boolean representing whether the request was accepted or not.
     * @throws NotLoggedInException
     */
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


    /**
     * Setter for the friendsChanged flag.
     * @param friendsChanged    The new status of the flag.
     */
    public void setFriendsChanged(boolean friendsChanged) {
        this.friendsChanged = friendsChanged;
    }


    /**
     * Setter for the friendRequestsChanged flag.
     * @param friendRequestsChanged The new status of the flag.
     */
    public void setFriendRequestsChanged(boolean friendRequestsChanged) {
        this.friendRequestsChanged = friendRequestsChanged;
    }


    /**
     * Clears all cached data.
     */
    public void clearCache() {
        friendCache.clear();
        pictureCache.clear();
        friendRequestCache.clear();
    }
}

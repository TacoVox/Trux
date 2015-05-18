package se.gu.tux.trux.application;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import se.gu.tux.trux.datastructure.ArrayResponse;
import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.Friend;
import se.gu.tux.trux.datastructure.Picture;
import se.gu.tux.trux.datastructure.ProtocolMessage;
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
    public void fetchFriends(final FriendFetchListener listener, FriendsUpdateMode reqUpdateMode) {
        final long[] friendIds = DataHandler.gI().getUser().getFriends();

        // No friends / friends not set
        if (friendIds == null) {
            System.out.println("Users friends was null.");
            listener.FriendsFetched(new ArrayList<Friend>());
        }

        if (reqUpdateMode == FriendsUpdateMode.NONE) {
            // If no forced update, still update ALL if the list doesn't have the correct objects
            if (!allFriendsInCache() || friendsChanged) {
                System.out.println("Forcing update of all friends.");
                reqUpdateMode = FriendsUpdateMode.ALL;
            }
        }
        final FriendsUpdateMode updateMode = reqUpdateMode;


        new Thread(new Runnable() {
            ArrayList<Friend> friends = new ArrayList<Friend>();

            @Override
            public void run() {
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
                            listener.FriendsFetched(new ArrayList<Friend>());
                        }
                        if (d instanceof Friend) {
                            System.out.println("Caching...");
                            // Join this Friend object with its matching picture and cache it
                            Friend cachedFriend = cacheFriend((Friend) d, friendCache);
                            // Simultaneously build the list that will be returned to the listener
                            friends.add(cachedFriend);

                        } else if (d instanceof ProtocolMessage) {
                            System.out.println("Friend fetch: " + ((ProtocolMessage)d).getMessage());
                        }
                    }
                    System.out.println("OK");
                } else if (updateMode == FriendsUpdateMode.ONLINE) {
                    // If forced update ONLINE fetch online friends and merge with cache
                    System.out.println("Updating online friends.");
                    Data d = null;
                    try {
                        d = DataHandler.gI().getData(
                                new ProtocolMessage(ProtocolMessage.Type.GET_ONLINE_FRIENDS));
                    } catch (NotLoggedInException e) {
                        listener.FriendsFetched(new ArrayList<Friend>());
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
                listener.FriendsFetched(friends);
            }
        }).start();
    }



    /**
     * Fetches in its own background thread, then calls back to the FriendRequestFetchListener
     * object. Only fetches if friendRequestsChanged is set to true, so notifications should
     * change this boolean. It is set to true in the constructor of this class as well, to
     * make sure requests are always fetched the first time the friend window is loaded.
     */
    public void fetchFriendRequests(final FriendRequestFetchListener listener) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                ArrayList<Friend> friendRequests = new ArrayList<Friend>();

                // Return immediately with the cached friend requests unless we know the requests have
                // changed recently
                if (friendRequestsChanged) {
                    friendRequestCache.clear();

                    Data d = null;
                    try {
                        d = DataHandler.gI().getData(new ProtocolMessage(ProtocolMessage.Type.
                                CAN_YOU_PLEASE_GIVE_ME_AN_ARRAY_WITH_EVERYONE_WHO_SENT_THIS_USER_A_FRIEND_REQUEST_THANK_YOU_IN_ADVANCE_DEAR_BROTHER));
                    } catch (NotLoggedInException e) {
                        // Return empty list if any problems with session
                        listener.FriendRequestsFetched(new ArrayList<Friend>());
                        return;
                    }

                    // Put friend request friends in friend request cache
                    if (d instanceof ArrayResponse && ((ArrayResponse) d).getArray() != null) {
                        Object[] responseArray = ((ArrayResponse) d).getArray();

                        for (Object friendO : ((ArrayResponse) d).getArray()) {
                            // Put in cache
                           cacheFriend((Friend) friendO, friendRequestCache);
                        }

                    } else if (d instanceof ProtocolMessage) {
                        System.out.println("Problem with friend request fetch: "
                                + ((ProtocolMessage)d).getMessage());
                    } else {
                        System.out.println("No friend requests.");
                    }
                }

                // Return the cached friend request list
                listener.FriendRequestsFetched(new ArrayList<Friend>(friendRequestCache.values()));
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
            System.out.println("Setting picture on friend...");
            f.setProfilePic(getPicture(f.getProfilePicId()));
        } catch (NotLoggedInException e) {
            e.printStackTrace();
        }
        friendMap.put(f.getFriendId(), f);
        return f;
    }


    private boolean allFriendsInCache() {
        if (DataHandler.gI().getUser().getFriends() == null) {
            return true;
        }
        for (Long friendId : DataHandler.gI().getUser().getFriends()) {
            if (!friendCache.containsKey(friendId)) {
                return false;
            }
        }
        return true;
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


    public void sendFriendRequest(final long friendId) throws NotLoggedInException {
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
            }
        }).start();
    }
}

package se.gu.tux.trux.application;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    private HashMap<Long, Picture> pictureCache;
    public enum FriendsUpdateMode {NONE, ALL, ONLINE};

    interface FriendFetchListener {
        public void FriendsFetched(List<Friend> friends);
    }


    /**
     * Fetches in its own background thread, then calls back to the FetchFriendListener object.
     * NOTE: FriendsUpdateMode ONLINE only fetches AND ALSO returns the friends that are online.
     * @param listener
     * @param updateMode
     */
    public void fetchFriends(FriendFetchListener listener, FriendsUpdateMode updateMode)
            throws NotLoggedInException {
        ArrayList<Friend> friends = new ArrayList<Friend>();
        long[] friendIds = DataHandler.gI().getUser().getFriends();

        // No friends / friends not set
        if () {
            System.out.println("Users friends was null.");
            listener.FriendsFetched(friends);
        }

        if (updateMode == FriendsUpdateMode.NONE) {
            // If no forced update, still update ALL if the list doesn't have the correct objects
            if (!allFriendsInCache()) {
                updateMode = FriendsUpdateMode.ALL;
            }
        }

        // If forced update ALL, fetch all friend objects
        if (updateMode == FriendsUpdateMode.ALL) {
            friendCache.clear();
            for (int i = 0; i < friendIds.length; i++) {
                Data d = DataHandler.gI().getData(new Friend(friendIds[i]));
                if (d instanceof Friend) {
                    friends.add((Friend)d);
                } else if (d instanceof ProtocolMessage) {
                    System.out.println("Friend fetch: " + ((ProtocolMessage)d).getMessage());
                }
            }
        } else if (updateMode == FriendsUpdateMode.ONLINE) {
            // If forced update ONLINE fetch online friends and merge with cache
            Data d = DataHandler.gI().getData(
                    new ProtocolMessage(ProtocolMessage.Type.GET_ONLINE_FRIENDS));
            if (d instanceof ArrayResponse && ((ArrayResponse) d).getArray() != null) {
                for (Object currentFriendO : ((ArrayResponse) d).getArray()) {
                    Friend currentFriend = (Friend) currentFriendO;
                    friendCache.put();
                }
            }

        }

        // Finally, regardless of update mode, return friends as list

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
}

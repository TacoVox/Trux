package se.gu.tux.trux.datastructure;

/**
 * Created by jerker on 2015-05-11.
 */
public class Notification extends Data {
    private boolean newMessages;
    private boolean newFriends;

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public void setValue(Object value) {

    }

    @Override
    public boolean isOnServerSide() {
        return true;
    }

    public boolean isNewMessages() {
        return newMessages;
    }

    public void setNewMessages(boolean newMessages) {
        this.newMessages = newMessages;
    }

    public boolean isNewFriends() {
        return newFriends;
    }

    public void setNewFriends(boolean newFriends) {
        this.newFriends = newFriends;
    }
}

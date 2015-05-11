package se.gu.tux.trux.datastructure;

/**
 * Created by jerker on 2015-05-11.
 */
public class Notification {

    public enum Type {NEW_MESSAGE, FRIEND_REQUEST};
    private Type type;
    private String message;


    public Notification(String message, Type type) {
        this.message = message;
        this.type = type;
    }

}

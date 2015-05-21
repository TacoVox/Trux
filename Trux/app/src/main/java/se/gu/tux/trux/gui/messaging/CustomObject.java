package se.gu.tux.trux.gui.messaging;

import se.gu.tux.trux.datastructure.Friend;
import se.gu.tux.trux.datastructure.Message;

/**
 * Created by ivryashkov on 2015-05-18.
 */
public class CustomObject
{

    private Friend friend;
    private Message message;


    public CustomObject(Friend friend, Message message)
    {
        this.friend = friend;
        this.message = message;
    }


    public Friend getFriend()               {return friend;}


    public void setFriend(Friend friend)    {this.friend = friend;}


    public Message getMessage()             {return message;}


    public void setMessage(Message message) {this.message = message;}


} // end class

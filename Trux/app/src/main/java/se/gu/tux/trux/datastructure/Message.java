package se.gu.tux.trux.datastructure;


public class Message extends Data implements Comparable<Message>
{
    // fields
    private long senderId;
    private long receiverId;
    private boolean isSeen;
    private String value;
    private long conversationId;



    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setValue(Object value) {
        this.value = (String) value;
    }

    @Override
    public boolean isOnServerSide() {
        return true;
    }

    public long getSenderId() {
        return senderId;
    }

    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }

    public long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(long receiverId) {
        this.receiverId = receiverId;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setIsSeen(boolean isSeen) {
        this.isSeen = isSeen;
    }

    public long getConversationId() {
        return conversationId;
    }

    public void setConversationId(long conversationId) {
        this.conversationId = conversationId;
    }


    @Override
    public int compareTo(Message message)
    {
        if (getTimeStamp() > message.getTimeStamp()) return 1;
        else if (getTimeStamp() == message.getTimeStamp()) return 0;
        else    return -1;
    }


} // end class

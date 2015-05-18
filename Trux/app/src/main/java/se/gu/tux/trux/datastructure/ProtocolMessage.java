package se.gu.tux.trux.datastructure;

/**
 * Created by jonas on 3/24/15.
 */
public class ProtocolMessage extends Data {
    public enum Type {LOGIN_SUCCESS, LOGIN_FAILED, AUTO_LOGIN_REQUEST, LOGOUT_REQUEST, DATA_RECEIVED,
        SUCCESS, ERROR, INVALID_SESSION, PEOPLE_SEARCH, FRIEND_REQUEST, FRIEND_REQUEST_SEEN, FRIEND_REMOVE, ACCEPT_FRIEND, GOODBYE,
        GET_LATEST_CONVERSATIONS, GET_LATEST_MESSAGES, MESSAGE_SEEN, GET_ONLINE_FRIENDS,
        CAN_YOU_PLEASE_GIVE_ME_AN_ARRAY_WITH_EVERYONE_WHO_SENT_THIS_USER_A_FRIEND_REQUEST_THANK_YOU_IN_ADVANCE_DEAR_BROTHER};
	private Type responseType;
    private String message;
	
	public ProtocolMessage(Type response) {
		this.responseType = response;
	}

    public ProtocolMessage(Type response, String message) {
        this.responseType = response;
        this.message = message;
    }

	@Override
	public Object getValue() {
		return responseType;
	}

	@Override
	public void setValue(Object value) {
		if (value instanceof Type) {
			responseType = (Type) value;
		}
	}

    public Type getType() {
        return responseType;
    }

    public String getMessage() {
        return message;
    }

    @Override
	public boolean isOnServerSide() {
		// TODO Auto-generated method stub
		return true;
	}
}

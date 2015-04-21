package se.gu.tux.trux.datastructure;

/**
 * Created by jonas on 3/24/15.
 */
public class ProtocolMessage extends Data {
	public enum Type {LOGIN_SUCCESS, LOGIN_FAILED, LOGOUT_REQUEST, DATA_RECEIVED, SUCCESS, ERROR};
	private Type responseType;
	
	public ProtocolMessage(Type response) {
		this.responseType = response;
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

	@Override
	public boolean isOnServerSide() {
		// TODO Auto-generated method stub
		return true;
	}
}

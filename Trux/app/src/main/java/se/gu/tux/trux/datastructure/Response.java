package se.gu.tux.trux.datastructure;

/**
 * Created by jonas on 3/24/15.
 */
public class Response extends Data {
	public enum Type {LOGIN_SUCCESS, LOGIN_FAILED, DATA_RECEIVED};
	private Type responseType;
	
	public Response(Type response) {
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

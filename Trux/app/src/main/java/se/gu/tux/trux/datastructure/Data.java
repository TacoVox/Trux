package se.gu.tux.trux.datastructure;

import java.io.Serializable;

/**
 * Created by jonas on 3/24/15.
 */
public abstract class Data <T> implements Serializable {
	private static final long serialVersionUID = 1L;
	public abstract T getValue();
    public abstract void setValue(T value);
    public abstract boolean isOnServerSide();
}

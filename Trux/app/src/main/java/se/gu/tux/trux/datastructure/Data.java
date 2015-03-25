package se.gu.tux.trux.datastructure;

/**
 * Created by jonas on 3/24/15.
 */
public interface Data <T> {
    public T getValue();
    public void setValue(T value);
    public boolean isOnServerSide();
}

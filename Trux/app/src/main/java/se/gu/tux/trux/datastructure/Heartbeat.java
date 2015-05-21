package se.gu.tux.trux.datastructure;

/**
 * Created by jerker on 2015-05-11.
 */
public class Heartbeat extends Data {

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
}

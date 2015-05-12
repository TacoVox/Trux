package se.gu.tux.trux.datastructure;

/**
 * Created by jerker on 2015-05-11.
 */
public class ArrayResponse extends Data {
    private Object[] content;

    public ArrayResponse(Object[] content) {
        this.content = content;
    }

    @Override
    public Object getValue() {
        return content;
    }

    public Object[] getArray() {
        return content;
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Object[]) {
            this.content = (Object[])value;
        }
    }

    @Override
    public boolean isOnServerSide() {
        return true;
    }
}

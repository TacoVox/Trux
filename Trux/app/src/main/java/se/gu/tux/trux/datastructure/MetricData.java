package se.gu.tux.trux.datastructure;

/**
 * Created by jonas on 3/24/15.
 */
public class MetricData extends Data
{
    /**
     * Private fields for value and the used timeframe
     */
    private Float value;
    private long tf;

    /**
     * Constructor.
     */
    public MetricData(long tf){
        this.tf = tf;
    }

    /**
     * Getter for the value
     * @return the value
     */
    @Override
    public Float getValue(){
        return value;
    }

    @Override
    public void setValue(Object value) { this.value = (Float) value; }


    /**
     * Getter for the used timeframe
     * @return the timeframe
     */
    public long getTimeFrame(){
        return tf;
    }

    public boolean isOnServerSide(){
        return tf != 0;
    }
}

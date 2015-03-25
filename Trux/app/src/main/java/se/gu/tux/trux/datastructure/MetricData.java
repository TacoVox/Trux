package se.gu.tux.trux.datastructure;

/**
 * Created by jonas on 3/24/15.
 */
public abstract class MetricData implements Data{
    /**
     * Private fields for value and the used timeframe
     */
    private int value;
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
    public Integer getValue(){
        return value;
    }

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

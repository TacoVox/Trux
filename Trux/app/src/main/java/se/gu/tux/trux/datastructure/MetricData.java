package se.gu.tux.trux.datastructure;

/**
 * Created by jonas on 3/24/15.
 */
public class MetricData extends Data
{
    /**
     * Private fields for value and the used timeframe
     */
    private Double value;
    private long tf;
    private static int signalId;

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
    public Double getValue(){
        return value;
    }

    @Override
    public void setValue(Object value) { this.value = (Double) value; }

    public static int getSignalId() {
        return signalId;
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

package se.gu.tux.trux.datastructure;

import android.swedspot.scs.data.SCSFloat;

/**
 * Created by jonas on 3/24/15.
 */
public class MetricData extends Data
{
    public final static int HOUR = 60 * 60 * 1000;
    public final static int DAY =  HOUR * 24;
    public final static int WEEK = DAY * 7;
    public final static int THIRTYDAYS = DAY * 30;

    /**
     * For the average or sum - should we sum it up over the whole time period or return results
     * for each day?
     * Note: for speed etc this will be average (per day or time period), for distance etc it will
     * be total distance for day or total time period.
     */
    public enum Mode {PER_DAY, TOTAL}

    /**
     * Private fields for value and the used timeframe
     */
    private Double value;
    private long tf;
    private Mode mode;

    /**
     * Constructor.
     */
    public MetricData(long tf){
        this.tf = tf;
    }

    public MetricData(long tf, Mode mode){
        this.tf = tf;
        this.mode = mode;
    }


    /**
     * Getter for the value
     * @return the value
     */
    @Override
    public Object getValue(){
        return value;
    }

    @Override
    public void setValue(Object value) {
        this.value = (Double) value;
    }

    public Integer getSignalId() {
        return 0;
    }

    /**
     * Getter for the used timeframe
     * @return the timeframe
     */
    public long getTimeFrame() {
        return tf;
    }

    public boolean isOnServerSide(){
        return tf != 0;
    }

    public boolean equals(Object o) {
        if (o instanceof MetricData) {
            return ((MetricData)o).getValue().equals(value);
        } else {
            return false;
        }
    }
}

package se.gu.tux.trux.datastructure;

import android.swedspot.automotiveapi.AutomotiveSignalId;

/**
 * Created by jonas on 3/24/15.
 */
public class Distance extends MetricData {
    public Distance(){ }
    public Distance(long tf){
        super(tf);
    }
    private Long distance;

    public Integer getSignalId() {
        return AutomotiveSignalId.FMS_HIGH_RESOLUTION_TOTAL_VEHICLE_DISTANCE;
    }

    @Override
    public void setValue (Object value) {
        distance = (Long) value;
    }

    public Object getValue() {
        return distance;
    }

    public boolean equals(Object o) {
        if (o instanceof Distance) {
            return ((Distance) o).getValue().equals(distance);
        } else {
            return false;
        }
    }
}

package se.gu.tux.trux.datastructure;

import android.swedspot.automotiveapi.AutomotiveSignalId;

/**
 * Created by jonas on 3/24/15.
 */
public class Distance extends MetricData {
    public Distance(long tf){
        super(tf);
    }
    private Long distance;

    public Integer getSignalId() {
        return AutomotiveSignalId.FMS_HIGH_RESOLUTION_TOTAL_VEHICLE_DISTANCE;
    }

    @Override
    public void setValue (Object value) {
        System.out.println("Setting value...");
        distance = (Long) value;
    }
}

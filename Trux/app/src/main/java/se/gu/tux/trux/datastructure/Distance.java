package se.gu.tux.trux.datastructure;

import android.swedspot.automotiveapi.AutomotiveSignalId;

/**
 * Created by jonas on 3/24/15.
 */
public class Distance extends MetricData {
    public Distance(long tf){
        super(tf);
    }

    public static int getSignalId() {
        return AutomotiveSignalId.FMS_HIGH_RESOLUTION_TOTAL_VEHICLE_DISTANCE;
    }
}

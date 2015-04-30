package se.gu.tux.trux.datastructure;

import android.swedspot.automotiveapi.AutomotiveSignalId;

/**
 * Created by jonas on 3/24/15.
 */
public class Speed extends MetricData {
    public Speed() { }
    public Speed(long tf){
        super(tf);
    }

    public Integer getSignalId() {
        return AutomotiveSignalId.FMS_WHEEL_BASED_SPEED;
    }
}

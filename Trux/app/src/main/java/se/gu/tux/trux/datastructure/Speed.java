package se.gu.tux.trux.datastructure;

import android.swedspot.automotiveapi.AutomotiveSignalId;

/**
 * Created by jonas on 3/24/15.
 */
public class Speed extends MetricData {
    public Speed(long tf){
        super(tf);
    }

    public static int getSignalId() {
        return AutomotiveSignalId.FMS_WHEEL_BASED_SPEED;
    }
}

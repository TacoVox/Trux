package se.gu.tux.trux.datastructure;

import android.swedspot.automotiveapi.AutomotiveSignalId;

/**
 * Created by jonas on 3/24/15.
 */
public class Fuel extends MetricData {
    public Fuel(long tf){
        super(tf);
    }

    public static int getSignalId() {
        return AutomotiveSignalId.FMS_INSTANTANEOUS_FUEL_ECONOMY;
    }
}

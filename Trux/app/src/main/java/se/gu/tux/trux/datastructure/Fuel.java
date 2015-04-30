package se.gu.tux.trux.datastructure;

import android.swedspot.automotiveapi.AutomotiveSignalId;

/**
 * Created by jonas on 3/24/15.
 */
public class Fuel extends MetricData {
    public Fuel() { }
    public Fuel(long tf){
        super(tf);
    }

    public Integer getSignalId() {
        return AutomotiveSignalId.FMS_FUEL_RATE;
    }
}

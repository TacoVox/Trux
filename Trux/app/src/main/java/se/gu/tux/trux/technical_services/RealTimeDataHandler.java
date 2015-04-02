package se.gu.tux.trux.technical_services;

/**
 * Created by ivryashkov on 2015-03-24.
 */

import android.swedspot.automotiveapi.AutomotiveSignalId;

import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.Distance;
import se.gu.tux.trux.datastructure.Fuel;
import se.gu.tux.trux.datastructure.MetricData;
import se.gu.tux.trux.datastructure.Speed;


/**
 *
 * @author ivryashkov
 */
public class RealTimeDataHandler
{

    RealTimeDataParser rtdp;

    /**
     * Constructor.
     */
    public RealTimeDataHandler()
    {
        rtdp = RealTimeDataParser.getInstance();
    }


    /**
     * Proposing this as a clean way to not have to have any logic around what metrics to use
     * in DataPoller. Instead here we decide what we see as relevant to send to the server
     * @return
     */
    public Data[] getCurrentMetrics() {
        Data metricArray[] = new Data[3];
        metricArray[0] = getSignalData(new Fuel(0));
        metricArray[1] = getSignalData(new Speed(0));
        metricArray[2] = getSignalData(new Distance(0));

        // Set timestamp for all data
        for (Data d : metricArray) {
            d.setTimeStamp(System.currentTimeMillis());
        }
        return metricArray;
    }


    public MetricData getSignalData(MetricData md) {
        md.setValue(rtdp.getValue(md.getSignalId()));
        return md;
    }






    public Data getSignalData(int automotiveSignalId)
    {
        switch (automotiveSignalId)
        {
            // speed signal case
            case AutomotiveSignalId.FMS_WHEEL_BASED_SPEED:

                Speed speed = new Speed(0);
                speed.setValue(rtdp.getValue(AutomotiveSignalId.FMS_WHEEL_BASED_SPEED));

                System.out.println("----------------------------------------------------");
                System.out.println("returning speed object from real-time data handler");
                System.out.println("object is null?: " + speed.equals(null));
                System.out.println("value: " + speed.getValue());
                System.out.println("----------------------------------------------------");

                return speed;
            
            // fuel signal case
            case AutomotiveSignalId.FMS_FUEL_RATE:

                Fuel fuel = new Fuel(0);
                fuel.setValue(rtdp.getValue(AutomotiveSignalId.FMS_FUEL_RATE));

                System.out.println("----------------------------------------------------");
                System.out.println("returning fuel object from real-time data handler");
                System.out.println("object is null?: " + fuel.equals(null));
                System.out.println("value: " + fuel.getValue());
                System.out.println("----------------------------------------------------");

                return fuel;

            default:
                return null;

        }

    } // end getSignalData()



} // end class RealTimeDataHandler

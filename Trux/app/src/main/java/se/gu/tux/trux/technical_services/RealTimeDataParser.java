package se.gu.tux.trux.technical_services;

import android.os.AsyncTask;
import android.swedspot.automotiveapi.AutomotiveSignal;
import android.swedspot.automotiveapi.AutomotiveSignalId;
import android.swedspot.scs.data.SCSFloat;
import android.swedspot.scs.data.SCSLong;

import com.swedspot.automotiveapi.AutomotiveFactory;
import com.swedspot.automotiveapi.AutomotiveListener;
import com.swedspot.vil.distraction.DriverDistractionLevel;
import com.swedspot.vil.distraction.DriverDistractionListener;
import com.swedspot.vil.distraction.LightMode;
import com.swedspot.vil.distraction.StealthMode;
import com.swedspot.vil.policy.AutomotiveCertificate;

import java.util.HashMap;

import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.Fuel;
import se.gu.tux.trux.datastructure.MetricData;

/**
 *
 * Handles the real-time data received from AGA.
 *
 * Created by ivryashkov on 2015-03-27.
 */
public class RealTimeDataParser
{

    private static RealTimeDataParser rtdp;

    private HashMap<Integer, MetricData> hashMap;


    static
    {
        if (rtdp == null)
        {
            rtdp = new RealTimeDataParser();
        }
    }


    /**
     * Returns an instance of RealTimeDataParser.
     *
     * @return      RealTimeDataParser instance
     */
    public static RealTimeDataParser getInstance()
    {
        return rtdp;

    } // end getInstance()



    /**
     * Constructor. Private, we keep an instance instead.
     */
    private RealTimeDataParser()
    {
        hashMap = new HashMap<>();
        init();
    }


    /**
     * Gets the value for a signal. Takes an Integer with the signal id
     * as parameter. Returns a Double with the value of the signal.
     *
     * @param automotiveSignalId    The signal id.
     * @return                      Double
     */
    public MetricData getValue(Integer automotiveSignalId)
    {
        // get the value for the specified signal and return it
        // do not remove it!
        MetricData value = hashMap.get(automotiveSignalId);
        System.out.println(hashMap.values());
        return value;
    }


    /**
     * Gets the data map for the registered signal. Returns a hash map with
     * the values.
     *
     * @return      HashMap
     */
    public HashMap<Integer, MetricData> getDataMap()
    {
        HashMap<Integer, MetricData> copy = new HashMap<>(hashMap);

        return copy;
    }


    /**
     * Initiliases the parser to listen for signals from AGA and store the values.
     */
    private void init()
    {
        new AsyncTask()
        {
            @Override
            protected Object doInBackground(Object[] objects)
            {
                AutomotiveFactory.createAutomotiveManagerInstance(
                        new AutomotiveCertificate(new byte[0]),
                        new AutomotiveListener()
                        {
                            @Override
                            public void receive(AutomotiveSignal automotiveSignal)
                            {
                                // get the signal id
                                Integer asId = automotiveSignal.getSignalId();
                                // get the signal value
                                Object value = automotiveSignal.getData();

                                // Store values wrapped in Double or Long java types...
                                if (value instanceof SCSFloat) {
                                    value = new Double(((Float)((SCSFloat) value).getFloatValue()).doubleValue());
                                } else if (value instanceof SCSLong) {
                                    value = new Long(((SCSLong) value).getLongValue());
                                }
                                MetricData f = new Fuel(0);
                                f.setValue(2.0);
                                // put into hash map
                                hashMap.put(asId, f);


                            } // end receive()

                            @Override
                            public void timeout(int i) {}

                            @Override
                            public void notAllowed(int i) {}
                        },
                        new DriverDistractionListener()
                        {
                            @Override
                            public void levelChanged(DriverDistractionLevel driverDistractionLevel) {}

                            @Override
                            public void lightModeChanged(LightMode lightMode) {}

                            @Override
                            public void stealthModeChanged(StealthMode stealthMode) {}
                        }

                ).register(AutomotiveSignalId.FMS_WHEEL_BASED_SPEED,
                        AutomotiveSignalId.FMS_FUEL_RATE,
                        AutomotiveSignalId.FMS_HIGH_RESOLUTION_TOTAL_VEHICLE_DISTANCE);

                return null;

            } // end doInBackground()

        }.execute();

    } // end init()


} // end class RealTimeDataParser

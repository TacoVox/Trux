package se.gu.tux.trux.technical_services;

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

/**
 * Created by ivryashkov on 2015-04-22.
 */
public class AGADataParser
{

    private static AGADataParser dataParser;

    private static HashMap<Integer, Object> dataMap;

    private Thread thread;
    private ParserRunnable runnable;


    private AGADataParser()
    {
        dataMap = new HashMap<>();
        initAGA();
    }


    private void init()
    {
        runnable = new ParserRunnable();
        thread = new Thread(runnable);
        thread.start();
    }


    private void initAGA()
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
                    if (value instanceof SCSFloat)
                    {
                        value = new Double(((Float) ((SCSFloat) value).getFloatValue()).doubleValue());
                    }
                    else if (value instanceof SCSLong)
                    {
                        value = new Long(((SCSLong) value).getLongValue());
                    }

                    // put into hash map
                    dataMap.put(asId, value);

                } // end receive()

                @Override
                public void timeout(int i)      {}

                @Override
                public void notAllowed(int i)   {}
            },
            new DriverDistractionListener()
            {
                @Override
                public void levelChanged(DriverDistractionLevel driverDistractionLevel) {}

                @Override
                public void lightModeChanged(LightMode lightMode)       {}

                @Override
                public void stealthModeChanged(StealthMode stealthMode) {}
            }

        ).register(AutomotiveSignalId.FMS_WHEEL_BASED_SPEED,
                AutomotiveSignalId.FMS_FUEL_RATE,
                AutomotiveSignalId.FMS_HIGH_RESOLUTION_TOTAL_VEHICLE_DISTANCE);

    } // end initAGA


    public static AGADataParser getInstance()
    {
        if (dataParser == null)
        {
            dataParser = new AGADataParser();
        }

        return dataParser;
    }


    public void stopParser()
    {
        runnable.stopRun();
    }


    /**
     * Gets the value for a signal. Takes an Integer with the signal id
     * as parameter. Returns a Double with the value of the signal.
     *
     * @param automotiveSignalId    The signal id.
     * @return                      Object
     */
    public Object getValue(Integer automotiveSignalId)
    {
        // get the value for the specified signal and return it
        // do not remove it!
        Object value = dataMap.get(automotiveSignalId);
        return value;
    }



    private class ParserRunnable implements Runnable
    {

        private volatile boolean isRunning;


        public ParserRunnable()
        {
            isRunning = true;
        }


        @Override
        public void run()
        {
            while (isRunning)
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
                                if (value instanceof SCSFloat)
                                {
                                    value = new Double(((Float) ((SCSFloat) value).getFloatValue()).doubleValue());
                                }
                                else if (value instanceof SCSLong)
                                {
                                    value = new Long(((SCSLong) value).getLongValue());
                                }

                                // put into hash map
                                dataMap.put(asId, value);

                            } // end receive()

                            @Override
                            public void timeout(int i)      {}

                            @Override
                            public void notAllowed(int i)   {}
                        },
                        new DriverDistractionListener()
                        {
                            @Override
                            public void levelChanged(DriverDistractionLevel driverDistractionLevel) {}

                            @Override
                            public void lightModeChanged(LightMode lightMode)       {}

                            @Override
                            public void stealthModeChanged(StealthMode stealthMode) {}
                        }

                ).register(AutomotiveSignalId.FMS_WHEEL_BASED_SPEED,
                        AutomotiveSignalId.FMS_FUEL_RATE,
                        AutomotiveSignalId.FMS_HIGH_RESOLUTION_TOTAL_VEHICLE_DISTANCE);

            } // end while

        } // end run()


        public void stopRun()
        {
            isRunning = false;
        }

    } // end inner class


} // end class

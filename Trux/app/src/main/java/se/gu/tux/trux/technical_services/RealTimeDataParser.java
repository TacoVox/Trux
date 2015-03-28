package se.gu.tux.trux.technical_services;

import android.os.AsyncTask;
import android.swedspot.automotiveapi.AutomotiveSignal;
import android.swedspot.automotiveapi.AutomotiveSignalId;
import android.swedspot.scs.data.SCSFloat;

import com.swedspot.automotiveapi.AutomotiveFactory;
import com.swedspot.automotiveapi.AutomotiveListener;
import com.swedspot.vil.distraction.DriverDistractionLevel;
import com.swedspot.vil.distraction.DriverDistractionListener;
import com.swedspot.vil.distraction.LightMode;
import com.swedspot.vil.distraction.StealthMode;
import com.swedspot.vil.policy.AutomotiveCertificate;

import java.util.HashMap;

import se.gu.tux.trux.datastructure.Data;

/**
 *
 * Handles the real-time data received from AGA.
 *
 * Created by ivryashkov on 2015-03-27.
 */
public class RealTimeDataParser
{

    private static RealTimeDataParser rtdp;

    private HashMap<Integer, Float> hashMap;


    static  { rtdp = new RealTimeDataParser(); }


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
     * as parameter. Returns a Float with the value of the signal.
     *
     * @param automotiveSignalId    The signal id.
     * @return                      Float
     */
    public Float getValue(Integer automotiveSignalId)
    {
        Float value = hashMap.remove(automotiveSignalId);

        System.out.println("------------------------------------------------");
        System.out.println("returning requested value from real-time parser");
        System.out.println("value: " + value);
        System.out.println("------------------------------------------------");

        return value;
    }


    /**
     * Gets the data map for the registered signal. Returns a hash map with
     * the values.
     *
     * @return      HashMap
     */
    public HashMap getDataMap()
    {
        HashMap<Integer, Float> copy = new HashMap<>(hashMap);

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
                                Integer asId = automotiveSignal.getSignalId();
                                Float value = ((SCSFloat) automotiveSignal.getData()).getFloatValue();

                                System.out.println("------------------------------------------------");
                                System.out.println("receiving signals from AGA...");
                                System.out.println("signal id: " + asId + ", value: " + value);
                                System.out.println("------------------------------------------------");

                                hashMap.put(asId, value);

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

                ).register(AutomotiveSignalId.FMS_WHEEL_BASED_SPEED);

                return null;

            } // end doInBackground()

        }.execute();

    } // end init()


} // end class RealTimeDataParser

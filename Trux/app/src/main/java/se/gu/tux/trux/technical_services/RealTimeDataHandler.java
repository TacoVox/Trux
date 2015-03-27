package se.gu.tux.trux.technical_services;

/**
 * Created by ivryashkov on 2015-03-24.
 */
import android.os.AsyncTask;
import android.swedspot.automotiveapi.AutomotiveSignal;
import android.swedspot.automotiveapi.AutomotiveSignalId;
import android.swedspot.scs.data.SCSDouble;
import android.swedspot.scs.data.SCSFloat;

import com.swedspot.automotiveapi.AutomotiveFactory;
import com.swedspot.automotiveapi.AutomotiveListener;
import com.swedspot.vil.distraction.DriverDistractionLevel;
import com.swedspot.vil.distraction.DriverDistractionListener;
import com.swedspot.vil.distraction.LightMode;
import com.swedspot.vil.distraction.StealthMode;
import com.swedspot.vil.policy.AutomotiveCertificate;


import se.gu.tux.trux.datastructure.Data;


/**
 *
 * @author ivryashkov
 */
public class RealTimeDataHandler
{

//    private AutomotiveManager manager;

    Data data;
    int automotiveSignalId;


    /**
     * Constructor.
     */
    public RealTimeDataHandler(int automotiveSignalId, Data data)
    {
        this.data = data;
        this.automotiveSignalId = automotiveSignalId;
        init();
    }


    private void init()
    {
        System.out.println("-------------------------------------------");
        System.out.println("in init() before async task call");
        System.out.println("-------------------------------------------");

        new AsyncTask()
        {
            @Override
            protected Object doInBackground(Object[] objects)
            {
                System.out.println("-------------------------------------------");
                System.out.println("in async task method");
                System.out.println("-------------------------------------------");

                AutomotiveFactory.createAutomotiveManagerInstance(
                        new AutomotiveCertificate(new byte[0]),
                        new AutomotiveListener()
                        {
                            @Override
                            public void receive(AutomotiveSignal automotiveSignal)
                            {
                                System.out.println("-------------------------------------------");
                                System.out.println("calling method receive() in listener class");
                                System.out.println("-------------------------------------------");

                                switch (automotiveSignal.getSignalId())
                                {
                                    case AutomotiveSignalId.FMS_WHEEL_BASED_SPEED:
                                        System.out.println("-------------------------------------------");
                                        System.out.println("setting value in data object");
                                        System.out.println("-------------------------------------------");

                                        Float value = ((SCSFloat) automotiveSignal.getData()).getFloatValue();
                                        data.setValue(value);

                                        System.out.println("-------------------------------------------");
                                        System.out.println("value set to: " + value);
                                        System.out.println("-------------------------------------------");

                                        break;

                                    default:
                                        System.out.println("-------------------------------------------");
                                        System.out.println("no value detected");
                                        System.out.println("-------------------------------------------");
                                        break;
                                }

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
                            public void lightModeChanged(LightMode lightMode)                       {}

                            @Override
                            public void stealthModeChanged(StealthMode stealthMode)                 {}
                        }

                ).register(AutomotiveSignalId.FMS_WHEEL_BASED_SPEED);

                return null;

            } // end doInBackground()

        }.execute();

    } // end init()


    /**
     * Gets the signal scsData for a specific signal. Takes a Data object as parameter.
     * Returns an Integer with the value of the signal.
     *
     * @return          Data
     */
    public Data getSignalData()     { return data; }


/*

    private class SignalListener implements AutomotiveListener
    {

        private int automotiveSignalId;

        public SignalListener(int automotiveSignalId)
        {
            this.automotiveSignalId = automotiveSignalId;
        }

        @Override
        public void receive(final AutomotiveSignal as)
        {
            System.out.println("-------------------------------------------");
            System.out.println("calling method receive() in listener class");
            System.out.println("-------------------------------------------");

            // store signals in local variables for access
            if (as.getSignalId() == automotiveSignalId)
            {
                System.out.println("--------------------------------------------");
                System.out.println("value is: " + ((SCSDouble) as.getData()).getDoubleValue());
                System.out.println("--------------------------------------------");

                data.setValue(((SCSDouble) as.getData()).getDoubleValue());
            }

        } // end receive()

        @Override
        public void timeout(int i) {}

        @Override
        public void notAllowed(int i) {}

    } // end class Listener



    private class DistractionLevel implements DriverDistractionListener
    {

        @Override
        public void levelChanged(DriverDistractionLevel ddl) {}

        @Override
        public void lightModeChanged(LightMode lm) {}

        @Override
        public void stealthModeChanged(StealthMode sm) {}

    } // end class DistractionLevel

*/


} // end class RealTimeDataHandler

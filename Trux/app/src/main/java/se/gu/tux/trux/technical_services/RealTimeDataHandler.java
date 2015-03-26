package se.gu.tux.trux.technical_services;

/**
 * Created by ivryashkov on 2015-03-24.
 */
import android.swedspot.automotiveapi.AutomotiveSignal;
import android.swedspot.scs.data.SCSData;
import android.swedspot.scs.data.SCSDouble;

import com.swedspot.automotiveapi.AutomotiveFactory;
import com.swedspot.automotiveapi.AutomotiveListener;
import com.swedspot.automotiveapi.AutomotiveManager;
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

    private AutomotiveManager manager;

    Data data;


    /**
     * Constructor.
     */
    public RealTimeDataHandler(int automotiveSignalId)
    {
        manager = AutomotiveFactory.createAutomotiveManagerInstance(new AutomotiveCertificate(new byte[0]),
                new SignalListener(automotiveSignalId), new DistractionLevel());
        manager.register(automotiveSignalId);

    }


    /**
     * Gets the signal scsData for a specific signal. Takes a Data object as parameter.
     * Returns an Integer with the value of the signal.
     *
     * @return          Data
     */
    public Data getSignalData()
    {

        return data;

    } // end getSignalData()


    /**
     * Inner class for listener.
     */
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
            // store signals in local variables for access
            if (as.getSignalId() == automotiveSignalId)
            {
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        data.setValue(((SCSDouble) as.getData()).getDoubleValue());
                    }
                };
            }

        } // end receive()

        @Override
        public void timeout(int i) {}

        @Override
        public void notAllowed(int i) {}

    } // end class Listener


    /**
     * Inner class for distraction level.
     */
    private class DistractionLevel implements DriverDistractionListener
    {

        @Override
        public void levelChanged(DriverDistractionLevel ddl) {}

        @Override
        public void lightModeChanged(LightMode lm) {}

        @Override
        public void stealthModeChanged(StealthMode sm) {}

    } // end class DistractionLevel



} // end class RealTimeDataHandler

package se.gu.tux.trux.technical_services;

/**
 * Created by ivryashkov on 2015-03-24.
 */
import android.swedspot.automotiveapi.AutomotiveSignal;
import android.swedspot.automotiveapi.AutomotiveSignalId;
import android.swedspot.scs.data.SCSInteger;
import com.swedspot.automotiveapi.AutomotiveFactory;
import com.swedspot.automotiveapi.AutomotiveListener;
import com.swedspot.automotiveapi.AutomotiveManager;
import com.swedspot.vil.distraction.DriverDistractionLevel;
import com.swedspot.vil.distraction.DriverDistractionListener;
import com.swedspot.vil.distraction.LightMode;
import com.swedspot.vil.distraction.StealthMode;
import com.swedspot.vil.policy.AutomotiveCertificate;





/**
 *
 * @author ivryashkov
 */
public class RealTimeConnector
{

    AutomotiveManager manager;

    SCSInteger fuel;
    SCSInteger speed;


    /**
     * Constructor.
     */
    public RealTimeConnector()
    {
        manager = AutomotiveFactory.createAutomotiveManagerInstance(new AutomotiveCertificate(new byte[0]),
                new SignalListener(), new DistractionLevel());
        manager.register(AutomotiveSignalId.FMS_FUEL_RATE, AutomotiveSignalId.FMS_WHEEL_BASED_SPEED);

    }


    /**
     * Gets the signal data for a specific signal. Takes a String with
     * the signal name as parameter.
     *
     * @param signal    The name of the signal.
     * @return          String
     */
    public int getSignalData(String signal)
    {
        switch (signal)
        {
            case "fuel":
                return fuel.getIntValue();

            case "speed":
                return speed.getIntValue();

            default:
                return -1;
        }

    } // end getSignalData()


    /**
     * Inner class for listener.
     */
    private class SignalListener implements AutomotiveListener
    {

        @Override
        public void receive(AutomotiveSignal as)
        {
            // store signals in local variables for access
            switch (as.getSignalId())
            {
                case AutomotiveSignalId.FMS_FUEL_RATE:
                    fuel = (SCSInteger) as.getData();
                    break;

                case AutomotiveSignalId.FMS_WHEEL_BASED_SPEED:
                    speed = (SCSInteger) as.getData();
                    break;

                default:
                    break;
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



} // end class RealTimeConnector

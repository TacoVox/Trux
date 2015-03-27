package se.gu.tux.trux.technical_services;

/**
 * Created by ivryashkov on 2015-03-24.
 */
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

import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.Speed;


/**
 *
 * @author ivryashkov
 */
public class RealTimeDataHandler
{

    int automotiveSignalId;

    Speed speed;


    /**
     * Constructor.
     */
    public RealTimeDataHandler(int automotiveSignalId)
    {
        speed = new Speed(0);
        this.automotiveSignalId = automotiveSignalId;
        init();
    }


    public Data getSignalData()
    {
        return speed;
    }


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
                                switch (automotiveSignal.getSignalId())
                                {
                                    case AutomotiveSignalId.FMS_WHEEL_BASED_SPEED:

                                        System.out.println("-------------------------------------------");
                                        System.out.println("in receive() in listener class");
                                        System.out.println("-------------------------------------------");

                                        Float value = ((SCSFloat) automotiveSignal.getData()).getFloatValue();

                                        speed.setValue(value);

                                        System.out.println("-------------------------------------------");
                                        System.out.println("value fetched: " + value);
                                        System.out.println("-------------------------------------------");

                                        break;

                                    default:
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


} // end class RealTimeDataHandler

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

    RealTimeDataParser rtdp;

    /**
     * Constructor.
     */
    public RealTimeDataHandler()
    {
        rtdp = RealTimeDataParser.getInstance();
    }


    public Data getSignalData(int automotiveSignalId)
    {


        switch (automotiveSignalId)
        {
            case AutomotiveSignalId.FMS_WHEEL_BASED_SPEED:

                Speed speed = new Speed(0);
                speed.setValue(rtdp.getValue(AutomotiveSignalId.FMS_WHEEL_BASED_SPEED));

                System.out.println("----------------------------------------------------");
                System.out.println("returning speed object from real-time data handler");
                System.out.println("object is null?: " + speed.equals(null));
                System.out.println("value: " + speed.getValue());
                System.out.println("----------------------------------------------------");

                return speed;

            default:
                return null;

        }

    } // end getSignalData()



} // end class RealTimeDataHandler

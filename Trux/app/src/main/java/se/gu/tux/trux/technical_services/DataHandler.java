package se.gu.tux.trux.technical_services;

import android.swedspot.scs.data.SCSData;
import android.widget.TextView;

import se.gu.tux.trux.datastructure.Data;

/**
 * Created by ivryashkov on 2015-03-25.
 */
public class DataHandler
{

    private static DataHandler dataHandler;

    private RealTimeDataHandler realTimeDataHandler;



    /**
     * Constructor. Declared private and not instantiated. We keep an
     * instance of DataHandler instead.
     */
    private DataHandler()    {}


    /**
     * Returns an instance of the DataHandler object.
     *
     * @return      instance of DataHandler
     */
    public static DataHandler getInstance()
    {
        if (dataHandler == null)
        {
            dataHandler = new DataHandler();
        }

        return dataHandler;

    } // end getInstance()



    public Data signalIn(int automotiveSignalId, boolean isOnServerSide)
    {
        Data data = null;

        if (isOnServerSide)
        {

        }
        else
        {
            realTimeDataHandler = new RealTimeDataHandler();

            System.out.println("----------------------------------------------------");
            System.out.println("returning speed object from data handler");
            System.out.println("----------------------------------------------------");

            data = realTimeDataHandler.getSignalData(automotiveSignalId);
        }

        return data;

    } // end signalIn()



} // end class DataHandler

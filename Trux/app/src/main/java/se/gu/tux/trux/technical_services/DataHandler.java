package se.gu.tux.trux.technical_services;

import android.swedspot.scs.data.SCSData;

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


    /**
     * Gets the scsData for the signal. Takes a Data object as parameter. Returns the scsData value
     * of the signal, -1 otherwise.
     *
     * @param automotiveSignalId        The signal id to listen to.
     * @return                          Data object
     */
    public Data signalIn(int automotiveSignalId, Data data)
    {
        if (!data.isOnServerSide())
        {
            realTimeDataHandler = new RealTimeDataHandler(automotiveSignalId, data);
            return realTimeDataHandler.getSignalData();
        }
        else
        {

        }

        return null;

    } // end signalIn()



} // end class DataHandler

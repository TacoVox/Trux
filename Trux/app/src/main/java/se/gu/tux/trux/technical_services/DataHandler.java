package se.gu.tux.trux.technical_services;

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
     * @param isOnServerSide            Is the signal on the server side or not?
     * @return                          Data object
     */
    public Data signalIn(int automotiveSignalId, boolean isOnServerSide)
    {
        if (isOnServerSide)
        {
            realTimeDataHandler = new RealTimeDataHandler(automotiveSignalId);
            return realTimeDataHandler.getSignalData();
        }
        else
        {

        }

        return null;

    } // end signalIn()



} // end class DataHandler

package se.gu.tux.trux.technical_services;

import se.gu.tux.trux.datastructure.Data;

/**
 * Created by ivryashkov on 2015-03-25.
 */
public class DataController
{

    private DataController data_instance;

    private RealTimeConnector rt_conn;



    /**
     * Constructor. Declared private and not instantiated. We keep an
     * instance of DataController instead.
     */
    private DataController()    {}


    /**
     * Returns an instance of the DataController object.
     *
     * @return      instance of DataController
     */
    public DataController getInstance()
    {
        if (data_instance == null)
        {
            data_instance = new DataController();
        }

        return data_instance;

    } // end getInstance()


    /**
     * Gets the data for the signal. Takes a Data object as parameter. Returns the data value
     * of the signal, -1 otherwise.
     *
     * @param signalIn      The Data object.
     * @return              Data object
     */
    public Data signalIn(Data signalIn)
    {
        if (signalIn.isOnServerSide())
        {
            rt_conn = new RealTimeConnector();
            return rt_conn.getSignalData(signalIn);
        }
        else
        {

        }

        return null;

    } // end signalIn()



} // end class DataController

package se.gu.tux.trux.appplication;

import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.MetricData;
import se.gu.tux.trux.technical_services.IServerConnector;
import se.gu.tux.trux.technical_services.RealTimeDataHandler;
import se.gu.tux.trux.technical_services.ServerConnector;

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
    public synchronized static DataHandler getInstance()
    {
        if (dataHandler == null)
        {
            dataHandler = new DataHandler();
        }

        return dataHandler;

    } // end getInstance()

    /**
     * Ivo, this is how i envisioned it - ask for data, get data.
     * So we don't need to keep logic about what signal id is needed everywhere.
     * Each metric datatype knows about their own signal id.
     * Using class can just do Data d = [...].getData(new Speed(0)); for example.'
     * I think this is a neat way for the GUI to get data
     * @param request
     * @return
     */
    public Data getData(Data request) {
        if (request.isOnServerSide()) {

            request = ServerConnector.gI().answerQuery(request);

            //request = IServerConnector.getInstance().answerQuery(request);

        } else {

            if (request instanceof MetricData){
                // Ask the real time data handler
                realTimeDataHandler = new RealTimeDataHandler();
                System.out.println("----------------------------------------------------");
                System.out.println("returning metric object from data handler");
                System.out.println("----------------------------------------------------");
                request = realTimeDataHandler.getSignalData(((MetricData)request));
            }
        }
        return request;
    }



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

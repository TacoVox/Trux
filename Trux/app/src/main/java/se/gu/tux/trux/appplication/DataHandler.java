package se.gu.tux.trux.appplication;

import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.MetricData;
import se.gu.tux.trux.datastructure.User;
import se.gu.tux.trux.technical_services.IServerConnector;
import se.gu.tux.trux.technical_services.RealTimeDataHandler;
import se.gu.tux.trux.technical_services.ServerConnector;

// TODO: possibly add a session handler class or whatever BUT anyway - it would be nice
// to have a centralized method so we only implement once the logic to decide whether
// we are logged in or not - right now in serverconnector we have this check:
/*
if (DataHandler.getInstance().getUser() != null &&
    (DataHandler.getInstance().getUser().getSessionId() == -1 ||
    DataHandler.getInstance().getUser().getUserId() == 0)) {
    System.out.println("Want to send queued data but is not logged in. Sleeping...");
    Thread.sleep(10000);
    }
*/

/**
 * Created by ivryashkov on 2015-03-25.
 */
public class DataHandler
{

    private static DataHandler dataHandler;

    private RealTimeDataHandler realTimeDataHandler;

    private User user;

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


    public void setUser(User user)
    {
        this.user = user;
    }


    public User getUser()
    {
        return user;
    }


} // end class DataHandler

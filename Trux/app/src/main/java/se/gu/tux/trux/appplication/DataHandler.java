package se.gu.tux.trux.appplication;

import java.util.Calendar;
import java.util.GregorianCalendar;

import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.MetricData;
import se.gu.tux.trux.datastructure.User;
import se.gu.tux.trux.technical_services.IServerConnector;
import se.gu.tux.trux.technical_services.NotLoggedInException;
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
    private DataHandler()    {
        realTimeDataHandler = new RealTimeDataHandler();
    }


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
     * Returns true if the user is logged in.
     * @return
     */
    public boolean isLoggedIn() {
        boolean isLoggedIn = false;

        if (user != null && user.getUserId() > 0 && user.getSessionId() > 0) {
            return isLoggedIn = true;
        }

        return isLoggedIn;
    }

    /**
     * Each metric datatype knows about their own signal id.
     * Using class can just do Data d = [...].getData(new Speed(0)); for example.'
     * I think this is a neat way for the GUI to get data
     * @param request
     * @return
     */
    public Data getData(Data request) throws NotLoggedInException {
        if (request.isOnServerSide()) {

            request = ServerConnector.gI().answerQuery(request);

            //request = IServerConnector.getInstance().answerQuery(request);

        } else {

            if (request instanceof MetricData){
                // Ask the real time data handler
                request = realTimeDataHandler.getSignalData(((MetricData) request));
            }
        }
        return request;
    }

    /**
     * Please provide a metric object of the right class and with the current timestamp
     * set.
     */
    public Data[] getPerDay (MetricData metricData, int days) throws NotLoggedInException {
        Data[] perDay = new Data[days];

        // Use a calendar to know when days start and end.
        // Initiate based on metricData's timestamp.
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(metricData.getTimeStamp());
        cal.add(Calendar.DATE, -days);

        // Here fetch metric data for each day, by setting timestamp to the end of the day
        // AND the timeframe to be one day : ))
        metricData.settTimeFrame(MetricData.DAY);

        for(int i = 0; i < days; i++) {
            /* GregorianCalendar calBeginning = new GregorianCalendar(
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE),
                    0, 0
            );*/
            // Find timestamp for end of day
            GregorianCalendar calEnd = new GregorianCalendar(
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE),
                    23, 59, 59
            );
            metricData.setTimeStamp(calEnd.getTimeInMillis());

            // Get data from server
            perDay[i] = getData(metricData);

            // Move forward one day
            cal.add(Calendar.DATE, +i);
        }

        return perDay;
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

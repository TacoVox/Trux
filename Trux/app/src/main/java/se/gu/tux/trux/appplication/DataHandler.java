package se.gu.tux.trux.appplication;

import com.jjoe64.graphview.series.DataPoint;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.Distance;
import se.gu.tux.trux.datastructure.Fuel;
import se.gu.tux.trux.datastructure.MetricData;
import se.gu.tux.trux.datastructure.Speed;
import se.gu.tux.trux.datastructure.User;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import se.gu.tux.trux.technical_services.RealTimeDataHandler;
import se.gu.tux.trux.technical_services.ServerConnector;



/**
 * Created by ivryashkov on 2015-03-25.
 */
public class DataHandler
{

    private static DataHandler dataHandler;

    private RealTimeDataHandler realTimeDataHandler;

    private User user;

    // Stores detailed stats with signal id as key
    private volatile HashMap<Integer, DetailedStatsBundle> detailedStats;
    // Stores time stamp
    private long detailedStatsFetched = 0;


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
     * Start fetching the stats - this method creates its own background thread for this.
     * Keep them cached for 15 minutes (hard-coding this for now)
     */
    public void cacheDetailedStats() {
        // Only fetch if they aren't there or aren't up to date
        if (detailedStats != null ||
                System.currentTimeMillis() - detailedStatsFetched > 1000 * 60 * 15) {

            detailedStats = new HashMap<Integer, DetailedStatsBundle>();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // NOTE would love to generalize this but slightly unsure on now how to
                        // handle the casting

                        // Create speed object and mark it with current time.
                        // Then request an array of speed average values for last 30 days.
                        Speed speed = new Speed(0);
                        speed.setTimeStamp(System.currentTimeMillis());
                        Data[] avgSpeedPerDay = DataHandler.getInstance().getPerDay(speed, 30);
                        DataPoint[] speedPoints = getDataPoints(avgSpeedPerDay, speed);
                        // Create bundle object
                        DetailedStatsBundle speedBundle = new DetailedStatsBundle(
                                (Speed) DataHandler.getInstance().getData(new Speed(MetricData.DAY)),
                                (Speed) DataHandler.getInstance().getData(new Speed(MetricData.WEEK)),
                                (Speed) DataHandler.getInstance().getData(new Speed(MetricData.THIRTYDAYS)),
                                (Speed) DataHandler.getInstance().getData(new Speed(MetricData.FOREVER)),
                                speedPoints);
                        // Store in hash map
                        detailedStats.put(speed.getSignalId(), speedBundle);

                        Fuel fuel = new Fuel(0);
                        fuel.setTimeStamp(System.currentTimeMillis());
                        Data[] avgFuelPerDay = DataHandler.getInstance().getPerDay(fuel, 30);
                        DataPoint[] fuelPoints = getDataPoints(avgFuelPerDay, fuel);
                        // Create bundle object
                        DetailedStatsBundle fuelBundle = new DetailedStatsBundle(
                                (Fuel) DataHandler.getInstance().getData(new Fuel(MetricData.DAY)),
                                (Fuel) DataHandler.getInstance().getData(new Fuel(MetricData.WEEK)),
                                (Fuel) DataHandler.getInstance().getData(new Fuel(MetricData.THIRTYDAYS)),
                                (Fuel) DataHandler.getInstance().getData(new Fuel(MetricData.FOREVER)),
                                fuelPoints);
                        // Store in hash map
                        detailedStats.put(fuel.getSignalId(), fuelBundle);

                        Distance dist = new Distance(0);
                        dist.setTimeStamp(System.currentTimeMillis());
                        Data[] avgDistancePerDay = DataHandler.getInstance().getPerDay(dist, 30);
                        DataPoint[] distPoints = getDataPoints(avgDistancePerDay, dist);
                        // Create bundle object
                        DetailedStatsBundle distBundle = new DetailedStatsBundle(
                                (Distance) DataHandler.getInstance().getData(new Distance(MetricData.DAY)),
                                (Distance) DataHandler.getInstance().getData(new Distance(MetricData.WEEK)),
                                (Distance) DataHandler.getInstance().getData(new Distance(MetricData.THIRTYDAYS)),
                                (Distance) DataHandler.getInstance().getData(new Distance(MetricData.FOREVER)),
                                distPoints);
                        // Store in hash map
                        detailedStats.put(dist.getSignalId(), distBundle);

                        // Keep track of when we finished fetching the detailed stats
                        detailedStatsFetched = System.currentTimeMillis();

                    } catch (NotLoggedInException e) {

                    }
                }
            }).start();
        }
    }

    /**
     * Returns true if there are cached detailed stats of the same type as md.
     * @param md
     * @return  boolean representing if there are cached stats or not.
     */
    public boolean detailedStatsReady(MetricData md) {
        // See if there is a stats object in the hashmap
        if (detailedStats != null && detailedStats.get(md.getSignalId()) != null) {
            return true;
        }
        return false;
    }


    /**
     * Returns detailed stats with the same type as md.
     * @param md
     * @return Detailed stats packed into a DetailedStatsBundle object.
     */
    public DetailedStatsBundle getDetailedStats(MetricData md) {
        DetailedStatsBundle dStats = null;

        // See if the stats are ready
        if (detailedStatsReady(md)) {
            dStats = detailedStats.get(md.getSignalId());
        }

        return dStats;
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
        //cal.setTimeInMillis(metricData.getTimeStamp());
        cal.add(Calendar.DATE, -(days - 1));

        // Here fetch metric data for each day, by setting timestamp to the end of the day
        // AND the timeframe to be one day : ))
        metricData.settTimeFrame(MetricData.DAY);

        for(int i = 0; i < days; i++) {
            MetricData md = metricData;
            try {
                md = metricData.getClass().newInstance();
                md.settTimeFrame(MetricData.DAY);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            /* GregorianCalendar calBeginning = new GregorianCalendar(
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE),
                    0, 0
            );*/


            // Find timestamp for end of day
            GregorianCalendar calEnd = new GregorianCalendar(
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE),
                    23, 59, 59
            );
            /*System.out.println(calEnd.getTimeInMillis());
            md.setTimeStamp(calEnd.getTimeInMillis());

            TEST:::: */
            md.settTimeFrame(MetricData.DAY);
            md.setTimeStamp(calEnd.getTimeInMillis());


            // Get data from server
            perDay[i] = ServerConnector.gI().answerTimestampedQuery(md);
            System.out.println("RESPONSE DATA: " + perDay[i].getValue());
            // Move forward one day
            cal.add(Calendar.DATE, +1);
        }

        return perDay;
    }





    public DataPoint[] getDataPoints(Data[] data, MetricData md) {

        DataPoint[] dataPoints = new DataPoint[30];
        for (int i = 0; i < 30; i++) {
            if (data[i].getValue() == null) {
                System.out.println("Assuming 0 at null value at pos: " + i );
                dataPoints[i] = new DataPoint(i + 1, 0);
            } else {
                if (md instanceof Speed || md instanceof Fuel) {
                    dataPoints[i] = new DataPoint(i + 1, (Double)(data[i]).getValue());
                    System.out.println("ddddd: " + data[i].getValue());
                } else {
                    dataPoints[i] = new DataPoint(i + 1, (Long)(data[i]).getValue());
                }
            }
        }
        return dataPoints;
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

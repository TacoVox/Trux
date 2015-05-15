package se.gu.tux.trux.application;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.jjoe64.graphview.series.DataPoint;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.Distance;
import se.gu.tux.trux.datastructure.Friend;
import se.gu.tux.trux.datastructure.Fuel;
import se.gu.tux.trux.datastructure.MetricData;
import se.gu.tux.trux.datastructure.Notification;
import se.gu.tux.trux.datastructure.Picture;
import se.gu.tux.trux.datastructure.ProtocolMessage;
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
    private SocialHandler sc;

    private volatile User user;
    private volatile Notification notificationStatus;

    // Stores detailed stats with signal id as key
    private volatile HashMap<Integer, DetailedStatsBundle> detailedStats;
    // Stores detailed stats with signal id as key
    private volatile HashMap<Long, Picture> imageCache;
    // Stores time stamp
    private volatile long detailedStatsFetched = 0;


    /**
     * Constructor. Declared private and not instantiated. We keep an
     * instance of DataHandler instead.
     */
    private DataHandler()    {
        sc = new SocialHandler();
    }


    /**
     * Returns an instance of the DataHandler object.
     * Note, removed synchronized on this while debugging stats data fetching.
     * Later realized that maybe we can use double check locking just for the instantiation
     *
     * @return      instance of DataHandler
     */
    public static DataHandler getInstance()
    {
        // YES, there should be two if checks
        if (dataHandler == null)
        {
            synchronized (DataHandler.class) {
                if (dataHandler == null) {
                    dataHandler = new DataHandler();
                }
            }

        }
        return dataHandler;
    } // end getInstance()

    public static DataHandler gI() {
        return getInstance();
    }


    public void setRealTimeDataHandler(RealTimeDataHandler rtdh) {
        realTimeDataHandler = rtdh;
    }


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
            System.out.println("Datahandler routing request: " + request.getClass().getSimpleName());
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
        if (detailedStats == null ||
                System.currentTimeMillis() - detailedStatsFetched > 1000 * 60 * 15) {

            detailedStats = new HashMap<Integer, DetailedStatsBundle>();

            System.out.println("Fetching detailed stats.");

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
                        Data[] avgSpeedPerDay = getPerDay(speed, 30);
                        DataPoint[] speedPoints = getDataPoints(avgSpeedPerDay, speed);
                        // Create bundle object
                        DetailedStatsBundle speedBundle = new DetailedStatsBundle(
                                (Speed) getData(new Speed(MetricData.DAY)),
                                (Speed) getData(new Speed(MetricData.WEEK)),
                                (Speed) getData(new Speed(MetricData.THIRTYDAYS)),
                                (Speed) getData(new Speed(MetricData.FOREVER)),
                                speedPoints);
                        // Store in hash map
                        detailedStats.put(speed.getSignalId(), speedBundle);

                        Fuel fuel = new Fuel(0);
                        fuel.setTimeStamp(System.currentTimeMillis());
                        Data[] avgFuelPerDay = getPerDay(fuel, 30);
                        DataPoint[] fuelPoints = getDataPoints(avgFuelPerDay, fuel);
                        // Create bundle object
                        DetailedStatsBundle fuelBundle = new DetailedStatsBundle(
                                (Fuel) getData(new Fuel(MetricData.DAY)),
                                (Fuel) getData(new Fuel(MetricData.WEEK)),
                                (Fuel) getData(new Fuel(MetricData.THIRTYDAYS)),
                                (Fuel) getData(new Fuel(MetricData.FOREVER)),
                                fuelPoints);
                        // Store in hash map
                        detailedStats.put(fuel.getSignalId(), fuelBundle);

                        Distance dist = new Distance(0);
                        dist.setTimeStamp(System.currentTimeMillis());
                        Data[] avgDistancePerDay = getPerDay(dist, 30);
                        DataPoint[] distPoints = getDataPoints(avgDistancePerDay, dist);
                        // Create bundle object
                        DetailedStatsBundle distBundle = new DetailedStatsBundle(
                                (Distance) getData(new Distance(MetricData.DAY)),
                                (Distance) getData(new Distance(MetricData.WEEK)),
                                (Distance) getData(new Distance(MetricData.THIRTYDAYS)),
                                (Distance) getData(new Distance(MetricData.FOREVER)),
                                distPoints);
                        // Store in hash map
                        detailedStats.put(dist.getSignalId(), distBundle);

                        // Keep track of when we finished fetching the detailed stats
                        detailedStatsFetched = System.currentTimeMillis();

                    } catch (NotLoggedInException e) {
                        System.out.println("Not logged in in datahandler cache");
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
            //System.out.println("Stats were ready: " + md.getClass().getSimpleName());
            return true;
        }
        //System.out.println("Stats were not ready: " + md.getClass().getSimpleName());
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
            //System.out.println("Returning stats.");
            dStats = detailedStats.get(md.getSignalId());
        } else {
            //System.out.println("Returning null.");
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
            // It seems we cannot send the exact same instance - guess some low-level details of java
            // will cache it or whatever? Need to create a new one
            MetricData md = metricData;
            try {
                md = metricData.getClass().newInstance();
                md.settTimeFrame(MetricData.DAY);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            // Find timestamp for end of day
            GregorianCalendar calEnd = new GregorianCalendar(
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE),
                    23, 59, 59
            );

            md.settTimeFrame(MetricData.DAY);
            md.setTimeStamp(calEnd.getTimeInMillis());

            // Get data from server
            perDay[i] = ServerConnector.gI().answerTimestampedQuery(md);

            // Move forward one day
            cal.add(Calendar.DATE, +1);

            // Let this thread sleep for a slight while just to make other threads able to
            // use the ServerConnector inbetween - since this is background prefetching
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return perDay;
    }


    public DataPoint[] getDataPoints(Data[] data, MetricData md) {

        DataPoint[] dataPoints = new DataPoint[30];
        for (int i = 0; i < 30; i++) {
            if (data[i].getValue() == null) {
                dataPoints[i] = new DataPoint(i + 1, new Double(0.0));
            } else {
                if (md instanceof Speed || md instanceof Fuel) {
                    dataPoints[i] = new DataPoint(i + 1, (Double)data[i].getValue());
                } else if (md instanceof Distance ){
                    // Distance are in m so divide by 1000 to get some more reasonable values
                    if ((Long)data[i].getValue() == 0) {
                        dataPoints[i] = new DataPoint(i + 1, new Double(0.0));
                    } else {
                        dataPoints[i] = new DataPoint(i + 1, new Double((Long)data[i].getValue() / 1000));
                    }
                    //System.out.println("datapoint " + i + ": " + dataPoints[i].getY());
                    //dataPoints[i] = new DataPoint(i + 1, new Double(i));
                    //System.out.println("datapoint " + i + ": " + dataPoints[i].getY());
                }
            }
        }
        return dataPoints;
    }

    public void setUser(User user)
    {
        if (user == null || this.user != user) {
            // The user has changed or been removed due to logout, so cleanup the session data
            cleanupSessionData();
        }
        this.user = user;
    }

    public User getUser()
    {
        return user;
    }

    public void cleanupSessionData() {
        detailedStats = null;
        detailedStatsFetched = 0;
    }

    public Friend[] getFriends() throws NotLoggedInException {
        Friend[] friends = null;

        // Make sure we are logged in so we have a user
        if (!isLoggedIn()) {
            throw new NotLoggedInException();
        }

        // No friends / friends not set
        if (user.getFriends() == null) {
            System.out.println("Users friends was null.");
            return null;
        }

        // Copy the array so we are sure no other thread messes with it during fetch
        long[] friendIds = Arrays.copyOf(user.getFriends(), user.getFriends().length);
        if (friendIds != null) {
            friends =  new Friend[friendIds.length];
            for (int i = 0; i < friendIds.length; i++) {
                Friend queryFriend = new Friend(friendIds[i]);
                Data d = getData(queryFriend);
				if (d instanceof Friend) {
					friends[i] = (Friend)d;
				} else if (d instanceof ProtocolMessage) {
					System.out.println("Friend fetch: " + ((ProtocolMessage)d).getMessage());
				}
			}
   	     }

        return friends;
    }


    /**
     * Get the requested picture as a Bitmap object.
     * @param pictureId
     * @return
     * @throws NotLoggedInException
     */
    public Bitmap getPicture(Long pictureId) throws NotLoggedInException {
        if (imageCache == null) {
            imageCache = new HashMap<Long, Picture>();
        }
        if (pictureId == -1) {
            return null;
        }

        // Empty cache if it is really big
        if (imageCache.size() > 500) {
            imageCache.clear();
        }

        // See if the image is not yet cached
        if (imageCache.get(pictureId) == null) {
            // Try to fecth it
            imageCache.put(pictureId, (Picture)getData(new Picture(pictureId)));
        }

        Picture p = imageCache.get(pictureId);

        // Now we have the picture - convert it to a bitmap so it can be used in the app
        Bitmap bmp = null;
        if (p != null && p.getImg() != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            bmp = BitmapFactory.decodeByteArray(p.getImg(), 0,
                    p.getImg().length, options);
        }

        return bmp;
    }

    public Notification getNotificationStatus() {
        return notificationStatus;
    }

    public void setNotificationStatus(Notification notificationStatus) {
        this.notificationStatus = notificationStatus;
    }

    public SocialHandler getSocialHandler() {
        return sc;
    }
} // end class DataHandler

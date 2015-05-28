package se.gu.tux.trux.application;


import android.content.SharedPreferences;

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
import se.gu.tux.trux.technical_services.AGADataParser;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import se.gu.tux.trux.technical_services.RealTimeDataHandler;
import se.gu.tux.trux.technical_services.ServerConnector;



/**
 * This singleton class routes data requests betweeen the GUI and the technical services.
 * The getData method is central for this.
 * The class also handles caching of detailed stats. Caching of friends is handled by a SocialHandler
 * available via a helper method from the instance of this class.
 * The state of the current logged in user is also stored by this class.
 *
 * TODO: The detailed stats caching could very well be put in a helper class as well.
 *
 *
 *
 *
 * TODO
 * Refactor class and xml names
 * Timer in homeactivity **** major *****
 * Check if AGA reconnection works **** major *****
 * Make a toast if a followed user goes offline
 * reduce map functionality  **** major *****
 * report if any major changes from today until the final submission
 * check font size and buttons
 */
public class DataHandler
{
    public enum SafetyStatus {IDLE, SLOW_MOVING, MOVING, FAST_MOVING};

    // Singleton instance
    private static DataHandler instance;

    // Helper handlers
    private RealTimeDataHandler realTimeDataHandler;
    private SocialHandler socialHandler;

    // The logged in user and current notification status
    private volatile User user;
    private volatile Notification notificationStatus;

    // Stores detailed stats with signal id as key
    private volatile HashMap<Integer, DetailedStatsBundle> detailedStats;
    // Stores time stamp
    private volatile long detailedStatsFetched = 0;


    /**
     * Private constructor. Only called once from getInstance().
     */
    private DataHandler()    {
        detailedStats = new HashMap<Integer, DetailedStatsBundle>();
        socialHandler = new SocialHandler();
    }


    /**
     * Returns an instance of the DataHandler object.
     *
     * @return      The instance of DataHandler.
     */
    public static DataHandler getInstance()
    {
        // Double checked locking
        if (instance == null)
        {
            synchronized (DataHandler.class) {
                if (instance == null) {
                    instance = new DataHandler();
                }
            }

        }
        return instance;
    }


    /**
     * Shorter method name, calls getInstance().
     *
     * @return  The instance of DataHandler.
     */
    public static DataHandler gI() {
        return getInstance();
    }

    public static void setInstance(DataHandler newInstance) {
        instance = newInstance;
    }


    /**
     * Returns true if a user is logged in.
     * @return
     */
    public boolean isLoggedIn() {
        boolean isLoggedIn = false;

        if (user != null && user.getUserId() > 0 && user.getSessionId() > 0) {
            isLoggedIn = true;
        }

        return isLoggedIn;
    }


    /**
     * Main routing method for requesting and sending data. For social data, sending an empty object
     * with just the id set will generally return an object with full data from the server.
     * For metric data, each metric datatype knows about their own signal id.
     * getData(new Speed(0)) where 0 is the timesframe ill return the CURRENT speeed, setting a
     * longer timeframe will send the request to the server to calculate average or total depending
     * on the datatype.
     *
     * Make sure to call this in a background thread if it will forward anything to the server.
     *
     * Also be sure to handle the NotLoggedInException - which is hard if you're in a runnable so
     * TODO: the best thing would probably be to return a new datatype like NotLoggedIn object
     * instead, and be sure to check before casting.
     *
     * @param request
     * @return
     */
    public Data getData(Data request) throws NotLoggedInException {
        if (request.isOnServerSide()) {

            //System.out.println("Datahandler routing request: " + request.getClass().getSimpleName());
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
     * Starts fetching the detailed stats - this method creates its own background thread for this.
     * Keep them cached for 15 minutes (currently hard-coded)
     */
    public void cacheDetailedStats() {
        // Only fetch if they aren't there or aren't up to date
        if (detailedStats == null || System.currentTimeMillis() - detailedStatsFetched >
                1000 * 60 * 15) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // NOTE would love to generalize this but slightly unsure on now how to
                        // handle the casting.

                        // Initialize hash map if necessary, else clear it
                        if (detailedStats == null) {
                            detailedStats = new HashMap<Integer, DetailedStatsBundle>();
                        } else {
                            detailedStats.clear();
                        }

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
     * Returns true if there are cached detailed stats cached of the same type as md.
     *
     * @param md    A MetricData object of the type the query concerns
     * @return      A boolean representing if there are cached stats available or not.
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
     * @param md    A MetricData object of the type the query concerns
     * @return      Detailed stats packed into a DetailedStatsBundle object.
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
     * Returns stats per day for the requested amount of days - for Distance this will be the total
     * per day, for others it will be the average per day.
     * @param metricData    A MetricData object of the type the query concerns
     * @param days          The amount of days from now and backwards.
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

            // Let this thread sleep for a slight while just to make it very clear that this is
            // a low-priority thread compared to other features needing to use the ServerConnector
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return perDay;
    }


    /**
     * Converts an array of Data[] into an array of DataPoints, intended for using when filling the
     * diagrams showing the detailed stats.
     * @param data  An array of Data (that we assume is MetricData).
     * @param md    A MetricData object of the type the query concerns.
     * @return      An array of DataPoints that can be used to initialize a diagram.
     */
    public DataPoint[] getDataPoints(Data[] data, MetricData md) {

        DataPoint[] dataPoints = new DataPoint[30];
        for (int i = 0; i < 30; i++) {
            if (data[i].getValue() == null) {
                dataPoints[i] = new DataPoint(i + 1, new Double(0.0));
            } else {
                if (md instanceof Speed || md instanceof Fuel) {

                    dataPoints[i] = new DataPoint(i + 1, (Double)data[i].getValue());

                } else if (md instanceof Distance ){

                    // Distance are in m so divide by 1000 to get some more reasonable values (km)
                    if ((Long)data[i].getValue() == 0) {
                        dataPoints[i] = new DataPoint(i + 1, new Double(0.0));
                    } else {
                        dataPoints[i] = new DataPoint(i + 1,
                                new Double((Long)data[i].getValue() / 1000));
                    }
                }
            }
        }
        return dataPoints;
    }


    /**
     * Set the currently logged in user. If the user id changes, we make sure to clear the session
     * data.
     * @param user  The currently logged in user.
     */
    public void setUser(User user)
    {
        if (user == null || (this.user != null && this.user.getUserId() != user.getUserId())) {
            // The user has changed or been removed due to logout, so cleanup the session data
            cleanupSessionData();
        }
        this.user = user;
    }


    /**
     * Returns the currently logged in user (or null, if noone is logged in - however,
     * please use the isLoggedIn method instead if you are checking for that fact)
     * @return  The currently logged in user, or null.
     */
    public User getUser()
    {
        return user;
    }


    /**
     * Used on logout to make sure no stats or social data is left in cache.
     */
    private void cleanupSessionData() {
        detailedStats.clear();
        detailedStatsFetched = 0;
        socialHandler.clearCache();
    }


    public void clearPrefs(SharedPreferences prefs) {
        prefs.edit().clear().commit();
    }

    public void storeToPrefs(SharedPreferences prefs) {
        System.out.println("Storing user to SharedPreferences...");

        // Started from an activity - set the user settings now
        User u = getUser();
        SharedPreferences.Editor edit = prefs.edit();
        edit.putLong("sessionid", u.getSessionId());
        edit.putLong("userid", u.getUserId());
        edit.putString("username", u.getUsername());
        edit.putString("passwordhash", u.getPasswordHash());
        edit.commit();
    }

    public void loadFromPrefs(SharedPreferences prefs) {
        // Started after being destroyed - try to recover user settings
        if (prefs.getString("username", null) != null) {
            final User recoveredUser = new User();
            recoveredUser.setUsername(prefs.getString("username", null));
            recoveredUser.setPasswordHash(prefs.getString("password", null));
            recoveredUser.setSessionId(prefs.getLong("sessionid", -1));
            recoveredUser.setUserId(prefs.getLong("userid", -1));
            setUser(recoveredUser);

            // After restoring the session-critical data, fetch full user details from server
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Data d = getData(recoveredUser);
                        setUser((User) d);
                    } catch (NotLoggedInException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } else {
            System.out.println("Failed to restore from shared preferences.");
        }
    }



    /**
     * Returns a notification object with the current status - if there are new messages or friend
     * requests.
     * @return A notification object with the current notification status.
     */
    public Notification getNotificationStatus() {
        return notificationStatus;
    }


    /**
     * Set the notification object to the current status - if there are new messages or friend
     * requests.
     * @param notificationStatus    A notification object with the current notification status.
     */
    public void setNotificationStatus(Notification notificationStatus) {
        this.notificationStatus = notificationStatus;

        // See if the notification status CHANGED to true for a field just now, so there wasn't
        // a notificiation before but now there is - in that case, make sure to notify SocialHandler
        if (!this.notificationStatus.isNewFriends()
                && notificationStatus.isNewFriends()) {

            // This could either be a friend request or a friend request was accepted,
            // so both list should be updated next time they're used
            socialHandler.setFriendsChanged(true);
            socialHandler.setFriendRequestsChanged(true);
        }

        // The messaging feature is an activity right now so it will query the status of
        // the new message notification boolean instead
    }


    /**
     * Gets the instance of the SocialHandler object that fetches and caches friend data.
     * @return  An instantiated SocialHandler instance.
     */
    public SocialHandler getSocialHandler() {
        return socialHandler;
    }


    /**
     * Returns the last known safety status from AGA. TODO: we should check the timestamp of this
     * or otherwise be sure to know if AGA was disconnected, to make sure not to lock the app
     * in the wrong state
     *
     * @return The last known safety status from AGA.
     */
    public SafetyStatus getSafetyStatus() {
        if(AGADataParser.getInstance().getDistLevel() >= 3)
            return SafetyStatus.FAST_MOVING;
        else if(AGADataParser.getInstance().getDistLevel() == 2)
            return SafetyStatus.MOVING;
        else if(AGADataParser.getInstance().getDistLevel() == 1)
            return SafetyStatus.SLOW_MOVING;
        else
            return SafetyStatus.IDLE;
    }


    /**
     * Set the RealTimeDataHandler instance.
     * @param rtdh  The new RealTimeDataHandler instance.
     */
    public void setRealTimeDataHandler(RealTimeDataHandler rtdh) {
        realTimeDataHandler = rtdh;
    }
}

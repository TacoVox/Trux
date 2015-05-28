package se.gu.tux.trux.technical_services;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingDeque;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.Heartbeat;
import se.gu.tux.trux.datastructure.Notification;
import se.gu.tux.trux.datastructure.ProtocolMessage;
import se.gu.tux.trux.datastructure.User;


/**
 *  This class handles the communication with the server, with the help of the inner class
 *  ConnectorRunnable that also handles its own thread and wraps the socket and stream objects.
 *  There are two modes of communication:
 *
 *      1. A background thread running and sending anything in a transmit queue to the server. This
 *      is used to send metric data as well as a Heartbeat object to the server (put in the queue
 *      by DataPoller every few seconds). Only the response to the Heartbeat is considered
 *      (it is a Notification object telling us if there are any new notifications), others are
 *      discarded. Use the method "send" to put data in the queue.
 *
 *      2. Manual requests for data. This class acts as an interface and for the ConnectorRunnable
 *      that owns the socket and stream objects. Use the methods answerQuery or answerTimestampedQuery
 *      for this. However most data used in the GUI should be requested from the DataHandler to
 *      make sure the request is routed properly.
 *
 */
public class ServerConnector {
    // Storing address here instead of just calling a setup method from an activity since
    // we may need to be able to recreate the instance from any context, possibly from the
    // BackgroundService if the garbage collector has been aggressive with our app
    // (However ideally both address and port should be in some config file, it would also be
    // possible to write it to shared preferences the way we do in DataHandler)
    private final static String ADDRESS = "trux.derkahler.de";

    // Singleton instance
    private static ServerConnector instance = null;

    // The thread running the ConnectorRunnable
    private Thread transmitThread = null;
    private volatile ConnectorRunnable connector = null;

    // The queue - actually a Deque, so we can put things at both ends
    private volatile LinkedBlockingDeque<Data> queue;


    /**
     * Private constructor - construct the connector, start a thread.
     * Note that the actual connection is not made until something is being sent.
     */
    private ServerConnector() {
        queue = new LinkedBlockingDeque<>();
        connector = new ConnectorRunnable(ADDRESS);
        transmitThread = new Thread(connector);
        transmitThread.start();
    }


    /**
     * Retuns the instance of the ServerConnector.
     * @return  The ServerConnector instance.
     */
    public static ServerConnector getInstance() {
        // Double checked locking
        if (instance == null) {
            synchronized (ServerConnector.class) {
                if (instance == null) {
                    instance = new ServerConnector();
                }
            }
        }
        return instance;
    }


    /**
     * Short for getInstance()
     * @return The ServerConnector instance.
     */
    public synchronized static ServerConnector gI()
    {
        return getInstance();
    }


    /**
     * Disconnect from the server.
     */
    public void disconnect() {
       try {
           answerQuery(new ProtocolMessage(ProtocolMessage.Type.GOODBYE), 20);
       } catch (NotLoggedInException e) {}

        // transmitThread will exit naturally on socket close
        if (connector != null) {
            connector.shutDown();
        }
    }


    /**
     * Sends data to the server, by putting it into the queue. The sending is then done from
     * a background thread.
     * @param d
     */
    public void send(Data d) throws InterruptedException {
        queue.putLast(d);
    }


    /**
     * Used by DataPoller to manage the queue size - keeping that logic outside of this class
     * so this class can focus on the technical details of transmisison. Still keeping the queue
     * encapsulated here.
     * */
    public int getQueueSize() { return queue.size(); }
    /**
     * Used by DataPoller to manage the queue size - keeping that logic outside of this class
     * so this class can focus on the technical details of transmisison. Still keeping the queue
     * encapsulated here. Note that we throw away the data.
     * */
    public void removeFirst() { queue.removeFirst(); }


    /**
     * Before sending a new HeartBeat object, this method is used to clear any old HeartBeats,
     * because they are not interesting to the server - they don't affect the connections life and
     * notifications will be returned when the newer HeartBeat is transmitted anyway.
     */
    public void purgeHeartbeats() {
        for (Iterator<Data> it = queue.iterator(); it.hasNext(); ) {
            Data d = it.next();
            if (d instanceof Heartbeat) {
                it.remove();
            }
        }
    }


    /**
     * Forwards a data query to the server and returns the reply.
     * @param d     The data to send to the server.
     * @return      The reply from the server.
     */
    public Data answerQuery(Data d) throws NotLoggedInException {
        return answerQuery(d, -1);
    }


    /**
     * Forwards a data query to the server and returns the reply, and stops trying after the
     * desired timeOut.
     * @param d         The data to send to the server.
     * @param timeOut   The timeout in seconds.
     * @return          The reply from the server, or null if timeout was reached.
     */
    public Data answerQuery(Data d, int timeOut) throws NotLoggedInException {
        //System.out.println("Serverconnector forwarding query: " + d.getClass().getSimpleName());
        d.setTimeStamp(System.currentTimeMillis());
        return connector.sendQuery(d, timeOut);
    }


    /**
     * Forwards a data query to the server and returns the reply. Does NOT timestamp the data query,
     * which is useful if you need to manually timestamp it, for example when requesting statistics
     * for past time.
     * @param d     The data to send to the server.
     * @return      The reply from the server.
     */
    public Data answerTimestampedQuery(Data d) throws NotLoggedInException {
        return connector.sendQuery(d, -1);
    }


    /**
     * The subclass wrapping the Socket and Object streams. It is runnable, and is continously
     * running in a thread that forwards anything that is put into the queue. However, the
     * connection (socket and streams) can be used from other threads as well, see the comments on
     * the main class above.
     */
    class ConnectorRunnable implements Runnable {
        // Socket, streams, server address
        private volatile Socket cs;
        private volatile ObjectInputStream in = null;
        private volatile ObjectOutputStream out = null;
        private String serverAddress;

        // Flag for main loop
        private boolean isRunning = true;


        /**
         * Constructor. The address is connected to when data is first sent.
         * @param address   The server address.
         */
        public ConnectorRunnable(String address) {
            serverAddress = address;
        }


        /**
         * Connects to the server, with a default timeout of never.
         * @return  True on a successful connection attempt.
         */
        private boolean connect() {
            return connect(-1);
        }


        /**
         * Connects to the server, with the specified timeout.
         * @return  True on a successful connection attempt, false on timeout.
         */
        private boolean connect(int timeOut) {
            int time = 0;

            // Proceed/continue if socket is null or closed (we close it on any critical exception)
            // AND the isRunning flag is still true
            while ((cs == null || cs.isClosed()) && isRunning) {
                try {
                    // Make sure no other thread uses the stream resources here
                    synchronized(this) {

                        // Double check if the socket is not closed
                        if (cs == null || cs.isClosed()) {

                            // Connect. TODO: The port should be in the config
                            System.out.println("Connecting to " + serverAddress + ".");
                            cs = new Socket(serverAddress, 12000);

                            // Create socket with 30 Second timeout
                            cs.setSoTimeout(1000 * 30);

                            // Connect streams
                            out = new ObjectOutputStream(cs.getOutputStream());
                            out.flush();
                            in = new ObjectInputStream(cs.getInputStream());
                            System.out.println("Connected.");
                        }
                    }
                } catch (IOException e) {

                    try {
                        // Problem connecting. Sleep for a while.
                        System.err.println("Could not connect to " + serverAddress +
                                ". Retrying in 10s...");
                        Thread.sleep(10000);

                        // Stop trying if we reach timeout
                        time += 10;
                        if (time > timeOut && timeOut != -1) {
                            System.err.println("Connect reached timeout and stopped trying.");
                            return false;
                        }
                    } catch (InterruptedException e1) {
                        // Print this, then the loop will return to another try
                        System.out.println("Connect interrupted!");
                    }
                }
            }
            return true;
        }


        /**
         * Disconnects by closing the sockets and setting the isRunning flag to false which
         * terminates the main queue loop.
         */
        public void shutDown() {
            if (cs != null) try {
                synchronized (this) {
                    cs.close();
                    in.close();
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            isRunning = false;
        }


        /**
         * Sends a query to the server and returns the reply. Returns null on timeout.
         * @param   query   Data to send to the server.
         * @return          The reply from the server.
         */
        public Data sendQuery(Data query, int timeOut) throws NotLoggedInException {
            boolean dataSent = false, hasTimedOut = false;
            long startTime = System.currentTimeMillis();
            Data answer = null;

            while (!dataSent && !hasTimedOut) {
                // If data is null, return null
                if (query == null) {
                    return null;
                }

                // Check if socket is not closed and the out and in streams are open, if not, connect
                if (cs == null || cs.isClosed() || out == null || in == null) {
                    System.out.println("SendQuery requesting connect...");
                    boolean connectedInTime = connect(timeOut);
                    if (!connectedInTime) {
                        System.out.println("Connection timed out, giving up.");
                        return null;
                    }
                }

                // If not logged in, throw exception so the using code may take approperiate action
                // Login attempts, auto login attempts and register requests etc are allowed regardless
                if (filter(query)) {

                    // Not allowed.
                    System.out.println("Not logged in: filtering "
                            + query.getClass().getSimpleName() + ": " + query.getValue());
                    throw new NotLoggedInException();

                } else if (!(query instanceof ProtocolMessage &&
                            ((ProtocolMessage) query).getType() == ProtocolMessage.Type.GOODBYE)) {

                    // Set user id and session id if it's not a goodbye message
                    query.setSessionId(DataHandler.getInstance().getUser().getSessionId());
                    query.setUserId(DataHandler.getInstance().getUser().getUserId());
                }

                // Make sure no other thread uses the stream resources here
                synchronized (this) {
                    try {

                        // Send and receive
                        out.writeObject(query);
                        out.flush();
                        answer = (Data)in.readObject();
                        dataSent = true;

                    } catch (IOException e) {

                        // Server probably shut down or we lost connection. Close sockets so we are
                        // sure to try to reconnect in the next iteration of while loop (unless we
                        // timed out, in which case any other send attempt will provoke a reconnect)
                        System.out.println("IOEXception in sendQuery: " + e.getMessage()
                            + " - closing connection.");
                        try {
                            cs.close();
                            in.close();
                            out.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }

                        // Check for timeout - then don't try to send again. -1 means never timeout.
                        if (timeOut != -1 && (System.currentTimeMillis() - startTime) > timeOut) {
                            System.out.println("Connection timed out.");
                            hasTimedOut = true;
                        }

                    } catch (ClassNotFoundException e) {
                        System.out.println("Class not found in sendQuery.");
                        return null;
                    }
                }
            }

            // If we requested something that required valid session and we didn't have one, throw
            // NotLoggedInException
            if (answer instanceof ProtocolMessage &&
                    ((ProtocolMessage)answer).getType() == ProtocolMessage.Type.INVALID_SESSION) {
                throw new NotLoggedInException();
            }

            return answer;
        }


        /**
         * Return true if anything should be filtered (blocked) due to not logged in.
         * Packages allowed to pass: login/register requests, autologin requests, goodbye
         * messages, logout requests.
         * @param query
         * @return
         */
        private boolean filter(Data query) {
            if (DataHandler.getInstance().isLoggedIn()) {

                // If logged in, everything is allowed
                return false;

                // Otherwise, block if its NOT login request OR ... etc  ...
            } else if (!(query.getSessionId() == User.LOGIN_REQUEST ||
                    query.getSessionId() == User.REGISTER_REQUEST ||
                    (query instanceof ProtocolMessage &&
                            (((ProtocolMessage) query).getType() == ProtocolMessage.Type.AUTO_LOGIN_REQUEST ||
                                    ((ProtocolMessage) query).getType() == ProtocolMessage.Type.GOODBYE ||
                                    ((ProtocolMessage) query).getType() == ProtocolMessage.Type.LOGOUT_REQUEST)))) {
                return true;
            }
            return false;
        }


        /**
         * The main thread sends anything that is put in the queue to the server, provided that the
         * user is logged in.
         */
        @Override
        public void run() {

            // Continue until this flag is set to false.
            while (isRunning) {

                Data upNext = null;
                try {

                    // Wait for queue to have objects
                    upNext = queue.takeFirst();

                    if (upNext != null) {

                        // Connect if not connected at this point.
                        if (cs == null || cs.isClosed() || out == null || in == null) {
                            connect();
                        }

                        // Make sure we're logged in.
                        if (DataHandler.getInstance().isLoggedIn()) {

                            // Mark data with session and user
                            upNext.setSessionId(DataHandler.getInstance().getUser().getSessionId());
                            upNext.setUserId(DataHandler.getInstance().getUser().getUserId());

                            // Then stop other threads from using this object (most importantly
                            // the in and out streams) while sending and receiving data
                            // Send the data

                            Data inD = null;
                            synchronized (this) {
                                out.writeObject(upNext);
                                out.flush();
                                inD = (Data) in.readObject();
                            }

                            // If we sent a heartbeat object just now, update the notification object
                            // in DataHandler
                            if (upNext instanceof Heartbeat && inD instanceof Notification) {
                                System.out.println("Serverconnector: Heartbeat sent.");
                                DataHandler.getInstance().setNotificationStatus((Notification)inD);
                            }

                        } else {
                            // Not logged in - put it FIRST in the queue and then sleep a while
                            System.out.println("Wanted to send queued data but is not logged in." +
                                    " Sleeping...");
                            queue.putFirst(upNext);
                            Thread.sleep(10000);
                        }
                    }
                } catch (InterruptedException e) {

                    // Thread is being interrupted, probably by this application shutting down.
                    // Break while loop
                    isRunning = false;
                    System.out.println("ServerConnector interrupted: " + e.getMessage());

                } catch (IOException e) {

                    // Error on sending to server - put the data back in queue
                    try {
                        queue.putFirst(upNext);
                    } catch (InterruptedException e1) {
                        // See InterruptedException above.
                        isRunning = false;
                    }

                    // Close sockets so we are sure to reconnect in next iteration
                    // of while loop - Note this also happens on SocketTimeoutException, if the
                    // connection timed out (timeout is currently 30s hardcoded)
                    System.out.println("IOException in ServerConnector thread: " + e.getMessage());
                    if (cs != null) {
                        try {
                            synchronized (this) {
                                cs.close();
                                in.close();
                                out.close();
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }

                } catch (ClassNotFoundException e) {
                    // The server returned something we don't recognise. For now, just quit.
                    // Later we should have a handshake asking the server for the current version
                    // and telling the user to upgrade if necessary.
                    isRunning = false;
                }
            }
        }
    }
}

package se.gu.tux.trux.technical_services;

import android.net.wifi.WifiConfiguration;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingDeque;

import se.gu.tux.trux.application.DataHandler;
import se.gu.tux.trux.datastructure.ArrayResponse;
import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.Heartbeat;
import se.gu.tux.trux.datastructure.Notification;
import se.gu.tux.trux.datastructure.ProtocolMessage;
import se.gu.tux.trux.datastructure.User;

/**
 *
 *
 *
 * Created by jerker on 2015-03-27.
 */
public class ServerConnector {

    private Thread transmitThread = null;
    private ConnectorRunnable connector = null;
    private LinkedBlockingDeque<Data> queue;

    /**
     * Some more singleton....!
     */
    private static ServerConnector instance = null;
    private ServerConnector() { queue = new LinkedBlockingDeque<>(); }

    public static ServerConnector getInstance() {
        if (instance == null) {
            synchronized (ServerConnector.class) {
                // Yes, double check!
                if (instance == null) {
                    instance = new ServerConnector();
                }
            }
        }
        return instance;
    }

    public synchronized static ServerConnector gI()
    {
        return getInstance();
    }


    /**
     * Define the address to connect to - the actual connection is not made until something
     * needs to be sent - so TODO: maybe rename this method later
     * @param address
     */
    public void connect(String address) {
        connector = new ConnectorRunnable(address, this);
        transmitThread = new Thread(connector);
        transmitThread.start();
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
     * Sends data to the server, like metric data, by putting it into the queue.
     * @param d
     */
    public void send(Data d) throws InterruptedException {
        queue.putLast(d);
    }


    /**
     * Used by DataPoller to manage the queue size - keeping that logic outside of this class
     * so this class can focus on the technical details of transmisison
     * */
    public int getQueueSize() { return queue.size(); }

    /**
     * Used by DataPoller to manage the queue size - keeping that logic outside of this class
     * so this class can focus on the technical details of transmisison.
     * Note that we throw away the data here.
     * */
    public void removeFirst() { queue.removeFirst(); }

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
     * @param d
     * @return
     */
    public Data answerQuery(Data d) throws NotLoggedInException {
        return answerQuery(d, -1);
    }


    /**
     * Forwards a data query to the server and returns the reply, and stops trying after the
     * desired timeOut.
     * @param d
     * @param timeOut
     * @return
     */
    public Data answerQuery(Data d, int timeOut) throws NotLoggedInException {
        d.setTimeStamp(System.currentTimeMillis());
        return connector.sendQuery(d, timeOut);
    }

    public Data answerTimestampedQuery(Data d) throws NotLoggedInException {
        return connector.sendQuery(d, -1);
    }

    class ConnectorRunnable implements Runnable {
        private Socket cs;
        private String serverAddress;
        private ObjectInputStream in = null;
        private ObjectOutputStream out = null;
        private boolean isRunning = true;
        // For debugging, checking the instance
        private ServerConnector sc = null;


        public ConnectorRunnable(String address) {
            serverAddress = address;
        }

        public ConnectorRunnable(String address, ServerConnector sc) {
            serverAddress = address;
            this.sc = sc;
        }

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
         * Sends a query to the server and returns the reply. Returns null on timeout.
         * @param query
         * @return
         */
        public Data sendQuery(Data query, int timeOut) throws NotLoggedInException {
            boolean dataSent = false;
            Data answer = null;

            while (!dataSent) {
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
                    System.out.println("Not logged in: filtering "
                            + query.getClass().getSimpleName() + ": " + query.getValue());
                    throw new NotLoggedInException();
                }

                // Make sure no other thread uses the stream resources here
                synchronized (this) {
                    try {

                        // Send and receive
                        System.out.println("Sending query...: " + query.getClass().getSimpleName());

                        // Set user id and session id if it's not a goodbye message
                        if (!(query instanceof ProtocolMessage &&
                                ((ProtocolMessage) query).getType() == ProtocolMessage.Type.GOODBYE)) {
                            query.setSessionId(DataHandler.getInstance().getUser().getSessionId());
                            query.setUserId(DataHandler.getInstance().getUser().getUserId());
                        }

                        out.writeObject(query);
                        answer = (Data)in.readObject();

                        System.out.println("Returned type: " + answer.getClass().getSimpleName());

                        dataSent = true;

                    } catch (IOException e) {

                        // Server probably shut down or we lost connection. Close sockets so we are sure
                        // to try to reconnect in the next iteration of while loop
                        System.out.println("IOEXception in sendQuery: ");
                        e.printStackTrace();
                        try {
                            cs.close();
                            in.close();
                            out.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
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

        private boolean connect() {
            return connect(-1);
        }

        private boolean connect(int timeOut) {
            int time = 0;
            while (cs == null || cs.isClosed()) {
                try {
                    // Make sure no other thread uses the stream resources here
                    synchronized(this) {
                        // Double checked locking
                        if (cs == null || cs.isClosed()) {
                            System.out.println("Connecting to " + serverAddress + ": ServerConnector " + this.toString());
                            cs = new Socket(serverAddress, 12000);
                            System.out.println("Connecting output stream...");
                            out = new ObjectOutputStream(cs.getOutputStream());
                            out.flush();
                            System.out.println("Connecting input stream...");
                            in = new ObjectInputStream(cs.getInputStream());
                            System.out.println("Connected.");
                        }
                    }
                } catch (IOException e) {
                    // Problem connecting.
                    System.err.println("Could not connect to " + serverAddress +
                            ". Retrying in 10s...");
                    try {
                        Thread.sleep(10000);
                        // Stop trying if we reach timeout
                        time += 10;
                        if (time > timeOut && timeOut != -1) {
                            System.err.println("Connect stopped trying.");
                            return false;
                        }
                    } catch (InterruptedException e1) {
                        System.out.println("Interrupted!");
                    }
                }
            }
            return true;
        }



        @Override
        public void run() {

            while (isRunning) {

                Data d = null;

                // Connected. Send anything in the queue.
                try {

                    // Wait for queue to have objects
                    //System.out.println("Server connector: waiting  at queue... (" + sc.toString() + ")");
                    d = queue.takeFirst();

                    //System.out.println("Server connector: Found object in queue.");
                    if (d != null) {
                        System.out.println("Server connector: next up is " + d.getClass().getSimpleName());

                        // Connect if not connected at this point.
                        if (cs == null || cs.isClosed() || out == null || in == null) {
                            connect();
                        }

                        if (DataHandler.getInstance().isLoggedIn()) {

                            // Mark data with session and user
                            d.setSessionId(DataHandler.getInstance().getUser().getSessionId());
                            d.setUserId(DataHandler.getInstance().getUser().getUserId());

                            // Then stop other threads from using this object (most importantly
                            // the in and out streams) while sending and receiving data
                            // Send the data
                            System.out.println("Server connector: sending " + d.getClass().getSimpleName());
                            Data inD = null;
                            synchronized (this) {
                                out.writeObject(d);
                                inD = (Data) in.readObject();
                            }
                            System.out.println("Server connector: received " + inD.getClass().getSimpleName());

                            // If we sent a heartbeat object just now, update the notification object
                            // in DataHandler
                            if (d instanceof Heartbeat && inD instanceof Notification) {
                                DataHandler.getInstance().setNotificationStatus((Notification)inD);
                            }

                        } else {
                            // Not logged in
                            System.out.println("Want to send queued data but is not logged in. Sleeping...");
                            queue.putFirst(d);
                            Thread.sleep(10000);
                        }
                    }
                } catch (InterruptedException e) {

                    // Thread is being interrupted by this application shutting down. Break while loop
                    isRunning = false;
                    System.out.println("ServerConnector interrupted: " + e.getMessage());

                } catch (IOException e) {

                    // Error on sending to server, close sockets so we reconnect next iteration of while loop
                    System.out.println("IOException in ServerConnector thread: " + e.getMessage());
                    try {
                        queue.putFirst(d);
                    } catch (InterruptedException e1) {
                        isRunning = false;
                    }

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
                    isRunning = false;
                }
            }
        }
    }
}

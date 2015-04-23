package se.gu.tux.trux.technical_services;

import android.os.AsyncTask;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import se.gu.tux.trux.appplication.DataHandler;
import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.Fuel;
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
    private static LinkedBlockingDeque<Data> queue;


    /**
     * Some more singleton....!
     */
    private static ServerConnector instance = null;
    private ServerConnector() { queue = new LinkedBlockingDeque<>(); }
    public synchronized static ServerConnector getInstance()
    {
        if (instance == null) {
            instance = new ServerConnector();
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

    /**
     * Forwards a data query to the server and returns the reply.
     * @param d
     * @return
     */
    public Data answerQuery(Data d) throws NotLoggedInException {
        d.setTimeStamp(System.currentTimeMillis());
        return connector.sendQuery(d);
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
                cs.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            isRunning = false;
        }


        /**
         * Synchronized, which means if the thread is busy sending/receiving,
         * this will have to wait, and vice versa
         * @param query
         * @return
         */
        public Data sendQuery(Data query) throws NotLoggedInException {
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
                    connect();
                }

                // If not logged in, throw exception so the using code may take approperiate action
                // Login attempts and register requests are allowed regardless
                if (!DataHandler.getInstance().isLoggedIn() &&
                        !(query.getSessionId() == User.LOGIN_REQUEST ||
                        query.getSessionId() == User.REGISTER_REQUEST)) {
                    throw new NotLoggedInException();
                }

                // Make sure no other thread uses the stream resources here
                synchronized (this) {
                    try {

                        // Send and receive
                        System.out.println("Sending query...");
                        query.setSessionId(DataHandler.getInstance().getUser().getSessionId());
                        query.setUserId(DataHandler.getInstance().getUser().getUserId());
                        out.writeObject(query);
                        answer = (Data)in.readObject();

                        System.out.println("returned values: " + answer.getSessionId() + " : " + answer.getUserId());

                        dataSent = true;

                    } catch (IOException e) {

                        // Server probably shut down or we lost connection. Close sockets so we are sure
                        // to try to reconnect in the next iteration of while loop
                        System.out.println("IOEXception in sendQuery.");
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

            return answer;
        }


        private void connect() {
            while (cs == null || cs.isClosed()) {
                try {
                    // Make sure no other thread uses the stream resources here
                    synchronized(this) {
                        System.out.println("Connecting to " + serverAddress + ": ServerConnector " + this.toString());
                        cs = new Socket(serverAddress, 12000);
                        out = new ObjectOutputStream(cs.getOutputStream());
                        in = new ObjectInputStream(cs.getInputStream());
                    }
                } catch (IOException e) {
                    // Problem connecting.
                    System.err.println("Could not connect to " + serverAddress +
                            ". Retrying in 10s...");
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e1) {
                        System.out.println("Interrupted!");
                    }
                }
            }
        }



        @Override
        public void run() {

            while (isRunning) {

                Data d = null;

                // Connected. Send anything in the queue.
                try {


                    // Wait for queue to have objects
                    System.out.println("Server connector: waiting  at queue... (" + sc.toString() + ")");
                    d = queue.takeFirst();

                    // Then STOP ANYTHING ELSE from using this object (most importantly
                    // the in and out streams) while sending and receiving data
                    //System.out.println("Server connector: Found object in queue.");
                    if (d != null) {
                        System.out.println("Server connector: Sending...");

                        // Connect if not already connected.
                        // If we ever lose connection, try reconnecting at regular intervals
                        if (cs == null || cs.isClosed() || out == null || in == null) {
                            connect();
                        }

                        if (DataHandler.getInstance().isLoggedIn()) {

                            // Mark data with session and user
                            d.setSessionId(DataHandler.getInstance().getUser().getSessionId());
                            d.setUserId(DataHandler.getInstance().getUser().getUserId());

                            // Send the data
                            synchronized (this) {
                                out.writeObject(d);
                                Data inD = (Data) in.readObject();

                                if (inD.getValue() == null) {
                                    System.out.println("Server connector: Received data with null value");
                                } else {
                                    System.out.println("Server connector: Received data " + inD.getValue().toString());
                                    System.out.println("returned values: " + inD.getSessionId() + " : " + inD.getUserId());
                                }
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
                            cs.close();
                            in.close();
                            out.close();

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

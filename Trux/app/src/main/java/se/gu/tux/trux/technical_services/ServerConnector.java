package se.gu.tux.trux.technical_services;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.Fuel;

/**
 *
 *
 *
 * Created by jerker on 2015-03-27.
 */
public class ServerConnector {
    private static ServerConnector instance = null;

    private Thread transmitThread = null;
    private ConnectorRunnable connector = null;
    private LinkedBlockingQueue<Data> queue = new LinkedBlockingQueue<Data>();

    /**
     * Some more singleton....!
     */
    static {
        if (instance == null) {
            instance = new ServerConnector();
        }
    }
    private ServerConnector() { }
    public static ServerConnector getInstance() { return instance; }
    public static ServerConnector gI() { return instance; }

    public void send(Data d) {
        queue.add(d);
    }

    public Data answerQuery(Data d) {
        return connector.sendQuery(d);
    }

    public void connect(String address) {
        connector = new ConnectorRunnable(address);
        transmitThread = new Thread(connector);
        transmitThread.start();
    }

    public void disconnect() {
        // transmitThread will exit naturally on socket close
        if (connector != null) {
            connector.closeSocket();
        }
    }


    class ConnectorRunnable implements Runnable {
        private Socket cs;
        private String serverAddress;
        private ObjectInputStream in = null;
        private ObjectOutputStream out = null;
        private boolean isRunning = true;

        public ConnectorRunnable(String address) {
            serverAddress = address;
        }

        public void closeSocket() {
            if (cs != null) try {
                cs.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        /**
         * Synchronized, which means if the thread is busy sending/receiving,
         * this will have to wait, and vice versa
         * @param query
         * @return
         */
        public synchronized Data sendQuery(Data query) {
            try {
                out.writeObject(query);
                return (Data)in.readObject();
            } catch (IOException e) {
                return null;
            } catch (ClassNotFoundException e) {
                return null;
            }
        }


        @Override
        public void run() {

            while (isRunning) {

                // Connect if not already connected.
                // If we ever lose connection, try reconnecting at regular intervals
                while (cs == null || !cs.isConnected()) {
                    try {
                        System.out.println("Connecting to " + serverAddress);
                        cs = new Socket(serverAddress, 12000);
                        out = new ObjectOutputStream(cs.getOutputStream());
                        in = new ObjectInputStream(cs.getInputStream());

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

                // Connected. Send anything in the queue.
                try {
                    Object o = null;
                    // Wait for queue to have objects
                    System.out.println("Server connector: waiting.");
                    o = queue.take();

                    // Then STOP ANYTHING ELSE from using this object (most importantly
                    // the in and out streams) while sending and receiving data
                    System.out.println("Server connector: Found object in queue.");
                    if (o != null) {
                        System.out.println("Server connector: Sending...");
                        synchronized (this) {
                            out.writeObject(o);
                            Data d = (Data) in.readObject();
                            System.out.println("Server connector: Received data " + d.getValue().toString());
                        }
                    }


                } catch (IOException e) {
                   // Socket probably closed
                   isRunning = false;
                    System.out.println("Socket closed!");
                    // TOOD we need to distinguish between this happening because app is shutting
                    // down and close being called on the socket by us, as opposed to this happening
                    // because we lost contact with server or similar!?

                } catch (ClassNotFoundException e) {
                    isRunning = false;
                 } catch (InterruptedException e) {
                    isRunning = false;
                    System.out.println("Interrupted!");
                }
            }
        }
    }
}

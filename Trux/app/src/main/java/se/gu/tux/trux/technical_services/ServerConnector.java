package se.gu.tux.trux.technical_services;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.Fuel;

/**
 * TODO: Make this reconnect after a short dealy if disconnected or connection failed!
 * TODO: Skip the idea below, instead synchronize around the socket and have one method
 * callable from other threads to handle queries!
 *
 * We need to be able to send both a continuous stream of metric data AND occasionally also queries
 * and the easiest (but not most neat) way to do this right now feels like having separate
 * connections (sockets)! Because otherwise it will be complicated to return the responses correctly
 * to the method that made the query request - so right now we try this;
 * - One thread runs all the time and sends metric data etc, this is done
 *  by putting the data in the queue in this class
 * - Asynctasks are used for quick questions - so this class will return
 *  an asynctask object initialized the right way upon request
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

        @Override
        public void run() {
            try {
                cs = new Socket(serverAddress, 12000);
                out = new ObjectOutputStream(cs.getOutputStream());
                in = new ObjectInputStream(cs.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            while (isRunning) {
                System.out.println("------------------------------------------------");
                try {

                    // Testing: put arbitrary object in queue, then poll queue and send object
                    Fuel f = new Fuel(0);
                    f.setValue(new Double(2.0));
                    queue.add(f);
                    out.writeObject(queue.take());
                    Fuel f2 = (Fuel)in.readObject();
                    System.out.println("Received f2: " + f2.getValue().toString());

                    // NOTE this is totally for testing, otherwise queue will naturally block
                    // thread execution when it is empty
                    Thread.sleep(10000);
                    System.out.println("Slept for a while.");
                } catch (IOException e) {
                   // Socket closed
                   isRunning = false;
                } catch (ClassNotFoundException e) {
                    isRunning = false;
                 } catch (InterruptedException e) {
                    isRunning = false;
                }
            }
        }
    }

    class QueryTask extends AsyncTask {

        public QueryTask() {

        }
        @Override
        protected Object doInBackground(Object[] objects) {
            return null;
        }
    }
}

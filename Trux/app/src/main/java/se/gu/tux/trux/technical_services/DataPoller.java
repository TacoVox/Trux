package se.gu.tux.trux.technical_services;

import se.gu.tux.trux.datastructure.Data;

/**
 * Created by jerker on 2015-04-01.
 *
 * This will run on its own thread, with configurable intervals query the real time data
 * handler and later on any social data that should be pushed regularly... and send it to
 * the ServerConnectors queue so it is sent to the server
 *
 */
public class DataPoller {
    private static DataPoller instance;
    // Seconds to sleep
    private final static int POLL_INTERVAL = 10;
    private Thread t;
    private PollRunnable pr;

    private DataPoller() {}

    public static DataPoller getInstance()
    {
        if (instance == null) {
            instance = new DataPoller();
        }
        return instance;
    }

    public static DataPoller gI()
    {
        if (instance == null) {
            instance = new DataPoller();
        }
        return instance;
    }

    public void start() {
        pr = new PollRunnable();
        t = new Thread(pr);
        t.start();

    }

    public void stop() {
        t.interrupt();
    }


    class PollRunnable implements Runnable {
        private boolean isRunning = true;

        @Override
        public void run() {
            RealTimeDataHandler rtdh = new RealTimeDataHandler();

            while (isRunning) {
                try {

                    // Get the current metric data from RTDH.
                    Data[] metrics = rtdh.getCurrentMetrics();

                    // Send all metric objects to server if they are not null.
                    for (Data d : metrics) {
                        if (d != null) {
                            ServerConnector.gI().send(d);
                            //IServerConnector.getInstance().receiveData(d);
                        }
                    }

                    // Wait POLL_INTERVAL seconds before continuing.
                    Thread.sleep(1000 * POLL_INTERVAL);

                } catch (InterruptedException e) {
                    // Interrupted - exit thread
                    isRunning = false;
                }
            }
        }
    }
}

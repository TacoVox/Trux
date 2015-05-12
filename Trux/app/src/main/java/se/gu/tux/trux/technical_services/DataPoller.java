package se.gu.tux.trux.technical_services;

import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.Heartbeat;

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
    private Data[] lastMetrics = null;

    private DataPoller() {}

    public static DataPoller getInstance()
    {
        // Double checked locking
        if (instance == null)
        {
            synchronized (DataPoller.class) {
                if (instance == null) {
                    instance = new DataPoller();
                }
            }

        }
        return instance;
    } // end getInstance()

    public synchronized static DataPoller gI()
    {
       return getInstance();
    }

    public void start() {
        if (pr == null) {
            pr = new PollRunnable();
            t = new Thread(pr);
            t.start();
        }
    }

    public void stop() {
        t.interrupt();
    }


    class PollRunnable implements Runnable {
        private boolean isRunning = true;


        /**
         * Returns true if the array has any value that is not null.
         * @param metrics
         * @return
         */
        private boolean hasValues(Data[] metrics) {
            boolean hasValues = false;
            for (Data d : metrics) {
                if (d.getValue() != null) {
                    hasValues = true;
                }
            }
            return hasValues;
        }


        @Override
        public void run() {
            RealTimeDataHandler rtdh = new RealTimeDataHandler();

            while (isRunning) {
                // Main loop - every POLL_INTERVAL seconds, pull data from AGA and also provide a
                // heartbeat to the server
                try {
                    // Make sure the queue doesn't grow to big by removing some things
                    // if it's really big
                    checkQueueSize();

                    // Get the current metric data from RTDH.
                    Data[] metrics = rtdh.getCurrentMetrics();

                    if (hasValues(metrics) && metricsChanged(metrics)) {

                        // Send and update the lastMetrics array
                        for (Data thisMetric : metrics) {
                            // Adding another null check just if some value happens to be missing
                            // from AGA input
                            if (thisMetric.getValue() != null) {
                                ServerConnector.gI().send(thisMetric);
                            }
                        }
                        lastMetrics = metrics;
                    }

                    // Also provide a heartbeat object - this keeps connection alive regardless
                    // of connection to AGA and makes sure server can send back notifications
                    ServerConnector.gI().send(new Heartbeat());

                    // Wait POLL_INTERVAL seconds before continuing.
                    Thread.sleep(1000 * POLL_INTERVAL);
                    System.out.println("DataPoller: " + this.toString());

                } catch (InterruptedException e) {
                    // Interrupted - exit thread
                    isRunning = false;
                }
            }
        }
    }


    private boolean metricsChanged(Data[] metrics) {
        // Compare with latest received metrics - yes, we compare the whole array
        // (We only transmit if some value has changed, otherwise there may be a
        // problem with aga connection + the information is just redundant)
        boolean send = false;
        if (lastMetrics == null) {
            // Nothing previously known
            send = true;
        } else {
            // Loop through and see if there is a difference
            for (int i = 0; i < metrics.length; i++) {
                // Compare with last sent

                if (metrics[i] != null && metrics[i].getValue() != null &&
                        !metrics[i].equals(lastMetrics[i])) {
                    // Difference detected
                    send = true;
                }
            }
        }
        return send;
    }

    /**
     * If we loose connection to the server but still get values continously, the queue may grow
     * unreasonably big. Here we make sure to thin out the data in that case.
     */
    private void checkQueueSize() {
        // TODO: let treshold values be configurable or at least not hard coded here
        if (ServerConnector.gI().getQueueSize() > 5000) {
            for (int i = 0; i < 20; i++) {
                ServerConnector.gI().removeFirst();
            }
        }

        // ALSO, if there are any unsent heartbeat messages, remove them since only the latest
        // heartbeat object needs to be sent - and that one will be appended to the queue after
        // running this cleaning method
        ServerConnector.gI().purgeHeartbeats();
    }
}

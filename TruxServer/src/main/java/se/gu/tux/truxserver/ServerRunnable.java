package se.gu.tux.truxserver;

import java.io.EOFException;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.MetricData;
import se.gu.tux.trux.datastructure.ProtocolMessage;
import se.gu.tux.truxserver.dataswitch.DataSwitcher;
import se.gu.tux.truxserver.logger.Logger;

public class ServerRunnable implements Runnable {

    private boolean isRunning = true;
    private Socket cs = null;
    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;
    private long connectionId;
    private Thread currentThread;
    private long idleTime = 0;
    private long maxIdleTime = 0;

    public ServerRunnable(Socket cs, long connectionId, long maxIdleTime) {
        this.cs = cs;
        this.connectionId = connectionId;
        this.maxIdleTime = maxIdleTime;
    }

    @Override
    public void run() {

        // Keep track of the thread provided by the thread pool
        this.currentThread = Thread.currentThread();
        Logger.gI().addDebug("Runnable " + connectionId + " starting...");

        try {
            // Set up socket timeout and object streams
            cs.setSoTimeout(1000);
            out = new ObjectOutputStream(cs.getOutputStream());
            in = new ObjectInputStream(cs.getInputStream());
        } catch (IOException e) {
            // Should not happen ;)
            Logger.gI().addDebug(connectionId + ": Fatal on stream creation in ServerRunnable: "
                    + e.getMessage());
            isRunning = false;
        }

        // Main loop for handling the client's requests
        while (isRunning) {
            try {

                // Receive data.
                // Since we are using the thread pool, when the thread pool shuts down it calls
                // interrupt() on all threads - if we managed the threads manually we could just
                // close the socket which would abort a blocking read, here instead we have set 
                // a soTimeout on the socket and regularly check if the thread has been interrupted.
                Data d = null;
                boolean timedOut = false;
                while (d == null && !currentThread.isInterrupted() && !timedOut) {
                    try {
                        // Read data - this blocks until the defined soTimeout, then repeats
                        // So we do nothing on the exception, it's just catched there to keep the 
                        // loop running at regular intervals
                        d = (Data) in.readObject();
                        idleTime = 0;
                        Logger.gI().addMsg(d.getClass().getSimpleName());
                    } catch (SocketTimeoutException e) {
                    	idleTime++;
                    	if (idleTime > maxIdleTime) {
                    		Logger.gI().addMsg(connectionId + ": Timed out!");
                    		timedOut = true;
                    	}
                    }
                }

                // If thread was interrupted while waiting for input, just shut down.
                // The same goes for if connection timeout was reached or the client said goodbye.
                if (currentThread.isInterrupted() || timedOut || 
                		(d instanceof ProtocolMessage && ((ProtocolMessage)d).getType() == ProtocolMessage.Type.GOODBYE) ) {
                    Logger.gI().addMsg(connectionId + ": Thread interrupted, shutting down...");
                    shutDown();
                    return;
                }

                // Debugging output
                if (d.getValue() != null) {
                    Logger.gI().addDebug(d.getValue().toString());
                    if (d instanceof MetricData) {
                        Logger.gI().addDebug("TS: " + Long.toString(d.getTimeStamp()));
                    }
                } else {
                    Logger.gI().addDebug(connectionId + ": Received object with null value from " + cs.getInetAddress());
                }

                // Send data to DataSwitcher
                d = DataSwitcher.gI().handleData(d);

                // Send data back to acknowledge.
                out.writeObject(d);

            } catch (ClassNotFoundException e) {

                Logger.gI().addError(connectionId + ": Class not found! Exiting.");
                isRunning = false;

            } catch (InvalidClassException e) {

                Logger.gI().addError(connectionId + ": Client sent invalid class: " + e.getMessage());

            } catch (EOFException e) {

                Logger.gI().addDebug(connectionId + ": Client disconnected.");
                shutDown();

            } catch (IOException e) {

                if (e instanceof SocketException) {
                    Logger.gI().addDebug(connectionId + ": Socket exception - assuming server is shutting down.");
                } else {
                    Logger.gI().addMsg(connectionId + ": Closing ServerRunnable socket... (" + e.getClass() + ")");
                }
                shutDown();
            }
        }
    }

    /**
     * Cleanly shut down this server runnable
     */
    public void shutDown() {
        // Could be called by closing socket from the outside, but also
        // from the stream input being interrupted
        if (cs != null && cs.isConnected()) {
            try {
                cs.close();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        isRunning = false;
    }

    public Socket getCs() {
        return cs;
    }
}
package se.gu.tux.truxserver;

import java.io.EOFException;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.ProtocolMessage;
import se.gu.tux.trux.datastructure.ProtocolMessage.Type;
import se.gu.tux.truxserver.dataswitch.DataSwitcher;
import se.gu.tux.truxserver.logger.Logger;


/**
 * Instances of this class handle the individual connections with clients.
 * ServerHandler manages the ServerRunnable objects in a thread pool.
 */
public class ServerRunnable implements Runnable {

    private boolean isRunning = true;
    private Socket cs = null;
    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;
    private long connectionId;
    private Thread currentThread;
    private long idleTime = 0;
    private long maxIdleTime = 0;

    /**
     * Constructor.
     * @param cs			The socket with the connection.
     * @param connectionId	The connection ID, used to mark debug output
     * @param maxIdleTime	A connection timeout
     */
    public ServerRunnable(Socket cs, long connectionId, long maxIdleTime) {
        this.cs = cs;
        this.connectionId = connectionId;
        this.maxIdleTime = maxIdleTime;
    }

    
    /**
     * The main loop. 
     */
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
//                        if(!(d instanceof MetricData) && !(d instanceof Heartbeat) && !(d instanceof Picture)) {
//                            Logger.gI().addError(d.getClass().getSimpleName());
//                        }
                    } catch (ClassCastException e) {
                        Logger.gI().addError(connectionId + ": Classcast:" + e.getLocalizedMessage());
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
                if (currentThread.isInterrupted() || timedOut
                        || (d instanceof ProtocolMessage && ((ProtocolMessage) d).getType()
                        == ProtocolMessage.Type.GOODBYE)) {
                    throw new InterruptedException();
                }

                // Send data to DataSwitcher
                d = DataSwitcher.gI().handleData(d);

                // If the server is shut down during the above handling, it will possibly return
                // a ProtocolMessage saying GOODBYE. No need to forward this to client, catch it
                // here to avoid casting errors on the client. The client will try to  reconnect 
                // eventually when it notices the connection went stale, and then it's up to the
                // server to be up and running...                
                if (d instanceof ProtocolMessage && ((ProtocolMessage) d).getType()
                        == Type.GOODBYE) {
                    Logger.gI().addMsg(connectionId + ": Received GOODBYE from DataSwitcher...");
                    throw new InterruptedException();
                }

                // Send data back to respond to the request or acknowledge.
                out.writeObject(d);
                out.flush();

                // Repeating the shutDown() call intentionally where needed below.
            } catch (InterruptedException e) {
                Logger.gI().addMsg(connectionId + ": Thread interrupted, shutting down...");
                shutDown();
            } catch (ClassNotFoundException e) {
                Logger.gI().addError(connectionId + ": Class not found! Exiting.");
                shutDown();
            } catch (InvalidClassException e) {
                Logger.gI().addError(connectionId + ": Client sent invalid class: " + e.getMessage());
            } catch (EOFException e) {
                Logger.gI().addDebug(connectionId + ": Client disconnected.");
                shutDown();
            } catch (StreamCorruptedException e) {
                Logger.gI().addError(connectionId + " Stream corrupted: " + e.getLocalizedMessage());
                shutDown();
            } catch (SocketException e) {
                Logger.gI().addDebug(connectionId + ": Socket exception - assuming server is "
                        + "shutting down or client disconnected.");
                shutDown();
            } catch (IOException e) {
                Logger.gI().addMsg(connectionId + ": Closing ServerRunnable socket... ("
                        + e.getClass() + ")");
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

    
    /**
     * Returns the socket with the client connection.
     * @return	The socket.
     */
    public Socket getCs() {
        return cs;
    }
}
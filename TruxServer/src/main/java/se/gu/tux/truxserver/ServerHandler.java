package se.gu.tux.truxserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import se.gu.tux.truxserver.logger.Logger;

public class ServerHandler implements Runnable {

    // Determines if the main loop should continue
    private boolean isRunning = true;
    // Server socket waits for connections
    private ServerSocket ss;
    private TruxServer truxServer;
    private ExecutorService threadPool
            = Executors.newFixedThreadPool(1000);
    private long nextConnectionId = 0;
    private int port = 0;
    private long connectionTimeout = 0;

    /**
     * Creates a new ServerHandler instance that listens for connections on the
     * specified port.
     *
     * @param truxServer	TruxServer object, in case we need to cancel execution
     * @param port	Port where the server listens to requests
     * @param connectionTimeout	Timeout in seconds for idle connections
     */
    public ServerHandler(TruxServer truxServer, int port, long connectionTimeout) {
        this.truxServer = truxServer;
        this.port = port;
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * The main server handler runnable. Listens for connections and diverts
     * them to threads running ServerRunnables.
     */
    @Override
    public void run() {

        // Initialize serversocket - NOTE port will be configurable later
        try {
            ss = new ServerSocket(port);
        } catch (IOException e2) {
            // Fatal error
            Logger.gI().addError("Fatal error on server socket initialization: "
                    + e2.getMessage());
            isRunning = false;
            truxServer.terminate();
        }

        // As long as the server is running - wait for connections, on connection
        // let the next server thread available handle the connection		
        while (isRunning()) {

            // An accepted incoming connection is handled in the thread pool
            try {
                Logger.gI().addDebug("Waiting for next connection...");
                Socket cs = ss.accept();

                Logger.gI().addDebug("Connection from " + cs.getInetAddress()
                        + ". Creating ServerRunnable instance...");

                threadPool.execute(new ServerRunnable(cs, nextConnectionId, connectionTimeout));
                nextConnectionId++;

            } catch (SocketException e) {

                // This can happen naturally when using stopServer() to shut down 
                // server. If so, isRunning is false. So if isRunning is true here,
                // socket was closed some other way and we need to make a nice shutdown
                if (isRunning()) {
                    stopServer();
                }

            } catch (IOException e) {

                Logger.gI().addDebug("IOException in ServerHandler. Resuming...");

            }
        }
    }

    public synchronized void stopServer() {
        // Switch flag so loop exits next time
        isRunning = false;

        // Stop all threads
        threadPool.shutdownNow();

        // Close server socket
        if (ss != null) {
            try {
                ss.close();
            } catch (IOException e) {
                Logger.gI().addError("Trouble shutting down server: " + e.getMessage());
            }
        }
        Logger.gI().addDebug("Server Handler shut down.");
    }

    public synchronized boolean isRunning() {
        return isRunning;
    }

}
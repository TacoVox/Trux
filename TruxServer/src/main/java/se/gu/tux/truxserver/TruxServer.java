package se.gu.tux.truxserver;

/*
 * TODO:
 *  - Add config file and argument parsing
 *  - Add database connection
 *  
 *  - Later: add user and session handling
 *  - Later: multiple server threads rotated by ServerHandler
 **/
import java.io.IOException;
import java.util.Scanner;

import se.gu.tux.truxserver.config.Config;
import se.gu.tux.truxserver.config.ConfigHandler;
import se.gu.tux.truxserver.dataswitch.DataSwitcher;
import se.gu.tux.truxserver.logger.Logger;

/**
 *
 * @author tville
 */
public class TruxServer {

    private Thread serverHandlerThread;
    private Thread mainThread;
    private Thread cleanupThread;
    private ServerHandler sh;
    private Scanner in = new Scanner(System.in);
    private boolean isRunning = true;

    public static void main(String[] args) {
        // Create instance immediately
        new TruxServer(args);
    }

    /**
     * Called by ServerHandler if for example port was not available to shut
     * down the whole application.
     */
    public synchronized void terminate() {
        // Interrupt the main thread waiting for keyboard input.
        // It will handle the interrupt properly.
        mainThread.interrupt();
    }

    private void stopServer() {
        // Stop the server
        mainThread.interrupt();
        cleanupThread.interrupt();
        isRunning = false;
        sh.stopServer();
        DataSwitcher.gI().stop();
    }

    public TruxServer(String[] args) {
        mainThread = Thread.currentThread();

        // Initialize config. Returns false on invalid arguments or on show help screen
        if (ConfigHandler.getInstance().setSettings(args)) {

            // Initialize logger with correct verbosity settings
            Logger.gI().setVerbose(Config.gI().isVerbose());
            Logger.gI().addDebug("Main thread: Starting the trux server...");

            // Start the data manager thread
            DataSwitcher.gI().start();

            // Start cleanup thread
            cleanupThread = new Thread(new CleanupRunnable(Config.gI().getCleanupInterval()));
            cleanupThread.start();

            // Start the server pool - start it in a wrapping thread so we can
            // interrupt it with keyboard input from the main thread.
            //sh = new ServerHandler(this, Config.gI().getServerPort(), Config.gI().getConnectionTimeout());
            sh = new ServerHandler(this, 12000, 60 * 15);

            serverHandlerThread = new Thread(sh);
            serverHandlerThread.start();
            Logger.gI().addMsg("Trux Server started.\nq followed by enter quits.");

            // While thread not interrupted, 
            // Check keyboard input for interruption or possibly options
            try {
                while (isRunning && hasNextLine()) {

                    // Handle keyboard input
                    String line = in.nextLine();
                    if (line.equals("q")) {

                        // Manual shutdown
                        stopServer();

                    }
                }
            } catch (InterruptedException e) {
                Logger.gI().addDebug("Main thread: Interrupted! Exiting...");
                // Interrupted - shut down
                stopServer();

            }

            Logger.gI().addDebug("Main thread: Bye!");
        }
    }

    /**
     * Need a way to be able to interrupt the thread waiting for keyboard input
     * if the server startup should be aborted.
     *
     * @return
     * @throws IOException
     */
    private boolean hasNextLine() throws InterruptedException {
        // Check if input is available every 100 ms. Throw InterruptedException
        // if interrupted.
        try {
            while (System.in.available() == 0) {
                Thread.sleep(100);
            }
        } catch (IOException e) {
            Logger.gI().addError("Main thread: Trouble reading System.in.");
        }
        return in.hasNextLine();
    }
}
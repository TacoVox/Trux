package se.gu.tux.truxserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import se.gu.tux.truxserver.dbconnect.UserHandler;
import se.gu.tux.truxserver.logger.Logger;

public class CleanupRunnable implements Runnable {
	// Determines if the main loop should continue
	private boolean isRunning = true;
	private int interval = 0;
	
	/**
	 * Construct a cleanupthread with the given interval in minutes
	 * between cleanup cycles.
	 * @param interval
	 */
	public CleanupRunnable(int intervalMinutes) {
		this.interval = 1000 * 60 * intervalMinutes;
	}
	
	@Override
	public void run() {
		Logger.gI().addMsg("Cleanup thread starting...");
		
    	// Run until interrupted
    	while(isRunning) {
    		try {
    			Logger.gI().addDebug("Cleanup thread performing tasks...");
    			
    			// Do any cleanup here below:
    			// UserHandler.gI().purgeSessions();
    			
    			// Sleep for a while
    			Thread.sleep(interval);
    			
    		} catch (InterruptedException e){
    			Logger.gI().addMsg("Cleanup thread stopping...");    			
    			isRunning = false;	
			} 
    	}
	}
}

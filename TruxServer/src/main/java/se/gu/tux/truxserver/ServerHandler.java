package se.gu.tux.truxserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import se.gu.tux.truxserver.logger.Logger;

public class ServerHandler implements Runnable {
	// Determines if the main loop should continue
	private boolean isRunning = true;
	// Server socket waits for connections
	private ServerSocket ss;
	
	
	@Override
	public void run() {
		
		// Initialize serversocket - NOTE port will be configurable later
		try {
			ss = new ServerSocket(12000);
		} catch (IOException e2) {
			// Fatal error
			Logger.gI().addError("Fatal error on server socket initialization: "
					+ e2.getMessage());
			isRunning = false;
		}
		
		
    	// As long as the server is running - wait for connections, on connection
		// let the next server thread available handle the connection		
    	while(isRunning) {
    		
    		// An accepted incoming connection is handled by a socket that is sent
    		// to the next available server thread
    		Thread t = null;
    		ServerRunnable sr = null;
    		try {
    			Logger.gI().addDebug("Waiting for next connection...");
				Socket cs = ss.accept();

	        	Logger.gI().addDebug("Connection from " + cs.getInetAddress() + 
	        			". Creating ServerRunnable instance...");
				
	    	  	sr = new ServerRunnable(cs);
	        	t = new Thread(sr);	        	
	        	t.start();
			
    		} catch (SocketException e){
    			// Switch flag so loop exits next time
				isRunning = false;		
				
				// NOTE later on this needs to interrupt ALL (active) pool threads
				// Call interrupt() and wait for t to quit
				Logger.gI().addDebug("Server Handler: Interrupting server runnable thread...");
				if (sr != null) {
					// Interrupt the "pool thread" by closing its socket
					try {
						sr.getCs().close();
					} catch (IOException e1) {}
				}				
		    	Logger.gI().addDebug("Server Handler: Bye....");
		    	
			} catch (IOException e) {
				
				Logger.gI().addDebug("IOException in ServerHandler. Resuming...");
				
			}    		
    	}    
	}


	public ServerSocket getSs() {
		return ss;
	}
}

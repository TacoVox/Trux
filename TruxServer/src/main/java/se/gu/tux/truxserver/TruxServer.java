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

import se.gu.tux.trux.datastructure.Fuel;
import se.gu.tux.truxserver.config.Config;
import se.gu.tux.truxserver.config.ConfigHandler;
import se.gu.tux.truxserver.dataswitch.DataSwitcher;
import se.gu.tux.truxserver.dbconnect.MetricInserter;
import se.gu.tux.truxserver.logger.Logger;

/**
 *
 * @author tville
 */
public class TruxServer {
	private Thread mainThread;
	private ServerHandler sh;
	private Scanner in = new Scanner(System.in);
	private boolean isRunning = true;
	
    public static void main(String[] args) {
    	// Create instance immediately
    	new TruxServer(args);
    }
    
    
    public TruxServer(String[] args) {
    	
    	// Initialize config. Returns false on invalid arguments or on show help screen
	    if (ConfigHandler.getInstance().setSettings(args)) {
	    	
	    	// Initialize logger with correct verbosity settings
	    	Logger.gI().setVerbose(Config.gI().isVerbose());
	    	Logger.gI().addDebug("Main method: Starting the trux server...");
	    	
	    	// Start the data manager thread
	    	DataSwitcher.gI().start();
	    	
	    	// Start the server pool - start it in a wrapping thread so we can
	    	// interrupt it with keyboard input from the main thread.
	    	sh = new ServerHandler();
	    	mainThread = new Thread(sh);
	    	mainThread.start();
	    	Logger.gI().addMsg("Trux Server started.\nq followed by enter quits.");
	    	
	    	// While thread not interrupted, 
	    	// Check keyboard input for interruption or possibly options
	    	while (isRunning && in.hasNextLine()) {
	    		
	    		// Handle keyboard input
	    		String line = in.nextLine();
	    		if (line.equals("q")) {
	    			
	    			// Close the socket in ServerHandler, it will notice the 
	    			// SocketException and close server pool sockets
	    			try {
						sh.getSs().close();
						isRunning = false;
						DataSwitcher.gI().stop();
					} catch (IOException e) {}
	    		}
	    	}    	
	    	
	    	Logger.gI().addDebug("Main method: Bye!");  
	    }
    }
}

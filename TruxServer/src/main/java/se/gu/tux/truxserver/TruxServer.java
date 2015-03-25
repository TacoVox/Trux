/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.gu.tux.truxserver;

import java.util.Scanner;

import se.gu.tux.truxserver.logger.Logger;

/**
 *
 * @author tville
 */
public class TruxServer {
	Thread mainThread;
	ServerHandler sh;
	Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
    	// Create instance immediately
    	new TruxServer();
    }
    
    
    public TruxServer() {
    	Logger.gI().addDebug("Main method: Starting the trux server...");
    	
    	// First create a wrapping thread here that can be interrupted
    	// by keyboard input
    	sh = new ServerHandler();
    	mainThread = new Thread(sh);
    	mainThread.start();
    	
    	Logger.gI().addMsg("Trux Server started.\nq followed by enter quits.");
    	
    	// While thread not interrupted, 
    	// Check keyboard input for interruption or possibly options
    	while (!mainThread.isInterrupted() && in.hasNextLine()) {
    		
    		// Handle keyboard input
    		String line = in.nextLine();
    		if (line.equals("q")) {
    			
    			// Quit by interrupting main thread
    			mainThread.interrupt();
    		}
    	}    	
    	
    	Logger.gI().addDebug("Main method: Bye!");  
    }    
}

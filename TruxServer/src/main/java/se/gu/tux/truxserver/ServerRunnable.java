package se.gu.tux.truxserver;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.channels.ClosedByInterruptException;

import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.Distance;
import se.gu.tux.trux.datastructure.Fuel;
import se.gu.tux.trux.datastructure.MetricData;
import se.gu.tux.trux.datastructure.Speed;
import se.gu.tux.truxserver.dataswitch.DataSwitcher;
import se.gu.tux.truxserver.logger.Logger;

public class ServerRunnable implements Runnable, ShutDownable {
	private boolean isRunning = true;
	private Socket cs = null;
	private ObjectInputStream in = null;
	private ObjectOutputStream out = null;
	private long connectionId;
	private Thread currentThread;
	
	public ServerRunnable(Socket cs, long connectionId) {
		this.cs = cs;
		this.connectionId = connectionId;
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
				while (d == null && !currentThread.isInterrupted()) {
					try {						
						// Read data - this blocks until the defined soTimeout, then repeats
						// So we do nothing on the exception, it's just catched there to keep the 
						// loop running at regular intervals
						d = (Data)in.readObject();
					} catch(SocketTimeoutException e) {}					
				}
				
				// If thread was interrupted while waiting for input, just shut down
				if (currentThread.isInterrupted()) {
					Logger.gI().addMsg(connectionId + ": Thread interrupted, shutting down...");
					shutDown();
					return;
				}				
				
				// Debugging output
				if (d.getValue() != null) {
					Logger.gI().addMsg(d.getValue().toString());
				} else {
					Logger.gI().addMsg(connectionId + ": Received object with null value from " + cs.getInetAddress());
				}
				if (d.getValue() != null) System.out.println("v: " + d.getValue().toString());
				
				// Send data to DataSwitcher
				if (!(d instanceof Distance)) {
					d = DataSwitcher.gI().handleData(d);
				} else {
					Logger.gI().addError(connectionId + ": Skipping distance object until datatype decisions are resolved!");			
				}
				
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
 
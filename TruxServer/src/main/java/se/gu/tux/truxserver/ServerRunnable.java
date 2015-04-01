package se.gu.tux.truxserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.Fuel;
import se.gu.tux.trux.datastructure.Speed;
import se.gu.tux.truxserver.logger.Logger;

public class ServerRunnable implements Runnable {
	private boolean isRunning = true;
	private Socket cs = null;
	private ObjectInputStream in = null;
	private ObjectOutputStream out = null;
	
	public ServerRunnable(Socket cs) {
		this.cs = cs;
	}
	
	@Override
	public void run() {		
		Logger.gI().addDebug("Inner Server Runnable: Starting server thread...");
		try {
			out = new ObjectOutputStream(cs.getOutputStream());
			in = new ObjectInputStream(cs.getInputStream());			
		} catch (IOException e) {
			Logger.gI().addDebug("Fatal on stream creation in ServerRunnable: "
					+ e.getMessage());
			isRunning = false;
		}
		
		while (isRunning) {
			try {
				
				// TODO: Here we should send objects to some data handling class
				// that checks wheter it's a metric or something else that goes into
				// the database, OR if its some query that should be responded to
				// Just let that class respond with some empty ACK data object if it's
				// just metric for the DB. 				
				
				// For now, just display the tostring and then return the object
				Data d = (Data)in.readObject();
				if (d.getValue() != null) {
					Logger.gI().addMsg(d.getValue().toString());
				} else {
					Logger.gI().addMsg("Received object with null value from " + cs.getInetAddress());
				}
				d.setValue(new Double(2.0));
				out.writeObject(d);
				
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				Logger.gI().addError("Class not found!");
				isRunning = false;
			} catch (IOException e) {
				Logger.gI().addMsg("Closing ServerRunnable socket...");
				isRunning = false;
			}
		}	
	}

	public Socket getCs() {
		return cs;
	}
}
 
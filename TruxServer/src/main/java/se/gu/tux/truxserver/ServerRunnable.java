package se.gu.tux.truxserver;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.Distance;
import se.gu.tux.trux.datastructure.Fuel;
import se.gu.tux.trux.datastructure.MetricData;
import se.gu.tux.trux.datastructure.Speed;
import se.gu.tux.truxserver.dataswitch.DataSwitcher;
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
				
				// For now, just display the tostring and then return the object
				Data d = (Data)in.readObject();
				if (d.getValue() != null) {
					Logger.gI().addMsg(d.getValue().toString());
				} else {
					Logger.gI().addMsg("Received object with null value from " + cs.getInetAddress());
				}
				
				System.out.println("O: " + d.getClass());
				if (d.getValue() != null) System.out.println("v: " + d.getValue().toString());
				
				if (!(d instanceof Distance)) {
					d = DataSwitcher.gI().handleData(d);
				} else {
					Logger.gI().addError("Skipping distance object until datatype decisions are resolved!");			
				}
				
				out.writeObject(d);
				
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				Logger.gI().addError("Class not found!");
				isRunning = false;
			} catch (InvalidClassException e) {
				Logger.gI().addError("Client sent invalid class: " + e.getMessage());
			} catch (IOException e) {
				Logger.gI().addMsg("Closing ServerRunnable socket... (" + e.getClass() + ")");
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
		}	
	}

	public Socket getCs() {
		return cs;
	}
}
 
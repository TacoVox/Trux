package se.gu.tux.truxserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import se.gu.tux.trux.datastructure.Fuel;
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
			in = new ObjectInputStream(cs.getInputStream());
			out = new ObjectOutputStream(cs.getOutputStream());
		} catch (IOException e) {
			Logger.gI().addDebug("Fatal on stream creation in ServerRunnable: "
					+ e.getMessage());
			isRunning = false;
		}
		
		while (isRunning) {
			try {
				// Log/display the objects toString() and then return it
				Fuel f = (Fuel)in.readObject();
				Logger.gI().addMsg(f.getValue().toString());
				f.setValue(new Double(2.0));
				out.writeObject(f);
				
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
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

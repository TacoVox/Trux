package se.gu.tux.truxserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import se.gu.tux.trux.datastructure.Fuel;

public class TestClient {

    private Socket cs = null;
    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;

    public static void main(String[] args) {
        new TestClient().run();
    }

    public TestClient() {

    }

    public void run() {
        try {
            System.out.println("Connecting...");
            cs = new Socket("127.0.0.1", 12000);
            out = new ObjectOutputStream(cs.getOutputStream());
            in = new ObjectInputStream(cs.getInputStream());

            System.out.println("Trying to send fuel object to server...");
            Fuel f = new Fuel(0);
            f.setValue(new Double(1.0));
            out.writeObject(f);
            f = (Fuel) in.readObject();
            System.out.println("Received: " + f.getValue().toString());

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
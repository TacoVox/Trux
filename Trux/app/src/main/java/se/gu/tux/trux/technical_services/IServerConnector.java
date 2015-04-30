package se.gu.tux.trux.technical_services;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;

import se.gu.tux.trux.datastructure.Data;

/**
 *
 * This class was created for testing purposes. It has the same function and structure as
 * ServerConnector.java. I was just thinking to introduce maybe two threads that run at the
 * same time -- one for querying the server when a message is received and one for just taking
 * what's in the queue and sending it to the server without worrying about anything else -- so
 * in this case perhaps simplify the process and have more control overall. The class as it is
 * now works fine with the server and is receiving and sending data accordingly.
 *
 * TODO: implement query thread to listen for query signals, then connect to server and execute.
 * This will require running in separate thread as proposed.
 *
 *
 *
 * Created by ivryashkov on 2015-04-06.
 */
public class IServerConnector
{

    private static IServerConnector iServerConnector = null;

    private Thread transmitThread = null;
    private ConnectorRunnable connector = null;
    private static LinkedBlockingQueue<Data> queue;


    private IServerConnector()
    {
        queue = new LinkedBlockingQueue<>();
    }


    public static IServerConnector getInstance()
    {
        if (iServerConnector == null)
        {
            iServerConnector = new IServerConnector();
        }

        return iServerConnector;

    }


    public void connectTo(String address)
    {
        connector = new ConnectorRunnable(address);
        transmitThread = new Thread(connector);
        transmitThread.start();

    }


    public void disconnect()
    {
        if (connector != null)
        {
            queue.clear();
            connector.stopRun();
        }

    }


    public void receiveData(Data data)
    {
        queue.add(data);
    }


    public Data answerQuery(Data data)
    {
        return connector.sendQuery(data);
    }






    private static class ConnectorRunnable implements Runnable
    {

        private static String serverAddress;

        private Socket cs = null;
        private ObjectInputStream in = null;
        private ObjectOutputStream out = null;

        // controls the main thread
        private volatile boolean isRunning;


        public ConnectorRunnable(String address)
        {
            serverAddress = address;
            isRunning = true;
        }


        public void stopRun()
        {
            if (cs != null || !cs.isClosed() || out != null || in != null)
            {
                try
                {
                    cs.close();
                    in.close();
                    out.close();
                    isRunning = false;
                }
                catch (IOException e) { e.printStackTrace(); }
            }

        }



        public synchronized Data sendQuery(Data data)
        {
            // if data is null, return
            if (data == null)   { return null; }

            // check if the out and in streams are open, if not connect
            if (cs == null || cs.isClosed() || out == null || in == null)
            { connect(); }

            // send the query, at this point should not be any problems
            // with the connection or the streams
            try
            {
                out.writeObject(data);

                return (Data) in.readObject();
            }
            catch (IOException e)
            {
                // in case something went wrong!
                // close the socket and streams, this will also invoke
                // main thread to try and connect
                try
                {
                    cs.close();
                    in.close();
                    out.close();
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                }

                System.out.println("\nIOException in ConnectorRunnable sendQuery()\n");
                e.printStackTrace();
            }
            catch (ClassNotFoundException e)
            {
                System.out.println("\nClassNotFoundException in ConnectorRunnable sendQuery()\n");
                e.printStackTrace();
            }

            return null;

        }


        private synchronized void connect()
        {

            while (cs == null || cs.isClosed() || out == null || in == null)
            {
                try
                {
                    System.out.println("Connecting to " + serverAddress);

                    cs = new Socket(serverAddress, 12000);
                    out = new ObjectOutputStream(cs.getOutputStream());
                    in = new ObjectInputStream(cs.getInputStream());

                }
                catch (StreamCorruptedException e)
                {
                    System.out.println("\nStreamCorruptException in ConnectorRunnable connect()\n");
                    e.printStackTrace();
                }
                catch (UnknownHostException e)
                {
                    System.out.println("\nUnknownHostException in ConnectorRunnable connect()\n");
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    // Problem connecting.
                    System.err.println("Could not connect to " + serverAddress +
                            ". Retrying in 10s...");
                    e.printStackTrace();

                    // close all channels
                    try
                    {
                        cs.close();
                        in.close();
                        out.close();
                    }
                    catch (IOException e1)
                    {
                        e1.printStackTrace();
                    }
                }

                try
                {
                    Thread.sleep(10000);
                }
                catch (InterruptedException e)
                {
                    System.out.println("\nInterrupted!\n");
                    e.printStackTrace();
                }

            } // end while

        } // end connect()


        @Override
        public void run()
        {

            while (isRunning)
            {

                // each iteration first check if the socket is open and we have
                // connection to server
                if (cs == null || cs.isClosed() || out == null || in == null)
                {
                    connect();
                }

                // we connected or have already running connection
                try
                {
                    Data data = null;

                    // if the queue is not empty, peek at the next available element
                    // do not remove!
                    // later when we are done with the object, we will remove it
                    // we do that in case transmission is interrupted, if it is
                    // then we don't want to remove the object, since it is in a queue
                    // and priority matters, so in that case we simply don't remove it
                    if (!queue.isEmpty())
                    {
                        data = queue.peek();
                    }


                    // if the element is not null, do stuff...
                    if (data != null)
                    {
                        System.out.println("Server connector: Sending...");

                        out.writeObject(data);

                        Data dataIn = (Data) in.readObject();

                        if (dataIn.getValue() == null)
                        {
                            System.out.println("Server connector: Received data with null value");
                        }
                        else
                        {
                            System.out.println("Server connector: Received data " + dataIn.getValue().toString());
                        }

                        // make sure everything is fine with channels
                        // after reading and writing
                        // remove object from queue here, last call in method
                        if (cs != null && out != null && in != null)
                        {
                            System.out.println("\nRemoving object from queue in ConnectorRunnable run()\n");

                            queue.poll();
                        }

                    }

                }
                catch (IOException e)
                {
                    // in case something went wrong with writing and reading
                    // to the streams close channels to invoke reconnection
                    try
                    {
                        cs.close();
                        in.close();
                        out.close();
                    }
                    catch (IOException e1)
                    {
                        e1.printStackTrace();
                    }

                    System.out.println("\nIOException in ConnectorRunnable run()\n");
                    e.printStackTrace();
                }
                catch (ClassNotFoundException e)
                {
                    System.out.println("\nClassNotFoundException in ConnectorRunnable run()\n");
                    e.printStackTrace();
                }

            } // end while (isRunning)

        } // end run()

    } // end nested class



} // end class

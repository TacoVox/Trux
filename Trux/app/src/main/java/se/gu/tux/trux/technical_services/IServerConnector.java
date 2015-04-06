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

/*
    public Data answerQuery(Data data)
    {
        //return connector.sendQuery(data);
    }
*/


    // to implement
    class QueryRunnable implements Runnable
    {

        @Override
        public void run()
        {

        }

    } // end nested class



    private static class ConnectorRunnable implements Runnable
    {

        private String serverAddress;

        private Socket cs = null;
        private ObjectInputStream in = null;
        private ObjectOutputStream out = null;

        private volatile boolean isRunning;


        public ConnectorRunnable(String address)
        {
            serverAddress = address;
            isRunning = true;
        }


        public void stopRun()
        {
            if (cs != null)
            {
                try
                {
                    cs.close();
                    isRunning = false;
                }
                catch (IOException e) { e.printStackTrace(); }
            }

        }


        // maybe have this method in a separate thread

/*
        public synchronized Data sendQuery(Data data)
        {
            try
            {
                out.writeObject(data);

                return (Data) in.readObject();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            }

            return null;

        }
*/

        @Override
        public void run()
        {

            while (isRunning)
            {

                while (cs == null)
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
                        e.printStackTrace();
                    }
                    catch (UnknownHostException e)
                    {
                        e.printStackTrace();
                    }
                    catch (IOException e)
                    {
                        // Problem connecting.
                        System.err.println("Could not connect to " + serverAddress +
                                ". Retrying in 10s...");
                        e.printStackTrace();
                    }

                    try
                    {
                        Thread.sleep(10000);
                    }
                    catch (InterruptedException e)
                    {
                        System.out.println("Interrupted!");
                        e.printStackTrace();
                    }

                } // end while (cs == null)


                try
                {

                    Data data = queue.take();

                    if (data != null)
                    {
                        System.out.println("Server connector: Sending...");

                        out.writeObject(data);

                        Data dataIn = (Data) in.readObject();

                        if (dataIn.getValue() == null) {
                            System.out.println("Server connector: Received data with null value");
                        } else {
                            System.out.println("Server connector: Received data " + dataIn.getValue().toString());
                        }

                    }

                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                catch (ClassNotFoundException e)
                {
                    e.printStackTrace();
                }

            } // end while (isRunning)

        } // end run()

    } // end nested class



} // end class

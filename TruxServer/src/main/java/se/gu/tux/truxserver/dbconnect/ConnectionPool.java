/*
 * Copyright 2015 jonas.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.gu.tux.truxserver.dbconnect;

import java.util.concurrent.LinkedBlockingQueue;
import se.gu.tux.truxserver.config.Config;
import se.gu.tux.truxserver.logger.Logger;

/**
 * This singleton class provides a pool of database connections.
 * This pool is threadsafe.
 * 
 * @author Jonas Kahler
 */
public class ConnectionPool {
    /**
     * Static part.
     */
    private static ConnectionPool cp = null;
    
    /**
     * Method for getting the instance of the pool.
     * 
     * @return an Instance of ConnectionPool
     */
    public static ConnectionPool getInstance() {
        if(cp == null) {
            synchronized (ConnectionPool.class) {
                if (cp == null) {
                    cp = new ConnectionPool();
                }
            }
        }
        return cp;
    }
    
    /**
     * Method for getting the instance of the pool.
     * 
     * @return an Instance of ConnectionPool
     */
    public static ConnectionPool gI() {
        return getInstance();
    }
    
    /**
     * Non-static part.
     */
    private final short MAXCONNECTIONS = Config.gI().getMaxNoDBConnections();

    private volatile LinkedBlockingQueue queue = null;
    //private int motherfucker = Config.gI().getMaxNoDBConnections();
    
    /**
     * Private Constructor.
     */
    private ConnectionPool() {
        queue = new LinkedBlockingQueue();
        
        for(int i = 0; i < MAXCONNECTIONS; i++)
            queue.add(addDBConnector());
    } 
    
    /**
     * Method for adding a connector to the pool.
     * 
     * @return a DBConnector object
     */
    private DBConnector addDBConnector() {
        DBConnector dbc = new DBConnector();
        dbc.openConnection();
        
        return dbc;
    }
    
    /**
     * Method for requesting a connector for the pool.
     * 
     * @return a connector as soon as a connector is available.
     */
    public DBConnector getDBC() throws InterruptedException{
        try {
            
            DBConnector dbc = (DBConnector)queue.poll();
            while (dbc == null) {
                Logger.gI().addDebug("Waiting for dbc");
                Thread.sleep(1000);
                dbc = (DBConnector)queue.poll();
            }
                  
            //motherfucker--;
            //if (dbc == null) {
            //    Logger.gI().addError("Queue take returned null!");
            //} else {
            //    Logger.gI().addDebug("Queue take returned a dbc. Amount: " + Integer.toString(motherfucker));
            //}
            return dbc;
            
        } catch (Exception e) {
            e.printStackTrace();
            Logger.gI().addError(e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Method for releasing a connector back to our pool when it is not used anymore.
     * 
     * @param dbc a DBConnector to be released back to the pool
     */
    public void releaseDBC(DBConnector dbc){
        //motherfucker++;
        if(dbc != null)
        {
            while(!queue.offer(dbc)) {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //Logger.gI().addMsg("A dbc was released. Amount: " + Integer.toString(motherfucker));
            
        } //else {
         //   Logger.gI().addError("Someone tried to insert a null pointer to a DBC.");
        //}
    }
}

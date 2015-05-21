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
    public synchronized static ConnectionPool getInstance() {
        if(cp == null)
            cp = new ConnectionPool();
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

    private LinkedBlockingQueue queue = null;
    
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
    public synchronized DBConnector getDBC() {
        try {
            return (DBConnector)queue.take();
        }
        catch (Exception e) {
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
    public synchronized void releaseDBC(DBConnector dbc){
        if(dbc != null)
        {
            while(!queue.offer(dbc)) {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            Logger.gI().addError("Someone tried to insert a null pointer to a DBC.");
        }
    }
}

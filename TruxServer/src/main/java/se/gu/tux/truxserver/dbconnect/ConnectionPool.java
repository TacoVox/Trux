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
 *
 * @author jonas
 */
public class ConnectionPool {
    /**
     * Static part.
     */
    private static ConnectionPool cp = null;
    
    public static ConnectionPool getInstance() {
        if(cp == null)
            cp = new ConnectionPool();
        return cp;
    }
    
    public static ConnectionPool gI() {
        return getInstance();
    }
    
    /**
     * Non-static part.
     */
    private final short MAXCONNECTIONS = Config.gI().getMaxNoDBConnections();

    private LinkedBlockingQueue queue = null;
    
    private ConnectionPool() {
        queue = new LinkedBlockingQueue();
        
        for(int i = 0; i < MAXCONNECTIONS; i++)
            queue.add(addDBConnector());
    } 
    
    private DBConnector addDBConnector() {
        DBConnector dbc = new DBConnector();
        dbc.openConnection();
        
        return dbc;
    }
    
    public DBConnector getDBC() {
        try {
            return (DBConnector)queue.take();
        }
        catch (Exception e) {
            Logger.gI().addError(e.getMessage());
        }
        
        return null;
    }
    
    public void releaseDBC(DBConnector dbc) {
        queue.add(dbc);
    }
}

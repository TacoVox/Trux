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

import java.sql.PreparedStatement;
import java.util.concurrent.LinkedBlockingQueue;

import se.gu.tux.trux.datastructure.MetricData;

import se.gu.tux.truxserver.logger.Logger;
/**
 *
 * @author jonas
 */
public class MetricInserter implements Runnable {
    /**
     * Static part.
     */
    private static MetricInserter mi;
    
    public static MetricInserter getInstance()
    {
        if(mi ==  null)
            mi = new MetricInserter();
        return mi;
    }
    
    public static MetricInserter gI()
    {
        return getInstance();
    }
    
    /**
     * Non-static part.
     */
    private LinkedBlockingQueue queue;
    
    private MetricInserter() {
        queue = new LinkedBlockingQueue();
    }
    
    @Override
    public void run() {
        Logger.gI().addMsg("InsertionHandler is running and waiting for input");
        
        boolean running = true;
        
        while(running) {
            try {
                insertMetric((MetricData)queue.take());
            }
            catch (InterruptedException e) {
            	// Received interrupt() call from managing thread
            	running = false;
            }
            catch (Exception e) {
                Logger.gI().addError(e.getMessage());
            }
        }
        
    }
    
    public synchronized void addToDB(MetricData md) {
        queue.add(md);
    }
    
    private boolean insertMetric(MetricData md)
    {
        String type = md.getClass().getSimpleName().toLowerCase();
        
    	if (md.getValue() == null) {
            Logger.getInstance().addError("Somebody tried to insert an empty data object.");
    		return false; 
        }
        
        DBConnector dbc = ConnectionPool.gI().getDBC();
        try
        {   
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    "INSERT INTO " + type + "(value, timestamp, userid) " + 
                            "VALUES(?, ?, ?);");
            
            pst.setObject(1, md.getValue());
            
            pst.setLong(2, md.getTimeStamp());
            pst.setLong(3, 0);
		
            pst.executeUpdate();
            
            return true;
        }
        catch (Exception e)
        {
            Logger.gI().addError(e.toString());
        }
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
        
	return false;
    }
}

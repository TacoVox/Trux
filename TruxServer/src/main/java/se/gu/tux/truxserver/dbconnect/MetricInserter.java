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
import se.gu.tux.trux.datastructure.Location;

import se.gu.tux.trux.datastructure.MetricData;

import se.gu.tux.truxserver.logger.Logger;

/**
 * Class taking care of all MetricData inserts to the DB.
 * 
 * @author Jonas Kahler
 */
public class MetricInserter implements Runnable {
    /**
     * Static part.
     */
    private static MetricInserter mi;
    
    /**
     * Method for getting the instance of the MetricInserter.
     * 
     * @return an Instance of MetricInserter
     */
    public static MetricInserter getInstance()
    {
        if(mi ==  null)
            mi = new MetricInserter();
        return mi;
    }
    
    /**
     * Method for getting the instance of the MetricInserter.
     * 
     * @return an Instance of MetricInserter
     */
    public static MetricInserter gI()
    {
        return getInstance();
    }
    
    /**
     * Non-static part.
     */
    private LinkedBlockingQueue queue;
    
    /**
     * Constructor.
     */
    private MetricInserter() {
        queue = new LinkedBlockingQueue();
    }
    
    /**
     * Run method.
     */
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
                Logger.gI().addError(e.getLocalizedMessage());
            }
        }
        
    }
    
    /**
     * Method to add MetricData to the queue.
     * This will add the MetricData to a Queue which will take care of all jobs.
     * 
     * @param md the MetricData object which shall be inserted to the DB
     */
    public synchronized void addToDB(MetricData md) {
        queue.add(md);
    }
    
    /**
     * Method which inserts the Metric to the DB.
     * 
     * @param md a MetricData object
     * 
     * @return success or notl
     */
    private boolean insertMetric(MetricData md)
    {
        String type = md.getClass().getSimpleName().toLowerCase();
        
    	if (md.getValue() == null) {
            Logger.getInstance().addError("Somebody tried to insert an empty data object. " +
                    "Type :" + md.getClass().getSimpleName());
    		return false; 
        }
        
        DBConnector dbc = ConnectionPool.gI().getDBC();
        try
        {   
            PreparedStatement pst;
            
            if(md instanceof Location) {
                pst = dbc.getConnection().prepareStatement(
                    "INSERT INTO location (latitude, logitude, timestamp, userid, sessionid) "
                            + "SELECT * FROM (SELECT ?, ?, ?, ?, ?) AS tmp");
                pst.setDouble(1, ((double[])md.getValue())[0]);
                pst.setObject(2, ((double[])md.getValue())[1]);
                pst.setLong(3, md.getTimeStamp());
                pst.setLong(4, md.getUserId());
                pst.setLong(5, md.getSignalId());
            } else {
                pst = dbc.getConnection().prepareStatement(
                    "INSERT INTO " + type + "(value, timestamp, userid) "
                            + "SELECT * FROM (SELECT ?, ?, ?) AS tmp");
                
                pst.setObject(1, md.getValue());
                pst.setLong(2, md.getTimeStamp());
                pst.setLong(3, md.getUserId());
            }
            
            dbc.execInsert(md, pst);
            
            return true;
        }
        catch (Exception e)
        {
            Logger.gI().addError(e.getLocalizedMessage());
        }
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
        
	return false;
    }
}

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
import java.sql.ResultSet;

import se.gu.tux.trux.datastructure.Distance;
import se.gu.tux.trux.datastructure.Fuel;
import se.gu.tux.trux.datastructure.MetricData;
import se.gu.tux.trux.datastructure.Speed;

import se.gu.tux.truxserver.logger.Logger;

/**
 * Method taking care of all MetricData receives from the DB.
 * 
 * @author Jonas Kahler
 */
public class MetricReceiver {
    /**
     * Static part.
     */
    private static MetricReceiver mr = null;
    
    /**
     * Method for getting the instance of the MetricReceiver.
     * 
     * @return an Instance of MetricReceiver
     */
    public static MetricReceiver getInstance() {
        if (mr == null)
            mr = new MetricReceiver();
        return mr;
    }
    
    /**
     * Method for getting the instance of the MetricReceiver.
     * 
     * @return an Instance of MetricReceiver
     */
    public static MetricReceiver gI() {
        return getInstance();
    }
    
    /**
     * Non-static part.
     */
    
    /**
     * Private Constructor. Not acessable.
     */
    private MetricReceiver() {}
    
    /**
     * Method figuring with what method to fetch MetricData from the DB.
     * 
     * @param md the scaletton of the MetricData to fetch.
     * 
     * @return a filled MetricData object.
     */
    public MetricData getMetric(MetricData md)
    {
        if(md instanceof Fuel || md instanceof Speed) {            
            //Set the value to a default 0
            md.setValue((Double) 0.0);
            
            return getAverage(md);
        }
        else if(md instanceof Distance) {
            //Set the value to a default 0
            md.setValue(new Long(0));
            
            return getDiff(md);
        }
        else
            return null;
    }
    
    /**
     * Method to fetch Metric Data from the DB with the AVG.
     * 
     * @param md the scaletton of the MetricData to fetch.
     * 
     * @return a filled MetricData object.
     */
    private MetricData getAverage(MetricData md)
    {
        String type = md.getClass().getSimpleName().toLowerCase();
        
        DBConnector dbc = ConnectionPool.gI().getDBC();
        
        try
	{
            String selectStmnt = "SELECT AVG(value) AS avg FROM " + type +
                    " WHERE userid = ? AND timestamp BETWEEN ? AND ?";
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    selectStmnt);

            pst.setLong(1, md.getUserId());
	    pst.setLong(2, (md.getTimeStamp() - md.getTimeFrame()));
            pst.setLong(3, md.getTimeStamp()); 
            
            ResultSet rs = dbc.execSelect(md, pst);
            
	    while (rs.next())
	    {
		md.setValue(rs.getDouble("avg"));
		break;
	    }
	}
	catch (Exception e)
	{
	    Logger.gI().addError(e.getMessage());
	}
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
        
        return md;
    }
    
    /**
     * Method to fetch Metric Data from the DB with the SUM.
     * 
     * @param md the scaletton of the MetricData to fetch.
     * 
     * @return a filled MetricData object.
     */
    private MetricData getSum(MetricData md)
    {
        String type = md.getClass().getSimpleName().toLowerCase();
        
        DBConnector dbc = ConnectionPool.gI().getDBC();
        
        try
	{
            String selectStmnt = "SELECT SUM(value) AS sum FROM " + type +
                    " WHERE userid = ? AND timestamp BETWEEN ? AND ?";
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    selectStmnt);
            
	    ResultSet rs = dbc.execSelect(md, pst);
            
	    while (rs.next())
	    {
		md.setValue(rs.getObject("sum"));
		break;
	    }
	}
	catch (Exception e)
	{
	    Logger.gI().addError(e.getLocalizedMessage());
	}
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
        
        return md;
    }
    
    /**
     * Method to fetch Metric Data from the DB with the DIFF.
     * 
     * @param md the scaletton of the MetricData to fetch.
     * 
     * @return a filled MetricData object.
     */
    private MetricData getDiff(MetricData md)
    {
        String type = md.getClass().getSimpleName().toLowerCase();
        
        DBConnector dbc = ConnectionPool.gI().getDBC();
        
        try
	{
            String selectStmnt = "SELECT (SELECT value FROM " + type + " WHERE "
                    + "userid = ? ORDER BY (timestamp - ?) DESC LIMIT 1) "
                    + "- (SELECT (value * - 1) FROM " + type + " WHERE "
                    + "userid = ? ORDER BY (timestamp - ?) ASC LIMIT 1) AS diff";
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    selectStmnt);
	    
            pst.setLong(1, md.getUserId());
            pst.setLong(2, md.getTimeStamp());
            pst.setLong(3, md.getUserId());
            pst.setLong(4, md.getTimeStamp() - md.getTimeFrame());          
	    
	    ResultSet rs = dbc.execSelect(md, pst);
            
	    while (rs.next())
	    {
		md.setValue(rs.getLong("diff"));
		break;
	    }
	}
	catch (Exception e)
	{
	    Logger.gI().addError(e.getLocalizedMessage());
	}
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
        
        return md;
    }
}

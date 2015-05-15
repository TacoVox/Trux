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
import se.gu.tux.trux.datastructure.Location;
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
        } else if(md instanceof Distance) {
            //Set the value to a default 0
            md.setValue(new Long(0));
            
            return getDiff(md);
        } else if(md instanceof Location) {
            return getLocation((Location)md);
        } else
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

            //Logger.gI().addDebug("Timestamp: " + Long.toString(md.getTimeStamp()));
            //Logger.gI().addDebug("Timeframe: " + Long.toString(md.getTimeFrame()));
            
            pst.setLong(1, md.getUserId());
            
            if(md.getTimeFrame() == MetricData.FOREVER)
                pst.setLong(2, 0);
            else
                pst.setLong(2, (md.getTimeStamp() - md.getTimeFrame()));
            
            pst.setLong(3, md.getTimeStamp()); 
            
            ResultSet rs = dbc.execSelect(md, pst);
            
	    while (rs.next())
	    {
		md.setValue((Double)rs.getDouble("avg"));
		break;
	    }
            
            //Logger.gI().addDebug("After query: " + Double.toString((Double)md.getValue()));
	}
	catch (Exception e)
	{
            e.printStackTrace();
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
            e.printStackTrace();
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
            long val = 0;
            
            String selectStmnt = "SELECT value FROM " + type + " WHERE "
                    + "userid = ? AND timestamp < ? ORDER BY timestamp DESC LIMIT 1";
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    selectStmnt);
	    
            pst.setLong(1, md.getUserId());
            pst.setLong(2, md.getTimeStamp());        
	    
	    ResultSet rs = dbc.execSelect(md, pst);
            
	    while (rs.next())
	    {
		val = rs.getLong("value");
		break;
	    }
            
            if(md.getTimeFrame() == MetricData.FOREVER)
                pst.setLong(2, 0);
            else
                pst.setLong(2, (md.getTimeStamp() - md.getTimeFrame())); 
            
            rs = dbc.execSelect(md, pst);
            
	    while (rs.next())
	    {
		val -= rs.getLong("value");
		break;
	    }
            
            md.setValue(val);
            
            return md;
	}
	catch (Exception e)
	{
            e.printStackTrace();
	    Logger.gI().addError(e.getLocalizedMessage());
	}
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
        
        return md;
    }
    
    private Location getLocation(Location loc) {
        DBConnector dbc = ConnectionPool.gI().getDBC();

        try
	{
            long val = 0;
            
            String selectStmnt = "SELECT latitude, longitude FROM location WHERE "
                    + "userid = ? AND timestamp < ? ORDER BY timestamp DESC LIMIT 1";
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    selectStmnt);
	    
            pst.setLong(1, loc.getUserId());
            pst.setLong(2, System.currentTimeMillis());        
	    
	    ResultSet rs = dbc.execSelect(loc, pst);
            
	    while (rs.next())
	    {
		val = rs.getLong("value");
		break;
	    }
            
            rs = dbc.execSelect(loc, pst);
            
	    while (rs.next())
	    {
		loc.setLatitude(rs.getDouble("latitude"));
                loc.setLongitude(rs.getDouble("longitude"));
		break;
            }
            
            return loc;
            
        } catch (Exception e)
	{
            e.printStackTrace();
	    Logger.gI().addError(e.getLocalizedMessage());
	}
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
        return loc;
    }
}

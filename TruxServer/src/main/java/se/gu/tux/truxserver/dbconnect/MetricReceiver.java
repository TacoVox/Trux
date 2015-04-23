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
 *
 * @author jonas
 */
public class MetricReceiver {
    /**
     * Static part.
     */
    private static MetricReceiver mr = null;
    
    public static MetricReceiver getInstance() {
        if (mr == null)
            mr = new MetricReceiver();
        return mr;
    }
    
    public static MetricReceiver gI() {
        return getInstance();
    }
    
    /**
     * Non-static part.
     */
    private MetricReceiver() {}
    
    public MetricData getMetric(MetricData md)
    {
        if(md instanceof Fuel || md instanceof Speed)
            return getAverage(md);
        else if(md instanceof Distance)
            return getDiff(md);
        else
            return null;
    }
    
    private MetricData getAverage(MetricData md)
    {
        String type = md.getClass().getSimpleName().toLowerCase();
        
        DBConnector dbc = ConnectionPool.gI().getDBC();
        
        try
	{
            String selectStmnt = "SELECT AVG(value) AS avg FROM " + type +
                    " WHERE userid = ? AND timestamp BETWEEN ? AND ?;";
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    selectStmnt);
	    
            pst.setLong(1, 0);
	    pst.setLong(2, md.getTimeStamp() - md.getTimeFrame());
            pst.setLong(3, md.getTimeStamp()); 
            
	    ResultSet rs = dbc.execSelect(md, pst);
	    
	    while (rs.next())
	    {
		md.setValue(rs.getObject("avg"));
		break;
	    }
	}
	catch (Exception e)
	{
	    Logger.gI().addError(e.toString());
	}
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
        
        return md;
    }
    
    private MetricData getSum(MetricData md)
    {
        String type = md.getClass().getSimpleName().toLowerCase();
        
        DBConnector dbc = ConnectionPool.gI().getDBC();
        
        try
	{
            String selectStmnt = "SELECT SUM(value) AS sum FROM " + type +
                    " WHERE userid = ? AND timestamp BETWEEN ? AND ?;";
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    selectStmnt);
	    
            pst.setLong(1, 0);
	    pst.setLong(2, md.getTimeStamp() - md.getTimeFrame());
            pst.setLong(3, md.getTimeStamp()); 
	    
	    ResultSet rs = dbc.execSelect(md, pst);
	    
	    while (rs.next())
	    {
		md.setValue(rs.getObject("sum"));
		break;
	    }
	}
	catch (Exception e)
	{
	    Logger.gI().addError(e.toString());
	}
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
        
        return md;
    }
    
    private MetricData getDiff(MetricData md)
    {
        String type = md.getClass().getSimpleName().toLowerCase();
        
        DBConnector dbc = ConnectionPool.gI().getDBC();
        
        try
	{
            String selectStmnt = "(SELECT value FROM " + type + " WHERE " +
                    "userid = ? ORDER BY ABS(value - ?) LIMIT 1;) - " +
                    "(SELECT value FROM " + type + " WHERE " +
                    "userid = ? ORDER BY ABS(value - ?) LIMIT 1;);";
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    selectStmnt);
	    
            pst.setLong(1, 0);
            pst.setLong(2, md.getTimeStamp());
            pst.setLong(3, 0);
            pst.setLong(4, md.getTimeStamp() - md.getTimeFrame());          
	    
	    ResultSet rs = dbc.execSelect(md, pst);
	    
	    while (rs.next())
	    {
		md.setValue(rs.getDouble("sum"));
		break;
	    }
	}
	catch (Exception e)
	{
	    Logger.gI().addError(e.toString());
	}
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
        
        return md;
    }
}

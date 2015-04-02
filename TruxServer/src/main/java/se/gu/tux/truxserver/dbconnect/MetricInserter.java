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

import se.gu.tux.trux.datastructure.MetricData;

import se.gu.tux.truxserver.logger.Logger;
/**
 *
 * @author jonas
 */
public class MetricHandler {
    /**
     * Static part.
     */
    private static MetricHandler mh;
    
    static
    {
        if(mh ==  null)
            mh = new MetricHandler();
    }
    
    protected static MetricHandler getInstance()
    {
        return mh;
    }
    
    protected static MetricHandler gI()
    {
        return mh;
    }
    
    /**
     * Non-static part.
     */
    private MetricHandler() {}
    
    public boolean insertMetric(MetricData md)
    {
        try
        {   
            PreparedStatement pst = DBConnector.gS().getConnection().prepareStatement(
                    "INSERT INTO metric(value, timestamp, type, userid) " + 
                            "VALUES(?, ?, ?, ?);");
		
            pst.setDouble(1, md.getValue());
            pst.setLong(2, md.getTimeStamp());
            pst.setString(3, md.getClass().getSimpleName());
            pst.setLong(4, 0);
		
            pst.executeUpdate();
            
            return true;
        }
        catch (Exception e)
        {
            Logger.gI().addError(e.toString());
        }

	return false;
    }
    
    public MetricData getMetric(MetricData md)
    {
        try
	{
            String selectStmnt = "SELECT AVG(value) AS avg FROM metric WHERE timestamp " +
                    "BETWEEN ? AND ? AND type = ? AND userid = ?;";
            
            Logger.getInstance().addDebug(selectStmnt);
            
            PreparedStatement pst = DBConnector.gR().getConnection().prepareStatement(
                    selectStmnt);
	    
	    pst.setLong(1, md.getTimeStamp() - md.getTimeFrame());
            pst.setLong(2, md.getTimeStamp());
            pst.setString(3, md.getClass().getSimpleName());
            pst.setLong(4, 0);
            
            System.out.println(pst.getParameterMetaData().toString());
            
            Logger.gI().addDebug(pst.toString());
	    
	    ResultSet rs = pst.executeQuery(selectStmnt);
	    
	    while (rs.next())
	    {
		md.setValue(rs.getDouble("avg"));
		break;
	    }
	}
	catch (Exception e)
	{
	    Logger.gI().addError(e.toString());
	}
        
        return md;
    }
}

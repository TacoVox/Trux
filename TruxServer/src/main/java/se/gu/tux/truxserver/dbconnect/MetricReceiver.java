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
public class MetricReceiver {
    /**
     * Static part.
     */
    private static MetricReceiver mr = null;
    
    static {
        if (mr == null)
            mr = new MetricReceiver();
    }
    
    public static MetricReceiver getInstance() {
        return mr;
    }
    
    public static MetricReceiver gI() {
        return mr;
    }
    
    /**
     * Non-static part.
     */
    private MetricReceiver() {}
    
    public MetricData getMetric(MetricData md)
    {
        DBConnector dbc = ConnectionPool.gI().getDBC();
        
        try
	{
            String selectStmnt = "SELECT AVG(value) AS avg FROM metric WHERE " +
                    "type = ? AND userid = ? AND timestamp BETWEEN ? AND ?;";
            
            Logger.getInstance().addDebug(selectStmnt);
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    selectStmnt);
	    
            pst.setString(1, md.getClass().getSimpleName());
            pst.setLong(2, 0);
	    pst.setLong(3, md.getTimeStamp() - md.getTimeFrame());
            pst.setLong(4, md.getTimeStamp()); 
            
            System.out.println(pst.getParameterMetaData().toString());
            
            Logger.gI().addDebug(pst.toString());
	    
	    ResultSet rs = pst.executeQuery();
	    
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
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
        
        return md;
    }
    
}

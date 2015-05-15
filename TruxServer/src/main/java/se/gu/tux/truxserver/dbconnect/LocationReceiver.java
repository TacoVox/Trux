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

import se.gu.tux.trux.datastructure.Location;
import se.gu.tux.truxserver.logger.Logger;

/**
 *
 * @author jonas
 */
public class LocationReceiver {
    /*
    * Static part.
    */
    private static LocationReceiver instance;
    
    public static LocationReceiver getInstance() {
        if(instance == null)
            instance = new LocationReceiver();
        
        return instance;
    }
    
    public static LocationReceiver gI() {
        return getInstance();
    }
    
    /*
    * Non-static part.
    */
    private LocationReceiver() {}
    
    public Location getLocation() {
        return null;
    }
    
    public Location getCurrent(long userid) {
        Location l = null;
        
        DBConnector dbc = ConnectionPool.gI().getDBC();
        
        //Logger.gI().addDebug(Long.toString(userid));
        
        try
	{
            String selectStmnt = "SELECT latitude, longitude " +
                    "FROM location WHERE userid = ? ORDER BY timestamp DESC LIMIT 1";
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    selectStmnt);
	    
            pst.setLong(1, userid);
            
	    ResultSet rs = pst.executeQuery();
	    
	    while (rs.next())
	    {
                l = new Location(rs.getDouble("latitude"), rs.getDouble("longitude"));
                
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

        return l;
    }
}

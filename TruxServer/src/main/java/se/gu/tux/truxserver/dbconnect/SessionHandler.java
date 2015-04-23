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
import java.sql.Statement;

import se.gu.tux.trux.datastructure.ProtocolMessage;
import se.gu.tux.trux.datastructure.User;

import se.gu.tux.truxserver.logger.Logger;

/**
 *
 * @author jonas
 */
public class SessionHandler {
    /**
     * Static part.
     */
    private static SessionHandler sh = null;
    
    public static SessionHandler getInstance()
    {
        if (sh == null)
            sh = new SessionHandler();
        return sh;
    }
    
    public static SessionHandler gI()
    {
        return getInstance();
    }
    
    /**
     * Non-static part.
     */
    private SessionHandler() {}
    
    public void updateActive(User u)
    {
        DBConnector dbc = ConnectionPool.gI().getDBC();
        
        try
	{
            String updateStmnt = "UPDATE session SET lastactive = ?" +
                    "WHERE userid = ? AND ISNULL(endtime);";
            
            Logger.getInstance().addDebug(updateStmnt);
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    updateStmnt);
	    
            pst.setLong(1, System.currentTimeMillis());
            pst.setLong(2, u.getUserId());
	    
	    pst.executeUpdate();
            
            //return new Response(Response.Type.DATA_RECEIVED);
	}
	catch (Exception e)
	{
	    Logger.gI().addError(e.toString());
	}
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
        
        //return null;
    }
    
    public int startSession(User u)
    {
        DBConnector dbc = ConnectionPool.gI().getDBC();
        
        try
	{
            String insertStmnt = "INSERT INTO session(starttime, userid, lastactive)" +
                    " VALUES(?, ?, ?);";
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    insertStmnt, Statement.RETURN_GENERATED_KEYS);
	    
            pst.setLong(1, System.currentTimeMillis());
            pst.setLong(2, u.getUserId());
            pst.setLong(3, System.currentTimeMillis());
	    
            Logger.getInstance().addDebug(pst.toString());
            
	    pst.executeUpdate();
            
            ResultSet keys = pst.getGeneratedKeys();
            
            while(keys.next())
                return keys.getInt(1);
            return -1;
	}
	catch (Exception e)
	{
	    Logger.gI().addError(e.toString());
	}
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
        
        return -1;
    }
    
    public ProtocolMessage endSession(ProtocolMessage pm)
    {
        DBConnector dbc = ConnectionPool.gI().getDBC();
        
        try
	{
            String updateStmnt = "UPDATE session SET endtime = ?" +
                    "WHERE userid = ? AND sessionid = ?;";
            
            Logger.getInstance().addDebug(updateStmnt);
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    updateStmnt);
	    
            pst.setLong(1, System.currentTimeMillis());
            pst.setLong(2, pm.getUserId());
            pst.setLong(3, pm.getSessionId());
	    
	    pst.executeUpdate();
            
            return new ProtocolMessage(ProtocolMessage.Type.SUCCESS);
	}
	catch (Exception e)
	{
	    Logger.gI().addError(e.toString());
	}
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
        
        return new ProtocolMessage(ProtocolMessage.Type.ERROR);
    }
}

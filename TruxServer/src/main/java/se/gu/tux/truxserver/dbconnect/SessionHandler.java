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
import se.gu.tux.truxserver.ServerSessions;
import se.gu.tux.truxserver.config.Config;

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
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    updateStmnt);
	    
            pst.setLong(1, System.currentTimeMillis());
            pst.setLong(2, u.getUserId());
	    
	    dbc.execUpdate(u, pst);
            
            //return new Response(Response.Type.DATA_RECEIVED);
	}
	catch (Exception e)
	{
	    Logger.gI().addError(e.getLocalizedMessage());
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
            String insertStmnt = "INSERT INTO session(starttime, userid, "
                    + "lastactive, keepalive) VALUES(?, ?, ?, ?);";
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    insertStmnt, Statement.RETURN_GENERATED_KEYS);
	    
            pst.setLong(1, System.currentTimeMillis());
            pst.setLong(2, u.getUserId());
            pst.setLong(3, System.currentTimeMillis());
            pst.setBoolean(4, u.getStayLoggedIn());
            
            pst.executeUpdate();
            
            ResultSet keys = pst.getGeneratedKeys();
            
            while(keys.next())
                return keys.getInt(1);
            
            return - 1;
            
	}
	catch (Exception e)
	{
	    Logger.gI().addError(e.getLocalizedMessage());
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
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    updateStmnt);
	    
            pst.setLong(1, System.currentTimeMillis());
            pst.setLong(2, pm.getUserId());
            pst.setLong(3, pm.getSessionId());
	    
	    dbc.execUpdate(pm, pst);
            
            ServerSessions.gI().closeSession(pm);
            
            return new ProtocolMessage(ProtocolMessage.Type.SUCCESS);
	}
	catch (Exception e)
	{
	    Logger.gI().addError(e.getLocalizedMessage());
	}
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
        
        return new ProtocolMessage(ProtocolMessage.Type.ERROR);
    }
    
    public ResultSet getCurrentSessions()
    {
        DBConnector dbc = ConnectionPool.gI().getDBC();
        
        try
	{
            String updateStmnt = "SELECT * FROM session WHERE endtime IS NULL;";
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    updateStmnt);
	    
            pst.setLong(1, System.currentTimeMillis());
            pst.setLong(2, System.currentTimeMillis() -
                    Config.gI().getSessionTimeout() * 60000);
	    
	    return pst.executeQuery();
	}
	catch (Exception e)
	{
	    Logger.gI().addError(e.getLocalizedMessage());
	}
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
        
        return null;
    }
    
    public ProtocolMessage purgeSessions()
    {
        DBConnector dbc = ConnectionPool.gI().getDBC();
        
        try
	{
            String updateStmnt = "UPDATE session SET endtime = ? " +
                    "WHERE lastactive < ? AND keepalive = FALSE;";
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    updateStmnt);
	    
            pst.setLong(1, System.currentTimeMillis());
            pst.setLong(2, System.currentTimeMillis() -
                    Config.gI().getSessionTimeout() * 60000);
	    
            pst.executeUpdate();
            
            return new ProtocolMessage(ProtocolMessage.Type.SUCCESS);
	}
	catch (Exception e)
	{
	    Logger.gI().addError(e.getLocalizedMessage());
	}
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
        
        return new ProtocolMessage(ProtocolMessage.Type.ERROR);
    }
}

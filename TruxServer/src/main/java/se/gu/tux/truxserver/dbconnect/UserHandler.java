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

import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.User;
import se.gu.tux.trux.datastructure.ProtocolMessage;

import se.gu.tux.truxserver.logger.Logger;

/**
 *
 * @author jonas
 */
public class UserHandler {
    /**
     * Static part.
     */
    private static UserHandler uh = null;
    
    /**
     * Method for getting the instance of the MetricInserter.
     * 
     * @return an Instance of MetricInserter
     */
    public static UserHandler getInstance()
    {
        if (uh == null)
            uh = new UserHandler();
        return uh;
    }
    
    /**
     * Method for getting the instance of the MetricInserter.
     * 
     * @return an Instance of MetricInserter
     */
    public static UserHandler gI()
    {
        return getInstance();
    }
    
    /**
     * Non-static part.
     */
    
    /**
     * Private Constructor. Not acessable.
     */
    private UserHandler() {}
    
    /**
     * Method to login a user to the system.
     * 
     * @param u the user who wants to login
     * 
     * @return either a filled in user object on success or a ProtocolMessage indicating an ERROR
     */
    public Data login(User u)
    {
        int userid = -1;
        String passwd = null;
        String firstname = null;
        String lastname = null;
        int sessionid = -1;
        
        DBConnector dbc = ConnectionPool.gI().getDBC();
        
        try
	{
            String selectStmnt = "SELECT userid, password, firstname, lastname" +
                    " FROM user WHERE username = ?";
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    selectStmnt);
	    
            pst.setString(1, u.getUsername()); 
            
	    ResultSet rs = pst.executeQuery();
	    
	    while (rs.next())
	    {
                u.setUserId(rs.getLong("userid"));
                passwd = rs.getString("password");
                u.setFirstName(rs.getString("firstname"));
                u.setLastName(rs.getString("lastname"));
                
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
        
        if(u.passwordMatch(passwd)) {
            u.setSessionId(SessionHandler.gI().startSession(u));
            
            SessionHandler.gI().startSession(u);
            
            return u;
        }
        else
            return new ProtocolMessage(ProtocolMessage.Type.LOGIN_FAILED);
    }
    
    /**
     * Method to register a user to our system.
     * 
     * @param u a filled in user object which will be stored
     * 
     * @return a ProtocolMessage indicating the success
     */
    public ProtocolMessage register(User u)
    {
        DBConnector dbc = ConnectionPool.gI().getDBC();
        try
        {   
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    "INSERT INTO user(username, password, firstname, lastname) " + 
                            "VALUES(?, ?, ?, ?)");
            
            pst.setString(1, u.getUsername());
            pst.setString(2, u.getPasswordHash());
            pst.setString(3, u.getFirstName());
            pst.setString(4, u.getLastName());
	
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
    
    /**
     * Method for getting user data from the DB.
     * 
     * @param u a scaletton user object -> will be filled in
     * 
     * @return either a filled in user object on success or a ProtocolMessage indicating an ERROR
     */
    public Data getUser(User u)
    {
        int userid = -1;
        String passwd = null;
        String firstname = null;
        String lastname = null;
        int sessionid = -1;
        
        DBConnector dbc = ConnectionPool.gI().getDBC();
        
        try
	{
            String selectStmnt = "SELECT userid, password, firstname, lastname" +
                    " FROM user WHERE username = ?";
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    selectStmnt);
	    
            pst.setString(1, u.getUsername());
	    
            ResultSet rs = pst.executeQuery();
	    
            if(rs == null)
                return new ProtocolMessage(ProtocolMessage.Type.ERROR);
            
	    while (rs.next())
	    {
                u.setUserId(rs.getLong("userid"));
                passwd = rs.getString("password");
                u.setFirstName(rs.getString("firstname"));
                u.setLastName(rs.getString("lastname"));
                
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
        if(u.passwordMatch(passwd))
            return u;
        else
            return new ProtocolMessage(ProtocolMessage.Type.ERROR);
    }
}

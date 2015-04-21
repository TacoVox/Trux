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
    
    static {
        if (uh == null)
            uh = new UserHandler();
    }
    
    public static UserHandler getInstance()
    {
        return uh;
    }
    
    public static UserHandler gI()
    {
        return uh;
    }
    
    /**
     * Non-static part.
     */
    private UserHandler() {}
    
    public boolean addUser()
    {
        return true;
    }
    
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
                    " FROM user WHERE userid = ?;";
            
            Logger.getInstance().addDebug(selectStmnt);
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    selectStmnt);
	    
            pst.setLong(1, 0);
	    
	    ResultSet rs = pst.executeQuery();
	    
	    while (rs.next())
	    {
                u.setUserId(rs.getInt("userid"));
                passwd = rs.getString("password");
                u.setFirstName(rs.getString("firstname"));
                u.setLastName(rs.getString("lastname"));
                
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
        
        if(u.passwordMatch(passwd)) {
            u.setSessionId(SessionHandler.gI().startSession(u));
            
            return u;
        }
        else
            return new ProtocolMessage(ProtocolMessage.Type.LOGIN_FAILED);
    }
    
    public void purgeSessions()
    {
        //Code follows
    }
}

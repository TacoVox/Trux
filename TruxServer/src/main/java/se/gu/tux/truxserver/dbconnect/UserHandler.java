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

import se.gu.tux.trux.datastructure.User;
import se.gu.tux.trux.datastructure.Response;

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
    
    public Response login(User u)
    {
        String passwd = null;
        
        DBConnector dbc = ConnectionPool.gI().getDBC();
        
        try
	{
            String selectStmnt = "SELECT password FROM user" +
                    " WHERE userid = ?;";
            
            Logger.getInstance().addDebug(selectStmnt);
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    selectStmnt);
	    
            pst.setLong(1, 0);
	    
	    ResultSet rs = pst.executeQuery();
	    
	    while (rs.next())
	    {
                passwd = rs.getString("password");
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
        
        if(u.passwordMatch(passwd))
            return new Response(Response.Type.LOGIN_SUCCESS);
        else
            return new Response(Response.Type.LOGIN_FAILED);
    }
}

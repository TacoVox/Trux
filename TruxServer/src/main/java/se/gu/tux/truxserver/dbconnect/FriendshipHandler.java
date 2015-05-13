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
import se.gu.tux.trux.datastructure.ProtocolMessage;
import se.gu.tux.truxserver.config.Config;
import se.gu.tux.truxserver.logger.Logger;

/**
 *
 * @author jonas
 */
public class FriendshipHandler {
    /*
    * Static part.
    */
    private static FriendshipHandler instance;
    
    public static FriendshipHandler getInstance() {
        if (instance == null)
            instance = new FriendshipHandler();
        
        return instance;
    }
    
    public static FriendshipHandler gI() {
        return getInstance();
    }
    
    /*
    * Non-static part.
    */
    private FriendshipHandler() {}
    
    public ProtocolMessage sendFriendRequest(ProtocolMessage pm) {
        DBConnector dbc = ConnectionPool.gI().getDBC();
        
        try
        {   
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    "INSERT INTO friendrequest (userid, friendid, timestamp) "
                            + "VALUES(?, ?, ?)");
            
            pst.setLong(1, pm.getUserId());
            pst.setLong(2, Long.parseLong(pm.getMessage()));
            pst.setLong(3, System.currentTimeMillis());
	
            dbc.execInsert(pm, pst);
            
            return new ProtocolMessage(ProtocolMessage.Type.SUCCESS);
        }
        catch (Exception e)
        {
            Logger.gI().addError(e.getLocalizedMessage());
            
            return new ProtocolMessage(ProtocolMessage.Type.ERROR, e.getLocalizedMessage());
        }
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
    }
    
    public ProtocolMessage unfriendUser(ProtocolMessage pm) {
        DBConnector dbc = ConnectionPool.gI().getDBC();
        
        try
	{
            String updateStmnt = "DELETE FROM isfriendwith " +
                    "WHERE userid = ? AND friendid = ? OR userid = ? AND friendid = ?";
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    updateStmnt);
	    
            pst.setLong(1, pm.getUserId());
            pst.setLong(2, Long.parseLong(pm.getMessage()));
            pst.setLong(3, pm.getUserId());
            pst.setLong(4, Long.parseLong(pm.getMessage()));
	    
            dbc.execDelete(pm, pst);
            
            return new ProtocolMessage(ProtocolMessage.Type.SUCCESS);
	}
	catch (Exception e)
	{
	    Logger.gI().addError(e.getLocalizedMessage());
            
            return new ProtocolMessage(ProtocolMessage.Type.ERROR, e.getLocalizedMessage());
	}
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
    }
    
    public boolean hasNewRequests(Data d) {
        DBConnector dbc = ConnectionPool.gI().getDBC();
        
        try
	{
            String updateStmnt = "SELECT * FROM friendrequest WHERE friendid = ? AND seen = FALSE";
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    updateStmnt);
	    
            pst.setLong(1, d.getUserId());
	    
	    ResultSet rs = dbc.execSelect(d, pst);
            
            while(rs.next()) {
                return true;
            }
            
            return false;
	}
	catch (Exception e)
	{
	    Logger.gI().addError(e.getLocalizedMessage());
            
            return false;
	}
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
    }
    
    public void sawRequest(Data d) {
        
    }
    
    public ProtocolMessage acceptFriend(ProtocolMessage pm) {
        DBConnector dbc = ConnectionPool.gI().getDBC();
        
        Long ts = System.currentTimeMillis();
        try
        {   
            //Way one
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    "INSERT INTO friendrequest (userid, friendid, timestamp) "
                            + "VALUES(?, ?, ?)");
            
            pst.setLong(1, pm.getUserId());
            pst.setLong(2, Long.parseLong(pm.getMessage()));
            pst.setLong(3, ts);
	
            dbc.execInsert(pm, pst);
            
            //Way two
            pst = dbc.getConnection().prepareStatement(
                    "INSERT INTO friendrequest (userid, friendid, timestamp) "
                            + "VALUES(?, ?, ?)");
            
            pst.setLong(1, Long.parseLong(pm.getMessage()));
            pst.setLong(2, pm.getUserId());
            pst.setLong(3, ts);
	
            dbc.execInsert(pm, pst);
            
            return new ProtocolMessage(ProtocolMessage.Type.SUCCESS);
        }
        catch (Exception e)
        {
            Logger.gI().addError(e.getLocalizedMessage());
            
            return new ProtocolMessage(ProtocolMessage.Type.ERROR, e.getLocalizedMessage());
        }
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
    }
}

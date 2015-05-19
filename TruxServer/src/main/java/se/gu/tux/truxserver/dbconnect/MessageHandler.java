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
import java.util.ArrayList;
import java.util.List;
import se.gu.tux.trux.datastructure.ArrayResponse;
import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.Message;
import se.gu.tux.trux.datastructure.ProtocolMessage;
import se.gu.tux.truxserver.logger.Logger;

/**
 *
 * @author jonas
 */
public class MessageHandler {
    /*
    * Static part.
    */
    private static MessageHandler instance;
    
    public static MessageHandler getInstance() {
        if (instance == null)
            instance = new MessageHandler();
        
        return instance;
    }
    
    public static MessageHandler gI() {
        return getInstance();
    }
    
    /*
    * Non-static part.
    */
    private MessageHandler() {}
    
    public ProtocolMessage newMessage(Message m) {
        DBConnector dbc = ConnectionPool.gI().getDBC();
        
        try
        {   
            long conversationid = -1;
            
            PreparedStatement pst = dbc.getConnection().prepareStatement("SELECT conversationid "
                    + "FROM conversation WHERE (persone = ? AND perstwo = ?) OR "
                    + "(persone = ? AND perstwo = ?)");
            
            pst.setLong(1, m.getSenderId());
            pst.setLong(2, m.getReceiverId());
            pst.setLong(3, m.getReceiverId());
            pst.setLong(4, m.getSenderId());
            
            ResultSet rs = dbc.execSelect(m, pst);
            
            while(rs.next()) {
                conversationid = rs.getLong("conversationid");
            }
            
            if(conversationid == -1) {
                pst = dbc.getConnection().prepareStatement(
                        "INSERT INTO conversation (persone, perstwo, timestamp) "
                                + "SELECT * FROM (SELECT ? AS A, ? AS B, ? AS C) AS tmp");
                
                pst.setLong(1, m.getSenderId());
                pst.setLong(2, m.getReceiverId());
                pst.setLong(3, m.getTimeStamp());
                
                ResultSet keys = dbc.execInsert(m, pst);
                
                conversationid = keys.getLong(1);
            }
            
            pst = dbc.getConnection().prepareStatement(
                "UPDATE conversation SET persone = ?, perstwo = ?, timestamp = ? "
                        + "WHERE conversationid = ?");
                
            pst.setLong(1, m.getSenderId());
            pst.setLong(2, m.getReceiverId());
            pst.setLong(3, System.currentTimeMillis());
            pst.setLong(4, conversationid);
            
            dbc.execUpdate(m, pst);
            
            pst = dbc.getConnection().prepareStatement(
                "INSERT INTO message (conversationid, senderid, receiverid, message, timestamp, seen) "
                        + "SELECT * FROM (SELECT ? AS A, ? AS B, ? AS C, ? AS D, ? AS E, ? AS F) AS tmp");
              
            pst.setLong(1, conversationid);
            pst.setLong(2, m.getSenderId());
            pst.setLong(3, m.getReceiverId());
            pst.setString(4, (String)m.getValue());
            pst.setLong(5, System.currentTimeMillis());
            pst.setBoolean(6, false);
            
            dbc.execInsert(m, pst);
            
            return new ProtocolMessage(ProtocolMessage.Type.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.gI().addError(e.getLocalizedMessage());
        }
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
        
	return new ProtocolMessage(ProtocolMessage.Type.ERROR, "Could not insert new message!");
    }
    
    public boolean hasNewMessage(Data d) {
        DBConnector dbc = ConnectionPool.gI().getDBC();
        
        try
	{
            String updateStmnt = "SELECT * FROM message WHERE receiverid = ? AND seen = FALSE";
            
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
            e.printStackTrace();
            
	    Logger.gI().addError(e.getLocalizedMessage());
            
            return false;
	}
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
    }
    
    public Data getLatestConv(ProtocolMessage pm) {
        List conversations = new ArrayList<Message>();
        
        DBConnector dbc = ConnectionPool.gI().getDBC();
        
        try
	{
            String updateStmnt = "SELECT * FROM conversation c "
                    + "JOIN message m on c.conversationid = m.conversationid JOIN "
                    + "(SELECT conversationid, MAX(timestamp) timestamp "
                    + "FROM message GROUP BY conversationid) x "
                    + "ON m.conversationid = x.conversationid AND "
                    + "x.timestamp = m.timestamp WHERE c.persone = ? OR c.perstwo = ?";
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    updateStmnt);
	    
            pst.setLong(1, pm.getUserId());
            pst.setLong(2, pm.getUserId());
	    
	    ResultSet rs = dbc.execSelect(pm, pst);
            
            while(rs.next()) {
                Message m = new Message();
                m.setConversationId(rs.getLong("conversationid"));
                m.setSenderId(rs.getLong("senderid"));
                m.setReceiverId(rs.getLong("receiverid"));
                m.setValue(rs.getString("message"));
                m.setTimeStamp(rs.getLong("timestamp"));
                m.setIsSeen(rs.getBoolean("seen"));
                
                conversations.add(m);
            }
            
            return new ArrayResponse(conversations.toArray());
	}
	catch (Exception e)
	{
            e.printStackTrace();
            
	    Logger.gI().addError(e.getLocalizedMessage());
            
            return new ProtocolMessage(ProtocolMessage.Type.ERROR);
	}
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
    }
    
    public Data getMessages(ProtocolMessage pm) {
        List messages = new ArrayList<Message>();
        
        DBConnector dbc = ConnectionPool.gI().getDBC();
        
        try
	{
            String updateStmnt = "SELECT conversationid, senderid, receiverid, message, timestamp, seen "
                    + "FROM message WHERE conversationid = "
                    + "(SELECT conversationid FROM conversation "
                    + "WHERE (persone = ? AND perstwo = ?) OR (persone = ? AND perstwo = ?)) " 
                    + "ORDER BY timestamp DESC LIMIT 100";
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    updateStmnt);
	    
            pst.setLong(1, pm.getUserId());
            pst.setLong(2, Long.parseLong(pm.getMessage()));
            pst.setLong(3, Long.parseLong(pm.getMessage()));
            pst.setLong(4, pm.getUserId());
	    
	    ResultSet rs = dbc.execSelect(pm, pst);
            
            while(rs.next()) {
                Message m = new Message();
                m.setConversationId(rs.getLong("conversationid"));
                m.setSenderId(rs.getLong("senderid"));
                m.setReceiverId(rs.getLong("receiverid"));
                m.setValue(rs.getString("message"));
                m.setTimeStamp(rs.getLong("timestamp"));
                m.setIsSeen(rs.getBoolean("seen"));
                
                messages.add(m);
            }
            
            return new ArrayResponse(messages.toArray());
	}
	catch (Exception e)
	{
            e.printStackTrace();
            
	    Logger.gI().addError(e.getLocalizedMessage());
            
            return new ProtocolMessage(ProtocolMessage.Type.ERROR);
	}
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }        
    }
    
    public ProtocolMessage markAsSeen(ProtocolMessage pm) {
        DBConnector dbc = ConnectionPool.gI().getDBC();
        
        try
        {   
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    "UPDATE message SET seen = ? WHERE receiverid = ? AND conversationid = "
                        + "(SELECT conversationid FROM conversation "
                        + "WHERE (persone = ? AND perstwo = ?) OR (persone = ? AND perstwo = ?))");
            
            pst.setBoolean(1, true);
            pst.setLong(2, pm.getUserId());
            pst.setLong(3, pm.getUserId());
            pst.setLong(4, Long.parseLong(pm.getMessage()));
            pst.setLong(5, Long.parseLong(pm.getMessage()));
            pst.setLong(6, pm.getUserId());
	
            dbc.execUpdate(pm, pst);
            
            return new ProtocolMessage(ProtocolMessage.Type.SUCCESS);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Logger.gI().addError(e.getLocalizedMessage());
        }
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
        return new ProtocolMessage(ProtocolMessage.Type.ERROR, "Can't mark the message as seen.");
    }
    
    public Data getUnreadMessages(ProtocolMessage pm) {
        List messages = new ArrayList<Message>();
        
        DBConnector dbc = ConnectionPool.gI().getDBC();
        
        try
	{
            String updateStmnt = "SELECT conversationid, senderid, receiverid, message, timestamp, seen "
                    + "FROM message WHERE conversationid = "
                    + "(SELECT conversationid FROM conversation "
                    + "WHERE (persone = ? AND perstwo = ?) OR (persone = ? AND perstwo = ?)) " 
                    + "AND seen = ? AND receiverid = ? ORDER BY timestamp DESC LIMIT 100";
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    updateStmnt);
	    
            pst.setLong(1, pm.getUserId());
            pst.setLong(2, Long.parseLong(pm.getMessage()));
            pst.setLong(3, Long.parseLong(pm.getMessage()));
            pst.setLong(4, pm.getUserId());
            pst.setBoolean(5, false);
            pst.setLong(6, pm.getUserId());
	    
	    ResultSet rs = dbc.execSelect(pm, pst);
            
            while(rs.next()) {
                Message m = new Message();
                m.setConversationId(rs.getLong("conversationid"));
                m.setSenderId(rs.getLong("senderid"));
                m.setReceiverId(rs.getLong("receiverid"));
                m.setValue(rs.getString("message"));
                m.setTimeStamp(rs.getLong("timestamp"));
                m.setIsSeen(rs.getBoolean("seen"));
                
                messages.add(m);
            }
            
            return new ArrayResponse(messages.toArray());
	}
	catch (Exception e)
	{
            e.printStackTrace();
            
	    Logger.gI().addError(e.getLocalizedMessage());
            
            return new ProtocolMessage(ProtocolMessage.Type.ERROR);
	}
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }      
    }
}

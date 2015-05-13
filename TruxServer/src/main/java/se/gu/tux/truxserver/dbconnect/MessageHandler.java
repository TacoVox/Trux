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
            String updateStmnt = "SELECT DISTINCT conversationid, senderid, receiverid, message, timestamp "
                    + "FROM message WHERE conversationid = "
                    + "(SELECT conversationid FROM conversation WHERE persone = 1 OR perstwo = 1 "
                    + "ORDER BY timestamp DESC LIMIT 20) ORDER BY timestamp DESC";
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    updateStmnt);
	    
            pst.setLong(1, pm.getUserId());
            pst.setLong(2, pm.getUserId());
	    
	    ResultSet rs = dbc.execSelect(pm, pst);
            
            while(rs.next()) {
                Message m = new Message();
                m.setConversationId(rs.getLong("converstaionid"));
                m.setSenderId(rs.getLong("senderid"));
                m.setReceiverId(rs.getLong("receiverid"));
                m.setValue(rs.getString("message"));
                m.setTimeStamp(rs.getLong("timestamp"));
                
                conversations.add(m);
            }
            
            return new ArrayResponse(conversations.toArray());
	}
	catch (Exception e)
	{
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
            String updateStmnt = "SELECT conversationid, senderid, receiverid, message, timestamp "
                    + "FROM message WHERE conversationid = ? ORDER BY timestamp DESC LIMIT 20";
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    updateStmnt);
	    
            pst.setLong(1, Long.parseLong(pm.getMessage()));
	    
	    ResultSet rs = dbc.execSelect(pm, pst);
            
            while(rs.next()) {
                Message m = new Message();
                m.setConversationId(rs.getLong("converstaionid"));
                m.setSenderId(rs.getLong("senderid"));
                m.setReceiverId(rs.getLong("receiverid"));
                m.setValue(rs.getString("message"));
                m.setTimeStamp(rs.getLong("timestamp"));
                
                messages.add(m);
            }
            
            return new ArrayResponse(messages.toArray());
	}
	catch (Exception e)
	{
	    Logger.gI().addError(e.getLocalizedMessage());
            
            return new ProtocolMessage(ProtocolMessage.Type.ERROR);
	}
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
    }
}

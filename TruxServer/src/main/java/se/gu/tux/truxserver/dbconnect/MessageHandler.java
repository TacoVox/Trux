package se.gu.tux.truxserver.dbconnect;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import se.gu.tux.trux.datastructure.Data;
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
            String updateStmnt = "SELECT * FROM message WHERE receiverid = ?";
            
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
}

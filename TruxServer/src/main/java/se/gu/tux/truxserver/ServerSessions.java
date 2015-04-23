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
package se.gu.tux.truxserver;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.truxserver.dbconnect.SessionHandler;
import se.gu.tux.truxserver.logger.Logger;

/**
 *
 * @author jonas
 */
public class ServerSessions {
    /**
     * Static part.
     */
    
    private static ServerSessions ss = null;
    
    public static ServerSessions getInstance()
    {
        if(ss == null)
            ss = new ServerSessions();
        return ss;
    }
    
    public static ServerSessions gI()
    {
        return getInstance();
    }
    
    /**
     * Non-static part.
     */
    private ConcurrentHashMap sessions;
    
    private ServerSessions()
    {
        sessions = new ConcurrentHashMap();
        
        ResultSet rs = SessionHandler.gI().getCurrentSessions();
        
        try {
            while(rs.next())
                sessions.putIfAbsent(rs.getLong("sessionid"), rs.getLong("userid"));
        }
        catch (SQLException e) {
            Logger.gI().addError(e.getMessage());
        }
    }
    
    public void startSession(Data d)
    {
        sessions.putIfAbsent(d.getSessionId(), d.getUserId());
    }
    
    public void closeSession(Data d)
    {
        sessions.remove(d.getSessionId(), d.getUserId());
    }
    
    public boolean isValid(Data d)
    {
        if(sessions.containsKey(d.getSessionId()))
            if(sessions.containsValue(d.getUserId()))
                return true;
        return false;
    }
}

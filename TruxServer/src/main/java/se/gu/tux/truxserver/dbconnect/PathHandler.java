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
import se.gu.tux.trux.datastructure.Location;
import se.gu.tux.trux.datastructure.Picture;
import se.gu.tux.trux.datastructure.ProtocolMessage;
import se.gu.tux.truxserver.logger.Logger;
/**
 *
 * @author jonas
 */
public class PathHandler {
    /**
     * Static part.
     */
    private static PathHandler ph;
    
    public static PathHandler getInstance() {
        if (ph == null)
            ph = new PathHandler();
        
        return ph;
    }
    
    public static PathHandler gI() {
        return getInstance();
    }
    
    /**
     * Non-static part.
     */
    public ProtocolMessage savePicPath(Picture pic, String path) {
        DBConnector dbc = ConnectionPool.gI().getDBC();
        try
        {   
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                "INSERT INTO picture (path, timestamp, userid) "
                    + "SELECT * FROM (SELECT ?, ?, ?) AS tmp");
                
            pst.setString(1,path);
            pst.setLong(2, pic.getTimeStamp());
            pst.setLong(3, pic.getUserId());
            
            dbc.execInsert(pic, pst);
            
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

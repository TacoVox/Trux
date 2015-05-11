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
import java.sql.SQLException;

import se.gu.tux.trux.datastructure.Picture;
import se.gu.tux.trux.datastructure.ProtocolMessage;
import se.gu.tux.truxserver.logger.Logger;
/**
 *
 * @author jonas
 */
public class PictureHandler {
    /**
     * Static part.
     */
    private static PictureHandler ph;
    
    public static PictureHandler getInstance() {
        if (ph == null)
            ph = new PictureHandler();
        
        return ph;
    }
    
    public static PictureHandler gI() {
        return getInstance();
    }
    
    /**
     * Non-static part.
     */
    public long savePicturePath(Picture pic, String path) {
        DBConnector dbc = ConnectionPool.gI().getDBC();
        
        try
        {   
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                "INSERT INTO picture (path, timestamp, userid) "
                    + "SELECT * FROM (SELECT ?, ?, ?) AS tmp");
                
            pst.setString(1,path);
            pst.setLong(2, pic.getTimeStamp());
            pst.setLong(3, pic.getUserId());
            
            ResultSet keys = dbc.execInsert(pic, pst);
            
            while(keys.next())
                return keys.getLong(1);
        } catch (Exception e) {
            Logger.gI().addError(e.getLocalizedMessage());
        }
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
        
	return -1;
    }
    
    public ProtocolMessage setProfilePicture(Picture pic) {
        DBConnector dbc = ConnectionPool.gI().getDBC();
        
        try
        {   
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                "REPLACE INTO profilepicture (userid, pictureid) "
                    + "SELECT * FROM (SELECT ?, ?) AS tmp");
                
            pst.setLong(1, pic.getUserId());
            pst.setLong(2, pic.getPictureid());
            
            dbc.execReplace(pic, pst);
            
            return new ProtocolMessage(ProtocolMessage.Type.SUCCESS);
        } catch (Exception e) {
            Logger.gI().addError(e.getLocalizedMessage());
        }
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
        
	return new ProtocolMessage(ProtocolMessage.Type.ERROR);
    }
    
    public String getProfilePicturePath(Picture p) {
        DBConnector dbc = ConnectionPool.gI().getDBC();
        
        try
	{
            String selectStmnt = "SELECT path FROM picture, profilepicture" +
                    " WHERE picture.pictureid = profilepicture.pictureid "
                    + "AND profilepicture.userid = ?";
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(selectStmnt);
            
            pst.setLong(1, p.getUserId());
            
            ResultSet rs = dbc.execSelect(p, pst);
            
	    while (rs.next())
		return rs.getString("path");
        } catch (SQLException e) {
            Logger.gI().addError(e.getLocalizedMessage());
        }
        
        return null;
    }
}

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

import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.Friend;
import se.gu.tux.trux.datastructure.Picture;
import se.gu.tux.trux.datastructure.ProtocolMessage;
import se.gu.tux.trux.datastructure.User;
import se.gu.tux.truxserver.logger.Logger;

/**
 * Class handeling all Picutre concerns in the database.
 * @author Jonas Kahler
 */
public class PictureHandler {
    /*
     * Static part.
     */
    private static PictureHandler instance;

    /**
     * Method returning a PictureHandler instance.
     * @return a PictureHandler instance.
     */
    public static PictureHandler getInstance() {
        if (instance == null) {
            instance = new PictureHandler();
        }

        return instance;
    }

    /**
     * Method returning a PictureHandler instance.
     * @return a PictureHandler instance.
     */
    public static PictureHandler gI() {
        return getInstance();
    }

    /*
     * Non-static part.
     */
    
    /**
     * Private Constructor.
     */
    private PictureHandler() {}
    
    /**
     * Method for storing a picture path in the db.
     * @param pic the Picture wrapper object
     * @param path the path where it is stored
     * @return the new ID of the pic
     */
    public long savePicturePath(Picture pic, String path) {
        DBConnector dbc = null;

        try {
            dbc = ConnectionPool.gI().getDBC();

            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    "INSERT INTO picture (path, timestamp, userid) "
                    + "SELECT * FROM (SELECT ? AS A, ? AS B, ? AS C) AS tmp");

            pst.setString(1, path);
            pst.setLong(2, pic.getTimeStamp());
            pst.setLong(3, pic.getUserId());

            ResultSet keys = dbc.execInsert(pic, pst);

            while (keys.next()) {
                return keys.getLong(1);
            }
        } catch (InterruptedException ie) {
            Logger.gI().addMsg("Received Interrupt. Server Shuttin' down.");
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            Logger.gI().addError(e.getLocalizedMessage());
        } finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }

        return -1;
    }

    /**
     * Method for setting a picture as a users profile pic.
     * @param pic the Picture object to set as the profile pic
     * @return ProtocolMessage with error or success
     */
    public ProtocolMessage setProfilePicture(Picture pic) {
        DBConnector dbc = null;

        try {
            dbc = ConnectionPool.gI().getDBC();

            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    "REPLACE INTO profilepicture (userid, pictureid) "
                    + "SELECT * FROM (SELECT ? AS A, ? AS B) AS tmp");

            pst.setLong(1, pic.getUserId());
            pst.setLong(2, pic.getPictureid());

            dbc.execReplace(pic, pst);

            return new ProtocolMessage(ProtocolMessage.Type.SUCCESS);
        } catch (InterruptedException ie) {
            Logger.gI().addMsg("Received Interrupt. Server Shuttin' down.");
            return new ProtocolMessage(ProtocolMessage.Type.GOODBYE, "Server shutting down.");
        } catch (Exception e) {
            e.printStackTrace();
            Logger.gI().addError(e.getLocalizedMessage());
        } finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }

        return new ProtocolMessage(ProtocolMessage.Type.ERROR);
    }

    /**
     * Method for getting the path to the profile picture.
     * @param p the empty Picture wrapper inlclduing the correct pictureid
     * @return the path to the picture
     */
    public String getProfilePicturePath(Picture p) {
        DBConnector dbc = null;

        try {
            dbc = ConnectionPool.gI().getDBC();

            String selectStmnt = "SELECT path FROM picture"
                    + " WHERE pictureid = ?";

            PreparedStatement pst = dbc.getConnection().prepareStatement(selectStmnt);

            pst.setLong(1, p.getPictureid());

            ResultSet rs = dbc.execSelect(p, pst);

            while (rs.next()) {
                return rs.getString("path");
            }
        } catch (InterruptedException ie) {
            Logger.gI().addMsg("Received Interrupt. Server Shuttin' down.");
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.gI().addError(e.getLocalizedMessage());
        } finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }

        return null;
    }

    /**
     * Method for getting the id of a profilepicture
     * @param d a Data object inlcuding the UserID
     * @return the ID of the profile picture
     */
    public long getProfilePictureID(Data d) {
        DBConnector dbc = null;

        try {
            dbc = ConnectionPool.gI().getDBC();

            String selectStmnt = "SELECT pictureid FROM profilepicture"
                    + " WHERE userid = ?";

            PreparedStatement pst = dbc.getConnection().prepareStatement(selectStmnt);

            if (d instanceof User) {
                pst.setLong(1, d.getUserId());
            } else {
                Friend f = (Friend) d;
                pst.setLong(1, f.getFriendId());
            }

            ResultSet rs = dbc.execSelect(d, pst);

            while (rs.next()) {
                return rs.getLong("pictureid");
            }
        } catch (InterruptedException ie) {
            Logger.gI().addMsg("Received Interrupt. Server Shuttin' down.");
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.gI().addError(e.getLocalizedMessage());
        } finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }

        return -1;
    }
}
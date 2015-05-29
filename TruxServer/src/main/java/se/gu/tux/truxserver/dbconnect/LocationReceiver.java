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

import se.gu.tux.trux.datastructure.Location;
import se.gu.tux.trux.datastructure.ProtocolMessage;
import se.gu.tux.truxserver.logger.Logger;

/**
 * Class responsilbe for fetching location from the db.
 * @author Jonas Kahler
 */
public class LocationReceiver {
    /*
     * Static part.
     */
    private static LocationReceiver instance;

    /**
     * Method returning a LocationReceiver instance.
     * @return a LocationReceiver instance.
     */
    public static LocationReceiver getInstance() {
        if (instance == null) {
            instance = new LocationReceiver();
        }

        return instance;
    }

    /**
     * Method returning a LocationReceiver instance.
     * @return a LocationReceiver instance.
     */
    public static LocationReceiver gI() {
        return getInstance();
    }

    /*
     * Non-static part.
     */
    
    /**
     * Private constructor.
     */
    private LocationReceiver() {
    }

    /**
     * Method for fetching the current location of a user.
     * @param userid id of the user to fetch the location from
     * @return a filled in Location object or ProtocolMessage on error
     */
    public Data getCurrent(long userid) {
        Location l = null;

        DBConnector dbc = null;

        try {
            dbc = ConnectionPool.gI().getDBC();

            String selectStmnt = "SELECT latitude, longitude "
                    + "FROM location WHERE userid = ? ORDER BY timestamp DESC LIMIT 1";

            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    selectStmnt);

            pst.setLong(1, userid);

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                l = new Location(rs.getDouble("latitude"), rs.getDouble("longitude"));

                break;
            }
        } catch (InterruptedException ie) {
            Logger.gI().addMsg("Received Interrupt. Server Shuttin' down.");
            return new ProtocolMessage(ProtocolMessage.Type.GOODBYE, "Server shutting down.");
        } catch (Exception e) {
            e.printStackTrace();

            Logger.gI().addError(e.getLocalizedMessage());
        } finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }

        return l;
    }
}
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
import java.sql.Statement;

import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.ProtocolMessage;
import se.gu.tux.trux.datastructure.User;
import se.gu.tux.truxserver.config.Config;
import se.gu.tux.truxserver.logger.Logger;

/**
 * Class taking care of all Session DB operations.
 * @author Jonas Kahler
 */
public class SessionHandler {

    /*
     * Static part.
     */
    private static SessionHandler instance = null;

    /**
     * Method for getting the instance of the SessionHandler.
     * @return an Instance of SessionHandler
     */
    public static SessionHandler getInstance() {
        if (instance == null) {
            instance = new SessionHandler();
        }
        return instance;
    }

    /**
     * Method for getting the instance of the SessionHandler.
     * @return an Instance of SessionHandler
     */
    public static SessionHandler gI() {
        return getInstance();
    }

    /*
     * Non-static part.
     */
    
    /**
     * Private Constructor. Not acessable.
     */
    private SessionHandler() {
    }

    /**
     * Method for updating the session of a user.
     * @param d Data object including the UserID
     */
    public void updateActive(Data d) {
        DBConnector dbc = null;

        try {
            dbc = ConnectionPool.gI().getDBC();

            String updateStmnt = "UPDATE session SET lastactive = ? "
                    + "WHERE userid = ? AND sessionid = ? AND ISNULL(endtime)";

            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    updateStmnt);

            pst.setLong(1, System.currentTimeMillis());
            pst.setLong(2, d.getUserId());
            pst.setLong(3, d.getSessionId());

            dbc.execUpdate(d, pst);
        } catch (InterruptedException ie) {
            Logger.gI().addMsg("Received Interrupt. Server Shuttin' down.");
        } catch (Exception e) {
            e.printStackTrace();
            Logger.gI().addError(e.getLocalizedMessage());
        } finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
    }

    /**
     * Method to start a new session.
     * @param u the user who will start the session
     * @return the session id.
     */
    public long startSession(User u) {
        DBConnector dbc = null;

        try {
            dbc = ConnectionPool.gI().getDBC();

            String insertStmnt = "INSERT INTO session(starttime, userid, "
                    + "lastactive, keepalive) VALUES(?, ?, ?, ?)";

            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    insertStmnt, Statement.RETURN_GENERATED_KEYS);

            pst.setLong(1, System.currentTimeMillis());
            pst.setLong(2, u.getUserId());
            pst.setLong(3, System.currentTimeMillis());
            pst.setBoolean(4, u.getStayLoggedIn());

            pst.executeUpdate();

            ResultSet keys = pst.getGeneratedKeys();

            while (keys.next()) {
                return keys.getLong(1);
            }

            return - 1;
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
     * Method to end a user's session.
     * @param pm a ProtocolMessage with a end session request.
     * @return a ProtocolMessage responding if the action was successful.
     */
    public ProtocolMessage endSession(ProtocolMessage pm) {
        DBConnector dbc = null;

        try {
            dbc = ConnectionPool.gI().getDBC();

            String updateStmnt = "UPDATE session SET endtime = ? "
                    + "WHERE userid = ? AND sessionid = ?";

            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    updateStmnt);

            pst.setLong(1, System.currentTimeMillis());
            pst.setLong(2, pm.getUserId());
            pst.setLong(3, pm.getSessionId());

            dbc.execUpdate(pm, pst);

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
     * Method for getting all active sessions from the DB.
     * @return a ResultSet containing all active sessions
     */
    public ResultSet getCurrentSessions() {
        DBConnector dbc = null;

        try {
            dbc = ConnectionPool.gI().getDBC();

            String updateStmnt = "SELECT * FROM session WHERE endtime IS NULL";

            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    updateStmnt);

            pst.setLong(1, System.currentTimeMillis());
            pst.setLong(2, System.currentTimeMillis()
                    - Config.gI().getSessionTimeout() * 60000);

            return pst.executeQuery();
        } catch (InterruptedException ie) {
            Logger.gI().addMsg("Received Interrupt. Server Shuttin' down.");
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            Logger.gI().addError(e.getLocalizedMessage());
        } finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }

        return null;
    }

    /**
     * Metod for closing all sessions which shall expire.
     * @return a ProtocolMessage indicating the success
     */
    public ProtocolMessage purgeSessions() {
        DBConnector dbc = null;

        try {
            dbc = ConnectionPool.gI().getDBC();

            String updateStmnt = "UPDATE session SET endtime = ? "
                    + "WHERE (lastactive < ? AND keepalive = FALSE"
                    + " AND endtime IS NULL) OR "
                    + "(lastactive < ? AND keepalive = TRUE"
                    + " AND endtime IS NULL)";

            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    updateStmnt);

            pst.setLong(1, System.currentTimeMillis());
            pst.setLong(2, System.currentTimeMillis()
                    - Config.gI().getSessionTimeout() * 60000);
            pst.setLong(3, System.currentTimeMillis()
                    - 168 * 60000);

            pst.executeUpdate();

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
}
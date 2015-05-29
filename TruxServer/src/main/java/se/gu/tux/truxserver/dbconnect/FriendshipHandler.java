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
import se.gu.tux.trux.datastructure.Friend;
import se.gu.tux.trux.datastructure.ProtocolMessage;
import se.gu.tux.truxserver.logger.Logger;

/**
 * Class handeling all friendship concerns in the db.
 * @author Jonas Kahler
 */
public class FriendshipHandler {
    /*
     * Static part.
     */
    private static FriendshipHandler instance;

    /**
     * Method returning a FriendshipHandler instance.
     * @return a FriendshipHandler instance.
     */
    public static FriendshipHandler getInstance() {
        if (instance == null) {
            instance = new FriendshipHandler();
        }

        return instance;
    }

    /**
     * Method returning a FriendshipHandler instance.
     * @return a FriendshipHandler instance.
     */
    public static FriendshipHandler gI() {
        return getInstance();
    }

    /*
     * Non-static part.
     */
    
    /**
     * Private Constructor.
     */
    private FriendshipHandler() {
    }

    /**
     * Method for sending a friend request.
     * @param pm ProtocolMessage including the id of the friend
     * @return response on success or fail
     */
    public ProtocolMessage sendFriendRequest(ProtocolMessage pm) {
        DBConnector dbc = null;

        try {
            dbc = ConnectionPool.gI().getDBC();

            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    "REPLACE INTO friendrequest (userid, friendid, timestamp) "
                    + "SELECT * FROM (SELECT ? AS A, ? AS B, ? AS C) AS tmp");

            pst.setLong(1, pm.getUserId());
            pst.setLong(2, Long.parseLong(pm.getMessage()));
            pst.setLong(3, System.currentTimeMillis());

            dbc.execReplace(pm, pst);

            return new ProtocolMessage(ProtocolMessage.Type.SUCCESS);
        } catch (InterruptedException ie) {
            Logger.gI().addMsg("Received Interrupt. Server Shuttin' down.");
            return new ProtocolMessage(ProtocolMessage.Type.GOODBYE, "Server shutting down.");
        } catch (Exception e) {
            e.printStackTrace();

            Logger.gI().addError(e.getLocalizedMessage());

            return new ProtocolMessage(ProtocolMessage.Type.ERROR, e.getLocalizedMessage());
        } finally {
            if (dbc != null) {
                ConnectionPool.gI().releaseDBC(dbc);
            }
        }
    }

    /**
     * Method for removing a user from a friendlist.
     * @param pm ProtocolMessage including the id of the friend
     * @return response on success or fail
     */
    public ProtocolMessage unfriendUser(ProtocolMessage pm) {
        DBConnector dbc = null;

        try {
            dbc = ConnectionPool.gI().getDBC();

            String updateStmnt = "DELETE FROM isfriendwith "
                    + "WHERE (userid = ? AND friendid = ?) OR (userid = ? AND friendid = ?)";

            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    updateStmnt);

            pst.setLong(1, pm.getUserId());
            pst.setLong(2, Long.parseLong(pm.getMessage()));
            pst.setLong(3, Long.parseLong(pm.getMessage()));
            pst.setLong(4, pm.getUserId());

            dbc.execDelete(pm, pst);
            
            updateStmnt = "UPDATE message SET seen = ? "
                    + "WHERE receiverid = ? AND senderid = ?";

            pst = dbc.getConnection().prepareStatement(
                    updateStmnt);

            pst.setBoolean(1, true);
            pst.setLong(2, Long.parseLong(pm.getMessage()));
            pst.setLong(3, pm.getUserId());

            dbc.execUpdate(pm, pst);

            return new ProtocolMessage(ProtocolMessage.Type.SUCCESS);
        } catch (InterruptedException ie) {
            Logger.gI().addMsg("Received Interrupt. Server Shuttin' down.");
            return new ProtocolMessage(ProtocolMessage.Type.GOODBYE, "Server shutting down.");
        } catch (Exception e) {
            e.printStackTrace();

            Logger.gI().addError(e.getLocalizedMessage());

            return new ProtocolMessage(ProtocolMessage.Type.ERROR, e.getLocalizedMessage());
        } finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
    }

    /**
     * Method to check if a user has new friendrequests.
     * @param d Data object including the UserID
     * @return true or false if there are new reqs
     */
    public boolean hasNewRequests(Data d) {
        DBConnector dbc = null;

        try {
            dbc = ConnectionPool.gI().getDBC();

            String updateStmnt = "SELECT * FROM friendrequest WHERE friendid = ? AND seen = FALSE";

            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    updateStmnt);

            pst.setLong(1, d.getUserId());

            ResultSet rs = dbc.execSelect(d, pst);

            while (rs.next()) {
                return true;
            }

            return false;
        } catch (InterruptedException ie) {
            Logger.gI().addMsg("Received Interrupt. Server Shuttin' down.");
            return false;
        } catch (Exception e) {
            e.printStackTrace();

            Logger.gI().addError(e.getLocalizedMessage());

            return false;
        } finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
    }

    /**
     * Method to accept a friend request.
     * @param pm ProtocolMessage including the id of the friend
     * @return response on success or fail
     */
    public ProtocolMessage acceptFriend(ProtocolMessage pm) {
        DBConnector dbc = null;

        try {
            dbc = ConnectionPool.gI().getDBC();

            //Way one
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    "REPLACE INTO isfriendwith (userid, friendid, timestamp) "
                    + "SELECT * FROM (SELECT ? AS A, ? AS B, ? AS C) AS tmp");

            pst.setLong(1, pm.getUserId());
            pst.setLong(2, Long.parseLong(pm.getMessage()));
            pst.setLong(3, System.currentTimeMillis());

            dbc.execReplace(pm, pst);

            //Way two
            pst = dbc.getConnection().prepareStatement(
                    "REPLACE INTO isfriendwith (userid, friendid, timestamp) "
                    + "SELECT * FROM (SELECT ? AS A, ? AS B, ? AS C) AS tmp");

            pst.setLong(1, Long.parseLong(pm.getMessage()));
            pst.setLong(2, pm.getUserId());
            pst.setLong(3, System.currentTimeMillis());

            dbc.execReplace(pm, pst);

            //Update friendrequest table
            pst = dbc.getConnection().prepareStatement(
                    "UPDATE friendrequest SET affirm = ?, seen = ?, reviewed = ? "
                    + "WHERE userid = ? AND friendid = ?");

            pst.setBoolean(1, true);
            pst.setBoolean(2, true);
            pst.setBoolean(3, true);
            pst.setLong(4, Long.parseLong(pm.getMessage()));
            pst.setLong(5, pm.getUserId());

            dbc.execUpdate(pm, pst);

            return new ProtocolMessage(ProtocolMessage.Type.SUCCESS);
        } catch (InterruptedException ie) {
            Logger.gI().addMsg("Received Interrupt. Server Shuttin' down.");
            return new ProtocolMessage(ProtocolMessage.Type.GOODBYE, "Server shutting down.");
        } catch (Exception e) {
            e.printStackTrace();

            Logger.gI().addError(e.getLocalizedMessage());

            return new ProtocolMessage(ProtocolMessage.Type.ERROR, e.getLocalizedMessage());
        } finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
    }

    /**
     * Method to decline friend requests.
     * @param pm ProtocolMessage including the id of the friend
     * @return response on success or fail
     */
    public ProtocolMessage declineRequest(ProtocolMessage pm) {
        DBConnector dbc = null;

        try {
            dbc = ConnectionPool.gI().getDBC();

            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    "UPDATE friendrequest SET affirm = ?, seen = ?, reviewed = ? "
                    + "WHERE userid = ? AND friendid = ?");

            pst.setBoolean(1, false);
            pst.setBoolean(2, true);
            pst.setBoolean(3, true);
            pst.setLong(4, Long.parseLong(pm.getMessage()));
            pst.setLong(5, pm.getUserId());

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
        return new ProtocolMessage(ProtocolMessage.Type.ERROR, "Can't mark the request as not accepted.");
    }

    /**
     * Method for getting all friend requests for a user from the db.
     * @param pm ProtocolMessage including the id of the friend
     * @return information about the people who send the reqs or error message
     */
    public Data getFriendRequests(ProtocolMessage pm) {
        List friendreqs = new ArrayList<Friend>();

        DBConnector dbc = null;

        try {
            dbc = ConnectionPool.gI().getDBC();

            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    "SELECT userid, timestamp FROM friendrequest WHERE friendid = ? "
                    + "AND reviewed = ? AND completed = ?");

            pst.setLong(1, pm.getUserId());
            pst.setBoolean(2, false);
            pst.setBoolean(3, false);

            ResultSet rs = dbc.execSelect(pm, pst);

            while (rs.next()) {
                Friend f = new Friend(rs.getLong("userid"));
                f.setTimeStamp(rs.getLong("timestamp"));

                Data d = UserHandler.gI().getFriend(f);

                if (d instanceof Friend) {
                    friendreqs.add((Friend) d);
                } else {
                    return new ProtocolMessage(ProtocolMessage.Type.ERROR, "Cannot fetch info about a user.");
                }
            }

            return new ArrayResponse(friendreqs.toArray());
        } catch (InterruptedException ie) {
            Logger.gI().addMsg("Received Interrupt. Server Shuttin' down.");
            return new ProtocolMessage(ProtocolMessage.Type.GOODBYE, "Server shutting down.");
        } catch (Exception e) {
            e.printStackTrace();

            Logger.gI().addError(e.getLocalizedMessage());

            return new ProtocolMessage(ProtocolMessage.Type.ERROR, e.getLocalizedMessage());
        } finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
    }

    /**
     * Methiod to check if there is a pending friendreq for a user.
     * @param pm ProtocolMessage including the id of the friend
     * @return true or fail depending on if there is such a pending req
     */
    public boolean isPending(ProtocolMessage pm) {
        DBConnector dbc = null;

        try {
            dbc = ConnectionPool.gI().getDBC();

            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    "SELECT * FROM friendrequest WHERE userid = ? AND friendid = ? "
                    + "AND reviewed = ? AND completed = ?");

            pst.setLong(1, pm.getUserId());
            pst.setLong(2, Long.parseLong(pm.getMessage()));
            pst.setBoolean(3, false);
            pst.setBoolean(4, false);

            ResultSet rs = dbc.execSelect(pm, pst);

            while (rs.next()) {
                return true;
            }

            return false;
        } catch (InterruptedException ie) {
            Logger.gI().addMsg("Received Interrupt. Server Shuttin' down.");
            return false;
        } catch (Exception e) {
            e.printStackTrace();

            Logger.gI().addError(e.getLocalizedMessage());

            return false;
        } finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
    }

    /**
     * Method to mark a friendreq as seen.
     * @param pm ProtocolMessage including the id of the friend
     * @return response on success or fail
     */
    public ProtocolMessage markAsSeen(ProtocolMessage pm) {
        DBConnector dbc = null;

        try {
            dbc = ConnectionPool.gI().getDBC();

            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    "UPDATE friendrequest SET seen = ? WHERE userid = ? AND friendid = ?");

            pst.setBoolean(1, true);
            pst.setLong(2, Long.parseLong(pm.getMessage()));
            pst.setLong(3, pm.getUserId());

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
        return new ProtocolMessage(ProtocolMessage.Type.ERROR, "Can't mark the friendrequest as seen.");
    }

    /**
     * Method to check if a friendreq is reviewed - sets it to completet.
     * @param d data including UserID
     * @return true or false depending on if the req is pending or not
     */
    public boolean isReviewed(Data d) {
        DBConnector dbc = null;

        try {
            dbc = ConnectionPool.gI().getDBC();

            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    "SELECT * FROM friendrequest WHERE userid = ? AND reviewed = ? AND completed = ?");

            pst.setLong(1, d.getUserId());
            pst.setBoolean(2, true);
            pst.setBoolean(3, false);

            ResultSet rs = dbc.execSelect(d, pst);

            if (rs.next()) {
                pst = dbc.getConnection().prepareStatement(
                        "UPDATE friendrequest SET completed = ? WHERE userid = ? "
                        + "AND reviewed = ? AND completed = ?");

                pst.setBoolean(1, true);
                pst.setLong(2, d.getUserId());
                pst.setBoolean(3, true);
                pst.setBoolean(4, false);

                dbc.execUpdate(d, pst);

                return true;
            } else {
                return false;
            }
        } catch (InterruptedException ie) {
            Logger.gI().addMsg("Received Interrupt. Server Shuttin' down.");
            return false;
        } catch (Exception e) {
            e.printStackTrace();

            Logger.gI().addError(e.getLocalizedMessage());

            return false;
        } finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
    }
}
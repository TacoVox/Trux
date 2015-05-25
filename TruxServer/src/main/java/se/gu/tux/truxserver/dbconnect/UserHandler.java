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
import se.gu.tux.trux.datastructure.Location;
import se.gu.tux.trux.datastructure.User;
import se.gu.tux.trux.datastructure.ProtocolMessage;

import se.gu.tux.truxserver.logger.Logger;
import se.gu.tux.truxserver.mail.EMailSender;

/**
 *
 * @author jonas
 */
public class UserHandler {
    /**
     * Static part.
     */
    private static UserHandler uh = null;
    
    /**
     * Method for getting the instance of the MetricInserter.
     * 
     * @return an Instance of MetricInserter
     */
    public static UserHandler getInstance()
    {
        if (uh == null)
            uh = new UserHandler();
        return uh;
    }
    
    /**
     * Method for getting the instance of the MetricInserter.
     * 
     * @return an Instance of MetricInserter
     */
    public static UserHandler gI()
    {
        return getInstance();
    }
    
    /**
     * Non-static part.
     */
    
    /**
     * Private Constructor. Not acessable.
     */
    private UserHandler() {}
    
    /**
     * Method to login a user to the system.
     * 
     * @param u the user who wants to login
     * 
     * @return either a filled in user object on success or a ProtocolMessage indicating an ERROR
     */
    public Data login(User u)
    {
        String passwd = null;
        
        DBConnector dbc = null;
        
        try
        {   
            dbc = ConnectionPool.gI().getDBC();
            
            String selectStmnt = "SELECT userid, password" +
                    " FROM user WHERE username = ?";
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    selectStmnt);
	    
            pst.setString(1, u.getUsername()); 
            
	    ResultSet rs = pst.executeQuery();
	    
	    while (rs.next())
	    {
                u.setUserId(rs.getLong("userid"));
                passwd = rs.getString("password");
                
		break;
	    }
	} catch (InterruptedException ie) {
            Logger.gI().addMsg("Received Interrupt. Server Shuttin' down.");
            return new ProtocolMessage(ProtocolMessage.Type.GOODBYE, "Server shutting down.");
        } catch (Exception e)
	{
            e.printStackTrace();
	    Logger.gI().addError(e.getLocalizedMessage());
	}
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
        
        if(u.passwordMatch(passwd) && u.getUserId() != -1) {
            ProtocolMessage pm = new ProtocolMessage(ProtocolMessage.Type.LOGIN_SUCCESS);
            
            pm.setUserId(u.getUserId());
            pm.setSessionId(SessionHandler.gI().startSession(u));
            
            return pm;
        }
        else {
            failedLogin(u.getUserId());
            return new ProtocolMessage(ProtocolMessage.Type.LOGIN_FAILED);
        }
    }
    
    /**
     * Auto-login method.
     * @param u
     * @return 
     */
    public Data autoLogin(ProtocolMessage pm)
    {
        long userid = -1;
        String passwd = null;
        long sessionid = -1;
        
        DBConnector dbc = null;
        
        try
        {   
            dbc = ConnectionPool.gI().getDBC();
            
            String selectStmnt = "SELECT user.userid, user.password, session.sessionid" +
                    " FROM user, session WHERE user.userid = session.userid AND"
                    + " user.userid = ? AND session.sessionid = ? AND session.endtime IS NULL";
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    selectStmnt);
	    
            pst.setLong(1, pm.getUserId()); 
            pst.setLong(2, pm.getSessionId());
            
	    ResultSet rs = pst.executeQuery();
	    
	    while (rs.next())
	    {
                userid = rs.getLong("userid");
                passwd = rs.getString("password");
                sessionid = rs.getLong("sessionid");
                
		break;
	    }
	} catch (InterruptedException ie) {
            Logger.gI().addMsg("Received Interrupt. Server Shuttin' down.");
            return new ProtocolMessage(ProtocolMessage.Type.GOODBYE, "Server shutting down.");
        } catch (Exception e)
	{
            e.printStackTrace();
	    Logger.gI().addError(e.getLocalizedMessage());
	}
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
        
        if(pm.getMessage().equals(passwd) && sessionid == pm.getSessionId() 
                && userid == pm.getUserId()) {
            ProtocolMessage m = new ProtocolMessage(ProtocolMessage.Type.LOGIN_SUCCESS);
            
            m.setUserId(userid);
            m.setSessionId(sessionid);
            
            return m;
        }
        else {
            failedLogin(pm.getUserId());
            return new ProtocolMessage(ProtocolMessage.Type.LOGIN_FAILED, "Session is not valid anymore.");
        }
    }
    
    private void failedLogin(long userid) {
        DBConnector dbc = null;
        
        try
        {   
            dbc = ConnectionPool.gI().getDBC();
               
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    "INSERT INTO loginattempts (userid, timestamp) "
                            + "SELECT * FROM (SELECT ? AS A, ? AS B) AS tmp");
            
            pst.setLong(1, userid);
            pst.setLong(2, System.currentTimeMillis());
	
            pst.executeUpdate();
        } catch (InterruptedException ie) {
            Logger.gI().addMsg("Received Interrupt. Server Shuttin' down.");
        } catch (Exception e)
        {
            e.printStackTrace();
            Logger.gI().addError(e.getLocalizedMessage());
        }
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
    }
    
    /**
     * Method for getting user data from the DB.
     * 
     * @param u a scaletton user object -> will be filled in
     * 
     * @return either a filled in user object on success or a ProtocolMessage indicating an ERROR
     */
    public Data getUser(User u)
    {   
        DBConnector dbc = null;
        
        try
        {   
            dbc = ConnectionPool.gI().getDBC();
            
            String selectStmnt = "SELECT userid, firstname, lastname, email" +
                    " FROM user WHERE userid = ?";
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    selectStmnt);
	    
            pst.setLong(1, u.getUserId());
	    
            ResultSet rs = dbc.execSelect(u, pst);
	    
            if(rs == null)
                return new ProtocolMessage(ProtocolMessage.Type.ERROR);
            
	    while (rs.next())
	    {
                u.setUserId(rs.getLong("userid"));
                u.setFirstName(rs.getString("firstname"));
                u.setLastName(rs.getString("lastname"));
                u.setEmail(rs.getString("email"));
                
		break;
	    }
            
            selectStmnt = "SELECT friendid" +
                    " FROM isfriendwith WHERE userid = ?";
            
            pst = dbc.getConnection().prepareStatement(
                    selectStmnt);
	    
            pst.setLong(1, u.getUserId());
	    
            rs = dbc.execSelect(u, pst);
            
            List friends = new ArrayList<Long>();
            
	    while (rs.next())
	    {
                friends.add(rs.getLong("friendid"));
	    }
            
            long[] ready = new long[friends.size()];
            
            for(int i = 0; i < friends.size(); i++)
                ready[i] = (long)friends.get(i);
            
            u.setFriends(ready);
            
            u.setProfilePicId(PictureHandler.gI().getProfilePictureID(u));
            
            return u;
	} catch (InterruptedException ie) {
            Logger.gI().addMsg("Received Interrupt. Server Shuttin' down.");
            return new ProtocolMessage(ProtocolMessage.Type.GOODBYE, "Server shutting down.");
        } catch (Exception e)
	{
            e.printStackTrace();
	    Logger.gI().addError(e.getLocalizedMessage());
            return new ProtocolMessage(ProtocolMessage.Type.ERROR, e.getLocalizedMessage());
	}
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
    }
    
    /**
     * Method to register a user to our system.
     * 
     * @param u a filled in user object which will be stored
     * 
     * @return a ProtocolMessage indicating the success
     */
    public ProtocolMessage register(User u)
    {
        DBConnector dbc = null;
        
        try
        {   
            dbc = ConnectionPool.gI().getDBC();
               
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    "INSERT INTO register (registerid, username, password, firstname, lastname, "
                            + "email, timestamp) VALUES(?, ?, ?, ?, ?, ?, ?)");
            
            pst.setInt(1, u.getEmail().hashCode());
            pst.setString(2, u.getUsername());
            pst.setString(3, u.getPasswordHash());
            pst.setString(4, u.getFirstName());
            pst.setString(5, u.getLastName());
            pst.setString(6, u.getEmail());
            pst.setLong(7, System.currentTimeMillis());
	
            pst.executeUpdate();
            
            EMailSender.gI().sendConfirmationMail(u.getEmail(), Integer.toString(u.getEmail().hashCode()));
            
            return new ProtocolMessage(ProtocolMessage.Type.SUCCESS);
        } catch (InterruptedException ie) {
            Logger.gI().addMsg("Received Interrupt. Server Shuttin' down.");
            return new ProtocolMessage(ProtocolMessage.Type.GOODBYE, "Server shutting down.");
        } catch (Exception e)
        {
            e.printStackTrace();
            Logger.gI().addError(e.getLocalizedMessage());
        }
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
        return new ProtocolMessage(ProtocolMessage.Type.ERROR, "Username is already taken. Please select another one.");
    }
        
    public ProtocolMessage updateUser(User u) {
        DBConnector dbc = null;
        
        try
        {   
            dbc = ConnectionPool.gI().getDBC();
               
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    "UPDATE user SET firstname = ?, lastname = ?, email = ? WHERE userid = ?");
            
            pst.setString(1, u.getFirstName());
            pst.setString(2, u.getLastName());
            pst.setString(3, u.getEmail());
            pst.setLong(4, u.getUserId());
	
            dbc.execUpdate(u, pst);
            
            return new ProtocolMessage(ProtocolMessage.Type.SUCCESS);
        } catch (InterruptedException ie) {
            Logger.gI().addMsg("Received Interrupt. Server Shuttin' down.");
            return new ProtocolMessage(ProtocolMessage.Type.GOODBYE, "Server shutting down.");
        } catch (Exception e)
        {
            e.printStackTrace();
            Logger.gI().addError(e.getLocalizedMessage());
        }
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
        return new ProtocolMessage(ProtocolMessage.Type.ERROR, "Update failed.");
    }
    
    public Data getFriend(Friend f)
    {
        long lastActive = Long.MAX_VALUE;
        
        DBConnector dbc = null;
        
        try
        {   
            dbc = ConnectionPool.gI().getDBC();
            
            String selectStmnt = "SELECT username, firstname, lastname" +
                    " FROM user WHERE userid = ?";
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    selectStmnt);
	    
            pst.setLong(1, f.getFriendId());
	    
            ResultSet rs = pst.executeQuery();
	    
            if(rs == null)
                return new ProtocolMessage(ProtocolMessage.Type.ERROR);
            
	    while (rs.next())
	    {
                f.setUsername(rs.getString("username"));
                f.setFirstname(rs.getString("firstname"));
                f.setLastname(rs.getString("lastname"));
                f.setFriendType(Friend.FriendType.FRIEND);
                
		break;
	    }
            
            selectStmnt = "SELECT MAX(lastactive) AS ts FROM session WHERE userid = ? "
                    + "AND endtime IS NULL";
            
            pst = dbc.getConnection().prepareStatement(
                    selectStmnt);
	    
            pst.setLong(1, f.getFriendId());
	    
            rs = pst.executeQuery();
	    
            
            f.setStatus(Friend.Status.OFFLINE);
            
	    while (rs.next())
	    {
                lastActive = System.currentTimeMillis() - rs.getLong("ts");
                
		break;
	    }
            
            if(lastActive <= 300000)
                f.setStatus(Friend.Status.ONLINE);
            
            f.setCurrentLoc((Location)LocationReceiver.gI().getCurrent(f.getFriendId()));
            f.setProfilePicId(PictureHandler.gI().getProfilePictureID(f));
            
            return f;
	} catch (InterruptedException ie) {
            Logger.gI().addMsg("Received Interrupt. Server Shuttin' down.");
            return new ProtocolMessage(ProtocolMessage.Type.GOODBYE, "Server shutting down.");
        } catch (Exception e)
	{
            e.printStackTrace();
	    Logger.gI().addError(e.getLocalizedMessage());
	}
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
        
        return new ProtocolMessage(ProtocolMessage.Type.ERROR, "Something went wrong while fetching information for your friend - plase contact Jerker");
    }
    
    public Data findUsers(ProtocolMessage pm) {
        ArrayResponse reqs = (ArrayResponse)FriendshipHandler.gI().getFriendRequests(pm);
        
        List users = new ArrayList<Friend>();
        
        String name = "%" + pm.getMessage() + "%";
        
        DBConnector dbc = null;
        
        try
        {   
            dbc = ConnectionPool.gI().getDBC();
            
            String selectStmnt = "SELECT DISTINCT userid, username, firstname, lastname "
                    + "FROM user WHERE "
                    + "(username LIKE ? OR firstname LIKE ? OR lastname LIKE ?) "
                    + "AND user.userid NOT IN "
                    + "(SELECT friendid FROM isfriendwith WHERE userid = ?) "
                    + "AND user.userid NOT IN (SELECT userid FROM friendrequest WHERE friendid = ? "
                    + "AND completed = ?)";
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    selectStmnt);
	    
            pst.setString(1, name);
            pst.setString(2, name);
            pst.setString(3, name);
            pst.setLong(4, pm.getUserId());
            pst.setLong(5, pm.getUserId());
            pst.setBoolean(6, false);
	    
            ResultSet rs = dbc.execSelect(pm, pst);
            
	    while (rs.next())
	    {
                Friend f = new Friend(rs.getLong("userid"));
                
                if(f.getFriendId() == pm.getUserId())
                    continue;
                
                f.setUsername(rs.getString("username"));
                f.setFirstname(rs.getString("firstname"));
                f.setLastname(rs.getString("lastname"));
                
                ProtocolMessage m = new ProtocolMessage(ProtocolMessage.Type.PEOPLE_SEARCH,
                        Long.toString(f.getFriendId()));
                
                m.setUserId(pm.getUserId());
                
                if(FriendshipHandler.gI().isPending(m))
                    f.setFriendType(Friend.FriendType.PENDING);
                else
                    f.setFriendType(Friend.FriendType.NONE);
                
                f.setProfilePicId(PictureHandler.gI().getProfilePictureID(f));
                
                users.add(f);
	    }
            
            return new ArrayResponse(users.toArray());
	} catch (InterruptedException ie) {
            Logger.gI().addMsg("Received Interrupt. Server Shuttin' down.");
            return new ProtocolMessage(ProtocolMessage.Type.GOODBYE, "Server shutting down.");
        } catch (Exception e)
	{
            e.printStackTrace();
            
	    Logger.gI().addError(e.getLocalizedMessage());
            
            return new ProtocolMessage(ProtocolMessage.Type.ERROR, e.getLocalizedMessage());
	}
        finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
    }
    
    public Data getOnlineFriends(ProtocolMessage pm) {       
        List onlineFriends = new ArrayList<Friend>();
        
        DBConnector dbc = null;
        
        try
        {   
            dbc = ConnectionPool.gI().getDBC();
            
            String selectStmnt = "SELECT friendid" +
                    " FROM isfriendwith WHERE userid = ?";
            
            PreparedStatement pst = dbc.getConnection().prepareStatement(
                    selectStmnt);
	    
            pst.setLong(1, pm.getUserId());
	    
            ResultSet rs = dbc.execSelect(pm, pst);
            
	    while (rs.next())
	    {
                Friend f = new Friend(rs.getLong("friendid"));
                
                Data d = getFriend(f);
                
                if(d instanceof ProtocolMessage)
                    continue;
                
                f = (Friend) d;
                    
                if(f.getStatus() == Friend.Status.ONLINE)
                    onlineFriends.add(f);
	    }
            
            return new ArrayResponse(onlineFriends.toArray());
        } catch (InterruptedException ie) {
            Logger.gI().addMsg("Received Interrupt. Server Shuttin' down.");
            return new ProtocolMessage(ProtocolMessage.Type.GOODBYE, "Server shutting down.");
        } catch (Exception e) {
            e.printStackTrace();
            Logger.gI().addError(e.getLocalizedMessage());
            
            return new ProtocolMessage(ProtocolMessage.Type.ERROR, "Could not fetch online friends.");
        } finally {
            ConnectionPool.gI().releaseDBC(dbc);
        }
    }
}
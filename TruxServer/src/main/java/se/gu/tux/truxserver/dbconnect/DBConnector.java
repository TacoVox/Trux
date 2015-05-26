/*                                                                                                                                                                                
 * Author: Jonas Kahler                                                                                                                                                            
 * eMail: jonas.kahler@icloud.com                                                                                                                                                   
 * Phone: +46760693760                                                                                                                                                             
 * ID: 940208-5915    
 *
 * Copyright (C) 2014-2015 Jonas Kahler
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package se.gu.tux.truxserver.dbconnect;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import se.gu.tux.trux.datastructure.Data;

import se.gu.tux.truxserver.config.Config;

import se.gu.tux.truxserver.logger.Logger;

/**
 * Class to create a connection to a database. It is also able to open and close
 * this connection.
 *
 * @author <a href="mailto:jonas.kahler@icloud.com">Jonas Kahler</a>
 * @version 4.4
 */
public class DBConnector {

    private Connection connection = null;

    private DatabaseMetaData dbmd = null;

    /**
     * Constructor
     *
     * Loads the driver and catches a possible exception
     */
    protected DBConnector() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Logger.gI().addMsg("MySQL driver loaded...");
        } catch (ClassNotFoundException | InstantiationException |
                IllegalAccessException e) {
            e.printStackTrace();
            Logger.gI().addError(e.toString());
        }
    }

    /**
     * Method to open the connection. The created connection is saved in a
     * private field of the this class. Uses the connection details assigned to
     * the private variables of this class.
     */
    protected void openConnection() {
        try {
            String addr = Config.gI().getDbaddress();
            String dbname = Config.gI().getDbname();
            String user = Config.gI().getDbuser();
            String password = Config.gI().getDbpass();

            connection = DriverManager.getConnection("jdbc:mysql://" + addr
                    + "/" + dbname + "?" + "user=" + user + "&password=" + password + "&autoReconnect=true");

            dbmd = connection.getMetaData();

            System.out.println("Connected to " + dbmd.getURL() + "...");
        } catch (SQLException ex) {
            ex.printStackTrace();
            Logger.gI().addError(ex.toString());
        }
    }

    /**
     * Method that returns the connection.
     *
     * @return connection created by openConnection()
     */
    protected Connection getConnection() {
        return connection;
    }

    /**
     * Method to check if the connection is still a proper connection to the db.
     *
     * @return boolean if the correction is proper or not
     */
    protected boolean isValid() {
        try {
            return connection.isValid(1);
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.gI().addError(e.getMessage());
        }

        return false;
    }

    /**
     * Method to close the connection.
     */
    protected void closeConnection() {
        try {
            connection.close();

            System.out.println("Disconnected from " + dbmd.getURL() + "...");
        } catch (SQLException ex) {
            ex.printStackTrace();
            Logger.gI().addError(ex.toString());
        }
    }

    /**
     * Method to execute a select statement. The method checks if the passed
     * object has a valid sessionid.
     *
     * @param d a Data object (for checking the sessionid)
     * @param pst a ready-to-use PreparedStatement
     *
     * @return a ResultSet including all things returned by the DB
     *
     * @throws SQLException
     */
    protected ResultSet execSelect(Data d, PreparedStatement pst) throws SQLException {
        String pststring = pst.toString().substring(pst.toString().indexOf("SELECT"), pst.toString().length());

        PreparedStatement finalpst = this.connection.prepareStatement(
                getAdvSessionCheck(pststring));

        finalpst.setLong(1, d.getSessionId());
        finalpst.setLong(2, d.getUserId());

        //Logger.gI().addDebug(finalpst.toString());
        return pst.executeQuery();
    }

    /**
     * Method to execute an Insert statement. The method checks if the passed
     * object has a valid sessionid.
     *
     * @param d a Data object (for checking the sessionid)
     * @param pst a ready-to-use PreparedStatement
     *
     * @return a Result set containing the inserted keys.
     *
     * @throws SQLException
     */
    protected ResultSet execInsert(Data d, PreparedStatement pst) throws SQLException {
        String pststring = pst.toString().substring(pst.toString().indexOf("INSERT"), pst.toString().length());

        PreparedStatement finalpst = this.connection.prepareStatement(
                getSessionCheck(pststring), Statement.RETURN_GENERATED_KEYS);

        finalpst.setLong(1, d.getSessionId());
        finalpst.setLong(2, d.getUserId());

        //Logger.gI().addDebug(finalpst.toString());
        finalpst.executeUpdate();

        return finalpst.getGeneratedKeys();
    }

    /**
     * Mehthod to execute an update statement. The method checks if the passed
     * object has a valid sessionid.
     *
     * @param d a Data object (for checking the sessionid)
     * @param pst a ready-to-use PreaparedStatement
     *
     * @throws SQLException
     */
    protected void execUpdate(Data d, PreparedStatement pst) throws SQLException {
        String pststring = pst.toString().substring(pst.toString().indexOf("UPDATE"), pst.toString().length());

        PreparedStatement finalpst = this.connection.prepareStatement(
                getAdvSessionCheck(pststring));

        finalpst.setLong(1, d.getSessionId());
        finalpst.setLong(2, d.getUserId());

        //Logger.gI().addDebug(finalpst.toString());
        pst.executeUpdate();
    }

    protected void execReplace(Data d, PreparedStatement pst) throws SQLException {
        String pststring = pst.toString().substring(pst.toString().indexOf("REPLACE"), pst.toString().length());

        PreparedStatement finalpst = this.connection.prepareStatement(
                getAdvSessionCheck(pststring));

        finalpst.setLong(1, d.getSessionId());
        finalpst.setLong(2, d.getUserId());

        //Logger.gI().addDebug(finalpst.toString());
        pst.executeUpdate();

        //return pst.getGeneratedKeys();
    }

    protected void execDelete(Data d, PreparedStatement pst) throws SQLException {
        String pststring = pst.toString().substring(pst.toString().indexOf("DELETE"), pst.toString().length());

        PreparedStatement finalpst = this.connection.prepareStatement(
                getAdvSessionCheck(pststring));

        finalpst.setLong(1, d.getSessionId());
        finalpst.setLong(2, d.getUserId());

        //Logger.gI().addDebug(finalpst.toString());
        pst.executeUpdate();
    }

    private String getSessionCheck(String statement) {
        return statement + " WHERE EXISTS (SELECT * FROM session WHERE "
                + "sessionid = ? AND userid = ? AND endtime IS NULL);";
    }

    private String getAdvSessionCheck(String statement) {
        int whereindex = statement.indexOf("WHERE");
        String firstpart = statement.toString().substring(0, whereindex + 6);
        String secondpart = statement.toString().substring(whereindex + 6, statement.length());

        return firstpart + "EXISTS (SELECT * FROM session WHERE "
                + "sessionid = ? AND userid = ? AND endtime IS NULL) AND " + secondpart;
    }
}
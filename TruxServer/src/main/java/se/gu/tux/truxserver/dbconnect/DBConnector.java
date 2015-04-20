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

import se.gu.tux.truxserver.config.Config;

import se.gu.tux.truxserver.logger.Logger;

/**
 * Class to create a connection to a database.
 * It is also able to open and close this connection.
 * 
 * @author <a href="mailto:jonas.kahler@icloud.com">Jonas Kahler</a>
 * @version 4.4
 */

public class DBConnector
{
    private Connection connection = null;
    
    private DatabaseMetaData dbmd = null;
    
    /**
     * Constructor
     * 
     * Loads the driver and catches a possible exception
     */
    protected DBConnector()
    {       
        try
	{
	    Class.forName("com.mysql.jdbc.Driver").newInstance();
            Logger.gI().addMsg("MySQL driver loaded...");
	}
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException e)
	{
	    Logger.gI().addError(e.toString());
	}
    }

    /**
     * Method to open the connection. The created connection is saved in a private field of the this class.
     * Uses the connection details assigned to the private variables of this class.
     */
    protected void openConnection()
    {
        try
        {
            String addr = Config.gI().getDbaddress();
            String dbname = Config.gI().getDbname();
            String user = Config.gI().getDbuser();
            String password = Config.gI().getDbpass();

	    connection = DriverManager.getConnection("jdbc:mysql://" + addr +
                    "/" + dbname + "?" + "user=" + user + "&password=" + password);
            dbmd = connection.getMetaData();
            
	    System.out.println("Connected to " + dbmd.getURL() + "...");
	}
        catch (SQLException ex)
	{
            Logger.gI().addError(ex.toString());
	}
    }

    /**
     * Method that returns the connection.
     *
     * @return connection created by openConnection()
     */
    protected Connection getConnection()
    {
        return connection;
    }

    /**
     * Method to close the connection.
     */
    protected void closeConnection()
    {
        try
	{
	    connection.close();
            
	    System.out.println("Disconnected from " + dbmd.getURL() + "...");
	}
        catch (SQLException ex)
	{
            Logger.gI().addError(ex.toString());
	}
    }

}
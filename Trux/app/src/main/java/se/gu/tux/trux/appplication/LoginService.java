package se.gu.tux.trux.appplication;


import android.content.Context;
import android.content.Intent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.ProtocolMessage;
import se.gu.tux.trux.datastructure.User;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import se.gu.tux.trux.technical_services.ServerConnector;

/**
 * Created by ivryashkov on 2015-04-20.
 *
 * Handles the login and logout to the application.
 *
 */
public class LoginService
{

    // user object for storing user info
    private User user;

    // context object, we get it from MainActivity, used for opening
    // file input and output streams
    private Context context;

    // reference to the file name for storing user info
    private String fileName = "";


    private static LoginService ls;


    /**
     * Constructor. Takes a Context object as parameter.
     *
     * @param context   The Context object to be used.
     */
    private LoginService(Context context, String fileName)
    {
        // initialise user
        user = new User();
        // get the context
        this.context = context;
        // get the file name
        this.fileName = fileName;
    }


    public static void createInstance(Context context, String fileName)
    {
        if (ls == null)
        {
            ls = new LoginService(context, fileName);
        }
    }


    public static LoginService getInstance()
    {
        return ls;
    }


    /**
     * Checks if the user credentials are correct. Takes a String with the user's
     * username and a String with the user's password as parameters. Returns true if
     * login is successful, false otherwise.
     *
     * @param username      The user's username.
     * @param password      The user's password.
     * @param sessionId     The session Id.
     * @param userId        The user Id.
     * @param keepFlag      1 -- keep logged in, 0 -- otherwise
     * @return              true if login successful, false otherwise
     */
    public boolean login(String username, String password, Long sessionId, Long userId, Short keepFlag)
    {
        // set username in user object
        user.setUsername(username);

        // create hash for the password
        String hashPass = createHash(password);
        // set password hash in user object
        user.setPasswordHash(hashPass);

        // set session ID
        user.setSessionId(sessionId);

        // set user ID
        user.setUserId(userId);

        // check if the user wants to stay logged in
        if (keepFlag == 1)
        {
            user.setStayLoggedIn(true);
        }
        else
        {
            user.setStayLoggedIn(false);
        }

        // set this user in DataHandler for use in ServerConnector
        // when called to answer query
        DataHandler.getInstance().setUser(user);

        ProtocolMessage response = null;

        try
        {
            // check if this user is allowed to login
            response = (ProtocolMessage) ServerConnector.getInstance().answerQuery(user);
        }
        catch (NotLoggedInException e)
        {
            e.printStackTrace();
        }
        catch (ClassCastException e)
        {
            e.printStackTrace();
        }

        System.out.println("------- user login info ----------------");
        System.out.println("user is null? " + response == null);
        System.out.println("session ID: " + response.getSessionId());
        System.out.println("user ID: " + response.getUserId());
        System.out.println("----------------------------------------");

        if (response.getType() == ProtocolMessage.Type.LOGIN_SUCCESS)
        {
            // if the login is approved, set the user in DataHandler
            // now the user has a valid session ID
            user.setUsername(username);
            user.setPasswordHash(hashPass);
            user.setSessionId(response.getSessionId());
            user.setUserId(response.getUserId());

            DataHandler.getInstance().setUser(user);

            String userInfo = DataHandler.getInstance().getUser().getUsername() + ":" +
                            DataHandler.getInstance().getUser().getPasswordHash() + ":" +
                            DataHandler.getInstance().getUser().getSessionId() + ":" +
                            DataHandler.getInstance().getUser().getUserId() + ":" +
                            DataHandler.getInstance().getUser().getStayLoggedIn();

            // save user info to file
            writeToFile(userInfo);

            return true;
        }
        else
        {
            return false;
        }

    } // end login()


    /**
     * Logs out the user from the application.
     */
    public boolean logout()
    {
        boolean isLoggedOut = false;

        ProtocolMessage pm = null;

        try
        {
            // send a protocol message with a request to log out
            pm = (ProtocolMessage) ServerConnector.getInstance().answerQuery(new ProtocolMessage(ProtocolMessage.Type.LOGOUT_REQUEST));

        }
        catch (NotLoggedInException e)
        {
            e.printStackTrace();
        }

        if (pm != null && pm.getType() == ProtocolMessage.Type.SUCCESS)
        {

            // Clear the user details file
            writeToFile("LOGGED_OUT");

            DataHandler.getInstance().setUser(null);
            isLoggedOut = true;
        } else {
            System.out.println("Received something else than sucess when trying to log out...!");
        }

        return isLoggedOut;
    }


    /**
     * Hashes the user password. Takes a String with the user password (not hashed)
     * as parameter. Returns a String with the hashed password.
     *
     * @param password      The user's password.
     * @return              String
     */
    public String createHash(String password)
    {
        String generatedPassword = "";

        try
        {
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            //Add password bytes to digest
            md.update(password.getBytes());

            //Get the hash's bytes
            byte[] bytes = md.digest();

            //This bytes[] has bytes in decimal format;
            //Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();

            for(int i=0; i< bytes.length ;i++)
            {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }

            //Get complete hashed password in hex format
            generatedPassword = sb.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }

        return generatedPassword;

    } // end createHash()


    /**
     * Writes the user data to the file. Takes a String with the user info.
     *
     * @param data      The user data.
     */
    public void writeToFile(String data)
    {
        try
        {
            // open output stream
            OutputStreamWriter outputStreamWriter =
                    new OutputStreamWriter(context.openFileOutput(fileName, Context.MODE_PRIVATE));

            System.out.println("-------- writing to file ----------");

            // write data
            outputStreamWriter.write(data);
            // make sure data is written
            outputStreamWriter.flush();
            // close output stream
            outputStreamWriter.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.out.println("-------- ERROR: writing to file ----------");
        }
    }


    /**
     * Reads the user's data from the file. Returns a String array with the results.
     *
     * @return      String[]
     */
    public String[] readFromFile()
    {
        // the array to be returned
        String[] results = null;

        try
        {
            // open input stream
            InputStream inputStream = context.openFileInput(fileName);

            if ( inputStream != null )
            {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String receiveString = "";

                StringBuilder stringBuilder = new StringBuilder();

                System.out.println("-------- reading from file ----------");

                // read the data
                while ( (receiveString = bufferedReader.readLine()) != null )
                {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();

                results = stringBuilder.toString().split(":");

                if (results.length == 1) {
                    // It most likely says LOGGED_OUT
                    results = null;
                }
                else if (results.length < 5)
                {
                    // Invalid! Replace it with LOGGED_OUT
                    writeToFile("LOGGED_OUT");
                    results = null;
                }
                else
                {
                    System.out.println("-------- user info in file -------------");
                    System.out.println("username: " + results[0]);
                    System.out.println("password hash: " + results[1]);
                    System.out.println("session ID: " + results[2]);
                    System.out.println("user ID: " + results[3]);
                    System.out.println("stay logged in? : " + results[4]);
                    System.out.println("----------------------------------------");
                }
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            System.out.println("-------- ERROR: reading from file ----------");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.out.println("-------- ERROR: reading from file ----------");
        }

        return results;
    }



} // end class

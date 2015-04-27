package se.gu.tux.trux.appplication;


import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.ProtocolMessage;
import se.gu.tux.trux.datastructure.User;
import se.gu.tux.trux.gui.MainActivity;
import se.gu.tux.trux.technical_services.NotLoggedInException;
import se.gu.tux.trux.technical_services.ServerConnector;

/**
 * Created by ivryashkov on 2015-04-20.
 *
 * Handles the login to the application.
 *
 */
public class LoginService
{

    // user object for storing user info
    private User user;

    // context object, we get it from MainActivity, used for opening
    // file input and output streams
    private Context context;

    // file object to store user info, used to read from
    // when the user checks the box to kept logged in
    private File file;

    // file name
    private static final String fileName = "trux_user_config";


    /**
     * Constructor. Takes a Context object as parameter.
     *
     * @param context   The Context object to be used.
     */
    public LoginService(Context context)
    {
        // initialise user
        user = new User();
        // get the context
        this.context = context;
        // create a file to store data
        file = new File(context.getFilesDir(), fileName);

    }


    /**
     * Checks if the user credentials are correct. Takes a String with the user's
     * username and a String with the user's password as parameters. Returns true if
     * login is successful, false otherwise.
     *
     * @param username      The user's username.
     * @param password      The user's password.
     * @return              true if login successful, false otherwise
     */
    public boolean isAllowed(String username, String password)
    {
        // set username in user object
        user.setUsername(username);

        // create hash for the password
        String hashPass = createHash(password);
        // set password hash in user object
        user.setPasswordHash(hashPass);

        // set session ID, -1 for a new login session
        user.setSessionId(-1);
        // set this user in DataHandler for future reference
        DataHandler.getInstance().setUser(user);

        Data response = null;

        try
        {
            // check if this user is allowed to login
            response = (Data) ServerConnector.getInstance().answerQuery(user);
        }
        catch (NotLoggedInException e)
        {
            e.printStackTrace();
        }

        System.out.println("------- user login info ----------------");
        System.out.println("user is null? " + response == null);
        System.out.println("session ID: " + response.getSessionId());
        System.out.println("user ID: " + response.getUserId());
        System.out.println("----------------------------------------");

        if (response instanceof User)
        {
            // if the login is approved, set the user in DataHandler
            // now the user has a valid session ID
            DataHandler.getInstance().setUser((User) response);

            String userInfo = DataHandler.getInstance().getUser().getUsername() + ":" +
                            DataHandler.getInstance().getUser().getPasswordHash() + ":" +
                            DataHandler.getInstance().getUser().getSessionId();

            // save user info to file
            writeToFile(userInfo);

            return true;
        }
        else
        {
            return false;
        }

    } // end isAllowed()


    /**
     * Logs out the user from the application.
     */
    public void logout()
    {
        try
        {
            // send a protocol message with a request to log out
            ServerConnector.getInstance().answerQuery(new ProtocolMessage(ProtocolMessage.Type.LOGOUT_REQUEST));
        }
        catch (NotLoggedInException e)
        {
            e.printStackTrace();
        }
    }


    /**
     * Hashes the user password. Takes a String with the user password (not hashed)
     * as parameter. Returns a String with the hashed password.
     *
     * @param password      The user's password.
     * @return              String
     */
    private String createHash(String password)
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

            outputStreamWriter.write(data);

            System.out.println("-------- writing to file ----------");

            outputStreamWriter.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
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
        String[] ret = null;

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

                // read the data
                while ( (receiveString = bufferedReader.readLine()) != null )
                {
                    stringBuilder.append(receiveString);
                }

                System.out.println("-------- reading from file ----------");

                inputStream.close();

                ret = stringBuilder.toString().split(":");

                System.out.println("-------- user info in file -------------");
                System.out.println("username: " + ret[0]);
                System.out.println("password hash: " + ret[1]);
                System.out.println("session ID: " + ret[2]);
                System.out.println("----------------------------------------");
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return ret;
    }



} // end class

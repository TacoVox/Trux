package se.gu.tux.trux.appplication;

/*
 * Password Hashing With PBKDF2 (http://crackstation.net/hashing-security.htm).
 * Copyright (c) 2013, Taylor Hornby
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

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
 */
public class LoginService
{

    private User user;

    private Context context;

    private File file;

    private static final String fileName = "trux_user_config";


    public LoginService(Context context)
    {
        user = new User();
        this.context = context;

        file = new File(context.getFilesDir(), fileName);

    }


    public boolean isAllowed(String username, String password)
    {
        user.setUsername(username);

        String hashPass = createHash(password);
        user.setPasswordHash(hashPass);

        user.setSessionId(-1);
        DataHandler.getInstance().setUser(user);

        Data response = null;

        try
        {
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
            DataHandler.getInstance().setUser((User) response);

            String userInfo = DataHandler.getInstance().getUser().getUsername() + ":" +
                            DataHandler.getInstance().getUser().getPasswordHash() + ":" +
                            DataHandler.getInstance().getUser().getSessionId();

            writeToFile(userInfo);

            return true;
        }
        else
        {
            return false;
        }

    } // end isAllowed()


    public void logout()
    {
        try
        {
            ServerConnector.getInstance().answerQuery(new ProtocolMessage(ProtocolMessage.Type.LOGOUT_REQUEST));
        }
        catch (NotLoggedInException e)
        {
            e.printStackTrace();
        }
    }


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



    public void writeToFile(String data)
    {

        try
        {
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



    public String[] readFromFile()
    {
        String[] ret = null;

        try
        {
            InputStream inputStream = context.openFileInput(fileName);

            if ( inputStream != null )
            {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String receiveString = "";

                StringBuilder stringBuilder = new StringBuilder();

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

package se.gu.tux.truxserver.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import java.util.Properties;

import se.gu.tux.truxserver.logger.Logger;

/**
 * Class to handle the configuration of the server
 * @author jonas
 */
public class ConfigHandler {
    /**
     * Static part.
     */
    private static ConfigHandler handler = null;

    static {
    	if (handler == null) {
    		handler = new ConfigHandler();
    	}
    }
    
    public static ConfigHandler getInstance()
    {
        return handler;
    }
    
    public static ConfigHandler gI()
    {
        return handler;
    }
    
    /**
     * Non-static part.
     */
    private Properties properties = null;
    private String configPath = null;
    
    private ConfigHandler()
    {  
        this(System.getProperty("user.dir") + "/config/server.conf");
    }
    
    public ConfigHandler(String path)
    {   
        loadConfig(path);
    }
    
    public boolean setSettings(String args[])
    {
        for(String s : args)
        {
            if(s.equals("-v") || s.equals("--verbose"))
                Config.gI().setVerbose(true);
            else if(s.equals("-h") || s.equals("--help")) {
                printHelpScreen();
                return false;
            }
        }
        
        return true;
    }
    
    private void printHelpScreen()
    {
        System.out.println("TruxServer v0.1");
        System.out.println("Helpscreen; The following commands for the TruxServer are available:");
        System.out.println("");
        System.out.println("-h or --help: prints this help screen.");
        System.out.println("-v or --verbose: prints out each logger output on the command line.");
    }
    
    private void loadConfig(String path)
    {
        properties = new Properties();

        try
        {
            InputStream input = new FileInputStream(path);
            
            properties.load(input);
        }
        catch(IOException ioe)
        {
            Logger.gI().addError("No config file found:\n"
                    + ioe.toString() + "\n Creating a new file.");
            
            newFile(path);
        }
        
        parseConfiguration();
    }
    
    /**
     * Method to create a config file
     * 
     * @param filename path to the new file
     */
    private void newFile(String path)
    {
        System.out.println("Creating a config file...");
            
        File dir = new File(System.getProperty("user.dir") + "/config");
        if(!dir.isDirectory())
            dir.mkdir();
        try
        {
            OutputStream newfile = new FileOutputStream(path);
            
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Do you want to run the server in verbose mode?" +
                    " (y or n) [y]");
            String y = br.readLine();
            if(y.equals("y") || y.equals("n"))
                properties.setProperty("Verbose", y);
            else
                properties.setProperty("Verbose", "y");
                
            System.out.println("Please enter information about you database!");
                
            System.out.println("MySQL host address:");
            properties.setProperty("address", br.readLine());
                
            System.out.println("Port:");
            properties.setProperty("port", br.readLine());
                
            System.out.println("Databasename:");
            properties.setProperty("dbname", br.readLine());
                
            System.out.println("Username:");
            properties.setProperty("user", br.readLine());
                
            System.out.println("Password:");
            properties.setProperty("password", br.readLine());
                
            properties.store(newfile, null);
        }
        catch(IOException ioe)
        {
            Logger.gI().addError("Cannot create a new config file:\n"
                    + ioe.toString());
        }
    }
    
    /**
     * Method to get a ready-to-use config String.
     * 
     * @return String which has a ready-to-use for the database connector
     */
    private void parseConfiguration()
    {
        if(properties.getProperty("Verbose").equals("y"))
            Config.gI().setVerbose(true);
        else
            Config.gI().setVerbose(false);
        
        Config.gI().setDbaddress(properties.getProperty("address"));
        Config.gI().setPort(Integer.parseInt(properties.getProperty("port")));
        Config.gI().setDbname(properties.getProperty("dbname"));
        Config.gI().setDbuser(properties.getProperty("user"));
        Config.gI().setDbpass(properties.getProperty("password"));
    }
}
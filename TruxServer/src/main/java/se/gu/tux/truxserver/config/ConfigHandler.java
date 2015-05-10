package se.gu.tux.truxserver.config;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.Properties;
import se.gu.tux.truxserver.file.ConfigIO;

import se.gu.tux.truxserver.logger.Logger;

/**
 * Class to read from and write to a config file.
 *
 * @author jonas
 */
public class ConfigHandler {

    /**
     * Static part.
     */
    private static ConfigHandler handler = null;

    /**
     * Method returning an instance of the ConfigHandler.
     *
     * @return ConfigHandler instance
     */
    public static ConfigHandler getInstance() {
        if (handler == null)
            handler = new ConfigHandler();
        return handler;
    }

    /**
     * Method returning an instance of the ConfigHandler.
     *
     * @return ConfigHandler instance
     */
    public static ConfigHandler gI() {
        return getInstance();
    }

    /**
     * Non-static part.
     */
    //Object wrapping the config file
    private Properties properties = null;

    /**
     * Private Constructor
     */
    private ConfigHandler() {
        this(System.getProperty("user.dir") + "/config/server.conf");
    }

    /**
     * Constructor to override the standart settings path.
     * 
     * @param path Path to a valid .conf file
     */
    public ConfigHandler(String path) {
        loadConfig(path);
    }

    /**
     * Method which store custom settings via command line arguments.
     * 
     * @param args Command line arguments
     * 
     * @return boolean depending on success
     */
    public boolean setSettings(String args[]) {
        for (String s : args) {
            if (s.equals("-v") || s.equals("--verbose")) {
                Config.gI().setVerbose(true);
            } else if (s.equals("-h") || s.equals("--help")) {
                printHelpScreen();
                return false;
            }
        }
        return true;
    }

    /**
     * Method for printing out the help screen.
     */
    private void printHelpScreen() {
        System.out.println("TruxServer v1.2");
        System.out.println("Helpscreen; The following commands for the TruxServer are available:");
        System.out.println("");
        System.out.println("-h or --help: prints this help screen.");
        System.out.println("-v or --verbose: prints out each logger output on the command line.");
    }

    /**
     * Method which loads the setting out of a config file.
     * 
     * @param path Path to a valid .conf file
     */
    private void loadConfig(String path) {
        properties = ConfigIO.gI().loadConfig(path);

        if(properties == null)
            newFile(path);
        
        parseConfiguration();
    }

    /**
     * Method to create a config file
     *
     * @param filename path to the new file
     */
    private void newFile(String path) {
        System.out.println("Creating a config file...");

        try {

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Do you want to run the server in verbose mode?"
                    + " (y or n) [y]");
            String y = br.readLine();
            if (y.equals("y") || y.equals("n")) {
                properties.setProperty("Verbose", y);
            } else {
                properties.setProperty("Verbose", "y");
            }

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

            System.out.println("How often should the server clean up non-current sessions?");
            properties.setProperty("cleanupinterval", br.readLine());

            System.out.println("After which time shall a session time out?");
            properties.setProperty("sessiontimeout", br.readLine());

            System.out.println("How many DB connections should be opened at the same time?");
            properties.setProperty("maxdbconnections", br.readLine());

            System.out.println("Which GMail account do you want to use?");
            properties.setProperty("gmailuser", br.readLine());

            System.out.println("What is the password of this account?");
            properties.setProperty("gmailpass", br.readLine());

            ConfigIO.gI().createConfig(properties);
        } catch (IOException ioe) {
            Logger.gI().addError("Cannot create a new config file:\n"
                    + ioe.toString());
        }
    }

    /**
     * Initiate the Config instance with the correct settings
     */
    private void parseConfiguration() {
        if (properties.getProperty("Verbose").equals("y")) {
            Config.gI().setVerbose(true);
        } else {
            Config.gI().setVerbose(false);
        }

        //Set the settings regarding the MySQL DB
        Config.gI().setDbaddress(properties.getProperty("address"));
        Config.gI().setPort(Integer.parseInt(properties.getProperty("port")));
        Config.gI().setDbname(properties.getProperty("dbname"));
        Config.gI().setDbuser(properties.getProperty("user"));
        Config.gI().setDbpass(properties.getProperty("password"));
        
        //Setting the settings regarding the session cleanup
        Config.gI().setCleanupInterval(Integer.parseInt(properties.getProperty("cleanupinterval")));
        Config.gI().setSessionTimeout(Integer.parseInt(properties.getProperty("sessiontimeout")));
        
        Config.gI().setMaxNoDBConnections(Short.parseShort(properties.getProperty("maxdbconnections")));
        
        //Setting the settings regarding the eMail
        Config.gI().setGmailUser((String) properties.getProperty("gmailuser"));
        Config.gI().setGmailPass((String) properties.getProperty("gmailpass"));
    }
}
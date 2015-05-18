package se.gu.tux.trux.application;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Class to handle and wrap settings.
 * Created by jonas on 15.05.15.
 */
public class SettingsHandler {
    /*
     * Static part.
     */
    private static SettingsHandler instance;

    public static SettingsHandler getInstance() {
        if(instance == null)
            instance = new SettingsHandler();

        return instance;
    }

    public static SettingsHandler gI() {
        return getInstance();
    }

    /*
     * Non-static part.
     */
    //Wrapper for the config file itself.
    private Properties properties;

    //Settings variables
    private boolean normalMap = true;

    private SettingsHandler() {
        properties = loadProperties();

        if(properties == null)
            createProperties();
        else
            parseProperties();
    }

    private Properties loadProperties() {
        Properties p = new Properties();

        try {
            InputStream input = new FileInputStream("trux.conf");
            p.load(input);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return p;
    }

    private void createProperties() {
        properties = new Properties();

        properties.setProperty("maptype", "normal");

        try {
            OutputStream newfile = new FileOutputStream("trux.conf");
            properties.store(newfile, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseProperties() {
        if (properties.getProperty("maptype").equals("hybrid"))
            normalMap = false;
    }

    public boolean isNormalMap() {
        return normalMap;
    }

    public void setNormalMap(boolean normalMap) {
        this.normalMap = normalMap;
    }
}

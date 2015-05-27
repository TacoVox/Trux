package se.gu.tux.trux.application;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

/**
 * Class to handle and wrap settings.
 * Created by jonas on 15.05.15.
 */
public class SettingsHandler implements Serializable {
    /*
     * Static part.
     */
    private static SettingsHandler instance;

    public static void createInstance(Context context) {
        instance = new SettingsHandler(context);
    }

    public static SettingsHandler getInstance() {
        return instance;
    }

    public static SettingsHandler gI() {
        return getInstance();
    }


    /**
     * Used by the activities' restore instance state methods, to be able to restore the settings
     * handler instance, otherwise we have problems with it getting garbage collected
     * @param i     The instance
     */
    public static void setInstance (SettingsHandler i) {
        instance = i;
    }

    /*
     * Non-static part.
     */
    //Wrapper for the config file itself.
    private Properties properties;
    private String configPath;

    //Settings variables
    private boolean normalMap = true;

    private SettingsHandler(Context context) {
        configPath = context.getFilesDir().getPath().toString() + "/trux.conf";

        loadProperties();

        if (properties.isEmpty())
            createProperties();
        else
            parseProperties();
    }

    private void loadProperties() {
        Properties p = new Properties();

        try {
            InputStream input = new FileInputStream(configPath);
            System.out.println("Loading properties.");
            p.load(input);
        } catch (Exception e) {
            e.printStackTrace();
        }

        properties = p;
    }

    private void createProperties() {
        System.out.println("Creating new properties.");
        properties = new Properties();

        properties.setProperty("maptype", "normal");

        writeProperties();
    }

    public void writeProperties()
    {
        System.out.println("Starting to write properties in a new file.");
        try {
            FileOutputStream out = new FileOutputStream(configPath);
            properties.store(out, null);
            out.close();
        } catch (IOException e) {
            System.err.println("Failed to open trux.conf file...");
            e.printStackTrace();
        }
    }

    private void parseProperties() {
        if (properties.getProperty("maptype").equals("hybrid")) {
            normalMap = false;
            System.out.println("Map is hybrid.");
        } else
            System.out.println("Map is normal");
    }

    public boolean isNormalMap() {
        return normalMap;
    }

    public void setNormalMap(boolean normalMap) {
        this.normalMap = normalMap;

        if (normalMap) {
            properties.setProperty("maptype", "normal");
            System.out.println("Changed map to normal.");
        } else {
            properties.setProperty("maptype", "hybrid");
            System.out.println("Changed map to hybrid.");
        }

        writeProperties();
    }
}

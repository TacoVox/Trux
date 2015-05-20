package se.gu.tux.trux.application;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    public static SettingsHandler getInstance(Context context) {
        if(instance == null)
            instance = new SettingsHandler(context);
        return instance;
    }

    public static SettingsHandler gI(Context context) {
        return getInstance(context);
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

        if (properties == null)
            createProperties();
        else
            parseProperties();
    }

    public void writeProperties()
    {
        try {
            FileOutputStream out = new FileOutputStream(configPath);
            properties.store(out, null);
            out.close();
        } catch (IOException e) {
            System.err.println("Failed to open trux.conf file...");
            e.printStackTrace();
        }
    }

    private Properties loadProperties() {
        Properties p = new Properties();

        try {
            InputStream input = new FileInputStream(configPath);
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

        writeProperties();
    }

    private void parseProperties() {
        if (properties.getProperty("maptype").equals("hybrid")) {
            normalMap = false;
            System.out.println("Map is hybrid.");
        } else
            System.out.println("Map is normal motherfucker");
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

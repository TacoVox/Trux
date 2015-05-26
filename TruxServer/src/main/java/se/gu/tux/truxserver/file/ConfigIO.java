/*
 * Copyright 2015 jonas.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.gu.tux.truxserver.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import se.gu.tux.truxserver.logger.Logger;

/**
 * Class responsible for reading and writing to a .conf file.
 *
 * @author jonas
 */
public class ConfigIO {

    /**
     * Static part.
     */
    private static ConfigIO cio;

    public static ConfigIO getInstance() {
        if (cio == null) {
            cio = new ConfigIO();
        }

        return cio;
    }

    public static ConfigIO gI() {
        return getInstance();
    }

    /**
     * Non-static part.
     */
    /**
     * Constructor.
     */
    private ConfigIO() {
    }

    public Properties loadConfig(String path) {
        Properties p = new Properties();

        try {
            InputStream input = new FileInputStream(path);

            p.load(input);
        } catch (IOException ioe) {
            Logger.gI().addError("No config file found:\n"
                    + ioe.toString() + "\n Creating a new file.");
        }

        return p;
    }

    public void createConfig(Properties p) {
        String path = System.getProperty("user.dir") + "/config";

        File dir = new File(path);
        if (!dir.isDirectory()) {
            dir.mkdir();
        }

        try {
            OutputStream newfile = new FileOutputStream(path + "server.conf");

            p.store(newfile, null);
        } catch (Exception e) {
            Logger.gI().addError(e.getLocalizedMessage());
        }
    }
}
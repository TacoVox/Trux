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
package se.gu.tux.truxserver.logger;

import se.gu.tux.truxserver.file.LogIO;

/**
 * Singelton-class for handeling logs.
 * @author Jonas Kahler
 */
public final class Logger {
    /*
     * Static part.
     */
    private static Logger instance = null;

    /**
     * Method returning a Logger instance.
     * @return a logger instance.
     */
    public static Logger getInstance() {
        if(instance == null)
            instance = new Logger();
        return instance;
    }

    /**
     * Method returning a Logger instance.
     * @return a logger instance.
     */
    public static Logger gI() {
        return getInstance();
    }

    /*
     * Non-static part.
     */
    private boolean isVerbose = false;
    private LogIO lio;

    /**
     * Private constructor.
     */
    private Logger() {
        lio = new LogIO();
    }

    /**
     * Method for adding an error to the logfile.
     * @param descr description of the error
     */
    public void addError(String descr) {
        if (isVerbose) {
            System.err.println(descr);
        }
        lio.appendText("ERROR: " + descr);
    }

    /**
     * Method for adding debug output to the logfile.
     * @param message debug output
     */
    public void addDebug(String message) {
        if (isVerbose) {
            System.out.println(message);
        }
        lio.appendText("DEBUG: " + message);
    }

    /**
     * Method for adding a message to the logfile.
     * @param message the message
     */
    public void addMsg(String message) {
        if (isVerbose) {
            System.out.println(message);
        }
        lio.appendText(message);
    }

    /**
     * Test purpose main method.
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        Logger.getInstance().addError("HILFE");
        Logger.getInstance().addError("Suck my balls");
        Logger.gI().addDebug("Testing the debugging.");
    }

    /**
     * Method to check if the server runs in verbose output mode.
     * @return boolean if the server runs in verbose.
     */
    public boolean isVerbose() {
        return isVerbose;
    }

    /**
     * Method for setting the verbose mode.
     * @param isVerbose new setting.
     */
    public void setVerbose(boolean isVerbose) {
        this.isVerbose = isVerbose;
    }
}
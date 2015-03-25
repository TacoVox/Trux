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

/**
 *
 * @author jonas
 */
public final class Logger {
    
    /**
     * Static parts of this class
     */
    private static Logger logger;
    
    static
    {
        logger = new Logger();
    }
    
    public static Logger getInstance()
    {
        return logger;
    }
    
    public static Logger gI()
    {
        return logger;
    }
    
    /**
     * Non-static parts of this class
     */
    private FileHandler fh;
    
    private Logger()
    {
        fh = new FileHandler();
    }
    
    public void addError(String descr)
    {
        fh.appendText("ERROR: " + descr);
    }
    
    public void addDebug(String message)
    {
        fh.appendText("DEBUG: " + message);
    }
    
    //Test purpose main method!
    public static void main(String[] args)
    {
        Logger.getInstance().addError("HILFE");
        Logger.getInstance().addError("Suck my balls");
        Logger.gI().addDebug("Testing the debugging.");
    }
}

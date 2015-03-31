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
package se.gu.tux.truxserver.dbconnect;

import java.util.concurrent.LinkedBlockingQueue;

import se.gu.tux.trux.datastructure.MetricData;
import se.gu.tux.truxserver.logger.Logger;

/**
 *
 * @author jonas
 */
public class InsertionHandler implements Runnable {
    /**
     * Static part
     */
    private static InsertionHandler ih;
    
    static {
        if(ih == null)
            ih = new InsertionHandler();
    }
    
    public static InsertionHandler getInstance() {
        return ih;
    }
    
    public static InsertionHandler gI() {
        return ih;
    }
    
    /**
     * Non-static part.
     */
    private LinkedBlockingQueue queue;
    
    private InsertionHandler() {
        queue = new LinkedBlockingQueue();
    }
    
    @Override
    public void run() {
        Logger.gI().addMsg("InsertionHandler is running and waiting for input.");
        
        while(true) {
            if(!queue.isEmpty()) {
                try {
                    if(DBConnector.gS().getConnection().isClosed())
                        DBConnector.gS().openConnection();
                }
                catch (Exception e)
                {
                    Logger.gI().addError(e.toString());
                }
                
                MetricHandler.gI().insertMetric((MetricData)queue.poll());
            }
            
            if(queue.isEmpty())
                DBConnector.gS().closeConnection();
        }
    }
    
    public synchronized void addToDB(MetricData md) {
        queue.add(md);
    }
    
}

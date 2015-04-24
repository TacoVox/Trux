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
package se.gu.tux.truxserver.dataswitch;

import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.MetricData;
import se.gu.tux.trux.datastructure.ProtocolMessage;
import se.gu.tux.trux.datastructure.User;
import se.gu.tux.truxserver.dbconnect.MetricInserter;

/**
 *
 * @author jonas
 */
public class DataSwitcher {
    /**
     * Static part.
     */
    private static DataSwitcher ds = null;
    
    public static DataSwitcher getInstance() {
        if(ds == null)
            ds = new DataSwitcher();
        return ds;
    }
    
    public static DataSwitcher gI() {
        return getInstance();
    }
    
    /**
     * Non-static part.
     */
    private Thread mi = null;
    
    private DataSwitcher() {
        if (mi == null)
            mi = new Thread(MetricInserter.gI());
    }
    
    public void start() {
        mi.start();
    }
    
    public void stop() {
        mi.interrupt();
    }
    
    public Data handleData(Data d) {
        if (d instanceof MetricData)
            return MetricSwitcher.gI().handleMetricData((MetricData)d);
        else if (d instanceof User)
            return UserSwitcher.gI().handleUser((User) d);
        else if (d instanceof ProtocolMessage)
            return MessageSwitcher.gI().handleMessage((ProtocolMessage) d);
        else
            return null;
    }
} 

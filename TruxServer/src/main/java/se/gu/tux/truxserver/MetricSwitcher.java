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
package se.gu.tux.truxserver;

import se.gu.tux.trux.datastructure.MetricData;
import se.gu.tux.truxserver.dbconnect.MetricInserter;
import se.gu.tux.truxserver.dbconnect.MetricReceiver;

/**
 *
 * @author jonas
 */
public class MetricSwitcher {
    /**
     * Static part.
     */
    private static MetricSwitcher ms = null;
    
    static {
        if(ms == null)
            ms = new MetricSwitcher();
    }
    
    public static MetricSwitcher getInstance() {
        return ms;
    }
    
    public static MetricSwitcher gI() {
        return ms;
    }
    
    /**
     * Non-static part.
     */
    private MetricSwitcher() {}
    
    public MetricData parseData(MetricData md) {
        if(md.getTimeFrame() == 0) {
            MetricInserter.gI().addToDB(md);
            
            return md;
        }
        else {
            return MetricReceiver.gI().getMetric(md);
        }
    }
}

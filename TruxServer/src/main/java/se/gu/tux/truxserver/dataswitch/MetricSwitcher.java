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

import se.gu.tux.trux.datastructure.MetricData;
import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.ProtocolMessage;
import se.gu.tux.truxserver.dbconnect.MetricInserter;
import se.gu.tux.truxserver.dbconnect.MetricReceiver;

/**
 *
 * @author Jonas Kahler
 */
public class MetricSwitcher {

    /*
     * Static part.
     */
    private static MetricSwitcher instance = null;

    /**
     * Method returning a MetricSwitcher instance.
     * @return a MetricSwitcher instance.
     */
    protected static MetricSwitcher getInstance() {
        if (instance == null) {
            instance = new MetricSwitcher();
        }
        return instance;
    }

    /**
     * Method returning a MetricSwitcher instance.
     * @return a MetricSwitcher instance.
     */    
    protected static MetricSwitcher gI() {
        return getInstance();
    }

    /*
     * Non-static part.
     */
    
    /**
     * Private Constructor.
     */
    private MetricSwitcher() {
    }

    /**
     * Method to handle MetricData 
     * @param md a MetricData object
     * @return some kind of Data
     */
    protected Data handleMetricData(MetricData md) {
        if (md.getTimeFrame() == 0) {
            MetricInserter.gI().addToDB(md);

            return new ProtocolMessage(ProtocolMessage.Type.DATA_RECEIVED);
        } else {
            return MetricReceiver.gI().getMetric(md);
        }
    }
}
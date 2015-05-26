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
import se.gu.tux.trux.datastructure.Location;
import se.gu.tux.trux.datastructure.ProtocolMessage;
import se.gu.tux.truxserver.dbconnect.LocationReceiver;
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

    protected static MetricSwitcher getInstance() {
        if (ms == null) {
            ms = new MetricSwitcher();
        }
        return ms;
    }

    protected static MetricSwitcher gI() {
        return getInstance();
    }

    /**
     * Non-static part.
     */
    private MetricSwitcher() {
    }

    protected Data handleMetricData(MetricData md) {
        if (md.getTimeFrame() == 0) {
            MetricInserter.gI().addToDB(md);

            return new ProtocolMessage(ProtocolMessage.Type.DATA_RECEIVED);
        } else {
            return MetricReceiver.gI().getMetric(md);
        }
    }
}
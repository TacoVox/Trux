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

import se.gu.tux.trux.datastructure.Message;
import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.Friend;
import se.gu.tux.trux.datastructure.Heartbeat;
import se.gu.tux.trux.datastructure.MetricData;
import se.gu.tux.trux.datastructure.Picture;
import se.gu.tux.trux.datastructure.ProtocolMessage;
import se.gu.tux.trux.datastructure.User;
import se.gu.tux.truxserver.HeartbeatHandler;
import se.gu.tux.truxserver.dbconnect.MetricInserter;

/**
 * Class switching Data into subparts
 * @author Jonas Kahler
 */
public class DataSwitcher {

    /*
     * Static part.
     */
    private static DataSwitcher instance = null;

    /**
     * Method returning a DataSwitcher instance.
     * @return a DataSwitcher instance.
     */
    public static DataSwitcher getInstance() {
        if (instance == null) {
            instance = new DataSwitcher();
        }
        return instance;
    }

    /**
     * Method returning a DataSwitcher instance.
     * @return a DataSwitcher instance.
     */
    public static DataSwitcher gI() {
        return getInstance();
    }

    /*
     * Non-static part.
     */
    private Thread mi = null;

    /**
     * Private Constructor.
     * Start new MetricInserter Thread.
     */
    private DataSwitcher() {
        if (mi == null) {
            mi = new Thread(MetricInserter.gI());
        }
    }

    /**
     * Starts the Insert Thread.
     */
    public void start() {
        mi.start();
    }

    /**
     * Stops the Insert Thread.
     */
    public void stop() {
        mi.interrupt();
    }

    /**
     * Method handeling data.
     * Redirects it to subswitches.
     * @param d Data to handle
     * @return some kind of Data
     */
    public Data handleData(Data d) {
        if (d instanceof MetricData) {
            return MetricSwitcher.gI().handleMetricData((MetricData) d);
        } else if (d instanceof User || d instanceof Friend) {
            return UserSwitcher.gI().handleUser(d);
        } else if (d instanceof ProtocolMessage || d instanceof Message) {
            return MessageSwitcher.gI().handleMessage(d);
        } else if (d instanceof Picture) {
            return PictureSwitcher.gI().handlePicture((Picture) d);
        } else if (d instanceof Heartbeat) {
            return HeartbeatHandler.gI().handleHB((Heartbeat) d);
        } else {
            return null;
        }
    }
}
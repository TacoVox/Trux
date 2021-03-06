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

import se.gu.tux.trux.datastructure.Heartbeat;
import se.gu.tux.trux.datastructure.Notification;
import se.gu.tux.truxserver.dbconnect.FriendshipHandler;
import se.gu.tux.truxserver.dbconnect.MessageHandler;
import se.gu.tux.truxserver.dbconnect.SessionHandler;

/**
 * Class Handeling Heartbeats and sending Notifications.
 * @author Jonas Kahler
 */
public class HeartbeatHandler {

    /*
     * Static part.
     */
    private static HeartbeatHandler instance;

    /**
     * Method returning an instance of the HeartbeatHandler.
     * @return HeartbeatHandler instance
     */
    public static HeartbeatHandler getInstance() {
        if (instance == null) {
            instance = new HeartbeatHandler();
        }

        return instance;
    }

    /**
     * Method returning an instance of the HeartbeatHandler.
     * @return HeartbeatHandler instance
     */
    public static HeartbeatHandler gI() {
        return getInstance();
    }

    /*
     * Non-static part.
     */
    private HeartbeatHandler() {
    }

    /**
     * Method for handeling a Heartbeat
     * @param hb a Heartbeat object
     * @return a Notification if necessary
     */
    public Notification handleHB(Heartbeat hb) {
        SessionHandler.gI().updateActive(hb);

        Notification n = new Notification();

        if (FriendshipHandler.gI().hasNewRequests(hb) || FriendshipHandler.gI().isReviewed(hb)) {
            n.setNewFriends(true);
        } else {
            n.setNewFriends(false);
        }

        n.setNewMessages(MessageHandler.gI().hasNewMessage(hb));

        return n;
    }
}
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
import se.gu.tux.trux.datastructure.ProtocolMessage;
import se.gu.tux.truxserver.dbconnect.FriendshipHandler;
import se.gu.tux.truxserver.dbconnect.MessageHandler;
import se.gu.tux.truxserver.dbconnect.SessionHandler;
import se.gu.tux.truxserver.dbconnect.UserHandler;

/**
 * Class switching Message or ProtocolMessage.
 * @author Jonas Kahler
 */
public class MessageSwitcher {
    /*
     * Static part.
     */
    private static MessageSwitcher instance = null;

    /**
     * Method returning a MessageSwitcher instance.
     * @return a MessageSwitcher instance.
     */
    public static MessageSwitcher getInstance() {
        if (instance == null) {
            instance = new MessageSwitcher();
        }
        return instance;
    }

    /**
     * Method returning a MessageSwitcher instance.
     * @return a MessageSwitcher instance.
     */
    public static MessageSwitcher gI() {
        return getInstance();
    }

    /*
     * Non-static part.
     */
    
    /**
     * Private Constructor.
     */
    private MessageSwitcher() {
    }

    /**
     * Method to handle all kinds of Messages.
     * @param m a ProtocolMessage OR a Message
     * @return some kind of Data
     */
    public Data handleMessage(Data m) {
        if (m instanceof ProtocolMessage) {
            ProtocolMessage pm = (ProtocolMessage) m;
            if (pm.getType() == ProtocolMessage.Type.AUTO_LOGIN_REQUEST) {
                return UserHandler.gI().autoLogin(pm);
            } else if (pm.getType() == ProtocolMessage.Type.LOGOUT_REQUEST) {
                return SessionHandler.gI().endSession(pm);
            } else if (pm.getType() == ProtocolMessage.Type.PEOPLE_SEARCH) {
                return UserHandler.gI().findUsers(pm);
            } else if (pm.getType() == ProtocolMessage.Type.FRIEND_REQUEST) {
                return FriendshipHandler.gI().sendFriendRequest(pm);
            } else if (pm.getType() == ProtocolMessage.Type.FRIEND_REMOVE) {
                return FriendshipHandler.gI().unfriendUser(pm);
            } else if (pm.getType() == ProtocolMessage.Type.CAN_YOU_PLEASE_GIVE_ME_AN_ARRAY_WITH_EVERYONE_WHO_SENT_THIS_USER_A_FRIEND_REQUEST_THANK_YOU_IN_ADVANCE_DEAR_BROTHER) {
                return FriendshipHandler.gI().getFriendRequests(pm);
            } else if (pm.getType() == ProtocolMessage.Type.FRIEND_REQUEST_SEEN) {
                return FriendshipHandler.gI().markAsSeen(pm);
            } else if (pm.getType() == ProtocolMessage.Type.ACCEPT_FRIEND) {
                return FriendshipHandler.gI().acceptFriend(pm);
            } else if (pm.getType() == ProtocolMessage.Type.DECLINE_FRIEND) {
                return FriendshipHandler.gI().declineRequest(pm);
            } else if (pm.getType() == ProtocolMessage.Type.GET_LATEST_CONVERSATIONS) {
                return MessageHandler.gI().getLatestConv(pm);
            } else if (pm.getType() == ProtocolMessage.Type.GET_LATEST_MESSAGES) {
                return MessageHandler.gI().getMessages(pm);
            } else if (pm.getType() == ProtocolMessage.Type.GET_UNREAD_MESSAGES) {
                return MessageHandler.gI().getUnreadMessages(pm);
            } else if (pm.getType() == ProtocolMessage.Type.MESSAGE_SEEN) {
                return MessageHandler.gI().markAsSeen(pm);
            } else if (pm.getType() == ProtocolMessage.Type.GET_ONLINE_FRIENDS) {
                return UserHandler.gI().getOnlineFriends(pm);
            }
        } else if (m instanceof Message) {
            return MessageHandler.gI().newMessage((Message) m);
        }

        return m;
    }
}
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
import se.gu.tux.trux.datastructure.Friend;
import se.gu.tux.trux.datastructure.User;
import se.gu.tux.truxserver.dbconnect.UserHandler;

/**
 * Class switching User or Friend.
 * @author Jonas Kahler
 */
public class UserSwitcher {

    /*
     * Static part.
     */
    private static UserSwitcher instance = null;

    /**
     * Method returning a UserSwitcher instance.
     * @return a UserSwitcher instance.
     */
    protected static UserSwitcher getInstance() {
        if (instance == null) {
            instance = new UserSwitcher();
        }
        return instance;
    }

    /**
     * Method returning a UserSwitcher instance.
     * @return a UserSwitcher instance.
     */
    protected static UserSwitcher gI() {
        return getInstance();
    }

    /*
     * Non-static part.
     */
    private UserSwitcher() {
    }

    /**
     * Method for handling User objects.
     * @param ud a User OR Friend object
     * @return some kind of Data
     */
    protected Data handleUser(Data ud) {
        if (ud instanceof User) {
            User u = (User) ud;

            if (u.getSessionId() == User.LOGIN_REQUEST) {
                return UserHandler.gI().login(u);
            } else if (u.getSessionId() == User.REGISTER_REQUEST) {
                return UserHandler.gI().register(u);
            } else if (u.isRequestProfileChange()) {
                return UserHandler.gI().updateUser((User) ud);
            } else {
                return UserHandler.gI().getUser((User) ud);
            }
        } else if (ud instanceof Friend) {
            return UserHandler.gI().getFriend((Friend) ud);
        } else {
            return null;
        }
    }
}
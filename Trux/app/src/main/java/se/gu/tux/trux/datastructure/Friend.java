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
package se.gu.tux.trux.datastructure;

/**
 *
 * @author jonas
 */
public class Friend extends Data {

    private long friendId;
    private long profilePicId;
    private boolean isFriend;

    private String username;
    private String firstname;
    private String lastname;
    
    private Location currentLoc;

    public Friend(String username) {
        this.username = username;
    }
    
    public Friend(long friendId) {
        this.friendId = friendId;
    }
    
    public Friend(String username, String firstname, String lanstname) {
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Location getCurrentLoc() {
        return currentLoc;
    }

    public void setCurrentLoc(Location currentLoc) {
        this.currentLoc = currentLoc;
    }

    public long getFriendId() {
        return friendId;
    }

    public void setFriendId(long friendId) {
        this.friendId = friendId;
    }

    @Override
    public Object getValue() {
        return friendId;
    }

    @Override
    public void setValue(Object value) {
        setFriendId((Long) value);
    }

    @Override
    public boolean isOnServerSide() {
        return true;
    }

    public long getProfilePicId() {
        return profilePicId;
    }

    public void setProfilePicId(long profilePic) {
        this.profilePicId = profilePic;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean isFriend) {
        this.isFriend = isFriend;
    }
}

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
public class Picture extends Data {
    private long pictureid;
    
    private byte[] imgData;
    
    public Picture(long pictureid) {
        this.pictureid = pictureid;
    }

    public byte[] getImg() {
        return imgData;
    }

    public void setImg(byte[] imgData) {
        this.imgData = imgData;
    }

    public long getPictureid() {
        return pictureid;
    }

    public void setPictureid(long pictureid) {
        this.pictureid = pictureid;
    }
    
    @Override
    public Object getValue() {
        return getImg();
    }

    @Override
    public void setValue(Object value) {
        setImg((byte[]) value);
    }

    @Override
    public boolean isOnServerSide() {
        return true;
    }
}

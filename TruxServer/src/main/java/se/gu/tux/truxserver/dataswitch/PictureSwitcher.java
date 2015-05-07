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
import se.gu.tux.trux.datastructure.Picture;

/**
 *
 * @author jonas
 */
public class PictureSwitcher {
    /**
     * Static part.
     */
    private static PictureSwitcher ps;
    
    public static PictureSwitcher getInstance() {
        if (ps == null)
            ps = new PictureSwitcher();
        
        return ps;
    }
    
    public static PictureSwitcher gI() {
        return getInstance();
    }
    
    /**
     * Non-static part.
    */
    private PictureSwitcher() {}
    
    public Data handlePicture(Picture p) {
        return p;
    }
}

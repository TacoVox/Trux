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

package se.gu.tux.truxserver.file;

/**
 *
 * @author jonas
 */
public class ConfigIO {
    /**
     * Static part.
     */
    private static ConfigIO cio;
    
    public static ConfigIO getInstance() {
        if(cio == null) 
            cio = new ConfigIO();
        
        return cio;
    }
    
    public static ConfigIO gI() {
        return getInstance();
    }
}

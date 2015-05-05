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
public class Location extends MetricData {
    private double[] loc;

    public double[] getLoc() {
        return loc;
    }
    
    public Location() {}
    
    public Location(double lat, double lng) {
        loc = new double[2];
        
        setLoc(lat, lng);
    }

    public void setLoc(double lat, double lng) {
        loc[0] = lat;
        loc[1] = lng;
    }
    
    public void setLatitude(double lat) {
        loc[0] = lat;
    }
    
    public void setLongitude(double lng) {
        loc[1] = lng;
    }
    
    @Override
    public Object getValue() {
        return loc;
    }
    
    @Override
    public void setValue(Object loc) {
        if(loc instanceof double[])
            this.loc = (double[]) loc;
    }
}

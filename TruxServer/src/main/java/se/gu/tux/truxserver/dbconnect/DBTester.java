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
package se.gu.tux.truxserver.dbconnect;

import java.util.Random;
import se.gu.tux.trux.datastructure.Speed;
import se.gu.tux.truxserver.config.ConfigHandler;
import se.gu.tux.truxserver.dataswitch.DataSwitcher;
import se.gu.tux.truxserver.logger.Logger;

/**
 *
 * @author jonas
 */
public class DBTester {

    public static void main(String args[]) {
        ConfigHandler.getInstance().setSettings(args);

        Thread mi = new Thread(MetricInserter.gI());
        mi.start();

        Logger.gI().setVerbose(true);

        Random rand = new Random(1245);

        for (int i = 0; i < 100; i++) {
            Speed s = new Speed(0);
            s.setValue(rand.nextDouble());
            s.setTimeStamp(System.currentTimeMillis());

            DataSwitcher.gI().handleData(s);

            try {
                Thread.sleep(100);
            } catch (Exception e) {
                Logger.gI().addError(e.getMessage());
            }
        }

        mi.interrupt();

        Speed s = new Speed(60000);
        s.setTimeStamp(System.currentTimeMillis());

        //MetricReceiver.gI().getMetric(d);
        s = (Speed) DataSwitcher.gI().handleData(s);

        System.out.println(s.getValue());
    }
}
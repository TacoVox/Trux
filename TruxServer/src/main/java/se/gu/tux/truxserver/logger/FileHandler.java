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
package se.gu.tux.truxserver.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author jonas
 */
public class FileHandler {
    private String execPath;
    private String filePath;
    private File file;
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    
    public FileHandler()
    {
        execPath = System.getProperty("user.dir");
        
        Date date = new Date();
        
        try
        {
            File dir = new File(execPath + "/logs");
            if(!dir.isDirectory())
            dir.mkdir();
            
            filePath = execPath + "/logs/" + dateFormat.format(date).toString() + ".log";
            
            file = new File(filePath);
            
            if(!file.exists())
                file.createNewFile();
            
            appendText("Started logging! Server is running.");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void appendText(String text)
    {
        try
        {
            OutputStreamWriter writer = new OutputStreamWriter(
                  new FileOutputStream(filePath, true), "UTF-8");
            BufferedWriter bw = new BufferedWriter(writer);

            Date date = new Date();
            bw.write(dateFormat.format(date).toString() + " ");
            bw.write(text);
            bw.write("\n");
            
            bw.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}

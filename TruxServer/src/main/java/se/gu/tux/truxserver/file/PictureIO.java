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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;

import se.gu.tux.trux.datastructure.Picture;
import se.gu.tux.trux.datastructure.ProtocolMessage;
import se.gu.tux.truxserver.dbconnect.PictureHandler;
import se.gu.tux.truxserver.logger.Logger;

/**
 *
 * @author jonas
 */
public class PictureIO {
    /**
     * Static part.
     */
    private static PictureIO pio;
    
    public static PictureIO getInstance() {
        if(pio == null)
            pio = new PictureIO();
        
        return pio;
    }
    
    public static PictureIO gI() {
        return getInstance();
    }
    
    /**
     * Non-static part.
     */
    private final DateFormat dateformat = new SimpleDateFormat("yyyy/MM/dd");
    
    private PictureIO() {}
    
    public ProtocolMessage saveProfilePicture(Picture p) {
        BufferedImage img = decodePicture(p.getImg());
        
        String path = storeOnFS(img);
        //Logger.gI().addDebug("The new path :" + path);
        
        p.setPictureid(PictureHandler.gI().savePicturePath(p, path));
        
        //Logger.gI().addDebug("Picture ID: " + p.getPictureid());
        
        return PictureHandler.gI().setProfilePicture(p);
    }
    
    public Picture receiveProfilePicture(Picture p) {
        String path = PictureHandler.gI().getProfilePicturePath(p);
        
        if (path != null)
        {
            BufferedImage img = getFromFS(path);
            
            p.setImg(encodePicture(img));
        }
        p.setTimeStamp(System.currentTimeMillis());
            
        return p;
    }
    
    private String storeOnFS(BufferedImage img) {
        Date date = new Date();
        String imgHash;
        
        if(img.hashCode() < 0)
            imgHash = Integer.toString(-img.hashCode());
        else
            imgHash = Integer.toString(img.hashCode());
        
        
        String path = System.getProperty("user.dir") + "/files/pictures/" +
                dateformat.format(date).toString() + "/" + Long.toString(System.currentTimeMillis()) + ".png";
        
        File pic = new File(path);
        
        pic.mkdirs();
        
        try {
            ImageIO.write(img, "png", pic);
        } catch(IOException e) {
            Logger.gI().addError(e.getLocalizedMessage());
        }
        
        return path;
    }
    
    private BufferedImage getFromFS(String path) {
        try {
            //Logger.gI().addDebug(path);
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            Logger.gI().addError(e.getLocalizedMessage());
        }
        
        return null;
    }

    private BufferedImage decodePicture(byte[] imgData) {
        try {
            InputStream in = new ByteArrayInputStream(imgData);
            return ImageIO.read(in);
        } catch(IOException e) {
            Logger.gI().addError(e.getLocalizedMessage());
        }
        
        return null;
    }
    
    private byte[] encodePicture(BufferedImage img) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos );
            baos.flush();
            byte[] imgData = baos.toByteArray();
            baos.close();
            
            return imgData;
 
        } catch(IOException e) {
            Logger.gI().addError(e.getLocalizedMessage());
        }
        
        return null;
    }	
    
    public static void main(String args[]) {
        BufferedImage a = PictureIO.gI().getFromFS("C:\\Users\\Jonas\\Desktop\\Screen_shot_2010-01-30_at_2.17.06_AM.png");
        
        byte[] b = PictureIO.gI().encodePicture(a);
        
        BufferedImage c = PictureIO.gI().decodePicture(b);
        
        PictureIO.gI().storeOnFS(c);
    }
}

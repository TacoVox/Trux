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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import se.gu.tux.trux.datastructure.Picture;
import se.gu.tux.truxserver.logger.Logger;

/**
 *
 * @author jonas
 */
public class PictureHandler {
    /**
     * Static part.
     */
    private static PictureHandler ph;
    
    public static PictureHandler getInstance() {
        if(ph == null)
            ph = new PictureHandler();
        
        return ph;
    }
    
    public static PictureHandler gI() {
        return getInstance();
    }
    
    /**
     * Non-static part.
     */
    private PictureHandler() {}
    
    public void savePicture(Picture p) {
            
    }
    
    public Picture receivePicture() {
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
            ImageIO.write(img, "jpg", baos );
            baos.flush();
            byte[] imgData = baos.toByteArray();
            baos.close();
            
            return imgData;
 
        } catch(IOException e) {
            Logger.gI().addError(e.getLocalizedMessage());
        }
        
        return null;
    }	
}

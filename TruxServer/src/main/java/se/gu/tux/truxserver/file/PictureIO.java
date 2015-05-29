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

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
 * Class responsible for writing to a .png file on HDD/SSD.
 * @author Jonas Kahler
 */
public class PictureIO {

    /*
     * Static part.
     */
    private static PictureIO instance;

    /**
     * Method returning a PictureIO instance.
     * @return a PictureIO instance.
     */
    public static PictureIO getInstance() {
        if (instance == null) {
            instance = new PictureIO();
        }

        return instance;
    }

    /**
     * Method returning a PictureIO instance.
     * @return a PictureIO instance.
     */
    public static PictureIO gI() {
        return getInstance();
    }

    /*
     * Non-static part.
     */
    private final DateFormat dateformat = new SimpleDateFormat("yyyy/MM/dd");

    /**
     * Private Constructor.
     */
    private PictureIO() {
    }

    /**
     * Method for saving a Picture Object as a profile picture.
     * @param p a Picture Object - See our protocol -
     * @return a ProtocolMessage with success or error message
     */
    public ProtocolMessage saveProfilePicture(Picture p) {
        BufferedImage img = decodePicture(p.getImg());

        String path = storeOnFS(img);

        p.setPictureid(PictureHandler.gI().savePicturePath(p, path));

        return PictureHandler.gI().setProfilePicture(p);
    }

    /**
     * Method to get a profile picture from the server.
     * @param p an empty Picture object to be filled in
     * @return a filled in Picture object
     */
    public Picture receiveProfilePicture(Picture p) {
        String path = PictureHandler.gI().getProfilePicturePath(p);

        if (path != null) {
            BufferedImage img = getFromFS(path);
            p.setImg(encodePicture(img));
        }
        
        p.setTimeStamp(System.currentTimeMillis());
        return p;
    }

    /**
     * Private method to store a BufferedImage on disk.
     * @param img the BI to be stored
     * @return the absolute file path
     */
    private String storeOnFS(BufferedImage img) {
        Date date = new Date();
        String imgHash;

        if (img.hashCode() < 0) {
            imgHash = Integer.toString(-img.hashCode());
        } else {
            imgHash = Integer.toString(img.hashCode());
        }

        String path = System.getProperty("user.dir") + "/files/pictures/"
                + dateformat.format(date).toString() + "/" + Long.toString(System.currentTimeMillis()) + ".png";

        File pic = new File(path);

        pic.mkdirs();

        try {
            ImageIO.write(resizeImage(img), "png", pic);
        } catch (IOException e) {
            Logger.gI().addError(e.getLocalizedMessage());
        }

        return path;
    }

    /**
     * Private method to get an image from disk.
     * @param path the absolute path to the image file
     * @return the image wrapped
     */
    private BufferedImage getFromFS(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            Logger.gI().addError(e.getLocalizedMessage());
        }

        return null;
    }

    /**
     * Private method to decode an image stored in a byte array.
     * @param imgData a byte array storing the image
     * @return BufferedImage containing the data from the image
     */
    private BufferedImage decodePicture(byte[] imgData) {
        try {
            InputStream in = new ByteArrayInputStream(imgData);
            return ImageIO.read(in);
        } catch (IOException e) {
            Logger.gI().addError(e.getLocalizedMessage());
        }

        return null;
    }

    /**
     * Private method to encode an image from BufferedImage to a byte array.
     * @param img the image to be encoded
     * @return the image stored in a byte array
     */
    private byte[] encodePicture(BufferedImage img) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            baos.flush();
            byte[] imgData = baos.toByteArray();
            baos.close();

            return imgData;

        } catch (IOException e) {
            Logger.gI().addError(e.getLocalizedMessage());
        }

        return null;
    }

    /**
     * Method to resize too large images in max 500x500.
     * @param originalImage the image to be resized
     * @return resized image
     */
    private static BufferedImage resizeImage(BufferedImage originalImage) {
        int x = originalImage.getWidth();
        int y = originalImage.getHeight();

        int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();

        if (x >= y) {
            double factor = (double) 500 / x;
            x = 500;
            double yside = y * factor;
            y = new Double(yside).intValue();
        } else {
            double factor = (double) 500 / y;
            y = 500;
            double xside = x * factor;
            x = new Double(xside).intValue();
        }

        BufferedImage resizedImage = new BufferedImage(x, y, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, x, y, null);
        g.dispose();
        g.setComposite(AlphaComposite.Src);

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        return resizedImage;
    }

    /**
     * Test method.
     * @param args command line arguments
     */
    public static void main(String args[]) {
        BufferedImage a = PictureIO.gI().getFromFS("/Users/jonas/Desktop/cc.jpg");

        byte[] b = PictureIO.gI().encodePicture(a);

        BufferedImage c = PictureIO.gI().decodePicture(b);

        PictureIO.gI().storeOnFS(c);
    }
}
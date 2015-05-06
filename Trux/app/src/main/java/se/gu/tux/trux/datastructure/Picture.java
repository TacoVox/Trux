package se.gu.tux.trux.datastructure;

public class Picture extends Data {
    private long pictureid;
    
  //  private BufferedImage img;
    
    public Picture(long pictureid) {
        this.pictureid = pictureid;
    }

 /*   public BufferedImage getImg() {
        return img;
    }
*//*
    public void setImg(BufferedImage img) {
        this.img = img;
    }*/

    public long getPictureid() {
        return pictureid;
    }

    public void setPictureid(long pictureid) {
        this.pictureid = pictureid;
    }

    @Override
    public Object getValue() {
        //return getImg();
        return null;
    }

    @Override
    public void setValue(Object value) {
        //setImg((BufferedImage) value);
    }

    @Override
    public boolean isOnServerSide() {
        return true;
    }
}

package cc.openhome.util;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.awt.Image;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import java.awt.Color;
import java.awt.Graphics;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImageCreator {
    private Rectangle screenRect;
    private Robot robot;
    private float imageQuality; // 0.0f ~ 1.0f

    public ImageCreator() {
        screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        try {
            robot = new Robot();
        } catch (AWTException ex) {
            throw new RuntimeException(ex);
        }
        imageQuality = 0.5f;
    }

    public BufferedImage emptyImage(int width, int height, Color color) {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.getGraphics();
        g.setColor(color);
        g.fillRect(0, 0, width, height);
        return bufferedImage;
    }    

    public Image capture() {
        return robot.createScreenCapture(screenRect);
    }

    public byte[] captureAndToByte() throws IOException {
        BufferedImage bufImage = robot.createScreenCapture(screenRect);

        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(byteArrayStream);

        JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bufImage);
        param.setQuality(imageQuality, false);

        encoder.encode(bufImage);

        return byteArrayStream.toByteArray();
    }

    public void setImageQuality(float imageQuality) {
        this.imageQuality = imageQuality;
    }

    public float getImageQuality() {
        return imageQuality;
    }
}

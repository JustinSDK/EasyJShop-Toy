package cc.openhome.img;

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

public class ScreenCaptureHelper {
	private Rectangle screenRect; 
    private Robot robot;
    private float imageQuality; // 0.0f ~ 1.0f
    
    public ScreenCaptureHelper() throws AWTException {
    	screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
	    robot = new Robot();
	    imageQuality = 0.5f;
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

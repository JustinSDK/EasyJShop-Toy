package cc.openhome.util;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Image;

public class ScreenCapturer {
    private Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
    private Robot robot = createRobot();

    private Robot createRobot() throws RuntimeException {
        try {
            return new Robot();
        } catch (AWTException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Image createScreenCapture() {
        return robot.createScreenCapture(screenRect);
    }    
}

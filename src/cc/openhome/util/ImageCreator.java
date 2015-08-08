package cc.openhome.util;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.Image;

import java.awt.Color;
import java.awt.Graphics;

public class ImageCreator {
    private Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
    private Robot robot = createRobot();

    private Robot createRobot() throws RuntimeException {
        try {
            return new Robot();
        } catch (AWTException ex) {
            throw new RuntimeException(ex);
        }
    }

    public BufferedImage emptyImage(int width, int height, Color color) {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.getGraphics();
        g.setColor(color);
        g.fillRect(0, 0, width, height);
        return bufferedImage;
    }    

    public Image createScreenCapture() {
        return robot.createScreenCapture(screenRect);
    }
}

package cc.openhome.util;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.RenderingHints;

public class ImageProcessor {
    public static Image copyRectImage(Image original, Rectangle2D rect) {
        int x = (int) rect.getX();
        int y = (int) rect.getY();
        int width = (int) rect.getWidth();
        int height = (int) rect.getHeight();
        return mirror(original, 0, 0, width, height, x, y, x + width, y + height);
    }
    
    public static Image resize(Image original, double percentage) {
        int width = (int) (original.getWidth(null) * percentage);
        int height = (int) (original.getHeight(null) * percentage);
        return resize(original, width, height);
    }
    
    public static Image resize(Image original, int width, int height) {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g2 = bufferedImage.createGraphics();
        g2.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(original, 0, 0, width, height, null);
        
        return bufferedImage;
    }
    
    public static Image horizontalMirror(Image original) {
        int width = original.getWidth(null);
        int height = original.getHeight(null);
        return mirror(original, width, 0, 0, height, 0, 0, width, height);
    }
    
    public static Image verticalMirror(Image original) {
        int width = original.getWidth(null);
        int height = original.getHeight(null);
        return mirror(original, 0, height, width, 0, 0, 0, width, height);
    }
    
    public static Image clockwise(Image original) {
        return rotate90(original, true);
    }
    
    public static Image counterClockwise(Image original) {
        return rotate90(original, false);
    }
    
    private static Image mirror(Image original, 
                              int dx1, int dy1, int dx2, int dy2,
                              int sx1, int sy1, int sx2, int sy2) {
        int width = Math.abs(sx1 - sx2);
        int height = Math.abs(sy1 - sy2);
        
        BufferedImage bufferedImage = new BufferedImage(
                width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.getGraphics();
        g.drawImage(original, dx1, dy1, dx2, dy2,
                sx1, sy1, sx2, sy2, null);
        
        return bufferedImage;
    }
    
    private static Image rotate90(Image original, boolean clockwise) {
        int width = original.getWidth(null);
        int height = original.getHeight(null);

        BufferedImage bufferedImage = new BufferedImage(
                height, width, BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g2 = bufferedImage.createGraphics();
        if(clockwise) {
            AffineTransform aff = AffineTransform.getRotateInstance(Math.toRadians(90), 0, 0);
            g2.setTransform(aff);
            g2.drawImage(original, 0, -height , null);
        }
        else {
            AffineTransform aff = AffineTransform.getRotateInstance(Math.toRadians(-90), 0, 0);
            g2.setTransform(aff);
            g2.drawImage(original, -width, 0, null);
        }
        
        return  bufferedImage;
    }
    
    public static Image copyImage(Image image) {
        Rectangle2D rect = new Rectangle2D.Double();
        rect.setRect(0, 0, image.getWidth(null), image.getHeight(null));
        return copyRectImage(image, rect);
    }    
    
    public static BufferedImage toBufferedImage(Image image) {
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null),
                image.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.getGraphics();
        g.drawImage(image, 0, 0, null);
        return bufferedImage;        
    }    
    
    public static BufferedImage emptyImage(int width, int height, Color color) {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.getGraphics();
        g.setColor(color);
        g.fillRect(0, 0, width, height);
        return bufferedImage;
    }        
}

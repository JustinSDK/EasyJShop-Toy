package cc.openhome.img;

import java.awt.Image;
import java.awt.image.ImageObserver;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.RenderingHints;

public class ImageProcessor {
    public Image copyRectImage(Image original, Rectangle2D rect, ImageObserver observer) {
        int x = (int) rect.getX();
        int y = (int) rect.getY();
        
        int width = (int) rect.getWidth();
        int height = (int) rect.getHeight();
        
        return mirror(original, 0, 0, width, height, x, y, x + width, y + height, observer);
    }
    
    public Image resize(Image original, double percentage, ImageObserver observer) {
        int width = (int) (original.getWidth(observer) * percentage);
        int height = (int) (original.getHeight(observer) * percentage);

        return resize(original, width, height, observer);
    }
    
    public Image resize(Image original, int width, int height, ImageObserver observer) {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g2 = bufferedImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(original, 0, 0, width, height, observer);
        
        return bufferedImage;
    }
    
    public Image horizontalMirror(Image original, ImageObserver observer) {
        int width = original.getWidth(observer);
        int height = original.getHeight(observer);
        return mirror(original, width, 0, 0, height, 0, 0, width, height, observer);
    }
    
    public Image verticalMirror(Image original, ImageObserver observer) {
        int width = original.getWidth(observer);
        int height = original.getHeight(observer);
        return mirror(original, 0, height, width, 0, 0, 0, width, height, observer);
    }
    
    public Image clockwise(Image original, ImageObserver observer) {
        return rotate90(original, true, observer);
    }
    
    public Image counterClockwise(Image original, ImageObserver observer) {
        return rotate90(original, false, observer);
    }
    
    private Image mirror(Image original, 
                              int dx1, int dy1, int dx2, int dy2,
                              int sx1, int sy1, int sx2, int sy2,
                              ImageObserver observer) {
        int width = Math.abs(sx1 - sx2);
        int height = Math.abs(sy1 - sy2);
        BufferedImage bufferedImage = new BufferedImage(width, 
                height, BufferedImage.TYPE_INT_RGB);
        
        Graphics g = bufferedImage.getGraphics();
        
        g.drawImage(original, dx1, dy1, dx2, dy2,
                sx1, sy1, sx2, sy2, observer);
        return bufferedImage;
    }
    
    private Image rotate90(Image original, boolean clockwise, ImageObserver observer) {
        int width = original.getWidth(observer);
        int height = original.getHeight(observer);

        BufferedImage bufferedImage = new BufferedImage(height, 
                width, BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g2 = bufferedImage.createGraphics();
        if(clockwise) {
            AffineTransform aff = AffineTransform.getRotateInstance(Math.toRadians(90), 0, 0);
            g2.setTransform(aff);
            g2.drawImage(original, 0, -height , observer);
        }
        else {
            AffineTransform aff = AffineTransform.getRotateInstance(Math.toRadians(-90), 0, 0);
            g2.setTransform(aff);
            g2.drawImage(original, -width, 0, observer);
        }
        
        return  bufferedImage;
    }
    
    public Image copyImage(Image image) {
        Rectangle2D rect = new Rectangle2D.Double();
        rect.setRect(0, 0, image.getWidth(null), image.getHeight(null));
        return copyRectImage(image, rect, null);
    }    
}

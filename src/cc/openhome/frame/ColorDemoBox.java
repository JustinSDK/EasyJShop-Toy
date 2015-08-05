package cc.openhome.frame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JComponent;

public class ColorDemoBox extends JComponent {
    private Color color;
    
    public ColorDemoBox() {
        this(Color.black);
    }
    
    public ColorDemoBox(Color color) {
        this.color = color;
        setSize(13, 13);
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
    
    public Color getColor() {
        return color;
    }
    
    public void paint(Graphics g) {
        g.setColor(color);
        g.fillRect(0, 0, getWidth(), getHeight());
    }
    
    public Dimension getPreferredSize() {
        Dimension dimension = super.getPreferredSize();        
        dimension.setSize(getWidth(), getHeight());
        return dimension;
    }
}

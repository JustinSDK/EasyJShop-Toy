package cc.openhome.frame;

import cc.openhome.img.ImageProcessor;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.JOptionPane;

public class CanvasComponent extends JComponent {
    public final static int SelectionMode = 0;
    public final static int BrushMode = 1;
    public final static int PasteMode = 2;
    public final static int TextMode = 3;
    public final static int ViewMode = 4;
    
    private Image image, pastedImage, fakeImage;
    private Point start, end;
    private Rectangle2D rect;
    private BasicStroke stroke, stroke2, lineStroke; 
    private Line2D line;
    
    private int editMode;
    private double scale;
    private Color foreColor, backColor;
    private int brushWidth;
    
    private String text;
    private Font textFont;
    
    private MainFrame mainFrame;
    
    public CanvasComponent() {
        rect = new Rectangle2D.Double();
        
        stroke = new BasicStroke(1, BasicStroke.CAP_ROUND, 0,
                BasicStroke.JOIN_ROUND, new float[] {5, 5}, 0);
        stroke2 = new BasicStroke(1, BasicStroke.CAP_ROUND, 0,
                BasicStroke.JOIN_ROUND, new float[] {5, 5}, 5);
        
        line = new Line2D.Double();
        
        brushWidth = 10;
        scale = 1.0;
        initEventListener();
    }
    
    public void initEventListener() {
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (mainFrame.getEditMenu().getEditMode() == CanvasComponent.ViewMode) {
                    setCursor(mainFrame.getEditMenu().getViewCursor());
                } else {
                    setCursor(null);
                }
            }

            public void mousePressed(MouseEvent e) {
                if (getEditMode() == CanvasComponent.PasteMode) {
                    if (mainFrame.getEditMenu().mergeImage(CanvasComponent.this) != JOptionPane.NO_OPTION) {
                        mainFrame.getEditMenu().setEditInfo(CanvasComponent.this);
                    }
                    return;
                }

                mainFrame.getEditMenu().setEditInfo(CanvasComponent.this);

                switch (getEditMode()) {
                    case 0: // SelectionMode
                        setStart(e.getPoint());
                        break;
                    case 1: // BrushMode
                        mainFrame.getMementoManager(CanvasComponent.this).addImage(ImageProcessor.copyImage(getImage()));
                        resetRect();
                        setStart(e.getPoint());
                        repaint();
                        mainFrame.setStarBeforeTitle();
                        break;
                    case 3: // TextMode
                        if (getText() != null) {
                            mainFrame.getEditMenu().mergeText(CanvasComponent.this);
                        } else {
                            mainFrame.getEditMenu().inputText(CanvasComponent.this);
                        }
                        break;
                    case 4: // ViewMode
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            increaseViewScale();
                        } else if (e.getButton() == MouseEvent.BUTTON3) {
                            decreaseViewScale();
                        }
                        mainFrame.getSelectedFrame().open();
                        repaint();
                        break;
                    default: // SelectionMode
                        setStart(e.getPoint());
                }
            }

            public void mouseReleased(MouseEvent e) {
                CanvasComponent canvas = (CanvasComponent) e.getSource();
                canvas.setStart(null);
                canvas.setEnd(null);
                mainFrame.updateEditMenuStatus();
            }
        });

        addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) {
                switch (getEditMode()) {
                    case 0: // SelectionMode
                        dragRect(e.getPoint());
                        break;
                    case 1: // BrushMode
                        setEnd(e.getPoint());
                        repaint();
                        break;
                    case 3:
                    case 4:
                        break;
                    default: // SelectionMode
                        dragRect(e.getPoint());
                }
            }

            public void mouseMoved(MouseEvent e) {
                CanvasComponent canvas = (CanvasComponent) e.getSource();
                if (canvas.getEditMode() == CanvasComponent.PasteMode || canvas.getEditMode() == CanvasComponent.TextMode) {
                    canvas.setStart(e.getPoint());
                    canvas.repaint();
                }
            }
        });        
    }
    
    public CanvasComponent(Image image, MainFrame mainFrame) {
        this();
        this.mainFrame = mainFrame;        
        setImage(image);
    }
        
    public void resetRect() {
        rect.setRect(0, 0, 0, 0);
    }
    
    public Dimension getPreferredSize() {
        Dimension dimension = super.getPreferredSize();        
        dimension.setSize(image.getWidth(this)*scale + 1, image.getHeight(this)*scale + 1);
        return dimension;
    }
    
    public Rectangle2D getSelectedRect() {
        return new Rectangle2D.Double(rect.getX()/scale, rect.getY()/scale, rect.getWidth()/scale, rect.getHeight()/scale);
    }
    
    public void setStart(Point start) {
        this.start = start;
    }
    
    public void setEnd(Point end) {
        this.end = end;
    }
    
    public void dragRect(Point point) {
        end = point;
        
        double tmpX = end.getX();
        double tmpY = end.getY();
        double tmpWidth = image.getWidth(this) * scale;
        double tmpHeight = image.getHeight(this) * scale;
        
        if(tmpX < 0)
            tmpX = 0;
        if(tmpX > tmpWidth)
            tmpX = tmpWidth;
        if(tmpY < 0)
            tmpY = 0;
        if(tmpY > tmpHeight)
            tmpY = tmpHeight;
        
        end.setLocation(tmpX, tmpY);
        
        calculateSelectedRect();
        repaint();
    }
 
    protected void paintComponent(Graphics g) {        
        switch(getEditMode()) {
            case 0: // SelectionMode
                drawDashedRect(g);
                break;
            case 1: // BrushMode
                brushImage(g);
                break;
            case 2: // PasteMode
                pasteImage(g);
                break;
            case 3:
                drawText(g);
                break;
            default: // SelectionMode
                drawDashedRect(g);
        }
    }
    
    public void mergePastedImage() {
        if(pastedImage != null) {
            Graphics g = image.getGraphics();
            g.drawImage(pastedImage, (int)(start.getX()/scale), (int)(start.getY()/scale), this);
            repaint();
        }
    }
    
    public void mergeText() {
        if(text != null) {
            Graphics g = image.getGraphics();
            g.setFont(textFont);
            g.setColor(getForeground());
            g.drawString(text, (int)(start.getX()/scale), (int)(start.getY()/scale));
            repaint();
        }
    }
    
    private void pasteImage(Graphics g) {
        Graphics fakeGraphics = fakeImage.getGraphics();
        fakeGraphics.drawImage(image, 0, 0, this);
        
        if(pastedImage != null) {
            if(start != null)
                fakeGraphics.drawImage(pastedImage, (int)(start.getX()/scale), (int)(start.getY()/scale), this);
            else
                g.drawImage(pastedImage, 0, 0, this);
        }
        
        g.drawImage(fakeImage, 0, 0, (int)(image.getWidth(this)*scale), (int)(image.getHeight(this)*scale), this);
    }
    
    private void drawText(Graphics g) {
        Graphics fakeGraphics = fakeImage.getGraphics();
        fakeGraphics.drawImage(image, 0, 0, this);
        
        if(text != null) {
            fakeGraphics.setFont(textFont);
            fakeGraphics.setColor(getForeground());
            if(start != null)
                fakeGraphics.drawString(text, (int)(start.getX()/scale), (int)(start.getY()/scale));
            else
                fakeGraphics.drawString(text, 0, 0);
        }
        g.drawImage(fakeImage, 0, 0, (int)(image.getWidth(this)*scale), (int)(image.getHeight(this)*scale), this);
    }
    
    private void drawDashedRect(Graphics g) {
        g.drawImage(image, 0, 0, 
                (int) (image.getWidth(this) * scale), (int) (image.getHeight(this) * scale), this);
        
        //g = fakeImage.getGraphics();
        //g.drawImage(image, 0, 0, this);
        
        Graphics2D g2 = (Graphics2D) g;
        
        g2.setStroke(stroke);
        g2.setPaint(Color.black);
        g2.draw(rect);
        
        g2.setStroke(stroke2);
        g2.setPaint(Color.white);
        g2.draw(rect);
    }
    
    private void brushImage(Graphics g) {
        int corner = getBrushWidth() / 2;

        Graphics imageGraphics = image.getGraphics();
        imageGraphics.setColor(getForeground());
        
        if(start != null) {
            imageGraphics.fillOval((int)(start.getX()/scale - corner), (int)(start.getY()/scale - corner), getBrushWidth(), getBrushWidth());

            if(end != null) {
                Graphics2D imageGraphics2D = (Graphics2D) imageGraphics;
                imageGraphics2D.setStroke(lineStroke);
                imageGraphics2D.setColor(getForeground());
                line.setLine(start.getX()/scale, start.getY()/scale, end.getX()/scale, end.getY()/scale);
                imageGraphics2D.draw(line);
                start = end;
            }
        }
        
        g.drawImage(image, 0, 0, 
                (int) (image.getWidth(this) * scale), (int) (image.getHeight(this) * scale), this);
    }
    
    private void calculateSelectedRect() {
        double startX = start.getX();
        double startY = start.getY();
        double endX = end.getX();
        double endY = end.getY();
        
        double swidth = endX - startX;
        double sheight = endY - startY;

        if(swidth > 0 && sheight > 0) {
            rect.setRect(startX, startY,
                          swidth, sheight);
        } 
        else if(swidth > 0 && sheight < 0) {
            rect.setRect(startX, endY,
                             swidth, -sheight);
        }
        else if(swidth < 0 && sheight > 0) {
            rect.setRect(endX, startY,
                              -swidth, sheight);
 
        }
        else if(swidth < 0 && sheight < 0) {
            rect.setRect(endX, endY, 
                             -swidth, -sheight);
        }
    }

    public void setImage(Image image) {
        this.image = image;
        fakeImage = new BufferedImage(image.getWidth(this) , image.getHeight(this), BufferedImage.TYPE_INT_RGB);
    }
    
    public Image getImage() {
        return image;
    }
    
    public void setEditMode(int editMode) {
        this.editMode = editMode;
    }
    
    public int getEditMode() {
        return editMode;
    }
    
    public Color getForeground() {
        return foreColor;
    }

    public void setForeground(Color foreColor) {
        this.foreColor = foreColor;
    }
    
    public Color getBackground() {
        return backColor;
    }

    public void setBackground(Color backColor) {
        this.backColor = backColor;
    }

    public int getBrushWidth() {
        return brushWidth;
    }
    public void setBrushWidth(int brushWidth) {
        this.brushWidth = brushWidth;
        lineStroke = new BasicStroke(brushWidth);
    }
    public void setPastedImage(Image pastedImage) {
        this.pastedImage = pastedImage;
        repaint();
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text, Font textFont) {
        this.text = text;
        this.textFont = textFont;
    }
    public double getScale() {
        return scale;
    }
    
    public void increaseViewScale() {
        scale = scale * 2;
        if(scale > 16.0)
            this.scale = 16.0;
    }
    
    public void decreaseViewScale() {
        scale = scale / 2;
        if(scale < 0.0625)
            this.scale = 0.0625;
    }
    
    public void setScale(double scale) {
        if(scale > 16.0)
            this.scale = 16.0;
        else if(scale < 0.0625)
            this.scale = 0.0625;
        else
            this.scale = scale;
    }
}

package cc.openhome.frame;

import cc.openhome.dialog.FontDialog;
import cc.openhome.util.ClipboardHelper;
import cc.openhome.util.ImageMementoManager;
import cc.openhome.util.ImageProcessor;
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
import java.util.function.Function;
import javax.swing.JOptionPane;

public class CanvasComponent extends JComponent {

    private Image image, pastedImage, fakeImage;
    private Point start, end;

    private Rectangle2D rect = new Rectangle2D.Double();
    private BasicStroke stroke = new BasicStroke(1, BasicStroke.CAP_ROUND, 0,
            BasicStroke.JOIN_ROUND, new float[]{5, 5}, 0);
    private BasicStroke stroke2 = new BasicStroke(1, BasicStroke.CAP_ROUND, 0,
            BasicStroke.JOIN_ROUND, new float[]{5, 5}, 5);
    private BasicStroke lineStroke = new BasicStroke(10);
    private Line2D line = new Line2D.Double();
    private double scale = 1.0;

    private String text;
    private Font textFont;

    private MainFrame mainFrame;

    private ImageMementoManager mementoManager = new ImageMementoManager();

    public CanvasComponent(Image image, MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initEventListeners();
        setImage(image);
    }

    public void initEventListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (mainFrame.getEditMode() == MainFrame.ViewMode) {
                    setCursor(mainFrame.getViewCursor());
                } else {
                    setCursor(null);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                switch (mainFrame.getEditMode()) {
                    case MainFrame.BrushMode:
                        doBrushOnMousePressed(e);
                        break;
                    case MainFrame.PasteMode:
                        doMergeImage();
                        break;
                    case MainFrame.TextMode:
                        doTextOnMousePressed();
                        break;
                    case MainFrame.ViewMode:
                        doViewOnMousePressed(e);
                        break;
                    default: // SelectionMode
                        setStart(e.getPoint());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setStart(null);
                setEnd(null);
                mainFrame.updateEditMenuStatus();
            }
        });

        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                switch (mainFrame.getEditMode()) {
                    case MainFrame.BrushMode:
                        setEnd(e.getPoint());
                        repaint();
                        break;
                    case MainFrame.TextMode:
                    case MainFrame.ViewMode:
                        break;
                    default: // SelectionMode
                        dragRect(e.getPoint());
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (mainFrame.getEditMode() == MainFrame.PasteMode || mainFrame.getEditMode() == MainFrame.TextMode) {
                    setStart(e.getPoint());
                    repaint();
                }
            }
        });
    }

    private void doViewOnMousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            increaseViewScale();
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            decreaseViewScale();
        }
        mainFrame.getSelectedFrame().open();
        repaint();
    }

    private void doTextOnMousePressed() {
        if (getText() != null) {
            mergeText();
        } else {
            inputText();
        }
    }

    private void doBrushOnMousePressed(MouseEvent e) {
        setUpUndo(ImageProcessor.copyImage(getImage()));
        resetRect();
        setBrushWidth(mainFrame.getBrushValue());
        setStart(e.getPoint());
        repaint();
        mainFrame.getSelectedFrame().setModifiedTitle();
    }

    public void resetRect() {
        rect.setRect(0, 0, 0, 0);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension dimension = super.getPreferredSize();
        dimension.setSize(image.getWidth(this) * scale + 1, image.getHeight(this) * scale + 1);
        return dimension;
    }

    public Rectangle2D getSelectedRect() {
        return new Rectangle2D.Double(rect.getX() / scale, rect.getY() / scale,
                rect.getWidth() / scale, rect.getHeight() / scale);
    }

    public void setStart(Point start) {
        this.start = start;
    }

    public void setEnd(Point end) {
        this.end = end;
    }

    public void dragRect(Point point) {
        adjustMargin(point);
        setEnd(point);
        calculateSelectedRect();
        repaint();
    }

    private void adjustMargin(Point point) {
        double x = adjustMarginX(point);
        double y = adjustMarginY(point);
        point.setLocation(x, y);
    }

    private double adjustMarginY(Point point) {
        double y = point.getY();
        if (y < 0) {
            y = 0;
        }
        double height = image.getHeight(this) * scale;
        if (y > height) {
            y = height;
        }
        return y;
    }

    private double adjustMarginX(Point point) {
        double x = point.getX();
        if (x < 0) {
            x = 0;
        }
        double width = image.getWidth(this) * scale;
        if (x > width) {
            x = width;
        }
        return x;
    }

    @Override
    protected void paintComponent(Graphics g) {
        switch (mainFrame.getEditMode()) {
            case MainFrame.BrushMode:
                brushImage(g);
                break;
            case MainFrame.PasteMode:
                pasteImage(g);
                break;
            case MainFrame.TextMode:
                drawText(g);
                break;
            default: // SelectionMode
                drawDashedRect(g);
        }
    }

    public void mergePastedImage() {
        if (pastedImage != null) {
            Graphics g = image.getGraphics();
            g.drawImage(pastedImage, (int) (start.getX() / scale), (int) (start.getY() / scale), this);
            repaint();
        }
    }

    private void pasteImage(Graphics g) {
        Graphics fakeGraphics = fakeImage.getGraphics();
        fakeGraphics.drawImage(image, 0, 0, this);
        if (pastedImage != null) {
            if (start != null) {
                fakeGraphics.drawImage(pastedImage, (int) (start.getX() / scale), (int) (start.getY() / scale), this);
            } else {
                g.drawImage(pastedImage, 0, 0, this);
            }
        }
        g.drawImage(fakeImage, 0, 0, (int) (image.getWidth(this) * scale), (int) (image.getHeight(this) * scale), this);
    }

    private void drawText(Graphics g) {
        Graphics fakeGraphics = fakeImage.getGraphics();
        fakeGraphics.drawImage(image, 0, 0, this);
        if (text != null) {
            drawString(fakeGraphics);
        }
        g.drawImage(fakeImage, 0, 0, (int) (image.getWidth(this) * scale), (int) (image.getHeight(this) * scale), this);
    }

    private void drawString(Graphics fakeGraphics) {
        fakeGraphics.setFont(textFont);
        fakeGraphics.setColor(mainFrame.getColorBoxForeground());
        if (start != null) {
            fakeGraphics.drawString(text, (int) (start.getX() / scale), (int) (start.getY() / scale));
        } else {
            fakeGraphics.drawString(text, 0, 0);
        }
    }

    private void drawDashedRect(Graphics g) {
        g.drawImage(image, 0, 0,
                (int) (image.getWidth(this) * scale), (int) (image.getHeight(this) * scale), this);
        blackDashedRect((Graphics2D) g);
        whiteDashedRect((Graphics2D) g);
    }

    private void whiteDashedRect(Graphics2D g2) {
        g2.setStroke(stroke2);
        g2.setPaint(Color.white);
        g2.draw(rect);
    }

    private void blackDashedRect(Graphics2D g2) {
        g2.setStroke(stroke);
        g2.setPaint(Color.black);
        g2.draw(rect);
    }

    private void brushImage(Graphics g) {
        if (start != null) {
            Graphics imageGraphics = image.getGraphics();
            imageGraphics.setColor(mainFrame.getColorBoxForeground());
            lineStart(imageGraphics);
            if (end != null) {
                lineToEnd(imageGraphics);
            }
        }
        g.drawImage(image, 0, 0,
                (int) (image.getWidth(this) * scale), (int) (image.getHeight(this) * scale), this);
    }

    private void lineToEnd(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(lineStroke);
        g2.setColor(mainFrame.getColorBoxForeground());
        line.setLine(start.getX() / scale, start.getY() / scale, end.getX() / scale, end.getY() / scale);
        g2.draw(line);
        start = end;
    }

    private void lineStart(Graphics g) {
        int corner = getBrushWidth() / 2;
        g.fillOval((int) (start.getX() / scale - corner), (int) (start.getY() / scale - corner), getBrushWidth(), getBrushWidth());
    }

    private void calculateSelectedRect() {
        double startX = start.getX();
        double startY = start.getY();
        double endX = end.getX();
        double endY = end.getY();
        double swidth = endX - startX;
        double sheight = endY - startY;

        if (swidth > 0 && sheight > 0) {
            rect.setRect(startX, startY, swidth, sheight);
        } else if (swidth > 0 && sheight < 0) {
            rect.setRect(startX, endY, swidth, -sheight);
        } else if (swidth < 0 && sheight > 0) {
            rect.setRect(endX, startY, -swidth, sheight);
        } else if (swidth < 0 && sheight < 0) {
            rect.setRect(endX, endY, -swidth, -sheight);
        }
    }

    public void setImage(Image image) {
        this.image = image;
        fakeImage = new BufferedImage(image.getWidth(this), image.getHeight(this), BufferedImage.TYPE_INT_RGB);
    }

    public Image getImage() {
        return image;
    }

    public int getBrushWidth() {
        return (int) lineStroke.getLineWidth();
    }

    public void setBrushWidth(int brushWidth) {
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
        if (scale > 16.0) {
            this.scale = 16.0;
        }
    }

    public void decreaseViewScale() {
        scale = scale / 2;
        if (scale < 0.0625) {
            this.scale = 0.0625;
        }
    }

    public void setScale(double scale) {
        if (scale > 16.0) {
            this.scale = 16.0;
        } else if (scale < 0.0625) {
            this.scale = 0.0625;
        } else {
            this.scale = scale;
        }
    }

    private void doMergeImage() {
        if (pastedImage != null) {
            int option = JOptionPane.showOptionDialog(null,
                    "merge images?", "merge?", JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE, mainFrame.smallLogo, null, null);

            switch (option) {
                case JOptionPane.YES_OPTION:
                    setUpUndo(ImageProcessor.copyImage(getImage()));
                    mergePastedImage();
                    mainFrame.getSelectedFrame().setModifiedTitle();
                    mainFrame.updateEditMenuStatus();
                case JOptionPane.CANCEL_OPTION:
                    setPastedImage(null);
                    setStart(null);
                    break;
            }
        }
    }

    private int mergeText() {
        int option = JOptionPane.showOptionDialog(null,
                "merge text into image?", "merge?", JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, mainFrame.smallLogo, null, null);
        switch (option) {
            case JOptionPane.YES_OPTION:
                setUpUndo(ImageProcessor.copyImage(getImage()));
                if (text != null) {
                    drawTextToImage();
                }
                mainFrame.getSelectedFrame().setModifiedTitle();
                mainFrame.updateEditMenuStatus();
            case JOptionPane.CANCEL_OPTION:
                setText(null, null);
                setStart(null);
                break;
        }
        return option;
    }

    private void drawTextToImage() {
        Graphics g = image.getGraphics();
        g.setFont(textFont);
        g.setColor(mainFrame.getColorBoxForeground());
        g.drawString(text, (int) (start.getX() / scale), (int) (start.getY() / scale));
        repaint();
    }

    private void inputText() {
        int option = FontDialog.showDialog(null, "Font information", mainFrame.smallLogo);
        if (option == JOptionPane.OK_OPTION) {
            setText(FontDialog.getInputText(), FontDialog.getFont());
        }
    }

    public boolean isUndable() {
        return mementoManager.isUndoable();
    }

    public boolean isRedoable() {
        return mementoManager.isRedoable();
    }

    public boolean isFirstUndo() {
        return mementoManager.isFirstUndo();
    }

    public Image undoImage() {
        return mementoManager.undoImage();
    }

    public Image redoImage() {
        return mementoManager.redoImage();
    }

    public void setUpUndo(Image image) {
        mementoManager.addImage(image);
    }

    public void cleanSelectedArea() {
        setUpUndo(getImage());
        Graphics g = getImage().getGraphics();
        g.setColor(mainFrame.getColorBoxBackground());
        g.fillRect((int) rect.getX(), (int) rect.getY(),
                (int) rect.getWidth(), (int) rect.getHeight());
    }

    public void paste() {
        setPastedImage(ClipboardHelper.getImageFromClipboard());
    }

    public void crop() {
        Image img = ImageProcessor.copyRectImage(getImage(), getSelectedRect());
        setUpUndo(getImage());
        setImage(img);
        resetRect();
    }

    public void process(Function<Image, Image> func) {
        setUpUndo(getImage());
        setImage(func.apply(getImage()));
    }

    public void preResize() {
        resetRect();
        setUpUndo(getImage());
    }

    public void firstUndo() {
        setUpUndo(getImage());
        undoImage();
    }

    public void undo() {
        setImage(undoImage());
    }

    public void redo() {
        setImage(redoImage());
    }

    public void resizeByScale(int scale) {
        preResize();
        setImage(ImageProcessor.resize(getImage(), scale * 0.01));
    }

    public void resizeByWidthHeight(int width, int height) {
        preResize();
        setImage(ImageProcessor.resize(getImage(), width, height));
    }

    public BufferedImage getImageAsBufferedImage() {
        return ImageProcessor.toBufferedImage(image);
    }
}

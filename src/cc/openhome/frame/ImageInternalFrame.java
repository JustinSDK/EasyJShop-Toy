package cc.openhome.frame;

import cc.openhome.util.ClipboardHelper;
import cc.openhome.util.ImageMementoManager;
import cc.openhome.util.ImageProcessor;
import cc.openhome.menu.SavableFileFilter;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

public class ImageInternalFrame extends JInternalFrame {

    private JFileChooser saveFileChooser;

    private MainFrame mainFrame;
    private CanvasComponent canvas;

    public ImageInternalFrame(MainFrame mainFrame, String title, Image image) {
        super(title, true, true, true, true);

        this.mainFrame = mainFrame;

        initComponents(mainFrame, image);
        initEventListeners();
    }

    private void initComponents(MainFrame mainFrame1, Image image) {
        canvas = new CanvasComponent(image, mainFrame1);
        saveFileChooser = new JFileChooser();
        saveFileChooser.addChoosableFileFilter(new SavableFileFilter());
        setFrameIcon(mainFrame1.getIcon());
        
        JPanel panel = new JPanel();
        canvas.setAlignmentY(Component.CENTER_ALIGNMENT);
        panel.add(canvas);
        JScrollPane scrollPanel = new JScrollPane(panel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        getContentPane().add(scrollPanel);
    }

    private void initEventListeners() {
        setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);

        addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameOpened(InternalFrameEvent e) {
                mainFrame.updateMenuStatus();
                canvas.updateEditInfo(); 
            }

            public void internalFrameClosing(InternalFrameEvent e) {
                saveOrNot();
            }

            public void internalFrameClosed(InternalFrameEvent e) {
                mainFrame.updateMenuStatus();
            }

            public void internalFrameIconified(InternalFrameEvent e) {
                try {
                    setSelected(false);
                    mainFrame.updateMenuStatus();
                } catch (PropertyVetoException ex) {
                    throw new RuntimeException(ex);
                }
            }

            public void internalFrameDeiconified(InternalFrameEvent e) {
                mainFrame.updateMenuStatus();
            }

            public void internalFrameActivated(InternalFrameEvent e) {
                mainFrame.updateMenuStatus();
            }
        });
    }

    private void saveOrNot() throws HeadlessException {
        deIconified();
        if (getTitle().startsWith("*")) {
            int option = JOptionPane.showOptionDialog(null,
                    getTitle().substring(1) + " is unsaved, save?", "save?", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE, mainFrame.smallLogo, null, null);
            if (option == JOptionPane.YES_OPTION) {
                saveImageFile();
            }
        }
        close();
    }

    public void deIconified() {
        try {
            setIcon(false);
            setSelected(true);
        } catch (PropertyVetoException ex) {
            throw new RuntimeException(ex);
        }

    }

    public void close() {
        setVisible(false);
        dispose();
    }

    public void saveImageFile() {
        if (title.endsWith("untitled")) {
            saveImageFileAs();
        } else if (title.startsWith("*")) {
            saveTo(new File(title));
        }
    }

    public void saveImageFileAs() {
        if (saveFileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = getSelectedFile();
            if (file.exists()) {
                confirmOverwrite(file);
            } else {
                saveTo(file);
            }
        }
    }

    private void confirmOverwrite(File file) throws HeadlessException {
        int option = JOptionPane.showOptionDialog(null,
                file.toString() + " exists, overwrite?", "overwrite?", JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, mainFrame.smallLogo, null, null);
        switch (option) {
            case JOptionPane.YES_OPTION: // overwrite
                saveTo(file);
                break;
            case JOptionPane.NO_OPTION:
                saveImageFileAs();
                break;
        }
    }

    private File getSelectedFile() {
        File file = saveFileChooser.getSelectedFile();
        String filename = file.toString();
        String lowerCaseFilename = filename.toLowerCase();
        // the default extension filename is 'jpg'.
        if (!lowerCaseFilename.endsWith(".jpg") && !lowerCaseFilename.endsWith(".png")) {
            filename = filename + ".jpg";
            file = new File(filename);
        }
        return file;
    }

    private void saveTo(File file) {
        saveImage(createImage(), file);
        setTitle(file.toString());
    }

    private BufferedImage createImage() {
        Image image = canvas.getImage();
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null),
                image.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.getGraphics();
        g.drawImage(image, 0, 0, null);
        return bufferedImage;
    }

    private void saveImage(BufferedImage bufferedImage, File file) {
        String filename = file.toString();
        try {
            ImageIO.write(bufferedImage, filename.substring(filename.lastIndexOf('.') + 1), file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void open() {
        try {
            pack();
            setVisible(true);
            setSelected(true);
            setMaximum(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setModifiedTitle() {
        if (!title.startsWith("*")) {
            setTitle("*" + title);
        }
    }

    public void clockwise(ImageExecutor executor) {
        canvas.resetRect();
        process(executor);
        open();
    }

    public void mirror(ImageExecutor executor) {
        process(executor);
        canvas.repaint();
    }

    private void process(ImageExecutor executor) {
        canvas.process(executor);
        setModifiedTitle();
    }

    public void resizeImage(int scale) {
        canvas.resizeByScale(scale);
        setModifiedTitle();
        open();
    }

    public void resizeImage(int width, int height) {
        canvas.resizeByWidthHeight(width, height);
        setModifiedTitle();
        open();
    }

    public int getImageWidth() {
        return canvas.getImage().getWidth(null);
    }

    public int getImageHeight() {
        return canvas.getImage().getHeight(null);
    }

    public boolean isAreaSelected() {
        Rectangle2D rect = canvas.getSelectedRect();
        return rect.getWidth() > 0 && rect.getWidth() > 0;
    }

    public Image copySelectedImage() {
        return ImageProcessor.copyRectImage(canvas.getImage(), canvas.getSelectedRect());
    }

    public void crop() {
        canvas.crop();
        setModifiedTitle();
        open();
    }

    public void cleanSelectedArea() {
        canvas.cleanSelectedArea();
        repaint();
        setModifiedTitle();
    }

    public void paste() {
        canvas.paste();
    }

    public boolean isUndoable() {
        return canvas.isUndable();
    }

    public boolean isRedoable() {
        return canvas.isRedoable();
    }

    public boolean isFirstUndo() {
        return canvas.isFirstUndo();
    }

    public void undo() {
        if (isFirstUndo()) {
            canvas.firstUndo();
        }

        if (isUndoable()) {
            canvas.undo();
            open();
        }
    }

    public void redo() {
        if (canvas.isRedoable()) {
            canvas.redo();
            open();
        }
    }
    
    public void setImageBackground(Color color) {
        canvas.setBackground(color);
    }

    public void setImageForeground(Color color) {
        canvas.setForeground(color);
    }    
}

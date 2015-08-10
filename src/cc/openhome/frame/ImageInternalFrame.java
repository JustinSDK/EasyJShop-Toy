package cc.openhome.frame;

import cc.openhome.menu.SavableFileFilter;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.function.Function;
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

    public MainFrame getMainFrame() {
        return mainFrame;
    }

    private void initComponents(MainFrame mainFrame, Image image) {
        canvas = new CanvasComponent(image, this);
        saveFileChooser = new JFileChooser();
        saveFileChooser.addChoosableFileFilter(new SavableFileFilter());
        setFrameIcon(mainFrame.getIcon());
        
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
            saveImageAsFile();
        } else if (title.startsWith("*")) {
            saveTo(new File(title));
        }
    }

    public void saveImageAsFile() {
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
                saveImageAsFile();
                break;
        }
    }

    private File getSelectedFile() {
        File file = saveFileChooser.getSelectedFile();
        String filename = file.toString();
        String lowerCaseFilename = filename.toLowerCase();
        if (!lowerCaseFilename.endsWith(".jpg") && !lowerCaseFilename.endsWith(".png")) {
            filename = filename + ".jpg";
            file = new File(filename);
        }
        return file;
    }

    private void saveTo(File file) {
        saveImage(canvas.getImageAsBufferedImage(), file); 
        setTitle(file.toString());
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

    public void rotate(Function<Image, Image> func) {
        canvas.resetRect();
        process(func);
        open();
    }

    public void mirror(Function<Image, Image> func) {
        process(func);
        canvas.repaint();
    }

    private void process(Function<Image, Image> func) {
        canvas.process(func);
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
    
    public Dimension getImageDimension() {
        return canvas.getImageDimension();
    }

    public boolean hasSelectedArea() {
        Rectangle2D rect = canvas.getSelectedRect();
        return rect.getWidth() > 0 && rect.getWidth() > 0;
    }

    public Image copySelectedImage() {
        return canvas.copySelectedImage();
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

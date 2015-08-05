package cc.openhome.main;

import cc.openhome.MainFrame;
import cc.openhome.img.ImageMementoManager;
import cc.openhome.menu.SavableFileFilter;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
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

    private MainFrame parent;
    private CanvasComponent canvas;

    public CanvasComponent getCanvas() {
        return canvas;
    }

    public ImageInternalFrame(MainFrame parent, String title, Image image) {
        super(title, true, true, true, true);

        this.parent = parent;
        canvas = new CanvasComponent(image);

        saveFileChooser = new JFileChooser();
        saveFileChooser.addChoosableFileFilter(new SavableFileFilter());

        setFrameIcon(parent.getIcon());

        parent.getMementoManagers().put(canvas, new ImageMementoManager());

        JPanel panel = new JPanel();
        canvas.setAlignmentY(Component.CENTER_ALIGNMENT);
        panel.add(canvas);

        JScrollPane scrollPanel = new JScrollPane(panel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        getContentPane().add(scrollPanel);

        initEventListeners();
    }

    private void initEventListeners() {
        setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);

        addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameOpened(InternalFrameEvent e) {
                // image menu
                parent.getImageMenu().enableSaveMenuItem();

                // edit menu
                parent.getEditMenu().checkEditMenuItem();
                parent.getEditMenu().setEditInfo(getCanvas());
            }

            public void internalFrameClosing(InternalFrameEvent e) {
                saveOrNot();
            }

            private void saveOrNot() throws HeadlessException {
                deIconified();
                if (getTitle().startsWith("*")) {
                    int option = JOptionPane.showOptionDialog(null,
                            getTitle().substring(1) + " is unsaved, save?", "save?", JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE, parent.smallLogo, null, null);
                    if (option == JOptionPane.YES_OPTION) {
                        saveImageFile();
                    }
                }
                close();
            }

            public void internalFrameClosed(InternalFrameEvent e) {
                parent.getImageMenu().checkImageMenuItem();
                parent.getEditMenu().checkEditMenuItem();
            }

            public void internalFrameIconified(InternalFrameEvent e) {
                parent.getImageMenu().checkImageMenuItem();
                parent.getEditMenu().checkEditMenuItem();
            }

            public void internalFrameDeiconified(InternalFrameEvent e) {
                parent.getImageMenu().checkImageMenuItem();
                parent.getEditMenu().checkEditMenuItem();
            }

            public void internalFrameActivated(InternalFrameEvent e) {
                parent.getImageMenu().checkImageMenuItem();
                parent.getEditMenu().checkEditMenuItem();
            }
        });

        canvas.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                CanvasComponent canvas = (CanvasComponent) e.getSource();

                if (parent.getEditMenu().getEditMode() == CanvasComponent.ViewMode) {
                    canvas.setCursor(parent.getEditMenu().getViewCursor());
                } else {
                    canvas.setCursor(null);
                }
            }

            public void mousePressed(MouseEvent e) {
                CanvasComponent canvas = (CanvasComponent) e.getSource();

                if (canvas.getEditMode() == CanvasComponent.PasteMode) {
                    if (parent.getEditMenu().mergeImage(canvas) != JOptionPane.NO_OPTION) {
                        parent.getEditMenu().setEditInfo(canvas);
                    }
                    return;
                }

                parent.getEditMenu().setEditInfo(canvas);

                switch (canvas.getEditMode()) {
                    case 0: // SelectionMode
                        canvas.setStart(e.getPoint());
                        break;
                    case 1: // BrushMode
                        parent.getMementoManager(canvas).addImage(parent.getEditMenu().copyImage(canvas));
                        canvas.resetRect();
                        canvas.setStart(e.getPoint());
                        canvas.repaint();
                        parent.setStarBeforeTitle();
                        break;
                    case 3: // TextMode
                        if (canvas.getText() != null) {
                            parent.getEditMenu().mergeText(canvas);
                        } else {
                            parent.getEditMenu().inputText(canvas);
                        }
                        break;
                    case 4: // ViewMode
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            canvas.increaseViewScale();
                        } else if (e.getButton() == MouseEvent.BUTTON3) {
                            canvas.decreaseViewScale();
                        }
                        showInMainFrame();
                        canvas.repaint();
                        break;
                    default: // SelectionMode
                        canvas.setStart(e.getPoint());
                }
            }

            public void mouseReleased(MouseEvent e) {
                CanvasComponent canvas = (CanvasComponent) e.getSource();
                canvas.setStart(null);
                canvas.setEnd(null);

                parent.getEditMenu().checkEditMenuItem();
            }
        });

        canvas.addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) {
                CanvasComponent canvas = (CanvasComponent) e.getSource();

                switch (canvas.getEditMode()) {
                    case 0: // SelectionMode
                        canvas.dragRect(e.getPoint());
                        break;
                    case 1: // BrushMode
                        canvas.setEnd(e.getPoint());
                        canvas.repaint();
                        break;
                    case 3:
                    case 4:
                        break;
                    default: // SelectionMode
                        canvas.dragRect(e.getPoint());
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

//    public void packOrFitParent() {
//        pack();
//        if (parent.getWidth() < getWidth() || parent.getHeight() < getHeight()) {
//            try {
//                setMaximum(true);
//            } catch (PropertyVetoException ex) {
//                parent.messageBox(ex.getMessage());
//            }
//        }
//    }

    public void deIconified() {
        try {
            setIcon(false);
            setSelected(true);
        } catch (PropertyVetoException ex) {
            parent.messageBox(ex.getMessage());
        }

    }

    public void close() {
        CanvasComponent canvas = getCanvas();
        parent.getMementoManagers().remove(canvas);
        setVisible(false);
        dispose();
    }

    public void saveImageFile() {
        String title = getTitle();

        if (title.endsWith("untitled")) {
            saveImageFileAs();
        } else if (title.startsWith("*")) {
            save(new File(title));
        }
    }

    public void saveImageFileAs() {
        if (saveFileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = getSelectedFile();

            if (file.exists()) {
                confirmOverwrite(file);
            } else {
                save(file);
            }
        }
    }

    private void confirmOverwrite(File file) throws HeadlessException {
        int option = JOptionPane.showOptionDialog(null,
                file.toString() + " exists, overwrite?", "overwrite?", JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, parent.smallLogo, null, null);
        switch (option) {
            case JOptionPane.YES_OPTION: // overwrite
                save(file);
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

    private void save(File file) {
        saveBufferedImageToFile(createBufferedImage(), file);
        setTitle(file.toString());
    }

    private BufferedImage createBufferedImage() {
        Image image = getCanvas().getImage();
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null),
                image.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.getGraphics();
        g.drawImage(image, 0, 0, null);
        return bufferedImage;
    }

    private void saveBufferedImageToFile(BufferedImage bufferedImage, File file) {
        String filename = file.toString();
        try {
            ImageIO.write(bufferedImage, filename.substring(filename.lastIndexOf('.') + 1), file);
        } catch (IOException e) {
            parent.messageBox(e.getMessage());
        }
    }

    public void showInMainFrame() {
        pack();
        try {
            setSelected(true);
            setMaximum(true);
            setVisible(true);
        } catch (Exception e) {
            parent.messageBox(e.getMessage());
        }
    }
}

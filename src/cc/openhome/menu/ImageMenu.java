package cc.openhome.menu;

import cc.openhome.MainFrame;
import java.awt.AWTException;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyVetoException;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.JLabel;

import cc.openhome.img.ScreenCaptureHelper;
import cc.openhome.main.ColorDemoBox;
import cc.openhome.main.ImageInternalFrame;

public class ImageMenu extends EasyJShopMenu {

    private ScreenCaptureHelper captureHelper;

    private JMenuItem captureMenuItem, newImageMenuItem;
    private JMenuItem openMenuItem, saveMenuItem, saveAsMenuItem, saveAllMenuItem;
    private JMenuItem exitMenuItem;

    private JSlider delaySlider;

    private JSpinner widthSpinner, heightSpinner;
    private ColorDemoBox backgroundColorBox;
    private JPanel newImagePanel;

    private JFileChooser openFileChooser;

    //private InternalFrameListener internalFrameListener;
    public ImageMenu(MainFrame easyJShop) {
        super(easyJShop);
        initResource();
        setupUIComponent();
        setupEventListener();
    }

    private void initResource() {

        try {
            captureHelper = new ScreenCaptureHelper();
        } catch (AWTException e) {
            parent.messageBox(e.getMessage());
        }
    }

    private void setupUIComponent() {
        delaySlider = new JSlider(SwingConstants.HORIZONTAL, 0, 20, 0);
        delaySlider.setPaintTrack(true);
        delaySlider.setPaintLabels(true);
        delaySlider.setMajorTickSpacing(5);
        delaySlider.setMajorTickSpacing(1);
        delaySlider.setPaintTicks(true);

        openFileChooser = new JFileChooser();
        openFileChooser.setMultiSelectionEnabled(true);
        openFileChooser.addChoosableFileFilter(new OpenableFileFilter());

        setText("Image");
        captureMenuItem = new JMenuItem("Get screen");
        newImageMenuItem = new JMenuItem("New");
        openMenuItem = new JMenuItem("Open..");
        saveMenuItem = new JMenuItem("Save");
        saveAsMenuItem = new JMenuItem("Save as..");
        saveAllMenuItem = new JMenuItem("Save all");
        exitMenuItem = new JMenuItem("Exit");

        add(captureMenuItem);
        add(newImageMenuItem);
        addSeparator();
        add(openMenuItem);
        add(saveMenuItem);
        add(saveAsMenuItem);
        add(saveAllMenuItem);
        addSeparator();
        add(exitMenuItem);

        saveMenuItem.setEnabled(false);
        saveAsMenuItem.setEnabled(false);
        saveAllMenuItem.setEnabled(false);

        widthSpinner = new JSpinner();
        widthSpinner.setValue(640);
        heightSpinner = new JSpinner();
        heightSpinner.setValue(480);
        backgroundColorBox = new ColorDemoBox(Color.white);
        newImagePanel = new JPanel();
        newImagePanel.add(new JLabel("Width"));
        newImagePanel.add(widthSpinner);
        newImagePanel.add(new JLabel("Height"));
        newImagePanel.add(heightSpinner);
        newImagePanel.add(new JLabel(" Background color"));
        newImagePanel.add(backgroundColorBox);
    }

    private void setupEventListener() {
        captureMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK));
        newImageMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
        openMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        saveMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        exitMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));

        captureMenuItem.addActionListener(e -> {
            captureScreen();
        });

        newImageMenuItem.addActionListener(e -> {
            newImageFile();
        });

        openMenuItem.addActionListener(e -> {
            openImageFile();
        });

        saveMenuItem.addActionListener(e -> {
            getSelectedFrame().saveImageFile();
        });

        saveAsMenuItem.addActionListener(e -> {
            getSelectedFrame().saveImageFileAs();
        });

        saveAllMenuItem.addActionListener(e -> {
            saveAllImageFile();
        });

        exitMenuItem.addActionListener(e -> {
            checkUnsavedImages();
            if (getDesktopPane().getAllFrames().length == 0) {
                System.exit(0);
            }
        });

        widthSpinner.addChangeListener(e -> {
            if (((Integer) widthSpinner.getValue()) <= 0) {
                widthSpinner.setValue(1);
            }
        });

        heightSpinner.addChangeListener(e -> {
            if (((Integer) heightSpinner.getValue()) <= 0) {
                heightSpinner.setValue(1);
            }
        });

        backgroundColorBox.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                Color color = JColorChooser.showDialog(null, "Color information", backgroundColorBox.getColor());
                if (color != null) {
                    backgroundColorBox.setColor(color);
                    backgroundColorBox.repaint();
                }
            }
        });

    }

    public void enableSaveMenuItem() {
        saveMenuItem.setEnabled(true);
        saveAsMenuItem.setEnabled(true);
        saveAllMenuItem.setEnabled(true);
    }

    public void checkImageMenuItem() {
        if (getDesktopPane().getAllFrames().length == 0 || getDesktopPane().getSelectedFrame() == null) {
            saveMenuItem.setEnabled(false);
            saveAsMenuItem.setEnabled(false);
            saveAllMenuItem.setEnabled(false);
        } else {
            saveMenuItem.setEnabled(true);
            saveAsMenuItem.setEnabled(true);
            saveAllMenuItem.setEnabled(true);
        }
    }

    private void captureScreen() {
        parent.setVisible(false);
        int option = JOptionPane.showOptionDialog(null,
                delaySlider, "delay ? (seconds)", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, parent.smallLogo, null, null);

        if (option == JOptionPane.CANCEL_OPTION) {
            parent.setVisible(true);
            return;
        }

        try {
            Thread.sleep(delaySlider.getValue() * 1000);
        } catch (InterruptedException e) {
            parent.messageBox(e.getMessage());
        }

        Image image = captureHelper.capture();

        parent.setVisible(true);

        parent.createInternalFrame("*untitled", image);
    }

    private void newImageFile() {
        int option = JOptionPane.showOptionDialog(null, newImagePanel, "New image",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, parent.smallLogo, null, null);

        if (option == JOptionPane.OK_OPTION) {
            int width = ((Integer) widthSpinner.getValue());
            int height = ((Integer) heightSpinner.getValue());
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics g = bufferedImage.getGraphics();
            g.setColor(backgroundColorBox.getColor());
            g.fillRect(0, 0, width, height);
            parent.createInternalFrame("untitled", bufferedImage);
        }
    }


    private void openImageFile() {
        new Thread(() -> {
            if (openFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                for (File file : openFileChooser.getSelectedFiles()) {
                    try {
                        parent.createInternalFrame(file.getAbsolutePath(), ImageIO.read(file));
                    } catch (Exception e) {
                        parent.messageBox(e.getMessage());
                    }
                }
            }
        }).start();
    }

    private void saveAllImageFile() {
        if (getDesktopPane().getAllFrames().length > 0) {
            new Thread(() -> {
                parent.allInternalFrames(() -> {
                    getSelectedFrame().saveImageFile();
                });
            }).start();
        }
    }

    public void checkUnsavedImages() {
        for (JInternalFrame internalFrame : getDesktopPane().getAllFrames()) {
            ((ImageInternalFrame) internalFrame).deIconified();
            if (internalFrame.getTitle().startsWith("*")) {
                int option = JOptionPane.showOptionDialog(null,
                        internalFrame.getTitle().substring(1) + " is unsaved, save?", "save?", JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE, parent.smallLogo, null, null);
                switch (option) {
                    case JOptionPane.CANCEL_OPTION:
                        return;
                    case JOptionPane.YES_OPTION:
                        getSelectedFrame().saveImageFile();
                }
            }
            ((ImageInternalFrame) internalFrame).close();
        }
    }
}

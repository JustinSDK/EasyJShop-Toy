package cc.openhome.menu;

import cc.openhome.frame.MainFrame;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.JLabel;

import cc.openhome.util.ScreenCapturer;
import cc.openhome.frame.ColorDemoBox;
import cc.openhome.util.ImageProcessor;
import javax.swing.JMenu;

public class ImageMenu extends JMenu {

    private JMenuItem captureMenuItem, newImageMenuItem;
    private JMenuItem openMenuItem, saveMenuItem, saveAsMenuItem, saveAllMenuItem;
    private JMenuItem exitMenuItem;

    private JSlider delaySlider;

    private JSpinner widthSpinner, heightSpinner;
    private ColorDemoBox backgroundColorBox;
    private JPanel newImagePanel;

    private JFileChooser openFileChooser;

    private ScreenCapturer screenCapture = new ScreenCapturer();

    private MainFrame mainFrame;

    public ImageMenu(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setupUIComponent();
        setupEventListener();
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
        captureMenuItem = new JMenuItem("Capture");
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
            createScreenCapture();
        });

        newImageMenuItem.addActionListener(e -> {
            newImage();
        });

        openMenuItem.addActionListener(e -> {
            openImage();
        });

        saveMenuItem.addActionListener(e -> {
            mainFrame.getSelectedFrame().saveImageFile();
        });

        saveAsMenuItem.addActionListener(e -> {
            mainFrame.getSelectedFrame().saveImageFileAs();
        });

        saveAllMenuItem.addActionListener(e -> {
            saveAllImages();
        });

        exitMenuItem.addActionListener(e -> {
            checkUnsavedImages();
            if (mainFrame.noInternalFrame()) {
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

    public void setSavingMenuItemsEnabled(boolean flag) {
        saveMenuItem.setEnabled(flag);
        saveAsMenuItem.setEnabled(flag);
        saveAllMenuItem.setEnabled(flag);
    }

    public void updateSavingMenuItems() {
        if (mainFrame.noInternalFrame() || mainFrame.noSelectedFrame()) {
            setSavingMenuItemsEnabled(false);
        } else {
            setSavingMenuItemsEnabled(true);
        }
    }

    private void createScreenCapture() {
        mainFrame.setVisible(false);
        int option = JOptionPane.showOptionDialog(null,
                delaySlider, "delay ? (seconds)", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, mainFrame.smallLogo, null, null);
        if (option == JOptionPane.OK_OPTION) {
            sleep(delaySlider.getValue() * 1000);
            mainFrame.createInternalFrame("*untitled", screenCapture.createScreenCapture());
        }
        mainFrame.setVisible(true);
    }

    private void sleep(int millis) throws RuntimeException {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void newImage() {
        int option = JOptionPane.showOptionDialog(null, newImagePanel, "New image",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, mainFrame.smallLogo, null, null);
        if (option == JOptionPane.OK_OPTION) {
            int width = ((Integer) widthSpinner.getValue());
            int height = ((Integer) heightSpinner.getValue());
            mainFrame.createInternalFrame("untitled", ImageProcessor.emptyImage(width, height, backgroundColorBox.getColor()));
        }
    }

    private void openImage() {
        new Thread(() -> {
            if (openFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                for (File file : openFileChooser.getSelectedFiles()) {
                    try {
                        mainFrame.createInternalFrame(file.getAbsolutePath(), ImageIO.read(file));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
    }

    private void saveAllImages() {
        mainFrame.forEachInternalFrame(internalFrame -> {
            new Thread(() -> {
                internalFrame.deIconified();
                internalFrame.saveImageFile();
            }).start();
        });
    }

    public void checkUnsavedImages() {
        class Operation {
            boolean notCancelled = true;
        }
        Operation operation = new Operation();

        mainFrame.forEachInternalFrame(internalFrame -> {
            if (operation.notCancelled) {
                if (internalFrame.getTitle().startsWith("*")) {
                    internalFrame.deIconified();
                    int option = JOptionPane.showOptionDialog(null,
                            internalFrame.getTitle().substring(1) + " is unsaved, save?", "save?", JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE, mainFrame.smallLogo, null, null);
                    switch (option) {
                        case JOptionPane.CANCEL_OPTION:
                            operation.notCancelled = false;
                            break;
                        case JOptionPane.YES_OPTION:
                            internalFrame.saveImageFile();
                    }
                }
                internalFrame.close();
            }
        });
    }
}

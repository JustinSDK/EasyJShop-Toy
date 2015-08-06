package cc.openhome.menu;

import cc.openhome.frame.MainFrame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import cc.openhome.dialog.FontDialog;
import cc.openhome.dialog.ResizeDialog;
import cc.openhome.img.ClipboardHelper;
import cc.openhome.img.ImageProcessor;
import cc.openhome.img.TransferableImage;
import cc.openhome.frame.CanvasComponent;
import cc.openhome.frame.ColorDemoBox;
import cc.openhome.frame.ImageExecutor;
import cc.openhome.frame.ImageInternalFrame;
import cc.openhome.frame.InternalFrameExecutor;
import cc.openhome.img.ImageMementoManager;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JDesktopPane;

public class EditMenu extends JMenu {

    private static final int RESIZE = 0;
    private static final int HZ_MIRROR = 1;
    private static final int VT_MIRROR = 2;
    private static final int CLK_ROTATE = 3;
    private static final int CT_CLK_ROTATE = 4;
    private static final int SCALE_RESIZE = 5;
    private static final int WH_RESIZE = 6;

    private TransferableImage transferableImage;

    private ImageIcon selectIcon, brushIcon, textIcon, viewIcon,
            cutIcon, copyIcon, pasteIcon, cropIcon;

    private Cursor viewCursor;

    private JMenuItem undoMenuItem, redoMenuItem;
    private JMenuItem cutMenuItem, cropMenuItem;
    private JMenuItem copyMenuItem;
    private JMenuItem pasteMenuItem, pasteToNewMenuItem;
    private JMenuItem resizeMenuItem, horizontalMirrorMenuItem, verticalMirrorMenuItem;
    private JMenuItem clockwiseMenuItem, counterClockwiseMenuItem;
    private JMenuItem batchMenuItem;

    private JComboBox batchComboBox;

    private JToolBar toolBar;
    private ColorDemoBox foreColorBox, backColorBox;
    private JSpinner brushSpinner;

    private JToggleButton selectBtn, brushBtn, textBtn, viewBtn;
    private JButton cutBtn, copyBtn, pasteBtn, cropBtn;

    private int editMode;
    private boolean resizeLocker;

    private MainFrame mainFrame;

    private Map<Integer, InternalFrameExecutor> executors = new HashMap<>();

    public EditMenu(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initResources();
        setupUIComponent();
        setupEventListener();

        Thread clipboradChecker = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        if (ClipboardHelper.getImageFromClipboard() == null) {
                            pasteMenuItem.setEnabled(false);
                            pasteToNewMenuItem.setEnabled(false);
                            pasteBtn.setEnabled(false);
                        } else {
                            pasteToNewMenuItem.setEnabled(true);

                            if (getDesktopPane() != null && getDesktopPane().getSelectedFrame() != null) {
                                setPasteEnabled(true);
                            }
                        }

                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        clipboradChecker.start();
    }

    private void initResources() {
        transferableImage = new TransferableImage();

        selectIcon = new ImageIcon(EditMenu.class.getResource("../images/select.gif"));
        brushIcon = new ImageIcon(EditMenu.class.getResource("../images/brush.gif"));
        textIcon = new ImageIcon(EditMenu.class.getResource("../images/text.gif"));
        viewIcon = new ImageIcon(EditMenu.class.getResource("../images/view.gif"));
        viewCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                viewIcon.getImage(), new Point(0, 0), "magnifier");
        cutIcon = new ImageIcon(EditMenu.class.getResource("../images/cut.gif"));
        copyIcon = new ImageIcon(EditMenu.class.getResource("../images/copy.gif"));
        pasteIcon = new ImageIcon(EditMenu.class.getResource("../images/paste.gif"));
        cropIcon = new ImageIcon(EditMenu.class.getResource("../images/crop.gif"));

        executors.put(HZ_MIRROR, internalFrame -> {
            internalFrame.mirror(ImageProcessor::horizontalMirror);
        });
        executors.put(VT_MIRROR, internalFrame -> {
            internalFrame.mirror(ImageProcessor::verticalMirror);
        });
        executors.put(CLK_ROTATE, internalFrame -> {
            internalFrame.clockwise(ImageProcessor::clockwise);
        });
        executors.put(CT_CLK_ROTATE, internalFrame -> {
            internalFrame.clockwise(ImageProcessor::counterClockwise);
        });
        executors.put(SCALE_RESIZE, internalFrame -> {
            internalFrame.resizeImage(ResizeDialog.getScalePercentage());
        });
        executors.put(WH_RESIZE, internalFrame -> {
            internalFrame.resizeImage(ResizeDialog.getPixelWidth(), ResizeDialog.getPixelHeight());
        });
    }

    private void setupUIComponent() {
        // set up menuitem
        setText("Edit");

        undoMenuItem = new JMenuItem("Undo");
        redoMenuItem = new JMenuItem("Redo");

        cutMenuItem = new JMenuItem("Cut");
        copyMenuItem = new JMenuItem("Copy");
        pasteMenuItem = new JMenuItem("Into current");

        pasteToNewMenuItem = new JMenuItem("To new");
        cropMenuItem = new JMenuItem("Crop");

        resizeMenuItem = new JMenuItem("Resize");
        horizontalMirrorMenuItem = new JMenuItem("Horizontal mirror");
        verticalMirrorMenuItem = new JMenuItem("Vertical mirror");
        clockwiseMenuItem = new JMenuItem("Rotate clockwise");
        counterClockwiseMenuItem = new JMenuItem("Rotate counter-clockwise");
        batchMenuItem = new JMenuItem("Batch..");

        add(undoMenuItem);
        add(redoMenuItem);
        addSeparator();

        add(cutMenuItem);
        add(copyMenuItem);

        JMenu pasteMenu = new JMenu("Paste");
        pasteMenu.add(pasteMenuItem);
        pasteMenu.add(pasteToNewMenuItem);
        add(pasteMenu);
        addSeparator();

        add(cropMenuItem);
        addSeparator();

        add(resizeMenuItem);
        add(horizontalMirrorMenuItem);
        add(verticalMirrorMenuItem);
        add(clockwiseMenuItem);
        add(counterClockwiseMenuItem);
        addSeparator();
        add(batchMenuItem);

        // batch box
        String[] items = {"Resize",
            "Horizontal mirror", "Vertical mirror",
            "Rotate clockwise", "Rotate counter-clockwise"};
        batchComboBox = new JComboBox(items);

        // set up toolbar
        toolBar = new JToolBar("Edit toolbar");
        toolBar.setLayout(new FlowLayout(FlowLayout.LEFT));

        JPanel colorPanel = new JPanel();
        colorPanel.setLayout(null);
        Dimension panelDim = colorPanel.getPreferredSize();
        panelDim.setSize(20, 20);
        colorPanel.setPreferredSize(panelDim);

        foreColorBox = new ColorDemoBox(Color.black);
        foreColorBox.setToolTipText("Foreground Color");
        foreColorBox.setLocation(0, 0);
        colorPanel.add(foreColorBox);

        backColorBox = new ColorDemoBox(Color.white);
        backColorBox.setToolTipText("Background Color");
        backColorBox.setLocation(7, 7);
        colorPanel.add(backColorBox);

        toolBar.add(colorPanel);

        toolBar.addSeparator();

        brushSpinner = new JSpinner();
        brushSpinner.setValue(new Integer(10));
        brushSpinner.setToolTipText("Brush width");
        toolBar.add(brushSpinner);

        toolBar.addSeparator();

        selectBtn = new JToggleButton(selectIcon);
        selectBtn.setSelected(true);
        selectBtn.setToolTipText("Selection");

        brushBtn = new JToggleButton(brushIcon);
        brushBtn.setToolTipText("Brush");
        textBtn = new JToggleButton(textIcon);
        textBtn.setToolTipText("Text");
        viewBtn = new JToggleButton(viewIcon);
        textBtn.setToolTipText("View");

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(selectBtn);
        buttonGroup.add(brushBtn);
        buttonGroup.add(textBtn);
        buttonGroup.add(viewBtn);

        toolBar.add(selectBtn);
        toolBar.add(brushBtn);
        toolBar.add(textBtn);
        toolBar.add(viewBtn);
        toolBar.addSeparator();

        cutBtn = new JButton(cutIcon);
        cutBtn.setToolTipText("Cut");
        copyBtn = new JButton(copyIcon);
        copyBtn.setToolTipText("Copy");
        pasteBtn = new JButton(pasteIcon);
        pasteBtn.setToolTipText("Paste");
        cropBtn = new JButton(cropIcon);
        cropBtn.setToolTipText("Crop");

        toolBar.add(cutBtn);
        toolBar.add(copyBtn);
        toolBar.add(pasteBtn);
        toolBar.addSeparator();

        toolBar.add(cropBtn);
    }

    private void setupEventListener() {
        // menuitem event listener
        undoMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
        redoMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
        cutMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
        copyMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
        pasteMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
        pasteToNewMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_MASK));
        cropMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_MASK));

        undoMenuItem.addActionListener((ActionEvent e) -> {
            if (getDesktopPane().getSelectedFrame() == null) {
                return;
            }

            if (getMementoManager(getCanvasOfSelectedFrame()).isFirstUndo()) {
                getMementoManager(getCanvasOfSelectedFrame())
                        .addImage(getCanvasOfSelectedFrame().getImage());
                getMementoManager(getCanvasOfSelectedFrame()).undoImage();
            }

            Image image = getMementoManager(getCanvasOfSelectedFrame()).undoImage();

            if (image != null) {
                getCanvasOfSelectedFrame().setImage(image);

                // if the image is full screen size, resize it to fit the frame size.
                getSelectedFrame().open();
            }

            checkEditMenuItemBtn();
        });

        redoMenuItem.addActionListener(e -> {
            if (getDesktopPane().getSelectedFrame() == null) {
                return;
            }

            Image image = getMementoManager(getCanvasOfSelectedFrame()).redoImage();

            if (image != null) {
                getCanvasOfSelectedFrame().setImage(image);

                // if the image is full screen size, resize it to fit the frame size.
                getSelectedFrame().open();
            }

            checkEditMenuItemBtn();
        });

        cutMenuItem.addActionListener(e -> {
            copyToClipboard();
            getSelectedFrame().cut();
            pasteToNewMenuItem.setEnabled(true);
            checkEditMenuItemBtn();
        });

        copyMenuItem.addActionListener(e -> {
            copyToClipboard();
            pasteToNewMenuItem.setEnabled(true);
            checkEditMenuItemBtn();
        });

        pasteMenuItem.addActionListener(e -> {
            getSelectedFrame().paste();
        });

        pasteToNewMenuItem.addActionListener(e -> {
            pasteToNew();
        });

        cropMenuItem.addActionListener(e -> {
            getSelectedFrame().crop();
            checkEditMenuItemBtn();
        });

        resizeMenuItem.addActionListener(e -> {
            resize();
            checkEditMenuItemBtn();
        });

        horizontalMirrorMenuItem.addActionListener(e -> {
            getSelectedFrame().mirror(ImageProcessor::horizontalMirror);
            checkEditMenuItemBtn();
        });

        verticalMirrorMenuItem.addActionListener(e -> {
            getSelectedFrame().mirror(ImageProcessor::verticalMirror);
            checkEditMenuItemBtn();
        });

        clockwiseMenuItem.addActionListener(e -> {
            getSelectedFrame().clockwise(ImageProcessor::clockwise);
            checkEditMenuItemBtn();
        });

        counterClockwiseMenuItem.addActionListener(e -> {
            getSelectedFrame().clockwise(ImageProcessor::counterClockwise);
            checkEditMenuItemBtn();
        });

        batchMenuItem.addActionListener(e -> {
            batch();
            checkEditMenuItemBtn();
        });

        // tool bar
        foreColorBox.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                Color color = JColorChooser.showDialog(null, "Color information", foreColorBox.getColor());
                if (color != null) {
                    foreColorBox.setColor(color);
                    foreColorBox.repaint();
                    JInternalFrame internalFrame = getDesktopPane().getSelectedFrame();

                    if (internalFrame != null) {
                        getCanvasOfSelectedFrame().setForeground(color);
                    }
                }
            }
        });

        backColorBox.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                Color color = JColorChooser.showDialog(null, "Color information", backColorBox.getColor());
                if (color != null) {
                    backColorBox.setColor(color);
                    backColorBox.repaint();
                    JInternalFrame internalFrame = getDesktopPane().getSelectedFrame();

                    if (internalFrame != null) {
                        getCanvasOfSelectedFrame().setBackground(color);
                    }
                }
            }
        });

        selectBtn.addActionListener(e -> {
            editMode = CanvasComponent.SelectionMode;
        });

        brushBtn.addActionListener(e -> {
            editMode = CanvasComponent.BrushMode;
        });

        textBtn.addActionListener(e -> {
            editMode = CanvasComponent.TextMode;
        });

        viewBtn.addActionListener(e -> {
            editMode = CanvasComponent.ViewMode;
        });

        brushSpinner.addChangeListener(e -> {
            if (((Integer) brushSpinner.getValue()) <= 0) {
                brushSpinner.setValue(1);
            }
        });

        cutBtn.addActionListener(e -> {
            copyToClipboard();
            getSelectedFrame().cut();
            pasteToNewMenuItem.setEnabled(true);
            checkEditMenuItemBtn();
        });

        copyBtn.addActionListener(e -> {
            copyToClipboard();
            pasteToNewMenuItem.setEnabled(true);
            checkEditMenuItemBtn();
        });

        pasteBtn.addActionListener(e -> {
            getSelectedFrame().paste();
        });

        cropBtn.addActionListener(e -> {
            getSelectedFrame().crop();
            checkEditMenuItemBtn();
        });

    }

    public JToolBar getToolBar() {
        return toolBar;
    }

    public int getEditMode() {
        return editMode;
    }

    public Cursor getViewCursor() {
        return viewCursor;
    }

    public void checkEditMenuItemBtn() {
        if (mainFrame.noSelectedFrame()) {
            undoMenuItem.setEnabled(false);
            redoMenuItem.setEnabled(false);
            setResizeMirrorRotateBatchEnabled(false);
            setCutCopyCropEnabled(false);
            setPasteEnabled(false);
        } else {
            setResizeMirrorRotateBatchEnabled(true);
            
            setCutCopyCropEnabled(getSelectedFrame().isAreaSelected());
            setPasteEnabled(ClipboardHelper.getImageFromClipboard() != null);
            undoMenuItem.setEnabled(getSelectedFrame().isUndoable());
            redoMenuItem.setEnabled(getSelectedFrame().isUndoable());
        }
    }

    private void setResizeMirrorRotateBatchEnabled(boolean flag) {
        resizeMenuItem.setEnabled(flag);
        horizontalMirrorMenuItem.setEnabled(flag);
        verticalMirrorMenuItem.setEnabled(flag);
        clockwiseMenuItem.setEnabled(flag);
        counterClockwiseMenuItem.setEnabled(flag);
        batchMenuItem.setEnabled(flag);
    }

    private void setCutCopyCropEnabled(boolean flag) {
        cutMenuItem.setEnabled(flag);
        copyMenuItem.setEnabled(flag);
        cropMenuItem.setEnabled(flag);
        
        cutBtn.setEnabled(flag);
        copyBtn.setEnabled(flag);
        cropBtn.setEnabled(flag);
    }

    private void setPasteEnabled(boolean flag) {
        pasteMenuItem.setEnabled(flag);
        pasteBtn.setEnabled(flag);
    }

    private void copyToClipboard() {
        Image image = getSelectedFrame().copySelectedImage();
        transferableImage.setImage(image);
        ClipboardHelper.imageToClipboard(transferableImage);
    }

    private void pasteToNew() {
        mainFrame.createInternalFrame("*untitled", ClipboardHelper.getImageFromClipboard());
    }

    private void resize() {
        int option = ResizeDialog.showDialog(null, "Resize Information",
                getSelectedFrame().getImageWidth(), getSelectedFrame().getImageHeight(), mainFrame.smallLogo);

        if (option == JOptionPane.OK_OPTION) {
            if (ResizeDialog.isPercentage()) {
                getSelectedFrame().resizeImage(ResizeDialog.getScalePercentage());
            } else {
                getSelectedFrame().resizeImage(ResizeDialog.getPixelWidth(), ResizeDialog.getPixelHeight());
            }
        }
    }

    private void batch() {
        int option = JOptionPane.showOptionDialog(null,
                batchComboBox, "batch..", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, mainFrame.smallLogo, null, null);

        if (option == JOptionPane.OK_OPTION) {
            int selected = batchComboBox.getSelectedIndex();
            if (selected == RESIZE) {
                batchResize();
            } else {
                mainFrame.forEachInternalFrame(executors.get(selected));
            }
        }

    }

    private void batchResize() {
        int option = ResizeDialog.showDialog(null, "Resize Information",
                getSelectedFrame().getImageWidth(), getSelectedFrame().getImageHeight(), mainFrame.smallLogo);
        if (option == JOptionPane.OK_OPTION) {
            mainFrame.forEachInternalFrame(executors.get(ResizeDialog.isPercentage() ? SCALE_RESIZE : WH_RESIZE));
        }
    }

    public void setEditInfo(CanvasComponent canvas) {
        canvas.setEditMode(editMode);
        canvas.setForeground(foreColorBox.getColor());
        canvas.setBackground(backColorBox.getColor());
        canvas.setBrushWidth(((Integer) brushSpinner.getValue()));
    }

    protected ImageInternalFrame getSelectedFrame() {
        return (ImageInternalFrame) getDesktopPane().getSelectedFrame();
    }

    protected CanvasComponent getCanvasOfSelectedFrame() {
        return mainFrame.getCanvasOfSelectedFrame();
    }

    protected JDesktopPane getDesktopPane() {
        return mainFrame.getDesktopPane();
    }

    protected ImageMementoManager getMementoManager(CanvasComponent canvas) {
        return mainFrame.getMementoManager(canvas);
    }
}

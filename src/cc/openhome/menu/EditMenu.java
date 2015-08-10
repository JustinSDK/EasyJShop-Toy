package cc.openhome.menu;

import cc.openhome.frame.MainFrame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import cc.openhome.dialog.ResizeDialog;
import cc.openhome.util.ClipboardHelper;
import cc.openhome.util.ImageProcessor;
import cc.openhome.util.TransferableImage;
import cc.openhome.frame.ColorDemoBox;
import cc.openhome.frame.ImageInternalFrame;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

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

    private MainFrame mainFrame;

    private Map<Integer, Consumer<ImageInternalFrame>> consumers = new HashMap<>();

    public EditMenu(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initResources();
        setupUIComponent();
        setupEventListener();

        new Thread(() -> {
            while (true) {
                if (ClipboardHelper.getImageFromClipboard() == null) {
                    pasteMenuItem.setEnabled(false);
                    pasteToNewMenuItem.setEnabled(false);
                    pasteBtn.setEnabled(false);
                } else {
                    pasteToNewMenuItem.setEnabled(true);
                    if (!mainFrame.noSelectedFrame()) {
                        setPasteEnabled(true);
                    }
                }
                sleep(1000);
            }
        }).start();
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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

        consumers.put(HZ_MIRROR, internalFrame -> {
            internalFrame.mirror(ImageProcessor::horizontalMirror);
        });
        consumers.put(VT_MIRROR, internalFrame -> {
            internalFrame.mirror(ImageProcessor::verticalMirror);
        });
        consumers.put(CLK_ROTATE, internalFrame -> {
            internalFrame.rotate(ImageProcessor::clockwise);
        });
        consumers.put(CT_CLK_ROTATE, internalFrame -> {
            internalFrame.rotate(ImageProcessor::counterClockwise);
        });
        consumers.put(SCALE_RESIZE, internalFrame -> {
            internalFrame.resizeImage(ResizeDialog.getScalePercentage());
        });
        consumers.put(WH_RESIZE, internalFrame -> {
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
        brushSpinner.setValue(10);
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

        undoMenuItem.addActionListener(e -> {
            mainFrame.undoSelectedFrame();
            updateEditMenuItemBtn();
        });

        redoMenuItem.addActionListener(e -> {
            mainFrame.redoSelectedFrame();
            updateEditMenuItemBtn();
        });

        cutMenuItem.addActionListener(e -> {
            copyToClipboard();
            mainFrame.cleanSelectedAreaOfSelectedFrame();
            pasteToNewMenuItem.setEnabled(true);
            updateEditMenuItemBtn();
        });

        copyMenuItem.addActionListener(e -> {
            copyToClipboard();
            pasteToNewMenuItem.setEnabled(true);
            updateEditMenuItemBtn();
        });

        pasteMenuItem.addActionListener(e -> {
            mainFrame.pasteToSelectedFrame();
        });

        pasteToNewMenuItem.addActionListener(e -> {
            pasteToNew();
        });

        cropMenuItem.addActionListener(e -> {
            mainFrame.cropSelectedFrame();
            updateEditMenuItemBtn();
        });

        resizeMenuItem.addActionListener(e -> {
            resize();
            updateEditMenuItemBtn();
        });

        horizontalMirrorMenuItem.addActionListener(e -> {
            mainFrame.mirrorSelectedFrame(ImageProcessor::horizontalMirror);
            updateEditMenuItemBtn();
        });

        verticalMirrorMenuItem.addActionListener(e -> {
            mainFrame.mirrorSelectedFrame(ImageProcessor::verticalMirror);
            updateEditMenuItemBtn();
        });

        clockwiseMenuItem.addActionListener(e -> {
            mainFrame.rotateSelectedFrame(ImageProcessor::clockwise);
            updateEditMenuItemBtn();
        });

        counterClockwiseMenuItem.addActionListener(e -> {
            mainFrame.rotateSelectedFrame(ImageProcessor::counterClockwise);
            updateEditMenuItemBtn();
        });

        batchMenuItem.addActionListener(e -> {
            batch();
            updateEditMenuItemBtn();
        });

        // tool bar
        foreColorBox.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                Color color = JColorChooser.showDialog(null, "Color information", foreColorBox.getColor());
                if (color != null) {
                    foreColorBox.setColor(color);
                    foreColorBox.repaint();
                    if(!mainFrame.noSelectedFrame()) {
                        mainFrame.setImageForegroundOfSelected(color);
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
                    if(!mainFrame.noSelectedFrame()) {
                        mainFrame.setImageBackgroundOfSelected(color);
                    }
                }
            }
        });

        selectBtn.addActionListener(e -> {
            editMode = MainFrame.SelectionMode;
        });

        brushBtn.addActionListener(e -> {
            editMode = MainFrame.BrushMode;
        });

        textBtn.addActionListener(e -> {
            editMode = MainFrame.TextMode;
        });

        viewBtn.addActionListener(e -> {
            editMode = MainFrame.ViewMode;
        });

        brushSpinner.addChangeListener(e -> {
            if (((Integer) brushSpinner.getValue()) <= 0) {
                brushSpinner.setValue(1);
            }
        });

        cutBtn.addActionListener(e -> {
            copyToClipboard();
            mainFrame.cleanSelectedAreaOfSelectedFrame();
            pasteToNewMenuItem.setEnabled(true);
            updateEditMenuItemBtn();
        });

        copyBtn.addActionListener(e -> {
            copyToClipboard();
            pasteToNewMenuItem.setEnabled(true);
            updateEditMenuItemBtn();
        });

        pasteBtn.addActionListener(e -> {
            editMode = MainFrame.PasteMode;
            mainFrame.pasteToSelectedFrame();
        });

        cropBtn.addActionListener(e -> {
            mainFrame.cropSelectedFrame();
            updateEditMenuItemBtn();
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

    public void updateEditMenuItemBtn() {
        if (mainFrame.noSelectedFrame()) {
            undoMenuItem.setEnabled(false);
            redoMenuItem.setEnabled(false);
            setResizeMirrorRotateBatchEnabled(false);
            setCutCopyCropEnabled(false);
            setPasteEnabled(false);
        } else {
            setResizeMirrorRotateBatchEnabled(true);
            setCutCopyCropEnabled(mainFrame.hasSelectedAreaInSelectedFrame()); 
            setPasteEnabled(ClipboardHelper.getImageFromClipboard() != null);
            undoMenuItem.setEnabled(mainFrame.isSelectedFrameUndoable());
            redoMenuItem.setEnabled(mainFrame.isSelectedFrameRedoable());
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
        Image image = mainFrame.copySelectedAreaOfSelectedFrame(); 
        transferableImage.setImage(image);
        ClipboardHelper.imageToClipboard(transferableImage);
    }

    private void pasteToNew() {
        mainFrame.createInternalFrame("*untitled", ClipboardHelper.getImageFromClipboard());
    }

    private void resize() {
        Dimension dimension = mainFrame.getDimensionOfSelectedFrame();
        int option = ResizeDialog.showDialog(null, "Resize Information",
                (int) dimension.getWidth(), (int) dimension.getHeight(), mainFrame.smallLogo);

        if (option == JOptionPane.OK_OPTION) {
            if (ResizeDialog.isPercentage()) {
                mainFrame.resizeSelectedFrameByPercent(); 
            } else {
                mainFrame.resizeSelectedFrameByWidthHeight();
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
                mainFrame.forEachInternalFrame(consumers.get(selected));
            }
        }

    }

    private void batchResize() {
        Dimension dimension = mainFrame.getDimensionOfSelectedFrame();
        int option = ResizeDialog.showDialog(null, "Resize Information",
                (int) dimension.getWidth(), (int) dimension.getHeight(), mainFrame.smallLogo);
        if (option == JOptionPane.OK_OPTION) {
            mainFrame.forEachInternalFrame(consumers.get(ResizeDialog.isPercentage() ? SCALE_RESIZE : WH_RESIZE));
        }
    }

    public int getBrushValue() {
        return (Integer) brushSpinner.getValue();
    }
    
    public Color getForegroundBoxColor() {
        return foreColorBox.getColor();
    }
    
    public Color getBackgroundBoxColor() {
        return backColorBox.getColor();
    }
}

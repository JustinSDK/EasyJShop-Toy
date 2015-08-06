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

    private interface ImageExecutor {

        Image execute(Image image);
    }

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
                                pasteMenuItem.setEnabled(true);
                                pasteBtn.setEnabled(true);
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
            mirror(internalFrame, ImageProcessor::horizontalMirror);
        });
        executors.put(VT_MIRROR, internalFrame -> {
            mirror(internalFrame, ImageProcessor::verticalMirror);
        });
        executors.put(CLK_ROTATE, internalFrame -> {
            clockwise(internalFrame, ImageProcessor::clockwise);
        });
        executors.put(CT_CLK_ROTATE, internalFrame -> {
            clockwise(internalFrame, ImageProcessor::counterClockwise);
        });
        executors.put(SCALE_RESIZE, internalFrame -> {
            resizeImage(ResizeDialog.getScalePercentage());
        });
        executors.put(WH_RESIZE, internalFrame -> {
            resizeImage(ResizeDialog.getPixelWidth(), ResizeDialog.getPixelHeight());
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

        undoMenuItem.setEnabled(false);
        redoMenuItem.setEnabled(false);
        cutMenuItem.setEnabled(false);
        copyMenuItem.setEnabled(false);
        pasteMenuItem.setEnabled(false);
        cropMenuItem.setEnabled(false);
        resizeMenuItem.setEnabled(false);
        horizontalMirrorMenuItem.setEnabled(false);
        verticalMirrorMenuItem.setEnabled(false);
        clockwiseMenuItem.setEnabled(false);
        counterClockwiseMenuItem.setEnabled(false);
        batchMenuItem.setEnabled(false);

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

        cutBtn.setEnabled(false);
        copyBtn.setEnabled(false);
        pasteBtn.setEnabled(false);
        cropBtn.setEnabled(false);
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

        undoMenuItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
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

                        checkEditMenuItem();
                    }
                }
        );

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

            checkEditMenuItem();
        });

        cutMenuItem.addActionListener(e -> {
            copyToClipBoard(true);
            pasteToNewMenuItem.setEnabled(true);
            checkEditMenuItem();
        });

        copyMenuItem.addActionListener(e -> {
            copyToClipBoard(false);
            pasteToNewMenuItem.setEnabled(true);
            checkEditMenuItem();
        });

        pasteMenuItem.addActionListener(e -> {
            paste();
        });

        pasteToNewMenuItem.addActionListener(e -> {
            pasteToNew();
        });

        cropMenuItem.addActionListener(e -> {
            crop();
            checkEditMenuItem();
        });

        resizeMenuItem.addActionListener(e -> {
            resize();
            checkEditMenuItem();
        });

        horizontalMirrorMenuItem.addActionListener(e -> {
            mirror(getSelectedFrame(), ImageProcessor::horizontalMirror);
            checkEditMenuItem();
        });

        verticalMirrorMenuItem.addActionListener(e -> {
            mirror(getSelectedFrame(), ImageProcessor::verticalMirror);
            checkEditMenuItem();
        });

        clockwiseMenuItem.addActionListener(e -> {
            clockwise(getSelectedFrame(), ImageProcessor::clockwise);
            checkEditMenuItem();
        });

        counterClockwiseMenuItem.addActionListener(e -> {
            clockwise(getSelectedFrame(), ImageProcessor::counterClockwise);
            checkEditMenuItem();
        });

        batchMenuItem.addActionListener(e -> {
            batch();
            checkEditMenuItem();
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
            copyToClipBoard(true);
            pasteToNewMenuItem.setEnabled(true);
            checkEditMenuItem();
        });

        copyBtn.addActionListener(e -> {
            copyToClipBoard(false);
            pasteToNewMenuItem.setEnabled(true);
            checkEditMenuItem();
        });

        pasteBtn.addActionListener(e -> {
            paste();
        });

        cropBtn.addActionListener(e -> {
            crop();
            checkEditMenuItem();
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

    public void checkEditMenuItem() {
        // check all
        if (getDesktopPane().getSelectedFrame() == null) {
            undoMenuItem.setEnabled(false);
            redoMenuItem.setEnabled(false);
            cutMenuItem.setEnabled(false);
            copyMenuItem.setEnabled(false);
            pasteMenuItem.setEnabled(false);
            cropMenuItem.setEnabled(false);
            resizeMenuItem.setEnabled(false);
            horizontalMirrorMenuItem.setEnabled(false);
            verticalMirrorMenuItem.setEnabled(false);
            clockwiseMenuItem.setEnabled(false);
            counterClockwiseMenuItem.setEnabled(false);
            batchMenuItem.setEnabled(false);

            cutBtn.setEnabled(false);
            copyBtn.setEnabled(false);
            pasteBtn.setEnabled(false);
            cropBtn.setEnabled(false);

            return;
        } else {
            resizeMenuItem.setEnabled(true);
            horizontalMirrorMenuItem.setEnabled(true);
            verticalMirrorMenuItem.setEnabled(true);
            clockwiseMenuItem.setEnabled(true);
            counterClockwiseMenuItem.setEnabled(true);
            batchMenuItem.setEnabled(true);
        }

        CanvasComponent canvas = getCanvasOfSelectedFrame();

        // check cut, copy and paste menuitem
        if (canvas.getSelectedRect().getWidth() <= 0) {
            cutMenuItem.setEnabled(false);
            copyMenuItem.setEnabled(false);
            cropMenuItem.setEnabled(false);

            cutBtn.setEnabled(false);
            copyBtn.setEnabled(false);
            cropBtn.setEnabled(false);
        } else {
            cutMenuItem.setEnabled(true);
            copyMenuItem.setEnabled(true);
            cropMenuItem.setEnabled(true);

            cutBtn.setEnabled(true);
            copyBtn.setEnabled(true);
            cropBtn.setEnabled(true);
        }

        // check paste menuitem
        if (ClipboardHelper.getImageFromClipboard() != null) {
            pasteMenuItem.setEnabled(true);
            pasteBtn.setEnabled(true);
        } else {
            pasteMenuItem.setEnabled(false);
            pasteBtn.setEnabled(false);
        }

        // check undo menuitem
        if (getMementoManager(canvas).isUndoable()) {
            undoMenuItem.setEnabled(true);
        } else {
            undoMenuItem.setEnabled(false);
        }

        // check redo menuitem
        if (getMementoManager(canvas).isRedoable()) {
            redoMenuItem.setEnabled(true);
        } else {
            redoMenuItem.setEnabled(false);
        }
    }

    private Image copySelectedImage() {
        CanvasComponent canvas = getCanvasOfSelectedFrame();
        Rectangle2D rect = canvas.getSelectedRect();

        if (rect.getWidth() <= 0 || rect.getWidth() <= 0) {
            JOptionPane.showMessageDialog(null, "No area selected.",
                    "Info.", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        return ImageProcessor.copyRectImage(canvas.getImage(), rect);
    }

    private void copyToClipBoard(boolean cut) {
        Image image = copySelectedImage();

        if (image == null) {
            return;
        }

        transferableImage.setImage(image);

        ClipboardHelper.imageToClipboard(transferableImage);

        if (cut) {
            CanvasComponent canvas = getCanvasOfSelectedFrame();

            image = ImageProcessor.copyImage(canvas.getImage());

            // set up undo
            getMementoManager(canvas).addImage(image);

            Graphics g = canvas.getImage().getGraphics();
            Rectangle2D rect = getCanvasOfSelectedFrame().getSelectedRect();
            g.setColor(getCanvasOfSelectedFrame().getBackground());
            g.fillRect((int) rect.getX(), (int) rect.getY(),
                    (int) rect.getWidth(), (int) rect.getHeight());
            getCanvasOfSelectedFrame().repaint();
            setStarBeforeTitle();
        }
    }

    private void pasteToNew() {
        Image image = ClipboardHelper.getImageFromClipboard();

        if (image == null) {
            return;
        }

        // new a internalFrame for the copied image
        mainFrame.createInternalFrame("*untitled", image);
    }

    private void crop() {
        Image image = copySelectedImage();

        if (image == null) {
            return;
        }

        CanvasComponent canvas = getCanvasOfSelectedFrame();

        // set up undo
        getMementoManager(canvas).addImage(canvas.getImage());

        // use current internalFrame for the corped image
        canvas.setImage(image);

        // let the dashed rect disappear
        canvas.resetRect();

        getSelectedFrame().open();

        setStarBeforeTitle();
    }

    private void paste() {
        CanvasComponent canvas = getCanvasOfSelectedFrame();

        if (canvas == null) {
            return;
        }

        Image image = ClipboardHelper.getImageFromClipboard();

        if (image != null) {
            canvas.setEditMode(CanvasComponent.PasteMode);
            canvas.setPastedImage(image);
        }
    }

    public int mergeImage(CanvasComponent canvas) {
        int option = JOptionPane.showOptionDialog(null,
                "merge images?", "merge?", JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, mainFrame.smallLogo, null, null);

        switch (option) {
            case JOptionPane.YES_OPTION:
                getMementoManager(canvas).addImage(ImageProcessor.copyImage(canvas.getImage()));
                canvas.mergePastedImage();
                setStarBeforeTitle();
                checkEditMenuItem();
                break;
            case JOptionPane.NO_OPTION:
                break;
            case JOptionPane.CANCEL_OPTION:
                canvas.setPastedImage(null);
                canvas.setStart(null);
                break;
        }

        return option;
    }

    public int mergeText(CanvasComponent canvas) {
        int option = JOptionPane.showOptionDialog(null,
                "merge text into image?", "merge?", JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, mainFrame.smallLogo, null, null);

        switch (option) {
            case JOptionPane.YES_OPTION:
                getMementoManager(canvas).addImage(ImageProcessor.copyImage(canvas.getImage()));
                canvas.mergeText();
                setStarBeforeTitle();
                checkEditMenuItem();
            //break;
            case JOptionPane.CANCEL_OPTION:
                canvas.setText(null, null);
                canvas.setStart(null);
                break;
            case JOptionPane.NO_OPTION:
                break;
        }

        return option;
    }

    public void inputText(CanvasComponent canvas) {
        int option = FontDialog.showDialog(null, "Font information", mainFrame.smallLogo);

        if (option == JOptionPane.OK_OPTION) {
            canvas.setText(FontDialog.getInputText(), FontDialog.getFont());
        }
    }

    private void resize() {
        Image image = getCanvasOfSelectedFrame().getImage();

        int option = ResizeDialog.showDialog(null, "Resize Information", image.getWidth(null), image.getHeight(null), mainFrame.smallLogo);

        if (option == JOptionPane.OK_OPTION) {
            try {
                if (ResizeDialog.isPercentage()) {
                    int scale = ResizeDialog.getScalePercentage();
                    resizeImage(scale);
                } else if (ResizeDialog.isCustomWidthHeight()) {
                    int width = ResizeDialog.getPixelWidth();
                    int height = ResizeDialog.getPixelHeight();
                    resizeImage(width, height);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Image preResize() {
        CanvasComponent canvas = getCanvasOfSelectedFrame();
        canvas.resetRect();

        // set up undo
        getMementoManager(canvas).addImage(canvas.getImage());

        return canvas.getImage();
    }

    private void resizeImage(int scale) {
        Image image = preResize();

        image = ImageProcessor.resize(image, scale * 0.01);

        postResize(image);
    }

    private void resizeImage(int width, int height) {
        Image image = preResize();

        image = ImageProcessor.resize(image, width, height);

        postResize(image);
    }

    private void postResize(Image image) {
        CanvasComponent canvas = getCanvasOfSelectedFrame();

        canvas.setImage(image);

        getSelectedFrame().open();

        setStarBeforeTitle();
    }

    private void mirror(ImageInternalFrame internalFrame, ImageExecutor executor) {
        CanvasComponent canvas = internalFrame.getCanvas();

        // set up undo
        mainFrame.getMementoManager(canvas).addImage(canvas.getImage());

        Image image = canvas.getImage();
        image = executor.execute(image);

        canvas.setImage(image);
        canvas.repaint();

        setStarBeforeTitle();
    }

    private void clockwise(ImageInternalFrame internalFrame, ImageExecutor executor) {
        CanvasComponent canvas = internalFrame.getCanvas();
        canvas.resetRect();

        // set up undo
        mainFrame.getMementoManager(canvas).addImage(canvas.getImage());

        Image image = canvas.getImage();
        image = executor.execute(image);

        canvas.setImage(image);

        internalFrame.open();

        setStarBeforeTitle();
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
        Image image = getCanvasOfSelectedFrame().getImage();
        int option = ResizeDialog.showDialog(null, "Resize Information", image.getWidth(null), image.getHeight(null), mainFrame.smallLogo);
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

    protected void setStarBeforeTitle() {
        mainFrame.setStarBeforeTitle();
    }

    protected ImageMementoManager getMementoManager(CanvasComponent canvas) {
        return mainFrame.getMementoManager(canvas);
    }
}

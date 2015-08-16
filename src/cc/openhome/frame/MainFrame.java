package cc.openhome.frame;

import cc.openhome.dialog.ResizeDialog;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import cc.openhome.menu.AboutMenu;
import cc.openhome.menu.EditMenu;
import cc.openhome.menu.ImageMenu;
import cc.openhome.util.ImageProcessor;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class MainFrame extends JFrame {

    public final static int SelectionMode = 0;
    public final static int BrushMode = 1;
    public final static int PasteMode = 2;
    public final static int TextMode = 3;
    public final static int ViewMode = 4;

    public static final int RESIZE = 0;
    public static final int HZ_MIRROR = 1;
    public static final int VT_MIRROR = 2;
    public static final int CLK_ROTATE = 3;
    public static final int CT_CLK_ROTATE = 4;
    public static final int SCALE_RESIZE = 5;
    public static final int WH_RESIZE = 6;
    public static final int SAVE_ALL = 7;

    private JDesktopPane desktopPane;

    private ImageMenu imageMenu = new ImageMenu(this);
    private EditMenu editMenu = new EditMenu(this);

    private ImageIcon icon = new ImageIcon(MainFrame.class.getResource("../images/appIcon.gif"));
    public ImageIcon smallLogo = new ImageIcon(MainFrame.class.getResource("../images/smallLogo.gif"));

    private Map<Integer, Consumer<ImageInternalFrame>> consumers = new HashMap<>();

    {
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
        consumers.put(SAVE_ALL, internalFrame -> {
            new Thread(() -> {
                internalFrame.deIconified();
                internalFrame.saveImageFile();
            }).start();
        });
    }

    public ImageMenu getImageMenu() {
        return imageMenu;
    }

    public EditMenu getEditMenu() {
        return editMenu;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public MainFrame() {
        super("EasyJShop");
        setUpUIComponent();
        setUpEventListener();
    }

    private void setUpUIComponent() {
        setIconImage(icon.getImage());
        setSize(640, 480);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        desktopPane = new JDesktopPane();
        getContentPane().add(desktopPane);

        JMenuBar bar = new JMenuBar();
        bar.add(imageMenu);
        bar.add(editMenu);
        getContentPane().add(editMenu.getToolBar(), BorderLayout.NORTH);
        bar.add(new AboutMenu());
        setJMenuBar(bar);

        updateMenuStatus();
    }

    private void setUpEventListener() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                checkUnsavedImages();
                if (noInternalFrame()) {
                    System.exit(0);
                }
            }
        });
    }

    public void forEachInternalFrame(int action) {
        for (JInternalFrame internalFrame : desktopPane.getAllFrames()) {
            consumers.get(action).accept((ImageInternalFrame) internalFrame);
        }
    }

    public void checkUnsavedImages() {
        for (JInternalFrame internalFrame : desktopPane.getAllFrames()) {
            if (internalFrame.getTitle().startsWith("*")) {
                ((ImageInternalFrame) internalFrame).deIconified();
                int option = JOptionPane.showOptionDialog(null,
                        internalFrame.getTitle().substring(1) + " is unsaved, save?", "save?", JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE, smallLogo, null, null);
                switch (option) {
                    case JOptionPane.CANCEL_OPTION:
                        return;
                    case JOptionPane.YES_OPTION:
                        ((ImageInternalFrame) internalFrame).saveImageFile();
                }
            }
            ((ImageInternalFrame) internalFrame).close();
        }
    }

    public void createInternalFrame(String title, Image image) {
        ImageInternalFrame internalFrame = new ImageInternalFrame(this, title, image);
        desktopPane.add(internalFrame);
        internalFrame.open();
    }

    public void updateMenuStatus() {
        getImageMenu().updateSavingMenuItems();
        getEditMenu().updateEditMenuItemBtn();
    }

    public void updateEditMenuStatus() {
        getEditMenu().updateEditMenuItemBtn();
    }

    public boolean noInternalFrame() {
        return desktopPane.getAllFrames().length == 0;
    }

    public boolean noSelectedFrame() {
        return desktopPane.getSelectedFrame() == null;
    }

    private ImageInternalFrame getSelectedFrame() {
        return (ImageInternalFrame) desktopPane.getSelectedFrame();
    }

    public Color getColorBoxForeground() {
        return getEditMenu().getForegroundBoxColor();
    }

    public Color getColorBoxBackground() {
        return getEditMenu().getBackgroundBoxColor();
    }

    public int getBrushValue() {
        return getEditMenu().getBrushValue();
    }

    public int getEditMode() {
        return getEditMenu().getEditMode();
    }

    public Cursor getViewCursor() {
        return getEditMenu().getViewCursor();
    }

    public void undoSelectedFrame() {
        getSelectedFrame().undo();
    }

    public void redoSelectedFrame() {
        getSelectedFrame().redo();
    }

    public void cleanSelectedAreaOfSelectedFrame() {
        getSelectedFrame().cleanSelectedArea();
    }

    public void pasteToSelectedFrame() {
        getSelectedFrame().paste();
    }

    public void cropSelectedFrame() {
        getSelectedFrame().crop();
    }

    public void mirrorSelectedFrame(Function<Image, Image> func) {
        getSelectedFrame().mirror(func);
    }

    public void rotateSelectedFrame(Function<Image, Image> func) {
        getSelectedFrame().rotate(ImageProcessor::clockwise);
    }

    public void setImageForegroundOfSelected(Color color) {
        getSelectedFrame().setImageForeground(color);
    }

    public void setImageBackgroundOfSelected(Color color) {
        getSelectedFrame().setImageBackground(color);
    }

    public Image copySelectedAreaOfSelectedFrame() {
        return getSelectedFrame().copySelectedImage();
    }

    public void resizeSelectedFrameByPercent() {
        getSelectedFrame().resizeImage(ResizeDialog.getScalePercentage());
    }

    public void resizeSelectedFrameByWidthHeight() {
        getSelectedFrame().resizeImage(ResizeDialog.getPixelWidth(), ResizeDialog.getPixelHeight());
    }

    public Dimension getDimensionOfSelectedFrame() {
        return getSelectedFrame().getImageDimension();
    }

    public boolean isSelectedFrameUndoable() {
        return getSelectedFrame().isUndoable();
    }

    public boolean isSelectedFrameRedoable() {
        return getSelectedFrame().isRedoable();
    }

    public boolean hasSelectedAreaInSelectedFrame() {
        return getSelectedFrame().hasSelectedArea();
    }

    public void saveImageOfSelectedFrame() {
        getSelectedFrame().saveImageFile();
    }

    public void saveImageAsFileOfSelectedFrame() {
        getSelectedFrame().saveImageAsFile();
    }
}

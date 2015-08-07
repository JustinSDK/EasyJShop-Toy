package cc.openhome.frame;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import cc.openhome.menu.AboutMenu;
import cc.openhome.menu.EditMenu;
import cc.openhome.menu.ImageMenu;

public class MainFrame extends JFrame {

    private JDesktopPane desktopPane;

    private ImageMenu imageMenu = new ImageMenu(this);
    private EditMenu editMenu = new EditMenu(this);

    private ImageIcon icon = new ImageIcon(MainFrame.class.getResource("../images/appIcon.gif"));
    public ImageIcon smallLogo = new ImageIcon(MainFrame.class.getResource("../images/smallLogo.gif"));

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
                imageMenu.checkUnsavedImages();
                if (noInternalFrame()) {
                    System.exit(0);
                }
            }
        });
    }

    public void forEachInternalFrame(InternalFrameExecutor executor) {
        for (JInternalFrame internalFrame : desktopPane.getAllFrames()) {
            executor.execute((ImageInternalFrame) internalFrame);
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
    
    public ImageInternalFrame getSelectedFrame() {
        return (ImageInternalFrame) desktopPane.getSelectedFrame();
    } 
    
    public Color getColorBoxForeground() {
        return getEditMenu().getForeground();
    }
    
    public Color getColorBoxBackground() {
        return getEditMenu().getBackground();
    }
    
    public int getBrushValue() {
        return getEditMenu().getBrushValue();
    }
    
    public int getEditMode() {
        return getEditMenu().getEditMode();
    }
}

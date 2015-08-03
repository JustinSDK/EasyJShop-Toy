package cc.openhome;

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import cc.openhome.img.ImageMementoManager;
import cc.openhome.main.EasyJShopMenu;
import cc.openhome.main.CanvasComponent;
import cc.openhome.main.ImageInternalFrame;
import cc.openhome.menu.AboutMenu;
import cc.openhome.menu.EditMenu;
import cc.openhome.menu.ImageMenu;
import java.beans.PropertyVetoException;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

public class EasyJShop extends JFrame {
    private JDesktopPane desktopPane;
    
    private Map mementoManagers = new HashMap();
    
    private ImageMenu imageMenu = new ImageMenu(this);
    private EditMenu editMenu = new EditMenu(this);
    
    private ImageIcon icon = new ImageIcon(EasyJShop.class.getResource("images/appIcon.gif"));

    public ImageMenu getImageMenu() {
        return imageMenu;
    }

    public EditMenu getEditMenu() {
        return editMenu;
    }

    public ImageIcon getIcon() {
        return icon;
    }
    
    public EasyJShop() {
        super("EasyJShop");
        setUpUIComponent();
        setUpEventListener();
    }
    
    private void setUpUIComponent() {
        setIconImage(icon.getImage());
        
        setSize(640, 480);
        
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        JMenuBar bar = new JMenuBar();
        bar.add(imageMenu.getMenu());
        bar.add(editMenu.getMenu());
        getContentPane().add(editMenu.getToolBar(), BorderLayout.NORTH);
        bar.add(new AboutMenu(this).getMenu());
        
        setJMenuBar(bar);
        
        desktopPane = new JDesktopPane();
        getContentPane().add(desktopPane);
    }
    
    private void setUpEventListener() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                imageMenu.checkUnsavedImages();
            }
        });
    }
    
    public JDesktopPane getDesktopPane() {
        return desktopPane;
    }
    
    public CanvasComponent getCanvasOfInternalFrame(JInternalFrame internalFrame) {
        JScrollPane scrollPanel = (JScrollPane) internalFrame.getContentPane().getComponent(0);
        return (CanvasComponent) ((JPanel) scrollPanel.getViewport().getComponent(0)).getComponent(0);
    }
    
    public void setStarBeforeTitle() {
        String title = desktopPane.getSelectedFrame().getTitle();
        if (!title.startsWith("*")) {
            desktopPane.getSelectedFrame().setTitle("*" + title);
        }
    }
    
    public ImageMementoManager getMementoManager(CanvasComponent canvas) {
        return (ImageMementoManager) mementoManagers.get(canvas);
    }
    
    public Map getMementoManagers() {
        return mementoManagers;
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(),
                    "Info.", JOptionPane.INFORMATION_MESSAGE);
        }
        
        new EasyJShop().setVisible(true);
    }
    
    
}

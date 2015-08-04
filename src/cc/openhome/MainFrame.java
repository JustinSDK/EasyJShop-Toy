package cc.openhome;

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import cc.openhome.img.ImageMementoManager;
import cc.openhome.main.CanvasComponent;
import cc.openhome.main.IBatcher;
import cc.openhome.main.ImageInternalFrame;
import cc.openhome.menu.AboutMenu;
import cc.openhome.menu.EditMenu;
import cc.openhome.menu.ImageMenu;
import java.beans.PropertyVetoException;

public class MainFrame extends JFrame {
    private JDesktopPane desktopPane;
    
    private Map mementoManagers = new HashMap();
    
    private ImageMenu imageMenu = new ImageMenu(this);
    private EditMenu editMenu = new EditMenu(this);
    
    private ImageIcon icon = new ImageIcon(MainFrame.class.getResource("images/appIcon.gif"));

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
        
        JMenuBar bar = new JMenuBar();
        bar.add(imageMenu);
        bar.add(editMenu);
        getContentPane().add(editMenu.getToolBar(), BorderLayout.NORTH);
        bar.add(new AboutMenu(this));
        
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
    
    public CanvasComponent getCanvasOfSelectedFrame() {
        return ((ImageInternalFrame) getDesktopPane().getSelectedFrame()).getCanvas();
    }   
    
    public void batch(IBatcher batcher) {
        for (JInternalFrame internalFrame : getDesktopPane().getAllFrames()) {
            try {
                internalFrame.setIcon(true);
                internalFrame.pack();
                batcher.execute();
            } catch (PropertyVetoException e) {
                messageBox(e.getMessage());
            }
        }
    }    
    
    public void messageBox(String message) {
        JOptionPane.showMessageDialog(null, message,
                "Info.", JOptionPane.INFORMATION_MESSAGE);
    }
}
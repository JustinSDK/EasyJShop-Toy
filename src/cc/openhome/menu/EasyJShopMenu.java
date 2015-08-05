package cc.openhome.menu;

import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JMenu;

import cc.openhome.frame.MainFrame;
import cc.openhome.img.ImageMementoManager;
import cc.openhome.frame.CanvasComponent;
import cc.openhome.frame.ImageInternalFrame;

public abstract class EasyJShopMenu extends JMenu {
    protected MainFrame mainFrame;

    public EasyJShopMenu(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
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

    protected Map getMementoManagers() {
        return mainFrame.getMementoManagers();
    }
}

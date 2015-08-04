package cc.openhome.menu;

import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JMenu;

import cc.openhome.MainFrame;
import cc.openhome.img.ImageMementoManager;
import cc.openhome.main.CanvasComponent;
import cc.openhome.main.ImageInternalFrame;

public abstract class EasyJShopMenu extends JMenu {
    protected MainFrame parent;

    public EasyJShopMenu(MainFrame parent) {
        this.parent = parent;
    }

     protected ImageInternalFrame getSelectedFrame() {
        return (ImageInternalFrame) getDesktopPane().getSelectedFrame();
    }
        
    protected CanvasComponent getCanvasOfSelectedFrame() {
        return parent.getCanvasOfSelectedFrame();
    }
    
    protected JDesktopPane getDesktopPane() {
        return parent.getDesktopPane();
    }

    protected void setStarBeforeTitle() {
        parent.setStarBeforeTitle();
    }

    protected ImageMementoManager getMementoManager(CanvasComponent canvas) {
        return parent.getMementoManager(canvas);
    }

    protected Map getMementoManagers() {
        return parent.getMementoManagers();
    }
}

package cc.openhome.main;

import java.beans.PropertyVetoException;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JOptionPane;

import cc.openhome.EasyJShop;
import cc.openhome.img.ImageMementoManager;
import cc.openhome.menu.EditMenu;

public abstract class EasyJShopMenu {
    protected EasyJShop parent;
    protected ImageIcon smallLogo = new ImageIcon(EditMenu.class.getResource("../images/smallLogo.gif"));

    public abstract JMenu getMenu();

    public EasyJShopMenu(EasyJShop parent) {
        this.parent = parent;
    }

    protected void batch(IBatcher batcher) {
        for (JInternalFrame internalFrame : getDesktopPane().getAllFrames()) {
            try {
                internalFrame.setIcon(true);
                internalFrame.pack();
                batcher.execute();
            } catch (PropertyVetoException e) {
                infoMessageBox(e.getMessage());
            }
        }
    }

    public void infoMessageBox(String message) {
        JOptionPane.showMessageDialog(null, message,
                "Info.", JOptionPane.INFORMATION_MESSAGE);
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

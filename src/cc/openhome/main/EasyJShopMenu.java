package cc.openhome.main;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.beans.PropertyVetoException;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

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
        JInternalFrame[] internalFrames = getDesktopPane().getAllFrames();
        
        for(int i = 0; i < internalFrames.length; i++) {
            try {
                internalFrames[i].setIcon(true);
                internalFrames[i].pack();
                batcher.execute();
            }
            catch(PropertyVetoException e) {
                infoMessageBox(e.getMessage());
            }
       }
    }
    
    public void infoMessageBox(String message) {
        JOptionPane.showMessageDialog(null, message,
                "Info.", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void fitAppSize(Image image) {
        CanvasComponent canvas = getCanvasOfSelectedFrame();
        double scale = canvas.getScale();
        canvas.setSize((int) (image.getWidth(canvas) * scale), (int) (image.getHeight(canvas) * scale));
        JInternalFrame internalFrame = getDesktopPane().getSelectedFrame();
        internalFrame.pack();
        
        // if the frame is larger than app size, resize it to fit the app size.
        
        if(internalFrame.getWidth() >= Toolkit.getDefaultToolkit().getScreenSize().getWidth()-20 && 
                internalFrame.getHeight() >= Toolkit.getDefaultToolkit().getScreenSize().getHeight()-120) {
            Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
            getDesktopPane().getSelectedFrame().setSize((int) size.getWidth() - 20, (int) size.getHeight() - 120);
        }
        else if(internalFrame.getWidth() >= Toolkit.getDefaultToolkit().getScreenSize().getWidth() - 20) {
            Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
            getDesktopPane().getSelectedFrame().setSize((int) size.getWidth() - 20, internalFrame.getHeight());
        }
        else if(internalFrame.getHeight() >= Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 120) {
            Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
            getDesktopPane().getSelectedFrame().setSize(internalFrame.getWidth(), (int) size.getHeight() - 120);
        }
    }
    
    protected JDesktopPane getDesktopPane() {
        return parent.getDesktopPane();
    }
    
    public CanvasComponent getCanvasOfSelectedFrame() {
        if(getDesktopPane() != null)
            return parent.getCanvasOfInternalFrame(getDesktopPane().getSelectedFrame());
        else
            return null;
    }
    
    protected CanvasComponent getCanvasOfInternalFrame(JInternalFrame internalFrame) {
        return parent.getCanvasOfInternalFrame(internalFrame);
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

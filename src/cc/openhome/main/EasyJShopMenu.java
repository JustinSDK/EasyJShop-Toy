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

        for (int i = 0; i < internalFrames.length; i++) {
            try {
                internalFrames[i].setIcon(true);
                internalFrames[i].pack();
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

    public void fitAppSize(Image image) {
        CanvasComponent canvas = getCanvasOfSelectedFrame();
        double scale = canvas.getScale();
        canvas.setSize((int) (image.getWidth(canvas) * scale), (int) (image.getHeight(canvas) * scale));
        JInternalFrame internalFrame = getDesktopPane().getSelectedFrame();
        internalFrame.pack();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        // if the frame is larger than app size, resize it to fit the app size.
        if (internalFrame.getWidth() >= screenSize.getWidth() - 20 && internalFrame.getHeight() >= screenSize.getHeight() - 120) {
            getDesktopPane().getSelectedFrame().setSize((int) screenSize.getWidth() - 20, (int) screenSize.getHeight() - 120);
        } else if (internalFrame.getWidth() >= screenSize.getWidth() - 20) {
            getDesktopPane().getSelectedFrame().setSize((int) screenSize.getWidth() - 20, internalFrame.getHeight());
        } else if (internalFrame.getHeight() >= screenSize.getHeight() - 120) {
            getDesktopPane().getSelectedFrame().setSize(internalFrame.getWidth(), (int) screenSize.getHeight() - 120);
        }
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

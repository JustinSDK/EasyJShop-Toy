package cc.openhome.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import cc.openhome.main.AbstractChild;

public class AboutMenu extends AbstractChild {
    private JMenu aboutMenu;
    private ImageIcon logoImage;
    private JMenuItem aboutEasyJShopMenuItem;
    
    public AboutMenu() {
        initResource();
        setupUIComponent();
        setupEventListener();
    }
    
    public void initResource() {
        logoImage = new ImageIcon(AboutMenu.class.getResource("../images/logo.jpg"));
    }
    
    public void setupUIComponent() {
        aboutMenu = new JMenu("About");
        aboutEasyJShopMenuItem = new JMenuItem("EasyJShop");
        aboutMenu.add(aboutEasyJShopMenuItem);
    }
    
    public void setupEventListener() {
        aboutEasyJShopMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        
        aboutEasyJShopMenuItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JOptionPane.showOptionDialog(null, 
                            "EasyJShop....  :)\n" +  
                            "http://openhome.cc\n" +
                            "caterpillar@openhome.cc",
                            "About EasyJShop",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            logoImage, null, null);
                    }
                }   
            );
    }
    
    public JMenu getMenu() {
        return aboutMenu;
    }
}

package cc.openhome.menu;

import cc.openhome.frame.MainFrame;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

public class AboutMenu extends EasyJShopMenu {
    private ImageIcon logoImage = new ImageIcon(AboutMenu.class.getResource("../images/logo.jpg"));
    private JMenuItem aboutEasyJShopMenuItem = new JMenuItem("EasyJShop");
    
    public AboutMenu(MainFrame mainFrame) {
        super(mainFrame);
        setupUIComponent();
        setupEventListener();
    }
    
    public void setupUIComponent() {
        setText("About");
        add(aboutEasyJShopMenuItem);
    }
    
    public void setupEventListener() {
        aboutEasyJShopMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        
        aboutEasyJShopMenuItem.addActionListener(e -> {
            JOptionPane.showOptionDialog(null,
                    "EasyJShop....  :)\n" +  
                            "http://openhome.cc\n" +
                            "caterpillar@openhome.cc",
                    "About EasyJShop",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    logoImage, null, null);
        });
    }
}

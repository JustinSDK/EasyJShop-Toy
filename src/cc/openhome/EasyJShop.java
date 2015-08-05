package cc.openhome;

import cc.openhome.frame.MainFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class EasyJShop {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            new MainFrame().setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.toString(),
                    "INFORMATION", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }    
}

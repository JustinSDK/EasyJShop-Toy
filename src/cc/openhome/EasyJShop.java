package cc.openhome;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class EasyJShop {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            new MainFrame().setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(),
                    "Info.", JOptionPane.INFORMATION_MESSAGE);
        }
    }    
}

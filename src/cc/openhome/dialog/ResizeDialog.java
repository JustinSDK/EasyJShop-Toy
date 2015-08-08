package cc.openhome.dialog;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ResizeDialog {
    private static JPanel resizePanel;
    
    private static JRadioButton customWidthHeightBtn, percentBtn;
    private static JCheckBox lockRatioBox;
    private static JSpinner resizePercentSpinner, resizeWidthSpinner, resizeHeightSpinner;
    
    private static double imageWidth, imageHeight;
    private static boolean resizeLocker;
    
    static {
        setUIComponent();
        setEventListener();
    }
    
    private static void setUIComponent() {
        resizePanel = new JPanel();
        resizePanel.setLayout(new GridLayout(4, 4, 5, 5));

        customWidthHeightBtn = new JRadioButton("Custom size");
        percentBtn = new JRadioButton("Percentage");
        percentBtn.setSelected(true);
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(customWidthHeightBtn);
        buttonGroup.add(percentBtn);
        
        lockRatioBox = new JCheckBox("lock");
        lockRatioBox.setSelected(true);
        lockRatioBox.setEnabled(false);
        
        resizeWidthSpinner = new JSpinner();
        resizeHeightSpinner = new JSpinner();
        
        resizeWidthSpinner.setEnabled(false);
        resizeHeightSpinner.setEnabled(false);
        
        resizePercentSpinner = new JSpinner();
        resizePercentSpinner.setValue(new Integer(100));
        
        resizePanel.add(customWidthHeightBtn);
        
        resizePanel.add(new JLabel("Width"));
        resizePanel.add(resizeWidthSpinner);
        resizePanel.add(new JLabel("pixel"));
        
        resizePanel.add(lockRatioBox);
        resizePanel.add(new JLabel("Height"));
        resizePanel.add(resizeHeightSpinner);
        resizePanel.add(new JLabel("pixel"));
        
        resizePanel.add(new JLabel("　"));
        resizePanel.add(new JLabel("　"));
        resizePanel.add(new JLabel("　"));
        resizePanel.add(new JLabel("　"));
        
        resizePanel.add(percentBtn);
        resizePanel.add(resizePercentSpinner);
        resizePanel.add(new JLabel("%"));
    }
    
    private static void setEventListener() {
        resizePercentSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if(((Integer) resizePercentSpinner.getValue()).intValue() <= 0) {
                    resizePercentSpinner.setValue(new Integer(1));
                }
            }
        });
        
        resizeWidthSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if(((Integer) resizeWidthSpinner.getValue()).intValue() <= 0) {
                    resizeWidthSpinner.setValue(new Integer(1));
                }
                
                if(lockRatioBox.isSelected() && !resizeLocker) {
                    resizeLocker = true;
                    
                    int value1 = ((Integer) resizeWidthSpinner.getValue()).intValue();
                    double diff1 = value1 - imageWidth;
                    double diff2 = (imageHeight / imageWidth) * diff1;
                    int value2 = (int) (imageHeight + diff2);
                    resizeHeightSpinner.setValue(new Integer(value2));
                    resizeLocker = false;
                }
            }
        });
        
        resizeHeightSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if(((Integer) resizeHeightSpinner.getValue()).intValue() <= 0) {
                    resizeHeightSpinner.setValue(new Integer(1));
                }
                
                if(lockRatioBox.isSelected() && !resizeLocker) {
                    resizeLocker = true;
                    
                    int value1 = ((Integer) resizeHeightSpinner.getValue()).intValue();
                    double diff1 = value1 - imageHeight;
                    double diff2 = (imageWidth / imageHeight) * diff1;
                    int value2 = (int) (imageWidth + diff2);
                    resizeWidthSpinner.setValue(new Integer(value2));
                    resizeLocker = false;
                }
            }
        });
        
        customWidthHeightBtn.addActionListener(new ActionListener() {           
            public void actionPerformed(ActionEvent e) {
                resizePercentSpinner.setEnabled(false);
                resizeWidthSpinner.setEnabled(true);
                resizeHeightSpinner.setEnabled(true);
                lockRatioBox.setEnabled(true);
            }
        });
        
        percentBtn.addActionListener(new ActionListener() {           
            public void actionPerformed(ActionEvent e) {
                resizeWidthSpinner.setEnabled(false);
                resizeHeightSpinner.setEnabled(false);
                lockRatioBox.setEnabled(false);
                resizePercentSpinner.setEnabled(true);
            }
        });
        
    }
    
    public static int showDialog(Component component, String title, Icon logoIcon) {
        return showDialog(component, title, 0, 0, logoIcon);
    }
    
    public static int showDialog(Component component, String title, int imWidth, int imHeight, Icon logoIcon) {
        imageWidth = imWidth;
        imageHeight = imHeight;
        
        resizeWidthSpinner.setValue(new Integer(imWidth));
        resizeHeightSpinner.setValue(new Integer(imHeight));
        
        return JOptionPane.showOptionDialog(component, 
                resizePanel, title, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, logoIcon, null, null);
    }

    public static int getScalePercentage() {
        return ((Integer) resizePercentSpinner.getValue()).intValue();
    }
    
    public static int getPixelWidth() {
        return ((Integer) resizeWidthSpinner.getValue()).intValue();
    }
    
    public static int getPixelHeight() {
        return ((Integer) resizeHeightSpinner.getValue()).intValue();
    }
    
    public static boolean isPercentage() {
        return percentBtn.isSelected();
    }
    
    public static boolean isCustomWidthHeight() {
        return customWidthHeightBtn.isSelected();
    }
}

package cc.openhome;

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.*;

import cc.openhome.img.ImageMementoManager;
import cc.openhome.main.AbstractChild;
import cc.openhome.main.CanvasComponent;
import cc.openhome.menu.AboutMenu;
import cc.openhome.menu.EditMenu;
import cc.openhome.menu.ImageMenu;
import java.beans.PropertyVetoException;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

public class EasyJShop extends JFrame {

    private JDesktopPane desktopPane;
    
    private Map mementoManagers = new HashMap();
    
    private ImageMenu imageMenu = new ImageMenu();
    private EditMenu editMenu = new EditMenu();
    
    private ImageIcon icon = new ImageIcon(EasyJShop.class.getResource("images/appIcon.gif"));
    
    public EasyJShop() {
        super("EasyJShop");
        setUpUIComponent();
        setUpEventListener();
    }
    
    private void setUpUIComponent() {
        setIconImage(icon.getImage());
        
        setSize(640, 480);
        
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        setJMenuBar(new JMenuBar());
        
        addMenu(imageMenu);
        
        addMenu(editMenu);
        getContentPane().add(editMenu.getToolBar(), BorderLayout.NORTH);
        
        addMenu(new AboutMenu());
        
        desktopPane = new JDesktopPane();
        getContentPane().add(desktopPane);
    }
    
    private void addMenu(AbstractChild menu) {
        menu.setParent(this);
        getJMenuBar().add(menu.getMenu());
    }
    
    private void setUpEventListener() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                imageMenu.checkUnsavedImages();
            }
        });
        
    }
    
    public JDesktopPane getDesktopPane() {
        return desktopPane;
    }

    // create an JInternalFrame and set image into it by
    // using JLabel and ImageIcon
    public JInternalFrame createImageInternalFrame(String title, Image image) {
        JInternalFrame internalFrame = new JInternalFrame(title, true, true, true, true);
        internalFrame.setFrameIcon(icon);
        internalFrame.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
        
        internalFrame.addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameOpened(InternalFrameEvent e) {
                imageMenu.enableSaveMenuItem();
            }
            
            public void internalFrameClosing(InternalFrameEvent e) {
                JInternalFrame internalFrame = (JInternalFrame) e.getSource();
                
                try {
                    internalFrame.setIcon(false);
                    internalFrame.setSelected(true);
                } catch (PropertyVetoException ex) {
                    imageMenu.infoMessageBox(ex.getMessage());
                }
                
                imageMenu.checkUnsavedImage(internalFrame);
            }
            
            public void internalFrameClosed(InternalFrameEvent e) {
                imageMenu.checkImageMenuItem();
            }
            
            public void internalFrameIconified(InternalFrameEvent e) {
                imageMenu.checkImageMenuItem();
            }
            
            public void internalFrameDeiconified(InternalFrameEvent e) {
                imageMenu.checkImageMenuItem();
            }
            
            public void internalFrameActivated(InternalFrameEvent e) {
                imageMenu.checkImageMenuItem();
            }
        });
        
        internalFrame.addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameOpened(InternalFrameEvent e) {
                editMenu.checkEditMenuItem();
                
                if (getDesktopPane().getSelectedFrame() == null) {
                    return;
                }
                
                editMenu.setEditInfo(editMenu.getCanvasOfSelectedFrame());
            }
            
            public void internalFrameClosed(InternalFrameEvent e) {
                editMenu.checkEditMenuItem();
            }
            
            public void internalFrameIconified(InternalFrameEvent e) {
                editMenu.checkEditMenuItem();
            }
            
            public void internalFrameDeiconified(InternalFrameEvent e) {
                editMenu.checkEditMenuItem();
            }
            
            public void internalFrameActivated(InternalFrameEvent e) {
                editMenu.checkEditMenuItem();
            }
        });
        
        CanvasComponent canvas = new CanvasComponent(image);
        
        canvas.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                CanvasComponent canvas = (CanvasComponent) e.getSource();
                
                if (editMenu.getEditMode() == CanvasComponent.ViewMode) {
                    canvas.setCursor(editMenu.getViewCursor());
                } else {
                    canvas.setCursor(null);
                }
            }
            
            public void mousePressed(MouseEvent e) {
                
                CanvasComponent canvas = (CanvasComponent) e.getSource();
                
                if (canvas.getEditMode() == CanvasComponent.PasteMode) {
                    if (editMenu.mergeImage(canvas) != JOptionPane.NO_OPTION) {
                        editMenu.setEditInfo(canvas);
                    }
                    return;
                }
                
                editMenu.setEditInfo(canvas);
                
                switch (canvas.getEditMode()) {
                    case 0: // SelectionMode
                        canvas.setStart(e.getPoint());
                        break;
                    case 1: // BrushMode
                        getMementoManager(canvas).addImage(editMenu.copyImage(canvas));
                        canvas.resetRect();
                        canvas.setStart(e.getPoint());
                        canvas.repaint();
                        setStarBeforeTitle();
                        break;
                    case 3: // TextMode
                        if (canvas.getText() != null) {
                            editMenu.mergeText(canvas);
                        } else {
                            editMenu.inputText(canvas);
                        }
                        break;
                    case 4: // ViewMode
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            canvas.increaseViewScale();
                        } else if (e.getButton() == MouseEvent.BUTTON3) {
                            canvas.decreaseViewScale();
                        }
                        editMenu.fitAppSize(canvas.getImage());
                        canvas.repaint();
                        break;
                    default: // SelectionMode
                        canvas.setStart(e.getPoint());
                }
            }
            
            public void mouseReleased(MouseEvent e) {
                CanvasComponent canvas = (CanvasComponent) e.getSource();
                canvas.setStart(null);
                canvas.setEnd(null);
                
                editMenu.checkEditMenuItem();
            }
        });
        
        canvas.addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) {
                CanvasComponent canvas = (CanvasComponent) e.getSource();
                
                switch (canvas.getEditMode()) {
                    case 0: // SelectionMode
                        canvas.dragRect(e.getPoint());
                        break;
                    case 1: // BrushMode
                        canvas.setEnd(e.getPoint());
                        canvas.repaint();
                        break;
                    case 3:
                    case 4:
                        break;
                    default: // SelectionMode
                        canvas.dragRect(e.getPoint());
                }
            }
            
            public void mouseMoved(MouseEvent e) {
                CanvasComponent canvas = (CanvasComponent) e.getSource();
                
                if (canvas.getEditMode() == CanvasComponent.PasteMode
                        || canvas.getEditMode() == CanvasComponent.TextMode) {
                    canvas.setStart(e.getPoint());
                    canvas.repaint();
                }
            }
        });

        mementoManagers.put(canvas, new ImageMementoManager());
        
        JPanel panel = new JPanel();
        canvas.setAlignmentY(Component.CENTER_ALIGNMENT);
        panel.add(canvas);
        
        JScrollPane scrollPanel = new JScrollPane(panel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        internalFrame.getContentPane().add(scrollPanel);
        
        return internalFrame;
    }
    
    public CanvasComponent getCanvasOfInternalFrame(JInternalFrame internalFrame) {
        JScrollPane scrollPanel = (JScrollPane) internalFrame.getContentPane().getComponent(0);
        return (CanvasComponent) ((JPanel) scrollPanel.getViewport().getComponent(0)).getComponent(0);
    }
    
    public void setStarBeforeTitle() {
        String title = desktopPane.getSelectedFrame().getTitle();
        if (!title.startsWith("*")) {
            desktopPane.getSelectedFrame().setTitle("*" + title);
        }
    }
    
    public ImageMementoManager getMementoManager(CanvasComponent canvas) {
        return (ImageMementoManager) mementoManagers.get(canvas);
    }
    
    public Map getMementoManagers() {
        return mementoManagers;
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(),
                    "Info.", JOptionPane.INFORMATION_MESSAGE);
        }
        
        new EasyJShop().setVisible(true);
    }
}

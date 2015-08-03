package cc.openhome;


import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.*;
import javax.swing.event.InternalFrameListener;

import cc.openhome.img.ImageMementoManager;
import cc.openhome.main.AbstractChild;
import cc.openhome.main.CanvasComponent;
import cc.openhome.menu.AboutMenu;
import cc.openhome.menu.EditMenu;
import cc.openhome.menu.ImageMenu;

public class EasyJShop extends JFrame {
    private JDesktopPane desktopPane;
    
    private List internalFrameListeners = new ArrayList();
    private List canvasMouseListeners = new ArrayList();
    private List canvasMouseMotionListeners = new ArrayList();
    private Map mementoManagers = new HashMap();
    
    private ImageMenu imageMenu = new ImageMenu();
    
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
        
        addMenu(imageMenu, null);
        addMenu(new EditMenu(), BorderLayout.NORTH);
        addMenu(new AboutMenu(), null);
                
        desktopPane = new JDesktopPane();
        getContentPane().add(desktopPane);
    }
    
    private void addMenu(AbstractChild menu, String toolBarLayout) {
        menu.setParent(this);
        if(menu.getMenu() != null)
            getJMenuBar().add(menu.getMenu());
        
        if(menu.getToolBar() != null)
            getContentPane().add(menu.getToolBar(), toolBarLayout);
        
        if(menu.getInternalFrameListener() != null)
            internalFrameListeners.add(menu.getInternalFrameListener());
        
        if(menu.getCanvasMouseListener() != null)
            canvasMouseListeners.add(menu.getCanvasMouseListener());
        
        if(menu.getCanvasMouseMotionListener() != null)
            canvasMouseMotionListeners.add(menu.getCanvasMouseMotionListener());
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
        
        Iterator iterator = internalFrameListeners.iterator();
        while(iterator.hasNext()) {
            internalFrame.addInternalFrameListener((InternalFrameListener) iterator.next());
        }
        
        CanvasComponent canvas = new CanvasComponent(image);
        
        iterator = canvasMouseListeners.iterator();
        while(iterator.hasNext()) {
            canvas.addMouseListener((MouseListener) iterator.next());
        }
        
        iterator = canvasMouseMotionListeners.iterator();
        while(iterator.hasNext()) {
            canvas.addMouseMotionListener((MouseMotionListener) iterator.next());
        }
        
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
        if(!title.startsWith("*"))
            desktopPane.getSelectedFrame().setTitle("*" + title);
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
        }
        catch(Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(),
                    "Info.", JOptionPane.INFORMATION_MESSAGE);
        }
        
        new EasyJShop().setVisible(true);  
    }
}

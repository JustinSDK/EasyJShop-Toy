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
    private ImageIcon appIcon;
    
    private JMenuBar menuBar;
    
    private JDesktopPane desktopPane;
    
    private List windowListeners;
    private List internalFrameListeners, canvasMouseListeners, canvasMouseMotionListeners;
    private Map mementoManagers;
    
    public EasyJShop() {
        super("EasyJShop");
        
        initResource();
        setUpUIComponent();
        setUpEventListener();
        
        setVisible(true);    
    }
    
    private void initResource() {
        appIcon = new ImageIcon(EasyJShop.class.getResource("images/appIcon.gif"));
        windowListeners = new ArrayList();
        internalFrameListeners = new ArrayList();
        canvasMouseListeners = new ArrayList();
        canvasMouseMotionListeners = new ArrayList();
        mementoManagers = new HashMap();
    }
    
    private void setUpUIComponent() {
        setIconImage(appIcon.getImage());
        setSize((int) getToolkit().getScreenSize().getWidth(), (int) getToolkit().getScreenSize().getHeight() - 20);
        
        menuBar = new JMenuBar();
        
        addChild(new ImageMenu(), null);
        addChild(new EditMenu(), BorderLayout.NORTH);
        addChild(new AboutMenu(), null);
        
        setJMenuBar(menuBar);
                
        desktopPane = new JDesktopPane();
        getContentPane().add(desktopPane);
    }
    
    private void addChild(AbstractChild menu, String toolBarLayout) {
        menu.setParent(this);
        
        if(menu.getMenu() != null)
            menuBar.add(menu.getMenu());
        
        if(menu.getToolBar() != null)
            getContentPane().add(menu.getToolBar(), toolBarLayout);
        
        if(menu.getWindowListener() != null)
            windowListeners.add(menu.getWindowListener());
        
        if(menu.getInternalFrameListener() != null)
            internalFrameListeners.add(menu.getInternalFrameListener());
        
        if(menu.getCanvasMouseListener() != null)
            canvasMouseListeners.add(menu.getCanvasMouseListener());
        
        if(menu.getCanvasMouseMotionListener() != null)
            canvasMouseMotionListeners.add(menu.getCanvasMouseMotionListener());
    }
    
    private void setUpEventListener() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        Iterator iterator = windowListeners.iterator();
        while(iterator.hasNext()) {
            addWindowListener((WindowListener) iterator.next());
        }
    }
    
    public JDesktopPane getDesktopPane() {
        return desktopPane;
    }
    
    // create an JInternalFrame and set image into it by
    // using JLabel and ImageIcon
    public JInternalFrame createImageInternalFrame(String title, Image image) {
        JInternalFrame internalFrame = new JInternalFrame(title, true, true, true, true);
        internalFrame.setFrameIcon(appIcon);
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
        
        new EasyJShop();
    }
}

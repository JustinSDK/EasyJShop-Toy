/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc.openhome.main;

import cc.openhome.EasyJShop;
import cc.openhome.img.ImageMementoManager;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyVetoException;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

public class ImageInternalFrame extends JInternalFrame {
    private EasyJShop easyJShop;
    private CanvasComponent canvas;
    public ImageInternalFrame(EasyJShop easyJShop, String title, Image image) {
        super(title, true, true, true, true);

        this.easyJShop = easyJShop;
        canvas = new CanvasComponent(image);
        
        setFrameIcon(easyJShop.getIcon());
        
        easyJShop.getMementoManagers().put(canvas, new ImageMementoManager());

        JPanel panel = new JPanel();
        canvas.setAlignmentY(Component.CENTER_ALIGNMENT);
        panel.add(canvas);

        JScrollPane scrollPanel = new JScrollPane(panel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        getContentPane().add(scrollPanel);

        initEventListeners();
    }

    private void initEventListeners() {
        setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);

        addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameOpened(InternalFrameEvent e) {
                // image menu
                easyJShop.getImageMenu().enableSaveMenuItem();

                // edit menu
                easyJShop.getEditMenu().checkEditMenuItem();
                if (getDesktopPane().getSelectedFrame() == null) {
                    return;
                }
                easyJShop.getEditMenu().setEditInfo(easyJShop.getEditMenu().getCanvasOfSelectedFrame());
            }

            public void internalFrameClosing(InternalFrameEvent e) {
                JInternalFrame internalFrame = (JInternalFrame) e.getSource();

                try {
                    internalFrame.setIcon(false);
                    internalFrame.setSelected(true);
                } catch (PropertyVetoException ex) {
                    easyJShop.getImageMenu().infoMessageBox(ex.getMessage());
                }

                easyJShop.getImageMenu().checkUnsavedImage(internalFrame);
            }

            public void internalFrameClosed(InternalFrameEvent e) {
                easyJShop.getImageMenu().checkImageMenuItem();
                easyJShop.getEditMenu().checkEditMenuItem();
            }

            public void internalFrameIconified(InternalFrameEvent e) {
                easyJShop.getImageMenu().checkImageMenuItem();
                easyJShop.getEditMenu().checkEditMenuItem();
            }

            public void internalFrameDeiconified(InternalFrameEvent e) {
                easyJShop.getImageMenu().checkImageMenuItem();
                easyJShop.getEditMenu().checkEditMenuItem();
            }

            public void internalFrameActivated(InternalFrameEvent e) {
                easyJShop.getImageMenu().checkImageMenuItem();
                easyJShop.getEditMenu().checkEditMenuItem();
            }
        });

        canvas.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                CanvasComponent canvas = (CanvasComponent) e.getSource();

                if (easyJShop.getEditMenu().getEditMode() == CanvasComponent.ViewMode) {
                    canvas.setCursor(easyJShop.getEditMenu().getViewCursor());
                } else {
                    canvas.setCursor(null);
                }
            }

            public void mousePressed(MouseEvent e) {
                CanvasComponent canvas = (CanvasComponent) e.getSource();

                if (canvas.getEditMode() == CanvasComponent.PasteMode) {
                    if (easyJShop.getEditMenu().mergeImage(canvas) != JOptionPane.NO_OPTION) {
                        easyJShop.getEditMenu().setEditInfo(canvas);
                    }
                    return;
                }

                easyJShop.getEditMenu().setEditInfo(canvas);

                switch (canvas.getEditMode()) {
                    case 0: // SelectionMode
                        canvas.setStart(e.getPoint());
                        break;
                    case 1: // BrushMode
                        easyJShop.getMementoManager(canvas).addImage(easyJShop.getEditMenu().copyImage(canvas));
                        canvas.resetRect();
                        canvas.setStart(e.getPoint());
                        canvas.repaint();
                        easyJShop.setStarBeforeTitle();
                        break;
                    case 3: // TextMode
                        if (canvas.getText() != null) {
                            easyJShop.getEditMenu().mergeText(canvas);
                        } else {
                            easyJShop.getEditMenu().inputText(canvas);
                        }
                        break;
                    case 4: // ViewMode
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            canvas.increaseViewScale();
                        } else if (e.getButton() == MouseEvent.BUTTON3) {
                            canvas.decreaseViewScale();
                        }
                        easyJShop.getEditMenu().fitAppSize(canvas.getImage());
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

                easyJShop.getEditMenu().checkEditMenuItem();
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
    }

}

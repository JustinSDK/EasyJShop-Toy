package cc.openhome.main;

import cc.openhome.MainFrame;
import cc.openhome.img.ImageMementoManager;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
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

    private MainFrame parent;
    private CanvasComponent canvas;

    public CanvasComponent getCanvas() {
        return canvas;
    }

    public ImageInternalFrame(MainFrame parent, String title, Image image) {
        super(title, true, true, true, true);

        this.parent = parent;
        canvas = new CanvasComponent(image);

        setFrameIcon(parent.getIcon());

        parent.getMementoManagers().put(canvas, new ImageMementoManager());

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
                parent.getImageMenu().enableSaveMenuItem();

                // edit menu
                parent.getEditMenu().checkEditMenuItem();
                if (getDesktopPane().getSelectedFrame() == null) {
                    return;
                }
                parent.getEditMenu().setEditInfo(parent.getCanvasOfSelectedFrame());
            }

            public void internalFrameClosing(InternalFrameEvent e) {
                JInternalFrame internalFrame = (JInternalFrame) e.getSource();

                try {
                    internalFrame.setIcon(false);
                    internalFrame.setSelected(true);
                } catch (PropertyVetoException ex) {
                    parent.getImageMenu().infoMessageBox(ex.getMessage());
                }

                parent.getImageMenu().checkUnsavedImage(internalFrame);
            }

            public void internalFrameClosed(InternalFrameEvent e) {
                parent.getImageMenu().checkImageMenuItem();
                parent.getEditMenu().checkEditMenuItem();
            }

            public void internalFrameIconified(InternalFrameEvent e) {
                parent.getImageMenu().checkImageMenuItem();
                parent.getEditMenu().checkEditMenuItem();
            }

            public void internalFrameDeiconified(InternalFrameEvent e) {
                parent.getImageMenu().checkImageMenuItem();
                parent.getEditMenu().checkEditMenuItem();
            }

            public void internalFrameActivated(InternalFrameEvent e) {
                parent.getImageMenu().checkImageMenuItem();
                parent.getEditMenu().checkEditMenuItem();
            }
        });

        canvas.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                CanvasComponent canvas = (CanvasComponent) e.getSource();

                if (parent.getEditMenu().getEditMode() == CanvasComponent.ViewMode) {
                    canvas.setCursor(parent.getEditMenu().getViewCursor());
                } else {
                    canvas.setCursor(null);
                }
            }

            public void mousePressed(MouseEvent e) {
                CanvasComponent canvas = (CanvasComponent) e.getSource();

                if (canvas.getEditMode() == CanvasComponent.PasteMode) {
                    if (parent.getEditMenu().mergeImage(canvas) != JOptionPane.NO_OPTION) {
                        parent.getEditMenu().setEditInfo(canvas);
                    }
                    return;
                }

                parent.getEditMenu().setEditInfo(canvas);

                switch (canvas.getEditMode()) {
                    case 0: // SelectionMode
                        canvas.setStart(e.getPoint());
                        break;
                    case 1: // BrushMode
                        parent.getMementoManager(canvas).addImage(parent.getEditMenu().copyImage(canvas));
                        canvas.resetRect();
                        canvas.setStart(e.getPoint());
                        canvas.repaint();
                        parent.setStarBeforeTitle();
                        break;
                    case 3: // TextMode
                        if (canvas.getText() != null) {
                            parent.getEditMenu().mergeText(canvas);
                        } else {
                            parent.getEditMenu().inputText(canvas);
                        }
                        break;
                    case 4: // ViewMode
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            canvas.increaseViewScale();
                        } else if (e.getButton() == MouseEvent.BUTTON3) {
                            canvas.decreaseViewScale();
                        }
                        fitAppSize(canvas.getImage());
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

                parent.getEditMenu().checkEditMenuItem();
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

                if (canvas.getEditMode() == CanvasComponent.PasteMode || canvas.getEditMode() == CanvasComponent.TextMode) {
                    canvas.setStart(e.getPoint());
                    canvas.repaint();
                }
            }
        });
    }

    public void fitAppSize(Image image) {
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

}
